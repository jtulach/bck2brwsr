/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.io.Closeable;
import org.apache.maven.plugin.AbstractMojo;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apidesign.bck2brwsr.launcher.Launcher;

/** Executes given HTML page in a browser. */
@Mojo(
    name="brwsr",
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    defaultPhase=LifecyclePhase.NONE
)
public class BrwsrMojo extends AbstractMojo {
    public BrwsrMojo() {
    }
    
    /** The identification of a launcher to use. Known values <code>fxbrwsr</code>, 
     * <code>bck2brwsr</code>, or 
     * name of an external process to execute.
     */
    @Parameter
    private String launcher;
    
    
    /** Resource to show as initial page */
    @Parameter
    private String startpage;

    @Parameter(defaultValue="${project}")
    private MavenProject prj;
    
    /** Root of the class files */
    @Parameter(defaultValue="${project.build.directory}/classes")
    private File classes;
    
    /** Root of all pages, and files, etc. */
    @Parameter
    private File directory;
    
    @Parameter(defaultValue="${project.build.directory}/bck2brwsr.js")
    private File javascript;

    @Override
    public void execute() throws MojoExecutionException {
        if (startpage == null) {
            throw new MojoExecutionException("You have to provide a start page");
        }
        if (javascript != null && javascript.isFile()) {
            System.setProperty("bck2brwsr.js", javascript.toURI().toString());
        }
        try {
            Closeable httpServer;
            if (directory != null) {
                URLClassLoader url = buildClassLoader(classes, prj.getArtifacts());
                httpServer = Launcher.showDir(launcher, directory, url, startpage);
            } else {
                URLClassLoader url = buildClassLoader(classes, prj.getArtifacts());
                try {
                    for (Resource r : prj.getResources()) {
                        File f = new File(r.getDirectory(), startpage().replace('/', File.separatorChar));
                        if (f.exists()) {
                            System.setProperty("startpage.file", f.getPath());
                        }
                    }
                    
                    httpServer = Launcher.showURL(launcher, url, startpage());
                } catch (Exception ex) {
                    throw new MojoExecutionException("Can't open " + startpage(), ex);
                }
            }
            System.in.read();
            httpServer.close();
        } catch (IOException ex) {
            throw new MojoExecutionException("Can't show the browser", ex);
        }
    }
    
    private String startpage() {
        return startpage;
    }

    private static URLClassLoader buildClassLoader(File root, Collection<Artifact> deps) throws MalformedURLException {
        List<URL> arr = new ArrayList<URL>();
        arr.add(root.toURI().toURL());
        for (Artifact a : deps) {
            final File f = a.getFile();
            if (f != null) {
                arr.add(f.toURI().toURL());
            }
        }
        return new URLClassLoader(arr.toArray(new URL[0]), BrwsrMojo.class.getClassLoader());
    }
}
