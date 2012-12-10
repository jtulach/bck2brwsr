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
import java.util.Map;
import java.util.WeakHashMap;
import javax.script.Invocable;
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
        Assert.assertEquals(v2, v1, "Comparing results");
    }
    
    @Override
    public String getTestName() {
        return m.getName() + "[Compare]";
    }
    
    public static final class Run implements ITest {
        private final Method m;
        private final boolean js;
        Object value;
        private Invocable code;
        private CharSequence codeSeq;
        private static final Map<Class,Object[]> compiled = new WeakHashMap<Class,Object[]>();

        private Run(Method m, boolean js) {
            this.m = m;
            this.js = js;
        }

        private void compileTheCode(Class<?> clazz) throws Exception {
            final Object[] data = compiled.get(clazz);
            if (data != null) {
                code = (Invocable) data[0];
                codeSeq = (CharSequence) data[1];
                return;
            }
            StringBuilder sb = new StringBuilder();
            class SkipMe extends GenJS {

                public SkipMe(Appendable out) {
                    super(out);
                }

                @Override
                protected boolean requireReference(String cn) {
                    if (cn.contains("CompareVMs")) {
                        return true;
                    }
                    return super.requireReference(cn);
                }
            }
            SkipMe sm = new SkipMe(sb);
            sm.doCompile(CompareVMs.class.getClassLoader(), StringArray.asList(
                clazz.getName().replace('.', '/')));

            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine js = sem.getEngineByExtension("js");

            Object res = js.eval(sb.toString());
            Assert.assertTrue(js instanceof Invocable, "It is invocable object: " + res);
            code = (Invocable) js;
            codeSeq = sb;
            compiled.put(clazz, new Object[] { code, codeSeq });
        }

        @Test(groups = "run") public void executeCode() throws Throwable {
            if (js) {
                try {
                    compileTheCode(m.getDeclaringClass());
                    Object inst = code.invokeFunction(m.getDeclaringClass().getName().replace('.', '_'), false);
                    value = code.invokeMethod(inst, m.getName() + "__" + computeSignature(m));
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
        
        private static String computeSignature(Method m) {
            StringBuilder sb = new StringBuilder();
            appendType(sb, m.getReturnType());
            for (Class<?> c : m.getParameterTypes()) {
                appendType(sb, c);
            }
            return sb.toString();
        }
        
        private static void appendType(StringBuilder sb, Class<?> t) {
            if (t == null) {
                sb.append('V');
                return;
            }
            if (t.isPrimitive()) {
                int ch = -1;
                if (t == int.class) {
                    ch = 'I';
                }
                if (t == short.class) {
                    ch = 'S';
                }
                if (t == byte.class) {
                    ch = 'B';
                }
                if (t == boolean.class) {
                    ch = 'Z';
                }
                if (t == long.class) {
                    ch = 'J';
                }
                if (t == float.class) {
                    ch = 'F';
                }
                if (t == double.class) {
                    ch = 'D';
                }
                assert ch != -1 : "Unknown primitive type " + t;
                sb.append((char)ch);
                return;
            }
            if (t.isArray()) {
                sb.append("_3");
                appendType(sb, t.getComponentType());
                return;
            }
            sb.append('L');
            sb.append(t.getName().replace('.', '_'));
            sb.append("_2");
        }
    }
}
