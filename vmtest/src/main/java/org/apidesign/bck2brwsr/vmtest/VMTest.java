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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.WeakHashMap;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apidesign.bck2brwsr.launcher.Bck2BrwsrLauncher;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** A TestNG {@link Factory} that seeks for {@link Compare} annotations
 * in provided class and builds set of tests that compare the computations
 * in real as well as JavaScript virtual machines. Use as:<pre>
 * {@code @}{@link Factory} public static create() {
 *   return @{link VMTest}.{@link #create(YourClass.class);
 * }</pre>
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class VMTest implements ITest {
    private final Run first, second;
    private final Method m;
    
    private VMTest(Method m, Run first, Run second) {
        this.first = first;
        this.second = second;
        this.m = m;
    }

    /** Inspects <code>clazz</code> and for each {@lik Compare} method creates
     * instances of tests. Each instance runs the test in different virtual
     * machine and at the end they compare the results.
     * 
     * @param clazz the class to inspect
     * @return the set of created tests
     */
    public static Object[] create(Class<?> clazz) {
        Method[] arr = clazz.getMethods();
        Object[] ret = new Object[5 * arr.length];
        int cnt = 0;
        for (Method m : arr) {
            Compare c = m.getAnnotation(Compare.class);
            if (c == null) {
                continue;
            }
            final Run real = new Run(m, 0);
            final Run js = new Run(m, 1);
            final Run brwsr = new Run(m, 2);
            ret[cnt++] = real;
            ret[cnt++] = js;
            ret[cnt++] = brwsr;
            ret[cnt++] = new VMTest(m, real, js);
            ret[cnt++] = new VMTest(m, real, brwsr);
        }
        Object[] r = new Object[cnt];
        for (int i = 0; i < cnt; i++) {
            r[i] = ret[i];
        }
        return r;
    }

    /** Test that compares the previous results.
     * @throws Throwable 
     */
    @Test(dependsOnGroups = "run") public void compareResults() throws Throwable {
        Object v1 = first.value;
        Object v2 = second.value;
        if (v1 instanceof Number) {
            v1 = ((Number)v1).doubleValue();
        }
        Assert.assertEquals(v2, v1, "Comparing results");
    }
    
    /** Test name.
     * @return name of the tested method followed by a suffix
     */
    @Override
    public String getTestName() {
        return m.getName() + "[Compare " + second.typeName() + "]";
    }

    /** Helper method that inspects the classpath and loads given resource
     * (usually a class file). Used while running tests in Rhino.
     * 
     * @param name resource name to find
     * @return the array of bytes in the given resource
     * @throws IOException I/O in case something goes wrong
     */
    public static byte[] read(String name) throws IOException {
        URL u = null;
        Enumeration<URL> en = VMTest.class.getClassLoader().getResources(name);
        while (en.hasMoreElements()) {
            u = en.nextElement();
        }
        if (u == null) {
            throw new IOException("Can't find " + name);
        }
        try (InputStream is = u.openStream()) {
            byte[] arr;
            arr = new byte[is.available()];
            int offset = 0;
            while (offset < arr.length) {
                int len = is.read(arr, offset, arr.length - offset);
                if (len == -1) {
                    throw new IOException("Can't read " + name);
                }
                offset += len;
            }
            return arr;
        }
    }
   
    public static final class Run implements ITest {
        private final Method m;
        private final int type;
        Object value;
        private Invocable code;
        private CharSequence codeSeq;
        private static final Map<Class,Object[]> compiled = new WeakHashMap<>();

        private Run(Method m, int type) {
            this.m = m;
            this.type = type;
        }

        private void compileTheCode(Class<?> clazz) throws Exception {
            final Object[] data = compiled.get(clazz);
            if (data != null) {
                code = (Invocable) data[0];
                codeSeq = (CharSequence) data[1];
                return;
            }
            StringBuilder sb = new StringBuilder();
            Bck2Brwsr.generate(sb, VMTest.class.getClassLoader());

            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine mach = sem.getEngineByExtension("js");
            
            sb.append(
                  "\nvar vm = bck2brwsr(org.apidesign.bck2brwsr.vmtest.VMTest.read);"
                + "\nfunction initVM() { return vm; };"
                + "\n");

            Object res = mach.eval(sb.toString());
            Assert.assertTrue(mach instanceof Invocable, "It is invocable object: " + res);
            code = (Invocable) mach;
            codeSeq = sb;
            compiled.put(clazz, new Object[] { code, codeSeq });
        }

        @Test(groups = "run") public void executeCode() throws Throwable {
            if (type == 1) {
                try {
                    compileTheCode(m.getDeclaringClass());
                    Object vm = code.invokeFunction("initVM");
                    Object inst = code.invokeMethod(vm, "loadClass", m.getDeclaringClass().getName());
                    value = code.invokeMethod(inst, m.getName() + "__" + computeSignature(m));
                } catch (Exception ex) {
                    throw new AssertionError(dumpJS(codeSeq)).initCause(ex);
                }
            } else if (type == 2) {
                Bck2BrwsrLauncher l = new Bck2BrwsrLauncher();
                l.setTimeout(5000);
                Bck2BrwsrLauncher.MethodInvocation c = l.addMethod(m.getDeclaringClass(), m.getName());
                l.execute();
                value = c.toString();
            } else {
                value = m.invoke(m.getDeclaringClass().newInstance());
            }
        }
        @Override
        public String getTestName() {
            return m.getName() + "[" + typeName() + "]";
        }
        
        final String typeName() {
            switch (type) {
                case 0: return "Java";
                case 1: return "JavaScript";
                case 2: return "Browser";
                default: return "Unknown type " + type;
            }
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
    
    static StringBuilder dumpJS(CharSequence sb) throws IOException {
        File f = File.createTempFile("execution", ".js");
        try (FileWriter w = new FileWriter(f)) {
            w.append(sb);
        }
        return new StringBuilder(f.getPath());
    }
}
