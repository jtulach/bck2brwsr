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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsniCommentTokenizer {

    /**
     * Tokenize the contents of JSNI comment into the provided {@linkplain Sink}.
     * @param in the contents of JSNI comment
     * @param out the sink that consumes parsed tokens
     */
    public void process(final String in, final Sink out) {
        final Matcher member = Pattern.compile("@([^:]+)::([a-zA-Z_$][a-zA-Z\\d_$]*)").matcher(in);
        final Matcher signature = Pattern.compile("\\(([^\\)]*)\\)").matcher(in);

        int i = 0;
        while (true) {
            if (member.find(i)) {
                final int memberStart = member.start();
                final int memberEnd = member.end();
                if (memberStart > i) out.javascript(in.substring(i, memberStart));

                final String clazz = member.group(1);
                final String name = member.group(2);

                if (in.charAt(memberEnd) == '(') {
                    if (!signature.find(memberEnd)) {
                        throw new IllegalStateException("Expected method signature");
                    }
                    assert signature.start() == memberEnd;
                    out.method(clazz, name, signature.group(1));
                    i = signature.end();
                } else {
                    out.field(clazz, name);
                    i = memberEnd;
                }
            } else {
                out.javascript(in.substring(i, in.length()));
                break;
            }
        }
    }


    public static interface Sink {
        void javascript(String s);

        void method(String clazz, String method, String signature);

        void field(String clazz, String field);
    }
}
