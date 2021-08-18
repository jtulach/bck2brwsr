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

class MetafactoryHandler extends IndyHandler {
    MetafactoryHandler() {
        super("java/lang/invoke/LambdaMetafactory", "metafactory");
    }

    MetafactoryHandler(String clazz, String method) {
        super(clazz, method);
    }

    @Override
    protected boolean handle(Ctx ctx) throws IOException {
        final int fixedArgsCount;
        ByteCodeParser.CPX2 methodHandle = ctx.bm.clazz.getCpoolEntry(ctx.bm.args[1]);
        boolean isStatic;
        boolean isVirtual = false;
        boolean isConstructor = false;
        switch (methodHandle.cpx1) {
            case 6: /* REF_invokeStatic */
                isStatic = true;
                break;
            case 5: /* REF_invokeVirtual */
            case 9: /* REF_invokeInterface */
                isVirtual = true;
                // fallthru
            case 7: /* REF_invokeSpecial */
                isStatic = false;
                break;
            case 8: /* REF_newInvokeSpecial */
                isStatic = true;
                isConstructor = true;
                break;
            case 1: /* REF_getField */
            case 2: /* REF_getStatic */
            case 3: /* REF_putField */
            case 4: /* REF_putStatic */
            default:
                // unsupported by this indy handler
                return false;
        }
        {
            final String sig = ctx.mt[1];
            int typeEnd = sig.lastIndexOf(')');
            String typeSig = sig.substring(typeEnd + 1);
            if (!typeSig.startsWith("L") || !typeSig.endsWith(";")) {
                return false;
            }
            final String type = typeSig.substring(1, typeSig.length() - 1);
            ctx.byteCodeToJavaScript.requireReference(type);
            final String mangledType = InternalSig.mangleClassName(type);
            String interfaceToCreate = ctx.byteCodeToJavaScript.accessClassFalse(mangledType);

            InternalSig internalSig = InternalSig.find(null, sig);

            fixedArgsCount = internalSig.getParameterCount();
            final CharSequence[] vars = new CharSequence[fixedArgsCount];
            for (int j = fixedArgsCount - 1; j >= 0; --j) {
                vars[j] = ctx.stackMapper.popValue();
            }

            assert internalSig.getMangledType().startsWith("L");

            ctx.stackMapper.flush(ctx.out);

            final Variable samVar = ctx.stackMapper.pushA();
            ctx.out.append("var ").append(samVar).append(" = ").append(interfaceToCreate).append(".constructor.$class.$lambda([");

            String sep = "";
            for (int j = 0; j < fixedArgsCount; j++) {
                ctx.out.append(sep).append(vars[j]);
                sep = ", ";
            }

            ctx.out.append("], function(args1, args2) {\n");
        }
        {
            String[] methodInfoName = ctx.bm.clazz.getFieldInfoName(methodHandle.cpx2);
            ctx.byteCodeToJavaScript.requireReference(methodInfoName[0]);
            final String mangledType = InternalSig.mangleClassName(methodInfoName[0]);
            InternalSig internalSig = InternalSig.find(methodInfoName[1], methodInfoName[2]);
            String mangledMethod = internalSig.getJniName();
            for (int i = 0; i < internalSig.getParameterCount(); i++) {
                String type = internalSig.getJvmParameterType(i);
                if (type.length() == 1) {
                    // primitive types
                    continue;
                }
                if (type.startsWith("L") && type.endsWith(";")) {
                    type = type.substring(1, type.length() - 1);
                }
                ctx.out.append("\n      ");
                int index = isStatic ? i : i + 1;
                if (index < fixedArgsCount) {
                    ctx.byteCodeToJavaScript.generateCheckcast(ctx.out, type, "args1[" + index + "]");
                } else {
                    ctx.byteCodeToJavaScript.generateCheckcast(ctx.out, type, "args2[" + (index - fixedArgsCount) + "]");
                }
            }

            String sep = "";
            ctx.out.append("\n      var type = ").append(ctx.byteCodeToJavaScript.accessClassFalse(mangledType)).append(";");
            ctx.out.append("\n      var ret = ");
            if (isVirtual) {
                if (fixedArgsCount > 0) {
                    ctx.out.append("args1[0]");
                } else {
                    ctx.out.append("args2[0]");
                }
                ctx.out.append(".").append(mangledMethod).append('(');
            } else {
                if (isConstructor) {
                    ctx.out.append("new ").append(ctx.byteCodeToJavaScript.accessClass(mangledType)).append(";");
                    ctx.out.append("\n      type.constructor['").append(mangledMethod).append("'].call(ret");
                    sep = ", ";
                } else {
                    ctx.out.append("type.").append(mangledMethod);
                    if (!isStatic) {
                        ctx.out.append(".call(");
                        if (fixedArgsCount > 0) {
                            ctx.out.append("args1[0]");
                        } else {
                            ctx.out.append("args2[0]");
                        }
                        sep = ", ";
                    } else {
                        ctx.out.append('(');
                    }
                }
            }
            for (int i = 0; i < internalSig.getParameterCount(); i++) {
                int index = isStatic ? i : i + 1;
                ctx.out.append(sep);
                if (index < fixedArgsCount) {
                    ctx.out.append("args1[" + index + "]");
                } else {
                    ctx.out.append("args2[" + (index - fixedArgsCount) + "]");
                }
                sep = ", ";
            }
            ctx.out.append(");");

            String convertType;
            String convertMethod;
            switch (internalSig.getMangledType().charAt(0)) {
                case 'I':
                    convertType = "java_lang_Integer";
                    convertMethod = "valueOf__Ljava_lang_Integer_2I";
                    break;
                case 'J':
                    convertType = "java_lang_Long";
                    convertMethod = "valueOf__Ljava_lang_Long_2J";
                    break;
                case 'D':
                    convertType = "java_lang_Double";
                    convertMethod = "valueOf__Ljava_lang_Double_2D";
                    break;
                case 'F':
                    convertType = "java_lang_Float";
                    convertMethod = "valueOf__Ljava_lang_Float_2F";
                    break;
                case 'B':
                    convertType = "java_lang_Byte";
                    convertMethod = "valueOf__Ljava_lang_Byte_2B";
                    break;
                case 'Z':
                    convertType = "java_lang_Boolean";
                    convertMethod = "valueOf__Ljava_lang_Boolean_2Z";
                    break;
                case 'S':
                    convertType = "java_lang_Short";
                    convertMethod = "valueOf__Ljava_lang_Short_2S";
                    break;
                case 'C':
                    convertType = "java_lang_Character";
                    convertMethod = "valueOf__Ljava_lang_Character_2C";
                    break;
                case 'L':
                case '[':
                case 'V':
                    convertType = null;
                    convertMethod = null;
                    break;
                default:
                    throw new IllegalStateException("Unexpected return type: " + internalSig.getMangledType());
            }

            if (convertType != null) {
                String jvmConvertType = convertType.replace('_', '/');
                ctx.byteCodeToJavaScript.requireReference(jvmConvertType);
                ctx.out.append("\n      ret = ").append(ctx.byteCodeToJavaScript.accessClassFalse(convertType));
                ctx.out.append(".").append(convertMethod).append("(ret);");
            }

            ctx.out.append("\n      return ret;");
            ctx.out.append("\n   });");
        }
        return true;
    }

}
