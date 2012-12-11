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
package org.apidesign.vm4brwsr;

import java.lang.reflect.Method;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** A TestNG {@link Factory} that seeks for {@link Compare} annotations
 * in provided class and builds set of tests that compare the computations
 * in real as well as JavaScript virtual machines. Use as:<pre>
 * {@code @}{@link Factory} public static create() {
 *   return @{link CompareVMs}.{@link #create(YourClass.class);
 * }</pre>
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class CompareVMs implements ITest {
    private final Run first, second;
    private final Method m;
    
    private CompareVMs(Method m, Run first, Run second) {
        this.first = first;
        this.second = second;
        this.m = m;
    }

    public static Object[] create(Class<?> clazz) {
        Method[] arr = clazz.getMethods();
        Object[] ret = new Object[3 * arr.length];
        int cnt = 0;
        for (Method m : arr) {
            Compare c = m.getAnnotation(Compare.class);
            if (c == null) {
                continue;
            }
            final Run real = new Run(m, false);
            final Run js = new Run(m, true);
            ret[cnt++] = real;
            ret[cnt++] = js;
            ret[cnt++] = new CompareVMs(m, real, js);
        }
        Object[] r = new Object[cnt];
        for (int i = 0; i < cnt; i++) {
            r[i] = ret[i];
        }
        return r;
    }
    
    @Test(dependsOnGroups = "run") public void compareResults() throws Throwable {
        Object v1 = first.value;
        Object v2 = second.value;
        if (v1 instanceof Number) {
            v1 = ((Number)v1).doubleValue();
        }
        Assert.assertEquals(v1, v2, "Comparing results");
    }
    
    @Override
    public String getTestName() {
        return m.getName() + "[Compare]";
    }
    
    public static final class Run implements ITest {
        private final Method m;
        private final boolean js;
        Object value;
        private static Invocable code;
        private static CharSequence codeSeq;

        private Run(Method m, boolean js) {
            this.m = m;
            this.js = js;
        }

        private static void compileTheCode(Class<?> clazz) throws Exception {
            if (code != null) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            Bck2Brwsr.generate(sb, CompareVMs.class.getClassLoader());

            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine js = sem.getEngineByExtension("js");
            js.getContext().setAttribute("loader", new BytesLoader(), ScriptContext.ENGINE_SCOPE);

            Object res = js.eval(sb.toString());
            Assert.assertTrue(js instanceof Invocable, "It is invocable object: " + res);
            code = (Invocable) js;
            codeSeq = sb;
        }

        @Test(groups = "run") public void executeCode() throws Throwable {
            if (js) {
                try {
                    compileTheCode(m.getDeclaringClass());
                    Object vm = code.invokeFunction("bck2brwsr");
                    Object inst = code.invokeMethod(vm, "loadClass", m.getDeclaringClass().getName());
                    value = code.invokeMethod(inst, m.getName() + "__I");
                } catch (Exception ex) {
                    throw new AssertionError(StaticMethodTest.dumpJS(codeSeq)).initCause(ex);
                }
            } else {
                value = m.invoke(m.getDeclaringClass().newInstance());
            }
        }
        @Override
        public String getTestName() {
            return m.getName() + (js ? "[JavaScript]" : "[Java]");
        }
    }
}
