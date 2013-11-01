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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import static org.testng.Assert.*;

public final class TestVM {
    private final Invocable code;
    private final CharSequence codeSeq;
    private final Object bck2brwsr;
    private BytesLoader resources;
    
    
    private TestVM(Invocable code, CharSequence codeSeq) throws ScriptException, NoSuchMethodException {
        this.code = code;
        this.codeSeq = codeSeq;
        this.bck2brwsr = ((ScriptEngine)code).eval("bck2brwsr(function(n) { return loader.get(n); })");
        ((ScriptEngine)code).getContext().setAttribute("loader", this, ScriptContext.ENGINE_SCOPE);
    }
    
    public void register(BytesLoader res) {
        this.resources = res;
    }
    
    public byte[] get(String res) throws IOException {
        return resources != null ? resources.get(res) : null;
    }

    public Object execCode(
        String msg, Class<?> clazz, String method, 
        Object expRes, Object... args
    ) throws Exception {
        Object ret = null;
        try {
            ret = code.invokeMethod(bck2brwsr, "loadClass", clazz.getName());
            ret = code.invokeMethod(ret, method, args);
        } catch (ScriptException ex) {
            fail("Execution failed in " + dumpJS(codeSeq) + ": " + ex.getMessage(), ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in " + dumpJS(codeSeq), ex);
        }
        if (ret == null && expRes == null) {
            return null;
        }
        if (expRes != null && expRes.equals(ret)) {
            return null;
        }
        if (expRes instanceof Number) {
            // in case of Long it is necessary convert it to number
            // since the Long is represented by two numbers in JavaScript
            try {
                final Object toFP = ((ScriptEngine)code).eval("Number.prototype.toFP");
                if (ret instanceof Long) {
                    ret = code.invokeMethod(toFP, "call", ret);
                }
                ret = code.invokeFunction("Number", ret);
            } catch (ScriptException ex) {
                fail("Conversion to number failed in " + dumpJS(codeSeq) + ": " + ex.getMessage(), ex);
            } catch (NoSuchMethodException ex) {
                fail("Cannot find global Number(x) function in " + dumpJS(codeSeq) + ": " + ex.getMessage(), ex);
            }
        }
        return ret;
    }
    
    void assertExec(
        String msg, Class clazz, String method, Object expRes, Object... args
    ) throws Exception {
        Object ret = execCode(msg, clazz, method, expRes, args);
        if (ret == null) {
            return;
        }
        if (expRes instanceof Integer && ret instanceof Double) {
            expRes = ((Integer)expRes).doubleValue();
        }
        if (expRes != null && expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + dumpJS(codeSeq));
    }    

    static TestVM compileClass(String... names) throws ScriptException, IOException {
        return compileClass(null, names);
    }
    
    static TestVM compileClass(StringBuilder sb, String... names) throws ScriptException, IOException {
        return compileClass(sb, null, names);
    }

    static TestVM compileClass(StringBuilder sb, ScriptEngine[] eng, String... names) throws ScriptException, IOException {
        if (sb == null) {
            sb = new StringBuilder();
        }
        Bck2Brwsr.generate(sb, new EmulationResources(), names);
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine js = sem.getEngineByExtension("js");
        if (eng != null) {
            eng[0] = js;
        }
        try {
            Object res = js.eval(sb.toString());
            assertTrue(js instanceof Invocable, "It is invocable object: " + res);
            return new TestVM((Invocable) js, sb);
        } catch (Exception ex) {
            if (sb.length() > 2000) {
                sb = dumpJS(sb);
            }
            fail("Could not evaluate:" + ex.getClass() + ":" + ex.getMessage() + "\n" + sb, ex);
            return null;
        }
    }

    Object loadClass(String loadClass, String name) throws ScriptException, NoSuchMethodException {
        return code.invokeMethod(bck2brwsr, "loadClass", Exceptions.class.getName());
    }
    
    Object invokeMethod(Object obj, String method, Object... params) throws ScriptException, NoSuchMethodException {
        return code.invokeMethod(obj, method, params);
    }

    Object invokeFunction(String methodName, Object... args) throws ScriptException, NoSuchMethodException {
        return code.invokeFunction(methodName, args);
    }

    static StringBuilder dumpJS(CharSequence sb) throws IOException {
        File f = File.createTempFile("execution", ".js");
        FileWriter w = new FileWriter(f);
        w.append(sb);
        w.close();
        return new StringBuilder(f.getPath());
    }

    @Override
    public String toString() {
        try {
            return dumpJS(codeSeq).toString();
        } catch (IOException ex) {
            return ex.toString();
        }
    }
    
    
    private static class EmulationResources implements Bck2Brwsr.Resources {
        @Override
        public InputStream get(String name) throws IOException {
            if ("java/net/URI.class".equals(name)) {
                // skip
                return null;
            }
            if ("java/net/URLConnection.class".equals(name)) {
                // skip
                return null;
            }
            if ("java/lang/System.class".equals(name)) {
                // skip
                return null;
            }
            Enumeration<URL> en = StaticMethodTest.class.getClassLoader().getResources(name);
            URL u = null;
            while (en.hasMoreElements()) {
                u = en.nextElement();
            }
            if (u == null) {
                throw new IOException("Can't find " + name);
            }
            if (u.toExternalForm().contains("rt.jar!")) {
                throw new IOException("No emulation for " + u);
            }
            return u.openStream();
        }
    }
}
