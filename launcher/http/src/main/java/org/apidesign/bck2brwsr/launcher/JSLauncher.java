/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import org.apidesign.bck2brwsr.launcher.impl.Console;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/**
 * Tests execution in Java's internal scripting engine.
 */
@ExtraJavaScript(processByteCode = false, resource="")
final class JSLauncher extends Launcher {
    private static final Logger LOG = Logger.getLogger(JSLauncher.class.getName());

    private Set<ClassLoader> loaders = new LinkedHashSet<>();
    private Invocable code;
    private List<Seq> codeSeq = new LinkedList<>();
    private Object console;

    JSLauncher() {
        addClassLoader(Bck2Brwsr.class.getClassLoader());
    }

    @Override InvocationContext runMethod(InvocationContext mi) {
        loaders.add(mi.clazz.getClassLoader());
        try {
            long time = System.currentTimeMillis();
            LOG.log(Level.FINE, "Invoking {0}.{1}", new Object[]{mi.clazz.getName(), mi.methodName});
            String res = code.invokeMethod(
                console,
                "invoke__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2",
                mi.clazz.getName(), mi.methodName).toString();
            time = System.currentTimeMillis() - time;
            LOG.log(Level.FINE, "Resut of {0}.{1} = {2} in {3} ms", new Object[]{mi.clazz.getName(), mi.methodName, res, time});
            mi.result(res, (int)time, null);
        } catch (ScriptException | NoSuchMethodException ex) {
            mi.result(null, -1, ex);
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

    private static ScriptEngine createEngine() {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine truffleJS = sem.getEngineByName("Graal.js");
        if (truffleJS != null) {
            return truffleJS;
        }
        ScriptEngine js = sem.getEngineByExtension("js");
        return js;
    }

    private void initRhino() throws IOException, ScriptException, NoSuchMethodException {
        StringBuilder sb = new StringBuilder();
        Bck2Brwsr.newCompiler()
            .standalone(false)
            .resources(new Res())
            .generate(sb);

        ScriptEngine mach = createEngine();
        if (!(mach instanceof Invocable)) {
            throw new IOException("It is invocable object: " + mach);
        }
        code = (Invocable) mach;

        sb.append(
              "function initVM(console) {\n"
            + "  return new bck2brwsr(function(res, skip) {\n"
            + "    return console.read(res, skip);"
            + "  });\n"
            + "};"
        );
        sb.append("atob = function(s) { return new String(org.apidesign.bck2brwsr.launcher.impl.Console.parseBase64Binary(s)); }");
        codeSeq.add(new Eval(null, sb));

        Object vm = code.invokeFunction("initVM", new Ldr());
        console = code.invokeMethod(vm, "loadClass", Console.class.getName());
    }

    @Override
    public void shutdown() throws IOException {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Seq seq : codeSeq) {
            sb.append(seq.toString());
        }
        return sb.toString();
    }

    private static URL getResource(String resource, int skip) throws IOException {
        URL u = null;
        Enumeration<URL> en = Console.class.getClassLoader().getResources(resource);
        while (en.hasMoreElements()) {
            final URL now = en.nextElement();
            if (--skip < 0) {
                u = now;
                break;
            }
        }
        return u;
    }

    public final class Ldr {
        public byte[] read(String name, int skip) throws IOException, ScriptException {
            URL u = null;
            if (!name.endsWith(".class")) {
                u = getResource(name, skip);
                if (u == null) {
                    return null;
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

            Enumeration<URL> en = Console.class.getClassLoader().getResources(name);
            while (en.hasMoreElements()) {
                u = en.nextElement();
            }
            if (u == null) {
                throw new IOException("Can't find " + name);
            }
            if ("jar".equals(u.getProtocol())) {
                u = ((JarURLConnection)u.openConnection()).getJarFileURL();
            }
            String compile = CompileCP.compileFromClassPath(u, null);
            LOG.fine("// eval: " + u);
            LOG.log(Level.FINEST, compile);
            new Eval(u, compile);
            LOG.finer("// end of " + u);
            return null;
        }
    }

    private interface Seq {
    }

    private class Eval implements Seq {
        public final URL src;
        public final String txt;

        public Eval(URL src, Object c) throws ScriptException {
            this.src = src;
            this.txt = c.toString();
            ((ScriptEngine)JSLauncher.this.code).eval(this.txt);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (src != null) {
                sb.append("// loading ").append(src).append("\n");
            }
            sb.append(txt);
            if (src != null) {
                sb.append("// end of ").append(src).append("\n");
            }
            return sb.toString();
        }


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
                    if (u.toExternalForm().contains("/rt.jar")) {
                        LOG.log(Level.WARNING, "No fallback to bootclasspath for {0}", u);
                        return null;
                    }
                    return u.openStream();
                }
            }
            throw new IOException("Can't find " + resource);
        }
    }
}
