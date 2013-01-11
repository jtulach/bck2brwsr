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
import java.io.IOException;
import java.net.URLClassLoader;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/** An abstraction for executing tests in a Bck2Brwsr virtual machine.
 * Either in JavaScript engine, or in external browser.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class Launcher {

    Launcher() {
    }
    
    abstract MethodInvocation addMethod(Class<?> clazz, String method) throws IOException; 

    public abstract void initialize() throws IOException;
    public abstract void shutdown() throws IOException;
    public MethodInvocation invokeMethod(Class<?> clazz, String method) throws IOException {
        return addMethod(clazz, method);
    }
    
    

    public static Launcher createJavaScript() {
        final JSLauncher l = new JSLauncher();
        l.addClassLoader(Bck2Brwsr.class.getClassLoader());
        return l;
    }
    
    public static Launcher createBrowser(String cmd) {
        final Bck2BrwsrLauncher l = new Bck2BrwsrLauncher(cmd);
        l.addClassLoader(Bck2Brwsr.class.getClassLoader());
        l.setTimeout(180000);
        return l;
    }
    public static Closeable showURL(URLClassLoader classes, String startpage) throws IOException {
        Bck2BrwsrLauncher l = new Bck2BrwsrLauncher(null);
        l.addClassLoader(classes);
        l.showURL(startpage);
        return l;
    }
}
