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

final class MetafactoryHandler extends IndyHandler {
    MetafactoryHandler() {
        super("java/lang/invoke/LambdaMetafactory", "metafactory");
    }

    @Override
    protected boolean handle(Ctx ctx) throws IOException {
        final int fixedArgsCount;
        {
            final String sig = ctx.mt[1];
            int typeEnd = sig.lastIndexOf(')');
            String typeSig = sig.substring(typeEnd + 1);
            if (!typeSig.startsWith("L") || !typeSig.endsWith(";")) {
                return false;
            }
            final String type = typeSig.substring(1, typeSig.length() - 1);
            ctx.byteCodeToJavaScript.requireReference(type);
            final String mangledType = ByteCodeToJavaScript.mangleClassName(type);
            String interfaceToCreate = ctx.byteCodeToJavaScript.accessClassFalse(mangledType);

            StringBuilder sigB = new StringBuilder();
            StringBuilder cnt = new StringBuilder();
            char[] returnType = { 'V' };
            boolean isStatic = true;
            ByteCodeToJavaScript.countArgs(sig, returnType, sigB, cnt);

            fixedArgsCount = isStatic ? cnt.length() : cnt.length() + 1;
            final CharSequence[] vars = new CharSequence[fixedArgsCount];
            for (int j = fixedArgsCount - 1; j >= 0; --j) {
                vars[j] = ctx.stackMapper.popValue();
            }

            assert returnType[0] == 'L';

            ctx.stackMapper.flush(ctx.out);

            final Variable samVar = ctx.stackMapper.pushA();
            ctx.out.append("var ").append(samVar).append(" = ").append(interfaceToCreate).append(".constructor.$class.functional([");

            String sep = "";
            for (int j = 0; j < fixedArgsCount; j++) {
                ctx.out.append(sep).append(vars[j]);
                sep = ", ";
            }

            ctx.out.append("], function(args1, args2) {\n");
        }
        {
            ByteCodeParser.CPX2 methodHandle = ctx.bm.clazz.getCpoolEntry(ctx.bm.args[1]);

            String[] methodInfoName = ctx.bm.clazz.getFieldInfoName(methodHandle.cpx2);
            ctx.byteCodeToJavaScript.requireReference(methodInfoName[0]);
            final String mangledType = ByteCodeToJavaScript.mangleClassName(methodInfoName[0]);
            StringBuilder cnt = new StringBuilder();
            char[] returnType = { 'V' };
            String mangledMethod = ByteCodeToJavaScript.findMethodName(methodInfoName, cnt, returnType);

            ctx.out.append("\n      return ");
            ctx.out.append(ctx.byteCodeToJavaScript.accessClassFalse(mangledType));
            ctx.out.append(".").append(mangledMethod).append('(');
            String sep = "";
            for (int i = 0; i < cnt.length(); i++) {
                ctx.out.append(sep);
                if (i < fixedArgsCount) {
                    ctx.out.append("args1[" + i + "]");
                } else {
                    ctx.out.append("args2[" + (i - fixedArgsCount) + "]");
                }
                sep = ", ";
            }
            ctx.out.append(");");
            ctx.out.append("\n   })");
        }
        return true;
    }

}
