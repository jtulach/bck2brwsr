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
import org.apidesign.bck2brwsr.launcher.Bck2BrwsrLauncher;
import org.apidesign.bck2brwsr.launcher.JSLauncher;
import org.apidesign.bck2brwsr.launcher.MethodInvocation;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeSuite;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Launchers {
    public static final Launchers INSTANCE = new Launchers();
    
    private JSLauncher jsl;
    private Bck2BrwsrLauncher brwsr;
    
    private Launchers() {
    }

    @BeforeGroups("run")
    public void initializeLauncher() throws IOException {
        jsl = new JSLauncher();
        jsl.addClassLoader(Bck2Brwsr.class.getClassLoader());
        jsl.initialize();
        Bck2BrwsrLauncher l = new Bck2BrwsrLauncher();
        l.addClassLoader(Bck2Brwsr.class.getClassLoader());
        l.initialize();
        l.setTimeout(180000);
        brwsr = l;
    }

    @AfterGroups("run")
    public void shutDownLauncher() throws IOException, InterruptedException {
        brwsr.shutdown();
    }

    public MethodInvocation addMethod(Class<?> clazz, String name, boolean inBrwsr) throws IOException {
        if (!inBrwsr) {
            return jsl.addMethod(clazz, name);
        } else {
            return brwsr.addMethod(clazz, name);
        }
    }
}
