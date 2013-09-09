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
import java.lang.reflect.Constructor;

/** An abstraction for executing tests in a Bck2Brwsr virtual machine.
 * Either in {@link Launcher#createJavaScript JavaScript engine}, 
 * or in {@link Launcher#createBrowser external browser}.
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
        try {
            Class<?> c = loadClass("org.apidesign.bck2brwsr.launcher.JSLauncher");
            return (Launcher) c.newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException("Please include org.apidesign.bck2brwsr:launcher.html dependency!", ex);
        }
    }
    
    /** Creates launcher that is using external browser.
     * 
     * @param cmd <code>null</code> to use <code>java.awt.Desktop</code> to show the launcher
     *    or a string to execute in an external process (with a parameter to the URL)
     * @return launcher executing in external browser.
     */
    public static Launcher createBrowser(String cmd) {
        String msg = "Trying to create browser '" + cmd + "'";
        try {
            Class<?> c;
            if ("fxbrwsr".equals(cmd)) { // NOI18N
                msg = "Please include org.apidesign.bck2brwsr:launcher.fx dependency!";
                c = loadClass("org.apidesign.bck2brwsr.launcher.FXBrwsrLauncher"); // NOI18N
            } else {
                msg = "Please include org.apidesign.bck2brwsr:launcher.html dependency!";
                c = loadClass("org.apidesign.bck2brwsr.launcher.Bck2BrwsrLauncher"); // NOI18N
                if ("bck2brwsr".equals(cmd)) { // NOI18N
                    // use default executable
                    cmd = null;
                }
            }
            Constructor<?> cnstr = c.getConstructor(String.class);
            return (Launcher) cnstr.newInstance(cmd);
        } catch (Exception ex) {
            throw new IllegalStateException(msg, ex);
        }
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
        return showURL(null, classes, startpage);
    }
    /** Starts an HTTP server which provides access to classes and resources
     * available in the <code>classes</code> URL and shows a start page
     * available as {@link ClassLoader#getResource(java.lang.String)} from the
     * provide classloader. Opens a browser with URL showing the start page.
     * 
     * @param brwsr name of browser to use or <code>null</code>
     * @param classes classloader offering access to classes and resources
     * @param startpage page to show in the browser
     * @return interface that allows one to stop the server
     * @throws IOException if something goes wrong
     * @since 0.7
     */
    public static Closeable showURL(String brwsr, ClassLoader classes, String startpage) throws IOException {
        Launcher l = createBrowser(brwsr);
        l.addClassLoader(classes);
        l.showURL(startpage);
        return (Closeable) l;
    }
    /** Starts an HTTP server which provides access to certain directory.
     * The <code>startpage</code> should be relative location inside the root 
     * directory. Opens a browser with URL showing the start page.
     * 
     * @param brwsr type of the browser to use
     * @param directory the root directory on disk
     * @param classes additional classloader with access to classes or <code>null</code>
     * @param startpage relative path from the root to the page
     * @return instance of server that can be closed
     * @exception IOException if something goes wrong.
     * @since 0.8
     */
    public static Closeable showDir(String brwsr, File directory, ClassLoader classes, String startpage) throws IOException {
        Launcher l = createBrowser(brwsr);
        if (classes != null) {
            l.addClassLoader(classes);
        }
        l.showDirectory(directory, startpage);
        return (Closeable) l;
    }
    
    /** Starts an HTTP server which provides access to certain directory.
     * The <code>startpage</code> should be relative location inside the root 
     * directory. Opens a browser with URL showing the start page.
     * 
     * @param directory the root directory on disk
     * @param startpage relative path from the root to the page
     * @return instance of server that can be closed
     * @exception IOException if something goes wrong.
     */
    public static Closeable showDir(File directory, String startpage) throws IOException {
        return showDir(null, directory, null, startpage);
    }

    abstract InvocationContext runMethod(InvocationContext c) throws IOException; 


    private static Class<?> loadClass(String cn) throws ClassNotFoundException {
        return Launcher.class.getClassLoader().loadClass(cn);
    }

    void showDirectory(File directory, String startpage) throws IOException {
        throw new UnsupportedOperationException();
    }

    void showURL(String startpage) throws IOException {
        throw new UnsupportedOperationException();
    }

    void addClassLoader(ClassLoader classes) {
        throw new UnsupportedOperationException();
    }
}
