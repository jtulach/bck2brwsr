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
package org.apidesign.bck2brwsr.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/**
 * Tests execution in Java's internal scripting engine.
 */
final class JSLauncher extends Launcher {
    private static final Logger LOG = Logger.getLogger(JSLauncher.class.getName());
    private Set<ClassLoader> loaders = new LinkedHashSet<>();
    private final Res resources = new Res();
    private Invocable code;
    private StringBuilder codeSeq;
    private Object console;
    
    
    @Override MethodInvocation addMethod(Class<?> clazz, String method, String html) {
        loaders.add(clazz.getClassLoader());
        MethodInvocation mi = new MethodInvocation(clazz.getName(), method, html);
        try {
            long time = System.currentTimeMillis();
            LOG.log(Level.FINE, "Invoking {0}.{1}", new Object[]{mi.className, mi.methodName});
            String res = code.invokeMethod(
                console,
                "invoke__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2",
                mi.className, mi.methodName).toString();
            time = System.currentTimeMillis() - time;
            LOG.log(Level.FINE, "Resut of {0}.{1} = {2} in {3} ms", new Object[]{mi.className, mi.methodName, res, time});
            mi.result(res, null);
        } catch (ScriptException | NoSuchMethodException ex) {
            mi.result(null, ex);
        }
        return mi;
    }
    
    public void addClassLoader(ClassLoader url) {
        this.loaders.add(url);
    }

    @Override
    public void initialize() throws IOException {
        try {
            initRhino();
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            throw new IOException(ex);
        }
    }
    
    private void initRhino() throws IOException, ScriptException, NoSuchMethodException {
        StringBuilder sb = new StringBuilder();
        Bck2Brwsr.generate(sb, new Res());

        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine mach = sem.getEngineByExtension("js");

        sb.append(
              "\nvar vm = new bck2brwsr(org.apidesign.bck2brwsr.launcher.Console.read);"
            + "\nfunction initVM() { return vm; };"
            + "\n");

        Object res = mach.eval(sb.toString());
        if (!(mach instanceof Invocable)) {
            throw new IOException("It is invocable object: " + res);
        }
        code = (Invocable) mach;
        codeSeq = sb;
        
        Object vm = code.invokeFunction("initVM");
        console = code.invokeMethod(vm, "loadClass", Console.class.getName());
    }

    @Override
    public void shutdown() throws IOException {
    }

    @Override
    public String toString() {
        return codeSeq.toString();
    }
    
    private class Res implements Bck2Brwsr.Resources {
        @Override
        public InputStream get(String resource) throws IOException {
            for (ClassLoader l : loaders) {
                URL u = null;
                Enumeration<URL> en = l.getResources(resource);
                while (en.hasMoreElements()) {
                    u = en.nextElement();
                }
                if (u != null) {
                    return u.openStream();
                }
            }
            throw new IOException("Can't find " + resource);
        }
    }
}
