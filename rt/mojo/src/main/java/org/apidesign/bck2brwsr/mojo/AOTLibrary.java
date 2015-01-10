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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apidesign.bck2brwsr.aot.Bck2BrwsrJars;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.apidesign.vm4brwsr.ObfuscationLevel;

/**
 *
 * @author Jaroslav Tulach
 * @since 0.12
 */
@Mojo(name = "library",
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    defaultPhase = LifecyclePhase.PACKAGE
)
public class AOTLibrary extends AbstractMojo {
    @Parameter(defaultValue = "${project}")
    private MavenProject prj;

    @Component
    private MavenProjectHelper projectHelper;
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    private File mainJar;    
    @Parameter(defaultValue = "${project.build.finalName}-min.js")
    private String minified;
    @Parameter(defaultValue = "${project.build.finalName}-debug.js")
    private String debug;
    
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}-bck2brwsr.jar")
    private File aotJar;
    
    @Parameter
    private String[] exports;
    
    @Parameter
    private String[] aotDeps;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        URLClassLoader loader;
        try {
            loader = buildClassLoader(mainJar, prj.getArtifacts());
        } catch (MalformedURLException ex) {
            throw new MojoFailureException("Can't initialize classloader");
        }

        try {
            Manifest m = new Manifest();
            if (!"false".equals(minified)) {
                Attributes attr = new Attributes();
                attr.putValue("Bck2BrwsrArtifactId", prj.getArtifactId());
                attr.putValue("Bck2BrwsrGroupId", prj.getGroupId());
                attr.putValue("Bck2BrwsrVersion", prj.getVersion());
                attr.putValue("Bck2BrwsrMinified", "true");
                m.getEntries().put(minified, attr);
            }
            if (!"false".equals(debug)) {
                Attributes attr = new Attributes();
                attr.putValue("Bck2BrwsrArtifactId", prj.getArtifactId());
                attr.putValue("Bck2BrwsrGroupId", prj.getGroupId());
                attr.putValue("Bck2BrwsrVersion", prj.getVersion());
                attr.putValue("Bck2BrwsrDebug", "true");
                m.getEntries().put(debug, attr);
            }
            
            if (aotDeps != null) {
                for (Artifact a : prj.getArtifacts()) {
                    if (!matches(aotDeps, a)) {
                        continue;
                    }
                    
                    {
                        Attributes attr = new Attributes();
                        attr.putValue("Bck2BrwsrArtifactId", a.getArtifactId());
                        attr.putValue("Bck2BrwsrGroupId", a.getGroupId());
                        attr.putValue("Bck2BrwsrVersion", a.getVersion());
                        attr.putValue("Bck2BrwsrDebug", "true");
                        m.getEntries().put(artifactName(a, true), attr);
                    }
                    {
                        Attributes attr = new Attributes();
                        attr.putValue("Bck2BrwsrArtifactId", a.getArtifactId());
                        attr.putValue("Bck2BrwsrGroupId", a.getGroupId());
                        attr.putValue("Bck2BrwsrVersion", a.getVersion());
                        attr.putValue("Bck2BrwsrMinified", "true");
                        m.getEntries().put(artifactName(a, false), attr);
                    }
                }
            }
            
            FileOutputStream fos = new FileOutputStream(this.aotJar);
            JarOutputStream os = new JarOutputStream(fos, m);

            if (!"false".equals(debug)) {
                os.putNextEntry(new JarEntry(debug));
                Writer w = new OutputStreamWriter(os, "UTF-8");
                configureMain(loader).
                    obfuscation(ObfuscationLevel.NONE).
                    generate(w);
                w.flush();
                os.closeEntry();
            }
            if (!"false".equals(minified)) {
                os.putNextEntry(new JarEntry(minified));
            
                Writer w = new OutputStreamWriter(os, "UTF-8");
                configureMain(loader).
                    obfuscation(ObfuscationLevel.FULL).
                    generate(w);
                w.flush();
                os.closeEntry();
            }
            
            if (aotDeps != null) {
                for (Artifact a : prj.getArtifacts()) {
                    if (!matches(aotDeps, a)) {
                        continue;
                    }
                    getLog().info("Generating bck2brwsr for " + a.getFile());
                    Bck2Brwsr c = Bck2BrwsrJars.configureFrom(null, a.getFile(), loader);
                    if (exports != null) {
                        for (String e : exports) {
                            c = c.addExported(e.replace('.', '/'));
                        }
                    }
                    {
                        os.putNextEntry(new JarEntry(artifactName(a, true)));
                        Writer w = new OutputStreamWriter(os, "UTF-8");
                        c.
                            obfuscation(ObfuscationLevel.NONE).
                            generate(w);
                        w.flush();
                        os.closeEntry();
                    }
                    {
                        os.putNextEntry(new JarEntry(artifactName(a, false)));

                        Writer w = new OutputStreamWriter(os, "UTF-8");
                        c.
                            obfuscation(ObfuscationLevel.FULL).
                            generate(w);
                        w.flush();
                        os.closeEntry();
                    }                    
                }
            }
            os.close();
            
            projectHelper.attachArtifact(prj, "jar", "bck2brwsr", aotJar);
        } catch (IOException ex) {
            throw new MojoFailureException("Cannot generate script for " + mainJar, ex);
        }
    }

    private Bck2Brwsr configureMain(URLClassLoader loader) throws IOException {
        Bck2Brwsr c = Bck2BrwsrJars.configureFrom(null, mainJar, loader);
        if (exports != null) {
            for (String e : exports) {
                c = c.addExported(e.replace('.', '/'));
            }
        }
        return c;
    }

    private static String artifactName(Artifact a, boolean debug) {
        return a.getGroupId() + "-" + a.getArtifactId() + (debug ? "-debug.js" : "-min.js");
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

    private static boolean matches(String[] aotDeps, Artifact a) {
        for (String d : aotDeps) {
            String[] parts = d.split(":");
            for (int i = 0; i < parts.length; i++) {
                if ("*".equals(parts[i])) {
                    parts[i] = null;
                }
            }
            
            if (parts[0] != null && !parts[0].equals(a.getGroupId())) {
                continue;
            }
            if (parts[1] != null && !parts[1].equals(a.getArtifactId())) {
                continue;
            }
            if (parts.length > 2 && parts[2] != null && !parts[2].equals(a.getClassifier())) {
                continue;
            }
            return true;
        }
        return false;
    }
}
