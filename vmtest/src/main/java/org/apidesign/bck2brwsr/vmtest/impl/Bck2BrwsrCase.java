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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apidesign.bck2brwsr.launcher.Launcher;
import org.apidesign.bck2brwsr.launcher.InvocationContext;
import org.apidesign.bck2brwsr.vmtest.HtmlFragment;
import org.apidesign.bck2brwsr.vmtest.HttpResource;
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
    private final boolean fail;
    private final HtmlFragment html;
    private final HttpResource http;
    Object value;

    Bck2BrwsrCase(Method m, String type, Launcher l, boolean fail, HtmlFragment html, HttpResource http) {
        this.l = l;
        this.m = m;
        this.type = type;
        this.fail = fail;
        this.html = html;
        this.http = http;
    }

    @Test(groups = "run")
    public void executeCode() throws Throwable {
        if (l != null) {
            InvocationContext c = l.createInvocation(m.getDeclaringClass(), m.getName());
            if (html != null) {
                c.setHtmlFragment(html.value());
            }
            if (http != null) {
                c.setHttpResource(http.path(), http.mimeType(), http.content());
            }
            String res = c.invoke();
            value = res;
            if (fail) {
                int idx = res.indexOf(':');
                if (idx >= 0) {
                    Class<? extends Throwable> thrwbl = null;
                    try {
                        Class<?> exCls = Class.forName(res.substring(0, idx));
                        if (Throwable.class.isAssignableFrom(exCls)) {
                            thrwbl = exCls.asSubclass(Throwable.class);
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                    if (thrwbl != null) {
                        Throwable t = null;
                        try {
                            for (Constructor<?> cnstr : thrwbl.getConstructors()) {
                                if (cnstr.getParameterTypes().length == 1 && cnstr.getParameterTypes()[0].isAssignableFrom(String.class)) {
                                    t = (Throwable) cnstr.newInstance(res.substring(idx + 1));
                                    break;
                                }
                            }
                        } catch (Throwable ex) {
                            t = thrwbl.newInstance().initCause(ex);
                        }
                        if (t == null) {
                            t = thrwbl.newInstance().initCause(new Exception(res.substring(idx)));
                        }
                        throw t;
                    }
                    throw new AssertionError(res);
                }
            }
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
