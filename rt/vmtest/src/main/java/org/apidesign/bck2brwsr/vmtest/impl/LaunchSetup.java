/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
            final String p = System.getProperty("vmtest.js", "script"); // NOI18N
            switch (p) {
                case "brwsr": // NOI18N
                    String cmd = null;
                    String pb = System.getProperty("vmtest.brwsrs"); // NOI18N
                    if (pb != null) {
                        cmd = pb.split(",")[0]; // NOI18N
                    }
                    js = brwsr(cmd);
                    break;
                case "script": js = Launcher.createJavaScript(); break; // NOI18N
                default: throw new IllegalArgumentException(
                    "Unknown value of vmtest.js property: " + p
                );
            }
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
        Set<Launcher> all = new HashSet<>(brwsrs.values());
        if (js(false) != null) {
            all.add(js(true));
        }
        for (Launcher launcher : all) {
            launcher.initialize();
        }
    }

    @AfterGroups("run")
    public void shutDownLauncher() throws IOException, InterruptedException {
        Set<Launcher> all = new HashSet<>(brwsrs.values());
        if (js(false) != null) {
            all.add(js(true));
        }
        for (Launcher launcher : all) {
            launcher.shutdown();
        }
    }
}
