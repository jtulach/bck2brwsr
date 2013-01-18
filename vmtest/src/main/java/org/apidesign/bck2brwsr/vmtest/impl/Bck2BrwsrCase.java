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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import org.apidesign.bck2brwsr.launcher.Launcher;
import org.apidesign.bck2brwsr.launcher.MethodInvocation;
import org.testng.ITest;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Bck2BrwsrCase implements ITest {
    private final Method m;
    private final Launcher l;
    private final String type;
    Object value;
    private static final Map<Class, Object[]> compiled = new WeakHashMap<>();
    private Object inst;

    Bck2BrwsrCase(Method m, String type, Launcher l) {
        this.l = l;
        this.m = m;
        this.type = type;
    }

    @Test(groups = "run")
    public void executeCode() throws Throwable {
        if (l != null) {
            MethodInvocation c = l.invokeMethod(m.getDeclaringClass(), m.getName());
            value = c.toString();
        } else {
            try {
                value = m.invoke(m.getDeclaringClass().newInstance());
            } catch (InvocationTargetException ex) {
                Throwable t = ex.getTargetException();
                value = t.getClass().getName() + ":" + t.getMessage();
            }
        }
    }

    @Override
    public String getTestName() {
        return m.getName() + "[" + typeName() + "]";
    }

    final String typeName() {
        return type;
    }
    static void dumpJS(StringBuilder sb, Bck2BrwsrCase c) throws IOException {
        File f = File.createTempFile(c.m.getName(), ".js");
        try (final FileWriter w = new FileWriter(f)) {
            w.append(c.l.toString());
        }
        sb.append("Path: ").append(f.getPath());
    }
}
