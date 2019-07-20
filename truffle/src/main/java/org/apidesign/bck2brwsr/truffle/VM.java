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
package org.apidesign.bck2brwsr.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.apidesign.bck2brwsr.aot.Bck2BrwsrJars;
import org.apidesign.vm4brwsr.Bck2Brwsr;

@ExportLibrary(InteropLibrary.class)
final class VM implements TruffleObject {
    private static Source rtJs;
    private static final ClassLoader rtJarClassLoader = createJarClassLoader();
    static {
        if (TruffleOptions.AOT) {
            try {
                rtJs();
            } catch (IOException ex) {
                throw new AssertionError("Cannot preload rt.js", ex);
            }
            assert rtJs != null;
        }
    }

    final TruffleLanguage.Env env;
    private TruffleObject vm;
    private Object jsNull;

    VM(TruffleLanguage.Env env) {
        this.env = env;
    }

    void initialize() {
        try {
            StringBuilder sb = new StringBuilder();
            Bck2Brwsr.newCompiler().standalone(false).generate(sb);

            Source bck2brwsr = Source.newBuilder("js", sb.toString(), "bck2brwsr.js").mimeType("text/javascript").build();

            CallTarget vmInit = env.parse(bck2brwsr);
            vmInit.call();

            CallTarget rtInit = env.parse(rtJs());
            jsNull = rtInit.call();

            Source atob = Source.newBuilder("js",
                "(function(f) {\n"
              + "  this.atob = f;\n"
              + "})\n", "atob.js"
            ).mimeType("text/javascript").build();
            TruffleObject registerAtob = (TruffleObject) env.parse(atob).call();
            try {
                InteropLibrary interop = InteropLibrary.getFactory().getUncached();
                interop.execute(registerAtob, new AtoB());
            } catch (InteropException ex) {
                throw raise(ex);
            }
            Source getVM = Source.newBuilder("js",
              "(function() {\n return bck2brwsr();\n})();\n", "getvm.js"
            ).mimeType("text/javascript").build();
            CallTarget get = env.parse(getVM);
            vm = (TruffleObject) get.call();

            try {
                InteropLibrary.getFactory().getUncached().writeMember(env.getPolyglotBindings(), "jvm", this);
            } catch (InteropException ex) {
                throw new IllegalStateException(ex);
            }

        } catch (IOException ex) {
            throw raise(ex);
        }
    }

    final Object jsNull() {
        return jsNull;
    }

    private static Source rtJs() throws IOException {
        if (rtJs == null) {
            InputStream rt = VM.class.getResourceAsStream("/emul-1.0-SNAPSHOT-debug.js");
            assert rt != null;
            InputStreamReader r = new InputStreamReader(rt);
            StringBuilder sb = new StringBuilder();
            char[] arr = new char[4096];
            for (;;) {
                int len = r.read(arr);
                if (len == -1) {
                    break;
                }
                sb.append(arr, 0, len);
            }
            rtJs = Source.newBuilder("js", sb, "rt.js").build();
        }
        return rtJs;
    }

    private static ClassLoader createJarClassLoader() {
        File rtJar = null;
        for (String jar : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (jar.contains("emul") && jar.endsWith("-rt.jar")) {
                rtJar = new File(jar);
                break;
            }
        }
        try {
            if (rtJar == null) {
                URL self = VM.class.getProtectionDomain().getCodeSource().getLocation();
                rtJar = new File(self.toURI());
            }
            return new SingleJarLoader(rtJar);
        } catch (IOException | URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @CompilerDirectives.TruffleBoundary
    void compileJar(File jar) throws IOException {
        Bck2Brwsr compiler = Bck2BrwsrJars.configureFrom(Bck2Brwsr.newCompiler(), jar, rtJarClassLoader);
        compiler = compiler.library();
        StringBuilder sb = new StringBuilder();
        compiler.generate(sb);
        Source src = Source.newBuilder(sb.toString()).uri(jar.toURI()).name(jsName(jar)).mimeType("text/javascript").build();
        env.parse(src).call();

        if (jar.isFile()) {
            Manifest manifest;
            try (JarInputStream is = new JarInputStream(new FileInputStream(jar), false)) {
                manifest = is.getManifest();
            }
            String mainClass = manifest != null && manifest.getMainAttributes() != null ?
                manifest.getMainAttributes().getValue("Main-Class") : null;
            if (mainClass != null) {
                InteropLibrary interop = InteropLibrary.getFactory().getUncached();
                try {
                    interop.invokeMember(findClass(interop, mainClass), "main");
                } catch (InteropException ex) {
                    throw raise(ex);
                }
            }
        }
    }

    protected String jsName(File jar) {
        String name = jar.getName();
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            name = name.substring(0, dot) + ".js";
        }
        return name;
    }

    final ClassObject findClass(InteropLibrary invoke, String globalName) {
        return new ClassObject(loadClass(invoke, globalName));
    }

    private TruffleObject loadClass(InteropLibrary invoke, String name) {
        Object clazz;
        try {
            clazz = invoke.invokeMember(vm, "loadClass", name);
        } catch (InteropException ex) {
            throw VM.raise(ex);
        }
        return (TruffleObject) clazz;
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

    static RuntimeException raise(Throwable ex) {
        return raise(RuntimeException.class, ex);
    }

    static <E extends Exception> E raise(Class<E> type, Throwable ex) throws E {
        throw (E)ex;
    }

    @ExportMessage
    static Object readMember(VM obj, String name, @CachedLibrary(limit = "3") InteropLibrary invoke) {
        ClassObject clazz = obj.findClass(invoke, name);
        return clazz;
    }

    @ExportMessage
    static boolean isMemberReadable(VM obj, String name) {
        return true;
    }

    @ExportMessage
    static boolean hasMembers(VM obj) {
        return true;
    }

    @ExportMessage
    static Object getMembers(VM obj, boolean includeInternal) {
        throw new IllegalStateException();
    }
}
