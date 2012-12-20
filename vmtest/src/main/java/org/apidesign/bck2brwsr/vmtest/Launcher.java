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

import org.apidesign.bck2brwsr.launcher.Bck2BrwsrLauncher;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Launcher {
    private final String sen;
    private Bck2BrwsrLauncher launcher;
    
    Launcher() {
        this(null);
    }
    Launcher(String sen) {
        this.sen = sen;
    }

    synchronized Bck2BrwsrLauncher clear() {
        Bck2BrwsrLauncher l = launcher;
        launcher = null;
        return l;
    }

    synchronized Bck2BrwsrLauncher.MethodInvocation addMethod(Class<?> clazz, String name) {
        if (launcher == null) {
            launcher = new Bck2BrwsrLauncher();
            launcher.setTimeout(5000);
            if (sen != null) {
                launcher.setScriptEngineName(sen);
            }
        }
        return launcher.addMethod(clazz, name);
    }

    void exec() throws Exception {
        Bck2BrwsrLauncher l = clear();
        if (l != null) {
            l.execute();
        }
    }
    
}
