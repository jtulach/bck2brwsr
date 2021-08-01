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
package org.apidesign.vm4brwsr;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public abstract class Base64Convert {
    static void defineAtoB(ScriptEngine js) throws ScriptException, NoSuchMethodException {
        Object thiz = js.eval("this");
        Object register = js.eval("\n" +
            "(function(global, convert) {\n" +
            "  global.atob = function(s) {\n" +
            "    return new String(convert.convert(s));\n" +
            "  }\n" +
            "})"
        );
        Base64Convert convert = Base64Convert.create();
        ((Invocable) js).invokeMethod(register, "call", thiz, thiz, convert);
    }

    private Base64Convert() {
    }

    public abstract Object convert(String s) throws UnsupportedEncodingException;

    private static Base64Convert create() {
        return new Impl8();
    }

    private static final class Impl8 extends Base64Convert {
        @Override
        public Object convert(String s) throws UnsupportedEncodingException {
            final byte[] arr = Base64.getDecoder().decode(s);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                int ch = arr[i];
                sb.append((char)ch);
            }
            return sb.toString();
        }
    }
}
