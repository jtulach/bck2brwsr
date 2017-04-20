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
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.java.JavaInterop;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;


@TruffleLanguage.Registration(
    name = "Java",
    mimeType = { "application/x-jar", "application/x-java-class", "text/java" },
    version = "0.20"
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

    @Override
    protected Object findExportedSymbol(VM context, String globalName, boolean onlyExplicit) {
        if (onlyExplicit) {
            return null;
        }
        return context.findClass(globalName);
    }

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
        Source src = request.getSource();
        InputStream is = src.getURL().openStream();
        PushbackInputStream ahead = new PushbackInputStream(is, 4);
        byte[] header = new byte[4];
        int len = ahead.read(header);
        if (len < 4) {
            throw new IOException("Can't read " + src.getURI());
        }
        ahead.unread(header, 0, len);
        if (header[0] == 0xCA && header[1] == 0xFE && header[2] == 0xBA && header[3] == 0xBE) {
            throw new IOException("No single class read " + src.getURI());
        }
        if (header[0] == 0x50 && header[1] == 0x4B) {
            final File jar = new File(src.getURI());
            final ContextReference<VM> ref = getContextReference();
            return Truffle.getRuntime().createCallTarget(new RootNode(this) {
                @Override
                public Object execute(VirtualFrame frame) {
                    try {
                        ref.get().compileJar(jar);
                    } catch (IOException ex) {
                        throw VM.raise(ex);
                    }
                    return JavaInterop.asTruffleValue(null);
                }
            });

        }
        throw new IOException("Unrecognized " + src.getURI());
    }

    public static String parseBase64Binary(String s) throws UnsupportedEncodingException {
        final byte[] arr = javax.xml.bind.DatatypeConverter.parseBase64Binary(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            int ch = arr[i];
            sb.append((char) ch);
        }
        return sb.toString();
    }
}
