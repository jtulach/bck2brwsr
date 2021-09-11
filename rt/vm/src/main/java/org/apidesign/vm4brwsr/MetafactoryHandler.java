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
        ByteCodeParser.CPX2 methodHandle = ctx.bm.clazz.getCpoolEntry(ctx.bm.args[1]);
        String samMethodType = getMethodTypeArg(ctx.bm, 0);
        String instantiatedMethodType = getMethodTypeArg(ctx.bm, 2);
        String implMethodType;
        if (samMethodType == null || instantiatedMethodType == null) {
            return false;
        }
        InternalSig samMethodSig = InternalSig.find(ctx.mt[0], samMethodType);
        InternalSig instantiatedMethodSig = InternalSig.find(null, instantiatedMethodType);
        InternalSig implMethodSig;
        String implMethodClass;
        {
            String[] methodInfoName = ctx.bm.clazz.getFieldInfoName(methodHandle.cpx2);
            ctx.byteCodeToJavaScript.requireReference(implMethodClass = methodInfoName[0]);
            implMethodSig = InternalSig.find(methodInfoName[1], implMethodType = methodInfoName[2]);
        }
        if (samMethodSig.getParameterCount() != instantiatedMethodSig.getParameterCount()) {
            return false;
        }
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
        final int firstArgIndex = isStatic ? 0 : 1;
        final int totalArgsCount = implMethodSig.getParameterCount() + firstArgIndex;
        final int interfaceArgsCount = samMethodSig.getParameterCount();
        final int fixedArgsCount = totalArgsCount - interfaceArgsCount;
        final String prefix = ctx.stackMapper.allocUniqueVariablePrefix();
        {
            final CharSequence[] vars = new CharSequence[fixedArgsCount];
            for (int j = fixedArgsCount - 1; j >= 0; --j) {
                vars[j] = ctx.stackMapper.popValue(ctx.out);
            }

            ctx.stackMapper.flush(ctx.out);

            for (int j = 0; j < fixedArgsCount; j++) {
                ctx.out.append("\n   var ").append(prefix).append(Integer.toString(j)).append(" = ").append(vars[j]).append(';');
            }
            ctx.out.append("\n   var ").append(prefix).append(" = function(");
            String sep = "";
            for (int j = fixedArgsCount; j < totalArgsCount; j++) {
                ctx.out.append(sep).append(prefix).append(Integer.toString(j));
                sep = ", ";
            }
            ctx.out.append(") {");
        }
        for (int i = 0; i < interfaceArgsCount; i++) {
            int index = fixedArgsCount + i;
            String fromType = samMethodSig.getJvmParameterType(i);
            String toType = instantiatedMethodSig.getJvmParameterType(i);
            if (toType.equals(fromType)) {
                // also primitive types
                continue;
            }
            if (toType.startsWith("L") && toType.endsWith(";")) {
                toType = toType.substring(1, toType.length() - 1);
            }
            ctx.out.append("\n      ");
            ctx.byteCodeToJavaScript.generateCheckcast(ctx.out, toType, prefix + index);
        }
        for (int i = 0; i < interfaceArgsCount; i++) {
            int index = fixedArgsCount + i;
            if (index < firstArgIndex) {
                // receiver, no implicit conversions
                continue;
            }
            String fromType = instantiatedMethodSig.getJvmParameterType(i);
            String toType = implMethodSig.getJvmParameterType(index - firstArgIndex);
            if (toType.equals(fromType)) {
                continue;
            }
            implicitConversion(ctx, fromType, toType, prefix + index);
        }
        {
            final String mangledType = InternalSig.mangleClassName(implMethodClass);
            final String mangledMethod = implMethodSig.getJniName();

            String sep = "";
            ctx.out.append("\n      var type = ").append(ctx.byteCodeToJavaScript.accessClassFalse(mangledType)).append(";");
            ctx.out.append("\n      var ret = ");
            if (isVirtual) {
                ctx.out.append(prefix).append("0.").append(mangledMethod).append('(');
            } else {
                if (isConstructor) {
                    ctx.out.append("new ").append(ctx.byteCodeToJavaScript.accessClass(mangledType)).append(";");
                    ctx.out.append("\n      type.constructor['").append(mangledMethod).append("'].call(ret");
                    sep = ", ";
                } else {
                    ctx.out.append("type.").append(mangledMethod);
                    if (!isStatic) {
                        ctx.out.append(".call(").append(prefix).append("0");
                        sep = ", ";
                    } else {
                        ctx.out.append('(');
                    }
                }
            }
            for (int i = firstArgIndex; i < totalArgsCount; i++) {
                ctx.out.append(sep).append(prefix).append(Integer.toString(i));
                sep = ", ";
            }
            ctx.out.append(");");

            if (!instantiatedMethodType.endsWith("V")) {
                if (!isConstructor) {
                    String fromType = implMethodType.substring(implMethodType.indexOf(')') + 1);
                    String toType = instantiatedMethodType.substring(instantiatedMethodType.indexOf(')') + 1);
                    implicitConversion(ctx, fromType, toType, "ret");
                }
                ctx.out.append("\n      return ret;");
            }
        }
        {
            ctx.out.append("\n   };");

            String sig = ctx.mt[1];
            int typeEnd = sig.lastIndexOf(')');
            String typeSig = sig.substring(typeEnd + 1);
            if (!typeSig.startsWith("L") || !typeSig.endsWith(";")) {
                return false;
            }
            final String type = typeSig.substring(1, typeSig.length() - 1);
            ctx.byteCodeToJavaScript.requireReference(type);
            final String mangledType = InternalSig.mangleClassName(type);
            String interfaceToCreate = ctx.byteCodeToJavaScript.accessClassFalse(mangledType);
            String interfaceMethod = samMethodSig.getJniName();

            final CharSequence samVar = ctx.stackMapper.pushA();
            ctx.out.append("\n   var ").append(samVar).append(" = new ").append(interfaceToCreate).append(".constructor();\n");
            ctx.out.append("\n   ").append(samVar).append("['").append(interfaceMethod).append("'] = " + prefix + ";");
        }
        return true;
    }

    private static String getMethodTypeArg(ByteCodeParser.BootMethodData bm, int i) {
        Object index = bm.clazz.getCpoolEntryobj(bm.args[i]);
        if (index instanceof ByteCodeParser.CPX) {
            Object value = bm.clazz.getCpoolEntryobj(((ByteCodeParser.CPX) index).cpx);
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;
    }

    private static void implicitConversion(Ctx ctx, String fromType, String toType, String var) throws IOException {
        boolean fromPrimitive = fromType.length() == 1;
        boolean toPrimitive = toType.length() == 1;
        if (toPrimitive) {
            final char fromLetter;
            if (!fromPrimitive) {
                // unboxing
                PrimitiveType pt = PrimitiveType.unbox(fromType);
                ctx.out.append("\n      ").append(var).append(" = ").append(var).append(".").append(pt.unboxMethod).append("();");
                fromLetter = pt.letter();
            } else {
                fromLetter = fromType.charAt(0);
            }
            // primitive widening conversion
            widen(ctx, fromLetter, toType.charAt(0), var);
        } else if (fromPrimitive) {
            // boxing
            PrimitiveType pt = PrimitiveType.valueOf(fromType);
            ctx.byteCodeToJavaScript.requireReference(pt.jvmWrapperType);
            ctx.out.append("\n      ").append(var).append(" = ").append(ctx.byteCodeToJavaScript.accessClassFalse(pt.wrapperType));
            ctx.out.append(".").append(pt.convertMethod).append("(").append(var).append(");");
        } else {
            // upcast
            // no check or conversion needed
        }
    }

    private static void widen(Ctx ctx, char fromType, char toType, String var) throws IOException {
        if (fromType == 'J') {
            if (toType == 'D') {
                ctx.out.append("\n      ").append(var).append(" = ").append(var).append(".doubleValue__D();");
            } else if (toType == 'F') {
                ctx.out.append("\n      ").append(var).append(" = ").append(var).append(".floatValue__F();");
            }
        } else if (toType == 'J') {
            ctx.out.append("\n      ").append(var).append(" = ").append(var).append(".toLong();");
        }
    }

    private enum PrimitiveType {
        I("java_lang_Integer", "valueOf__Ljava_lang_Integer_2I", "intValue__I"),
        J("java_lang_Long", "valueOf__Ljava_lang_Long_2J", "longValue__J"),
        D("java_lang_Double", "valueOf__Ljava_lang_Double_2D", "doubleValue__D"),
        F("java_lang_Float", "valueOf__Ljava_lang_Float_2F", "floatValue__F"),
        B("java_lang_Byte", "valueOf__Ljava_lang_Byte_2B", "byteValue__B"),
        Z("java_lang_Boolean", "valueOf__Ljava_lang_Boolean_2Z", "booleanValue__Z"),
        S("java_lang_Short", "valueOf__Ljava_lang_Short_2S", "shortValue__S"),
        C("java_lang_Character", "valueOf__Ljava_lang_Character_2C", "charValue__C");

        final String wrapperType;
        final String convertMethod;
        final String jvmWrapperType;
        final String unboxMethod;

        PrimitiveType(String wrapperType, String convertMethod, String unboxMethod) {
            this.wrapperType = wrapperType;
            this.convertMethod = convertMethod;
            this.jvmWrapperType = wrapperType.replace('_', '/');
            this.unboxMethod = unboxMethod;
        }

        static PrimitiveType unbox(String wrapper) {
            switch (wrapper) {
                case "Ljava/lang/Integer;": return I;
                case "Ljava/lang/Long;": return J;
                case "Ljava/lang/Double;": return D;
                case "Ljava/lang/Float;": return F;
                case "Ljava/lang/Byte;": return B;
                case "Ljava/lang/Boolean;": return Z;
                case "Ljava/lang/Short;": return S;
                case "Ljava/lang/Character;": return C;
                default: throw new IllegalStateException("Cannot unbox " + wrapper);
            }
        }

        char letter() {
            return name().charAt(0);
        }
    }
}
