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
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class LaunchSetup {
    static LaunchSetup INSTANCE = new LaunchSetup();
    
    private Launcher js;
    private final Map<String,Launcher> brwsrs = new LinkedHashMap<>();
    
    private LaunchSetup() {
    }
    
    public Launcher javaScript() {
        return js(true);
    } 
    private synchronized  Launcher js(boolean create) {
        if (js == null && create) {
            js = Launcher.createJavaScript();
        }
        return js;
    } 
    
    public synchronized Launcher brwsr(String cmd) {
        Launcher s = brwsrs.get(cmd);
        if (s == null) {
            s = Launcher.createBrowser(cmd);
            brwsrs.put(cmd, s);
        }
        return s;
    }

    @BeforeGroups("run")
    public void initializeLauncher() throws IOException {
        if (js(false) != null) {
            js(true).initialize();
        }
        for (Launcher launcher : brwsrs.values()) {
            launcher.initialize();
        }
    }

    @AfterGroups("run")
    public void shutDownLauncher() throws IOException, InterruptedException {
        if (js(false) != null) {
            js(true).shutdown();
        }
        for (Launcher launcher : brwsrs.values()) {
            launcher.shutdown();
        }
    }
}
