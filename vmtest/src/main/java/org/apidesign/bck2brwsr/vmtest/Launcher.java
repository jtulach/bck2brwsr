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
package org.apidesign.bck2brwsr.vmtest;

import java.io.IOException;
import org.apidesign.bck2brwsr.launcher.Bck2BrwsrLauncher;
import org.apidesign.bck2brwsr.launcher.JSLauncher;
import org.apidesign.bck2brwsr.launcher.MethodInvocation;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Launcher {
    private final String sen;
    private Object launcher;
    
    Launcher() {
        this(null);
    }
    Launcher(String sen) {
        this.sen = sen;
    }

    synchronized Object clear() {
        Object l = launcher;
        launcher = null;
        return l;
    }

    synchronized MethodInvocation addMethod(Class<?> clazz, String name) throws IOException {
        if (launcher == null) {
            if (sen != null) {
                JSLauncher js = new JSLauncher();
                js.addClassLoader(clazz.getClassLoader());
                js.initialize();
                launcher = js;
            } else {
                Bck2BrwsrLauncher l = new Bck2BrwsrLauncher();
                l.setTimeout(180000);
                launcher = l;
            }
        }
        if (launcher instanceof JSLauncher) {
            return ((JSLauncher)launcher).addMethod(clazz, name);
        } else {
            return ((Bck2BrwsrLauncher)launcher).addMethod(clazz, name);
        }
    }

    void exec() throws Exception {
        Object l = clear();
        if (l instanceof Bck2BrwsrLauncher) {
            ((Bck2BrwsrLauncher)l).execute();
        }
    }
    
}
