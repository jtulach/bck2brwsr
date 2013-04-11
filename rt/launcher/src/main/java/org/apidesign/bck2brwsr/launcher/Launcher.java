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
package org.apidesign.bck2brwsr.launcher;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/** An abstraction for executing tests in a Bck2Brwsr virtual machine.
 * Either in {@linkm Launcher#createJavaScript JavaScript engine}, 
 * or in {@linkm Launcher#createBrowser external browser}.
 * <p>
 * There also are methods to {@link #showDir(java.io.File, java.lang.String) display pages} 
 * in an external browser served by internal HTTP server.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class Launcher {

    Launcher() {
    }

    /** Initializes the launcher. This may mean starting a web browser or
     * initializing execution engine.
     * @throws IOException if something goes wrong
     */
    public abstract void initialize() throws IOException;
    
    /** Shuts down the launcher.
     * @throws IOException if something goes wrong
     */
    public abstract void shutdown() throws IOException;
    
    
    /** Builds an invocation context. The context can later be customized
     * and {@link InvocationContext#invoke() invoked}.
     * 
     * @param clazz the class to execute method from
     * @param method the method to execute
     * @return the context pointing to the selected method
     */
    public InvocationContext createInvocation(Class<?> clazz, String method) {
        return new InvocationContext(this, clazz, method);
    }
    

    /** Creates launcher that uses internal JavaScript engine (Rhino).
     * @return the launcher
     */
    public static Launcher createJavaScript() {
        final JSLauncher l = new JSLauncher();
        l.addClassLoader(Bck2Brwsr.class.getClassLoader());
        return l;
    }
    
    /** Creates launcher that is using external browser.
     * 
     * @param cmd <code>null</code> to use <code>java.awt.Desktop</code> to show the launcher
     *    or a string to execute in an external process (with a parameter to the URL)
     * @return launcher executing in external browser.
     */
    public static Launcher createBrowser(String cmd) {
        final Bck2BrwsrLauncher l = new Bck2BrwsrLauncher(cmd);
        l.addClassLoader(Bck2Brwsr.class.getClassLoader());
        l.setTimeout(180000);
        return l;
    }
    
    /** Starts an HTTP server which provides access to classes and resources
     * available in the <code>classes</code> URL and shows a start page
     * available as {@link ClassLoader#getResource(java.lang.String)} from the
     * provide classloader. Opens a browser with URL showing the start page.
     * 
     * @param classes classloader offering access to classes and resources
     * @param startpage page to show in the browser
     * @return interface that allows one to stop the server
     * @throws IOException if something goes wrong
     */
    public static Closeable showURL(ClassLoader classes, String startpage) throws IOException {
        Bck2BrwsrLauncher l = new Bck2BrwsrLauncher(null);
        l.addClassLoader(classes);
        l.showURL(startpage);
        return l;
    }
    /** Starts an HTTP server which provides access to certain directory.
     * The <code>startpage</code> should be relative location inside the root 
     * directory. Opens a browser with URL showing the start page.
     * 
     * @param directory the root directory on disk
     * @param startpage relative path from the root to the page
     * @exception IOException if something goes wrong.
     */
    public static Closeable showDir(File directory, String startpage) throws IOException {
        Bck2BrwsrLauncher l = new Bck2BrwsrLauncher(null);
        l.showDirectory(directory, startpage);
        return l;
    }

    abstract InvocationContext runMethod(InvocationContext c) throws IOException; 
}
