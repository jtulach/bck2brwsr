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
package org.apidesign.bck2brwsr.vmtest.impl;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apidesign.bck2brwsr.launcher.Launcher;
import org.apidesign.bck2brwsr.launcher.MethodInvocation;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class LaunchSetup {
    private static final LaunchSetup JS = new LaunchSetup(Launcher.createJavaScript());
    private static final Map<String,LaunchSetup> BRWSRS = new LinkedHashMap<>();
    
    private final Launcher launcher;
    
    private LaunchSetup(Launcher l) {
        launcher = l;
    }
    
    public static LaunchSetup javaScript() {
        return JS;
    } 
    
    public static synchronized LaunchSetup brwsr(String cmd) {
        LaunchSetup s = BRWSRS.get(cmd);
        if (s == null) {
            s = new LaunchSetup(Launcher.createBrowser(cmd));
            BRWSRS.put(cmd, s);
        }
        return s;
    }

    @BeforeGroups("run")
    public void initializeLauncher() throws IOException {
        launcher.initialize();
    }

    @AfterGroups("run")
    public void shutDownLauncher() throws IOException, InterruptedException {
        launcher.shutdown();
    }

    public MethodInvocation invokeMethod(Class<?> clazz, String name) throws IOException {
        return launcher.invokeMethod(clazz, name);
    }
}
