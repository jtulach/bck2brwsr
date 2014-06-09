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
    name="show",
    requiresDependencyResolution = ResolutionScope.RUNTIME,
    defaultPhase=LifecyclePhase.NONE
)
public class ShowMojo extends AbstractMojo {
    public ShowMojo() {
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

    /** Root of all pages, and files, etc. */
    @Parameter
    private File directory;
    
    @Override
    public void execute() throws MojoExecutionException {
        if (startpage == null) {
            throw new MojoExecutionException("You have to provide a start page");
        }
        if (directory == null) {
            throw new MojoExecutionException("You have to provide a root directory");
        }
        try {
            Closeable httpServer;
            httpServer = Launcher.showDir(launcher, directory, null, startpage);
            System.in.read();
            httpServer.close();
        } catch (IOException ex) {
            throw new MojoExecutionException("Can't show the browser", ex);
        }
    }
}
