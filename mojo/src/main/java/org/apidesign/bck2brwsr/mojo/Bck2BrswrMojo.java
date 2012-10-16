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

import org.apache.maven.plugin.AbstractMojo;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/** Compiles classes into JavaScript. */
@Mojo(name="j2js", defaultPhase=LifecyclePhase.PROCESS_CLASSES)
public class Bck2BrswrMojo extends AbstractMojo {
    public Bck2BrswrMojo() {
    }
    /** Root of the class files */
    @Parameter(defaultValue="${project.build.directory}/classes")
    private File classes;
    /** File to generate. Defaults bootjava.js in the first non-empty 
     package under the classes directory */
    @Parameter
    private File javascript;
    
    @Parameter(defaultValue="${project}")
    private MavenProject prj;
    
    

    @Override
    public void execute() throws MojoExecutionException {
        if (!classes.isDirectory()) {
            throw new MojoExecutionException("Can't find " + classes);
        }

        if (javascript == null) {
            javascript = new File(findNonEmptyFolder(classes), "bootjava.js");
        }

        List<String> arr = new ArrayList<String>();
        collectAllClasses("", classes, arr);
        
        

        try {
            URLClassLoader url = buildClassLoader(classes, prj.getDependencyArtifacts());
            
            Class<?> c = Class.forName("org.apidesign.vm4brwsr.GenJS");
            Method m = c.getDeclaredMethod("compile", ClassLoader.class, Appendable.class, List.class);
            m.setAccessible(true);
            FileWriter w = new FileWriter(javascript);
            m.invoke(null, url, w, arr);
            w.close();
        } catch (Exception ex) {
            throw new MojoExecutionException("Can't compile", ex);
        }
    }

    private static File findNonEmptyFolder(File dir) throws MojoExecutionException {
        if (!dir.isDirectory()) {
            throw new MojoExecutionException("Not a directory " + dir);
        }
        File[] arr = dir.listFiles();
        if (arr.length == 1 && arr[0].isDirectory()) {
            return findNonEmptyFolder(arr[0]);
        }
        return dir;
    }

    private static void collectAllClasses(String prefix, File toCheck, List<String> arr) {
        File[] files = toCheck.listFiles();
        if (files != null) {
            for (File f : files) {
                collectAllClasses(prefix + f.getName() + "/", f, arr);
            }
        } else if (toCheck.getName().endsWith(".class")) {
            arr.add(prefix.substring(0, prefix.length() - 7));
        }
    }

    private static URLClassLoader buildClassLoader(File root, Collection<Artifact> deps) throws MalformedURLException {
        List<URL> arr = new ArrayList<URL>();
        arr.add(root.toURI().toURL());
        for (Artifact a : deps) {
            arr.add(a.getFile().toURI().toURL());
        }
        return new URLClassLoader(arr.toArray(new URL[0]), Bck2BrswrMojo.class.getClassLoader());
    }
}
