/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import org.apidesign.bck2brwsr.launcher.Launcher;
import org.apidesign.bck2brwsr.launcher.InvocationContext;
import org.apidesign.bck2brwsr.vmtest.HtmlFragment;
import org.apidesign.bck2brwsr.vmtest.Http;
import org.testng.ITest;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public final class Bck2BrwsrCase implements ITest {
    private final Method m;
    private final Launcher l;
    private final String type;
    private final boolean fail;
    private final HtmlFragment html;
    private final Http.Resource[] http;
    private final InvocationContext c;
    Object value;
    int time;

    Bck2BrwsrCase(Method m, String type, Launcher l, boolean fail, HtmlFragment html, Http.Resource[] http) {
        this.l = l;
        this.m = m;
        this.type = type;
        this.fail = fail;
        this.html = html;
        this.http = http;
        this.c = l != null ? l.createInvocation(m.getDeclaringClass(), m.getName()) : null;
    }

    @Test(groups = "run")
    public void executeCode() throws Throwable {
        if (l != null) {
            if (html != null) {
                c.setHtmlFragment(html.value());
            }
            if (http != null) {
                for (Http.Resource r : http) {
                    if (!r.content().isEmpty()) {
                        InputStream is = new ByteArrayInputStream(r.content().getBytes("UTF-8"));
                        c.addHttpResource(r.path(), r.mimeType(), r.parameters(), is);
                    } else {
                        InputStream is = m.getDeclaringClass().getResourceAsStream(r.resource());
                        c.addHttpResource(r.path(), r.mimeType(), r.parameters(), is);
                    }
                }
            }
            int[] time = { 0 };
            String res = c.invoke(time);
            this.value = res;
            this.time = time[0];
            if (fail) {
                int idx = res == null ? -1 : res.indexOf(':');
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
                long now = System.currentTimeMillis();
                value = m.invoke(m.getDeclaringClass().newInstance());
                time = (int) (System.currentTimeMillis() - now);
            } catch (InvocationTargetException ex) {
                Throwable t = ex.getTargetException();
                value = t.getClass().getName() + ":" + t.getMessage();
                if (t instanceof AssertionError) {
                    throw t;
                }
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

    private static final Map<Class<?>, File> dumps = new WeakHashMap<>();
    final void dumpJS(StringBuilder sb) throws IOException {
        final Class<?> declaringClass = this.m.getDeclaringClass();
        File dumpFile = dumps.get(declaringClass);
        if (dumpFile == null) {
            String outputdir = System.getProperty("vmtest.output");
            String fileName = declaringClass.getSimpleName();
            if (outputdir != null) {
                dumpFile = new File(outputdir, fileName + ".js");
                dumpFile.getParentFile().mkdirs();
            } else {
                dumpFile = File.createTempFile(fileName, ".js");
            }
            try (final Writer w = new OutputStreamWriter(new FileOutputStream(dumpFile), "UTF-8")) {
                w.append(l.toString());
            }

            try (final Writer w = new OutputStreamWriter(new FileOutputStream(indexDumpFile(dumpFile)), "UTF-8")) {
                w.append("<h1>" + m.getName() + "</h1>\n");
                w.append("<script src='" + dumpFile.getName() + "'></script>\n");
            }

            dumps.put(declaringClass, dumpFile);
        }

        sb.append("\nOpen: ").append(indexDumpFile(dumpFile).getPath());
    }

    private static File indexDumpFile(File dumpFile) {
        return new File(dumpFile.getPath().substring(0, dumpFile.getPath().length() - 2) + "html");
    }
}
