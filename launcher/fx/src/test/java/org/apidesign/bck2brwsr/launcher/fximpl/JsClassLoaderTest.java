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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
        
        URL my = JsClassLoaderTest.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader parent = JsClassLoaderTest.class.getClassLoader().getParent();
        loader = new JsClassLoader(new URL[] { my }, parent) {
            @Override
            protected Fn defineFn(String code, String... names) {
                StringBuilder sb = new StringBuilder();
                sb.append("(function() {");
                sb.append("var r = {};");
                sb.append("r.fn = function(");
                String sep = "";
                for (String n : names) {
                    sb.append(sep);
                    sb.append(n);
                    sep = ", ";
                }
                sb.append(") {");
                sb.append(code);
                sb.append("};");
                sb.append("return r;");
                sb.append("})()");
                try {
                    final Object val = eng.eval(sb.toString());
                    return new Fn() {
                        @Override
                        public Object invoke(Object... args) throws Exception {
                            Invocable inv = (Invocable)eng;
                            return inv.invokeMethod(val, "fn", args);
                        }
                    };
                } catch (ScriptException ex) {
                    throw new LinkageError("Can't parse: " + sb, ex);
                }
            }
        };
        
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
    /*
    @Test public void testExecuteScript() throws Throwable {
        Method plus = methodClass.getMethod("plus", int.class, int.class);
        try {
            assertEquals(plus.invoke(null, 10, 20), 30);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }
    */

}