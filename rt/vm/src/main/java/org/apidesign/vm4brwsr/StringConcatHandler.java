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

import java.io.IOException;

final class StringConcatHandler extends IndyHandler {
    StringConcatHandler() {
        super("java/lang/invoke/StringConcatFactory", "makeConcatWithConstants");
    }

    @Override
    protected boolean handle(Ctx ctx) throws IOException {
        final int fixedArgsCount;
        String recipe = ctx.bm.clazz.StringValue(ctx.bm.args[0]);
        final String sig = ctx.mt[1];
        StringBuilder sigB = new StringBuilder();
        StringBuilder cnt = new StringBuilder();
        char[] returnType = {'V'};
        ByteCodeToJavaScript.countArgs(sig, returnType, sigB, cnt);
        assert returnType[0] == 'L';

        fixedArgsCount = cnt.length();
        final CharSequence[] vars = new CharSequence[fixedArgsCount];
        for (int j = fixedArgsCount - 1; j >= 0; --j) {
            vars[j] = ctx.stackMapper.popValue(ctx.out);
        }


        ctx.stackMapper.flush(ctx.out);

        final CharSequence stringVar = ctx.stackMapper.pushA();
        ctx.out.append("\nvar ").append(stringVar).append(" = ");

        StringBuilder sep = new StringBuilder();
        sep.append("'");
        int atVar = 0;
        for (int j = 0; j < recipe.length(); j++) {
            char ch = recipe.charAt(j);
            switch (ch) {
                case '\1':
                    ctx.out.append(sep).append("' + ");
                    ctx.out.append(vars[atVar++]);
                    sep = new StringBuilder(" + '");
                    break;
                case '\2':
                    throw new IllegalStateException("#2 stackvalue not supported yet");
                case '\n':
                    sep.append("\\n");
                    break;
                case '\r':
                    sep.append("\\r");
                    break;
                case '\t':
                    sep.append("\\t");
                    break;
                case '\'':
                    sep.append("\\'");
                    break;
                case '\\':
                    sep.append("\\\\");
                    break;
                default:
                    if (ch < 32) {
                        String four = "0000" + Integer.toHexString(ch);
                        sep.append("\\u").append(four.substring(four.length() - 4));
                    } else {
                        sep.append(ch);
                    }
            }
        }
        ctx.out.append(sep).append("';");
        return true;
    }
}
