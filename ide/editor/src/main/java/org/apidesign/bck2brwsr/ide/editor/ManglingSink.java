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
package org.apidesign.bck2brwsr.ide.editor;

/**
 * An implementation of {@linkplain JsniCommentTokenizer.Sink} that generates B2B
 */
class ManglingSink implements JsniCommentTokenizer.Sink {

    final StringBuilder out = new StringBuilder();

    public void javascript(String s) {
        out.append(s);
    }

    public void method(String clazz, String method, String signature) {
        out.append(mangle(clazz, method, signature));
    }

    public void field(String clazz, String field) {
//        out.append(field);
        out.append('_').append(field).append('(').append(')');
    }


    @Override
    public String toString() {
        return out.toString();
    }


    static String mangle(String clazz, String method, String signature) {
        final StringBuilder builder = new StringBuilder();
        builder.append(method);
        builder.append("__");
        builder.append(mangle(JNIHelper.signature(JNIHelper.method(clazz, method, signature).getReturnType())));
        builder.append(mangle(signature));
        return builder.toString();
    }


    static String mangle(String txt) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < txt.length(); i++) {
            final char ch = txt.charAt(i);
            switch (ch) {
                case '/': sb.append('_'); break;
                case '_': sb.append("_1"); break;
                case ';': sb.append("_2"); break;
                case '[': sb.append("_3"); break;
                default: sb.append(ch); break;
            }
        }
        return sb.toString();
    }
}
