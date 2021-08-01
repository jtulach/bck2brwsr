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
import java.io.Flushable;
import java.io.IOException;
import java.util.Set;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
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

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    private File mainJar;

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.js")
    private File mainJavaScript;

    @Override
    public void execute() throws MojoExecutionException {
        if (startpage == null) {
            if (mainJavaScript != null && mainJavaScript.exists()) {
                directory = mainJavaScript.getParentFile();
                try {
                    Set<String> mainClasses = UtilAsm.findMainClass(mainJar);
                    if (mainClasses.isEmpty()) {
                        throw new MojoExecutionException("Cannot find main class in " + mainJar);
                    }
                    UtilBase.verifyIndexHtml(directory, mainJavaScript.getName(), mainClasses.iterator().next());
                } catch (IOException ex) {
                    throw new MojoExecutionException("Cannot generate index.html in " + directory, ex);
                }
                startpage = "index.html";
            } else {
                throw new MojoExecutionException("You have to provide a start page");
            }
        }
        if (directory == null) {
            if (startpage.startsWith("http:") || startpage.startsWith("https:")) {
                try {
                    Launcher.showURL(launcher, null, startpage);
                } catch (IOException ex) {
                    throw new MojoExecutionException("Can't show the browser", ex);
                }
                return;
            }
            throw new MojoExecutionException("You have to provide a root directory");
        }
        try {
            Closeable httpServer;
            httpServer = Launcher.showDir(launcher, directory, null, startpage);
            if (httpServer instanceof Flushable) {
                ((Flushable)httpServer).flush();
            } else {
                System.in.read();
            }
            httpServer.close();
        } catch (IOException ex) {
            throw new MojoExecutionException("Can't show the browser", ex);
        }
    }
}
