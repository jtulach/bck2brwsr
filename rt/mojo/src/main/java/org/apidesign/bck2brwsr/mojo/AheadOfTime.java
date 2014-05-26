/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */

package org.apidesign.bck2brwsr.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.apidesign.vm4brwsr.ObfuscationLevel;

/**
 *
 * @author Jaroslav Tulach
 * @since 0.9
 */
@Mojo(name = "aot",
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    defaultPhase = LifecyclePhase.PACKAGE
)
public class AheadOfTime extends AbstractMojo {
    @Parameter(defaultValue = "${project}")
    private MavenProject prj;

    /**
     * Directory where to generate ahead-of-time JavaScript files for
     * required libraries.
     */
    @Parameter(defaultValue = "${project.build.directory}/lib")
    private File aot;

    /** Root JavaScript file to generate */
    @Parameter(defaultValue="${project.build.directory}/bck2brwsr.js")
    private File vm;
    
    /**
     * The obfuscation level for the generated JavaScript file.
     *
     * @since 0.5
     */
    @Parameter(defaultValue = "NONE")
    private ObfuscationLevel obfuscation;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        URLClassLoader loader;
        try {
            loader = buildClassLoader(null, prj.getArtifacts());
        } catch (MalformedURLException ex) {
            throw new MojoFailureException("Can't initialize classloader");
        }
        for (Artifact a : prj.getArtifacts()) {
            if (a.getFile() == null) {
                continue;
            }
            String n = a.getFile().getName();
            if (!n.endsWith(".jar")) {
                continue;
            }
            if ("provided".equals(a.getScope())) {
                continue;
            }
            aot.mkdirs();
            File js = new File(aot, n.substring(0, n.length() - 4) + ".js");
            try {
                aotLibrary(a, js , loader);
            } catch (IOException ex) {
                throw new MojoFailureException("Can't compile" + a.getFile(), ex);
            }
        }
            
        try {
            FileWriter w = new FileWriter(vm);
            Bck2Brwsr.newCompiler().
                    obfuscation(obfuscation).
                    standalone(false).
                    resources(new Bck2Brwsr.Resources() {

                @Override
                public InputStream get(String resource) throws IOException {
                    return null;
                }
            }).
                    generate(w);
            w.close();
            
        } catch (IOException ex) {
            throw new MojoExecutionException("Can't compile", ex);
        }
    }

    private void aotLibrary(Artifact a, File js, URLClassLoader loader) throws IOException {
        List<String> classes = new ArrayList<String>();
        List<String> resources = new ArrayList<String>();
        Set<String> exported = new HashSet<String>();
        
        JarFile jf = new JarFile(a.getFile());
        listJAR(jf, classes , resources, exported);
        
        FileWriter w = new FileWriter(js);
        Bck2Brwsr.newCompiler().
                obfuscation(obfuscation).
                library(true).
                resources(loader).
                addResources(resources.toArray(new String[0])).
                addClasses(classes.toArray(new String[0])).
                addExported(exported.toArray(new String[0])).
                generate(w);
        w.close();
    }
    private static URLClassLoader buildClassLoader(File root, Collection<Artifact> deps) throws MalformedURLException {
        List<URL> arr = new ArrayList<URL>();
        if (root != null) {
            arr.add(root.toURI().toURL());
        }
        for (Artifact a : deps) {
            if (a.getFile() != null) {
                arr.add(a.getFile().toURI().toURL());
            }
        }
        return new URLClassLoader(arr.toArray(new URL[0]), Java2JavaScript.class.getClassLoader());
    }
    
    private static void listJAR(
            JarFile j, List<String> classes,
            List<String> resources, Set<String> exported
    ) throws IOException {
        Enumeration<JarEntry> en = j.entries();
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            final String n = e.getName();
            if (n.endsWith("/")) {
                continue;
            }
            int last = n.lastIndexOf('/');
            String pkg = n.substring(0, last + 1);
            if (n.endsWith(".class")) {
                classes.add(n.substring(0, n.length() - 6));
            } else {
                resources.add(n);
                if (n.startsWith("META-INF/services/") && exported != null) {
                    final InputStream is = j.getInputStream(e);
                    exportedServices(is, exported);
                    is.close();
                }
            }
        }
        String exp = j.getManifest().getMainAttributes().getValue("Export-Package");
        if (exp != null && exported != null) {
            for (String def : exp.split(",")) {
                for (String sep : def.split(";")) {
                    exported.add(sep.replace('.', '/') + "/");
                    break;
                }
            }
        }
    }

    static void exportedServices(final InputStream is, Set<String> exported) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        for (;;) {
            String l = r.readLine();
            if (l == null) {
                break;
            }
            if (l.startsWith("#")) {
                continue;
            }
            exported.add(l.replace('.', '/'));
        }
    }
    
}
