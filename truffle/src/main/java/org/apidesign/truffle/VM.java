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
package org.apidesign.truffle;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.source.Source;
import java.io.IOException;
import java.net.URL;
import org.apidesign.vm4brwsr.Bck2Brwsr;

final class VM {
    final TruffleLanguage.Env env;

    VM(TruffleLanguage.Env env) {
        this.env = env;
    }

    void initialize() {
        try {
            StringBuilder sb = new StringBuilder();
            Bck2Brwsr.newCompiler().standalone(false).generate(sb);

            Source bck2brwsr = Source.newBuilder(sb.toString()).mimeType("text/javascript").name("bck2brwsr.js").build();

            CallTarget vmInit = env.parse(bck2brwsr);
            Object res = vmInit.call();
            System.err.println("res: " + res);

            URL rt = VM.class.getResource("/emul-1.0-SNAPSHOT-debug.js");
            assert rt != null;

            Source rtJs = Source.newBuilder(rt).name("rt.js").mimeType("text/javascript").build();

            CallTarget rtInit = env.parse(rtJs);
            res = rtInit.call();
            System.err.println("res: " + res);

            Source getVM = Source.newBuilder(
                "function atob() { return null };\n"
              + "bck2brwsr()\n"
            ).mimeType("text/javascript").name("getvm.js").build();
            CallTarget get = env.parse(getVM);
            res = get.call();
            System.err.println("res: " + res);


        } catch (IOException ex) {
            throw raise(ex);
        }
    }

    static RuntimeException raise(Throwable ex) {
        return raise(RuntimeException.class, ex);
    }

    static <E extends Exception> E raise(Class<E> type, Throwable ex) throws E {
        throw (E)ex;
    }
}
