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
package org.apidesign.bck2brwsr.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.java.JavaInterop;
import com.oracle.truffle.api.source.Source;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import org.apidesign.bck2brwsr.aot.Bck2BrwsrJars;
import org.apidesign.vm4brwsr.Bck2Brwsr;

final class VM {
    final TruffleLanguage.Env env;
    private Object vm;
    private LoadClass loadClass;

    VM(TruffleLanguage.Env env) {
        this.env = env;
    }

    void initialize() {
        try {
            StringBuilder sb = new StringBuilder();
            Bck2Brwsr.newCompiler().standalone(false).generate(sb);

            Source bck2brwsr = Source.newBuilder(sb.toString()).mimeType("text/javascript").name("bck2brwsr.js").build();

            CallTarget vmInit = env.parse(bck2brwsr);
            vmInit.call();

            URL rt = VM.class.getResource("/emul-1.0-SNAPSHOT-debug.js");
            assert rt != null;

            Source rtJs = Source.newBuilder(rt).name("rt.js").mimeType("text/javascript").build();

            CallTarget rtInit = env.parse(rtJs);
            rtInit.call();

            Source atob = Source.newBuilder(
                "atob = function(s) {\n"
              + "  return new String(org.apidesign.bck2brwsr.truffle.Bck2BrwsrLanguage.parseBase64Binary(s));\n"
              + "}\n"
            ).name("atob.js").mimeType("text/javascript").build();
            env.parse(atob).call();

            Source getVM = Source.newBuilder(
              "(function() { return bck2brwsr(); })();\n"
            ).mimeType("text/javascript").name("getvm.js").build();
            CallTarget get = env.parse(getVM);
            vm = get.call();

            Source loadSrc = Source.newBuilder(
                "(function(vm, name) { return vm.loadClass(name); })"
            ).mimeType("text/javascript").name("loadsrc.js").build();
            loadClass = JavaInterop.asJavaFunction(LoadClass.class, (TruffleObject) env.parse(loadSrc).call());
        } catch (IOException ex) {
            throw raise(ex);
        }
    }

    @CompilerDirectives.TruffleBoundary
    void compileJar(File jar) throws IOException {
        Bck2Brwsr compiler = Bck2BrwsrJars.configureFrom(Bck2Brwsr.newCompiler(), jar);
        compiler = compiler.library();
        StringBuilder sb = new StringBuilder();
        compiler.generate(sb);
        Source src = Source.newBuilder(sb.toString()).uri(jar.toURI()).name(jar.getName()).mimeType("text/javascript").build();
        env.parse(src).call();
    }

    Object findClass(String globalName) {
        return loadClass.loadClass(vm, globalName);
    }

    @CompilerDirectives.TruffleBoundary
    void compileClasses(Source src, final Map<String, byte[]> classes) throws Exception {
        class Res implements Bck2Brwsr.Resources {
            @Override
            public InputStream get(String resource) throws IOException {
                byte[] arr = classes.get(resource);
                if (arr == null) {
                    Enumeration<URL> en = Res.class.getClassLoader().getResources(resource);
                    while (en.hasMoreElements()) {
                        URL u = en.nextElement();
                        if (u.toExternalForm().contains("/rt.jar")) {
                            continue;
                        }
                        return u.openStream();
                    }
                    throw new IOException("Cannot find " + resource);
                }
                return new ByteArrayInputStream(arr);
            }
        }
        StringBuilder sb = new StringBuilder();
        Bck2Brwsr compiler = Bck2Brwsr.newCompiler();
        for (String c : classes.keySet()) {
            if (c.endsWith(".class")) {
                compiler = compiler.addRootClasses(c.substring(0, c.length() - 6));
            }
        }
        compiler
            .resources(new Res())
            .library()
            .generate(sb);
        Source jsSrc = Source.newBuilder(sb.toString()).uri(src.getURI()).name(src.getName()).mimeType("text/javascript").build();
        env.parse(jsSrc).call();
    }

    private static interface LoadClass {
        public Object loadClass(Object vm, String name);
    }

    static RuntimeException raise(Throwable ex) {
        return raise(RuntimeException.class, ex);
    }

    static <E extends Exception> E raise(Class<E> type, Throwable ex) throws E {
        throw (E)ex;
    }
}
