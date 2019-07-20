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
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URI;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;


@TruffleLanguage.Registration(
    id = "Java",
    name = "Java",
    byteMimeTypes = {
        "application/x-jar", "application/x-java-archive",
        "application/x-java-class",
        "application/x-dir"
    },
    characterMimeTypes = {
        "text/java"
    },
    defaultMimeType = "application/x-jar",
    dependentLanguages = "js",
    version = "0.30"
)
public class Bck2BrwsrLanguage extends TruffleLanguage<VM> {

    @Override
    protected VM createContext(Env env) {
        return new VM(env);
    }

    @Override
    protected void initializeContext(VM context) throws Exception {
        context.initialize();
    }
/* TBD:
    @Override
    protected Object findExportedSymbol(VM context, String globalName, boolean onlyExplicit) {
        if (onlyExplicit) {
            return null;
        }
        return context.findClass(globalName);
    }
*/
    @Override
    protected Object getLanguageGlobal(VM context) {
        return null;
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        return false;
    }

    @Override
    protected CallTarget parse(ParsingRequest request) throws Exception {
        final Source src = request.getSource();
        if (src.hasBytes()) {
            final ContextReference<VM> ref = getContextReference();
            InputStream is = openStream(src);
            PushbackInputStream ahead = new PushbackInputStream(is, 4);
            byte[] header = new byte[4];
            int len = ahead.read(header);
            if (len < 4) {
                final URI uri = src.getURI();
                if ("file".equals(uri.getScheme())) {
                    File dir = new File(uri);
                    if (dir.isDirectory()) {
                        return Truffle.getRuntime().createCallTarget(new ProcessJarDir(this, ref, dir));
                    }
                }
                throw new IOException("Can't read " + uri);
            }
            ahead.unread(header, 0, len);
            if (header[0] == 0xCA && header[1] == 0xFE && header[2] == 0xBA && header[3] == 0xBE) {
                throw new IOException("No single class read " + src.getURI());
            }
            if (header[0] == 0x50 && header[1] == 0x4B) {
                final File jar = new File(src.getURI());
                return Truffle.getRuntime().createCallTarget(new ProcessJarDir(this, ref, jar));
            }
        }

        if (TruffleOptions.AOT) {
            throw new IOException("Can't compile Java source: " + src.getURI());
        } else {
            return compileJavaSource(src);
        }
    }

    private CallTarget compileJavaSource(final Source src) throws IOException {
        final ContextReference<VM> ref = getContextReference();
        final Compile result = Compile.create(src);
        if (!result.getErrors().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> error : result.getErrors()) {
                sb.append(error).append("\n");
            }
            throw new IllegalStateException(sb.toString());
        }
        return Truffle.getRuntime().createCallTarget(new RootNode(this) {
            @Override
            public Object execute(VirtualFrame frame) {
                try {
                    ref.get().compileClasses(src, result.getClasses());
                } catch (Exception ex) {
                    throw VM.raise(ex);
                }
                return nullValue(ref.get());
            }
        });
    }

    private static Object nullValue(VM vm) {
        return vm.jsNull();
    }

    private static InputStream openStream(Source src) throws IOException {
        if (src.getURL() != null) {
            return src.getURL().openStream();
        }
        if (src.getPath() != null) {
            File f = new File(src.getPath());
            if (f.exists()) {
                return new FileInputStream(f);
            }
        }
        return new ByteArrayInputStream(src.getBytes().toByteArray());
    }

    private static class ProcessJarDir extends RootNode {

        private final ContextReference<VM> ref;
        private final File jar;

        ProcessJarDir(TruffleLanguage<?> language, ContextReference<VM> ref, File jar) {
            super(language);
            this.ref = ref;
            this.jar = jar;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            try {
                ref.get().compileJar(jar);
            } catch (IOException ex) {
                throw VM.raise(ex);
            }
            return nullValue(ref.get());
        }
    }
}
