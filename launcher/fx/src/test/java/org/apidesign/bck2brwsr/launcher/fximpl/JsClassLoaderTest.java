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
package org.apidesign.bck2brwsr.launcher.fximpl;

import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apidesign.html.boot.spi.Fn;
import org.apidesign.html.boot.impl.FindResources;
import org.apidesign.html.boot.impl.FnUtils;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public class JsClassLoaderTest {
    private static ClassLoader loader;
    private static Class<?> methodClass;
    
    public JsClassLoaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ScriptEngineManager sem = new ScriptEngineManager();
        final ScriptEngine eng = sem.getEngineByMimeType("text/javascript");
        
        final URL my = JsClassLoaderTest.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader parent = JsClassLoaderTest.class.getClassLoader().getParent();
        final URLClassLoader ul = new URLClassLoader(new URL[] { my }, parent);
        class Fr implements FindResources, Fn.Presenter {
            @Override
            public void findResources(String path, Collection<? super URL> results, boolean oneIsEnough) {
                URL u = ul.getResource(path);
                if (u != null) {
                    results.add(u);
                }
            }

            @Override
            public Fn defineFn(String code, String... names) {
                StringBuilder sb = new StringBuilder();
                sb.append("(function() {");
                sb.append("return function(");
                String sep = "";
                for (String n : names) {
                    sb.append(sep);
                    sb.append(n);
                    sep = ", ";
                }
                sb.append(") {");
                sb.append(code);
                sb.append("};");
                sb.append("})()");
                try {
                    final Object val = eng.eval(sb.toString());
                    return new Fn() {
                        @Override
                        public Object invoke(Object thiz, Object... args) throws Exception {
                            List<Object> all = new ArrayList<Object>(args.length + 1);
                            all.add(thiz == null ? val : thiz);
                            all.addAll(Arrays.asList(args));
                            Invocable inv = (Invocable)eng;
                            Object ret = inv.invokeMethod(val, "call", all.toArray());
                            return ret == val ? null : ret;
                        }
                    };
                } catch (ScriptException ex) {
                    throw new LinkageError("Can't parse: " + sb, ex);
                }
            }

            @Override
            public void displayPage(URL page, Runnable onPageLoad) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void loadScript(Reader code) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }
        
        loader = FnUtils.newLoader(new Fr(), new Fr(), parent);
        methodClass = loader.loadClass(JsMethods.class.getName());
    }
    
    @Test public void noParamMethod() throws Throwable {
        Method plus = methodClass.getMethod("fortyTwo");
        try {
            final Object val = plus.invoke(null);
            assertTrue(val instanceof Number, "A number returned " + val);
            assertEquals(((Number)val).intValue(), 42);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void testExecuteScript() throws Throwable {
        Method plus = methodClass.getMethod("plus", int.class, int.class);
        try {
            assertEquals(plus.invoke(null, 10, 20), 30);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    @Test public void overloadedMethod() throws Throwable {
        Method plus = methodClass.getMethod("plus", int.class);
        try {
            assertEquals(plus.invoke(null, 10), 10);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void instanceMethod() throws Throwable {
        Method plus = methodClass.getMethod("plusInst", int.class);
        Object inst = methodClass.newInstance();
        try {
            assertEquals(plus.invoke(inst, 10), 10);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
    @Test public void staticThis() throws Throwable {
        Method st = methodClass.getMethod("staticThis");
        try {
            assertNull(st.invoke(null));
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    @Test public void getThis() throws Throwable {
        Object th = methodClass.newInstance();
        Method st = methodClass.getMethod("getThis");
        try {
            assertEquals(st.invoke(th), th);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    
}