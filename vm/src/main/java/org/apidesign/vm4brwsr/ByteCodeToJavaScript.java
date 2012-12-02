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
package org.apidesign.vm4brwsr;

import java.io.IOException;
import java.io.InputStream;
import org.apidesign.javap.AnnotationParser;
import org.apidesign.javap.ClassData;
import org.apidesign.javap.FieldData;
import org.apidesign.javap.MethodData;
import static org.apidesign.javap.RuntimeConstants.*;

/** Translator of the code inside class files to JavaScript.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class ByteCodeToJavaScript {
    private ClassData jc;
    private final Appendable out;

    protected ByteCodeToJavaScript(Appendable out) {
        this.out = out;
    }
    
    /* Collects additional required resources.
     * 
     * @param internalClassName classes that were referenced and should be loaded in order the
     *   generated JavaScript code works properly. The names are in internal 
     *   JVM form so String is <code>java/lang/String</code>. 
     */
    protected abstract boolean requireReference(String internalClassName);
    
    /*
     * @param resourcePath name of resources to read
     */
    protected abstract void requireScript(String resourcePath);
    
    /** Allows subclasses to redefine what field a function representing a
     * class gets assigned. By default it returns the suggested name followed
     * by <code>" = "</code>;
     * 
     * @param className suggested name of the class
     */
    protected String assignClass(String className) {
        return className + " = ";
    }

    /**
     * Converts a given class file to a JavaScript version.
     *
     * @param classFile input stream with code of the .class file
     * @return the initialization code for this class, if any. Otherwise <code>null</code>
     * 
     * @throws IOException if something goes wrong during read or write or translating
     */
    
    public String compile(InputStream classFile) throws IOException {
        this.jc = new ClassData(classFile);
        byte[] arrData = jc.findAnnotationData(true);
        String[] arr = findAnnotation(arrData, jc, 
            "org.apidesign.bck2brwsr.core.ExtraJavaScript", 
            "resource", "processByteCode"
        );
        if (arr != null) {
            requireScript(arr[0]);
            if ("0".equals(arr[1])) {
                return null;
            }
        }
        StringArray toInitilize = new StringArray();
        final String className = className(jc);
        out.append("\n\n").append(assignClass(className));
        out.append("function CLS() {");
        out.append("\n  if (!CLS.prototype.$instOf_").append(className).append(") {");
        for (FieldData v : jc.getFields()) {
            if (v.isStatic()) {
                out.append("\n  CLS.").append(v.getName()).append(initField(v));
            }
        }
        // ClassName sc = jc.getSuperClass();
        String sc = jc.getSuperClassName(); // with _
        if (sc != null) {
            out.append("\n    var pp = ").
                append(sc.replace('/', '_')).append("(true);");
            out.append("\n    var p = CLS.prototype = pp;");
            out.append("\n    var sprcls = pp.constructor.$class;");
        } else {
            out.append("\n    var p = CLS.prototype;");
            out.append("\n    var sprcls = null;");
        }
        for (MethodData m : jc.getMethods()) {
            if (m.isStatic()) {
                generateStaticMethod("\n    p.", m, toInitilize);
            } else {
                generateInstanceMethod("\n    p.", m);
            }
        }
        out.append("\n    p.constructor = CLS;");
        out.append("\n    p.$instOf_").append(className).append(" = true;");
        for (String superInterface : jc.getSuperInterfaces()) {
            out.append("\n    p.$instOf_").append(superInterface.replace('/', '_')).append(" = true;");
        }
        out.append("\n    CLS.$class = java_lang_Class(true);");
        out.append("\n    CLS.$class.jvmName = '").append(jc.getClassName()).append("';");
        out.append("\n    CLS.$class.superclass = sprcls;");
        out.append("\n    CLS.$class.cnstr = CLS;");
        byte[] classAnno = jc.findAnnotationData(false);
        if (classAnno != null) {
            out.append("\n    CLS.$class.anno = {");
            generateAnno(jc, out, classAnno);
            out.append("\n    };");
        }
        out.append("\n  }");
        out.append("\n  if (arguments.length === 0) {");
        out.append("\n    if (!(this instanceof CLS)) {");
        out.append("\n      return new CLS();");
        out.append("\n    }");
        for (FieldData v : jc.getFields()) {
            if (!v.isStatic()) {
                out.append("\n    this.fld_").
                    append(v.getName()).append(initField(v));
            }
        }
        out.append("\n    return this;");
        out.append("\n  }");
        out.append("\n  return arguments[0] ? new CLS() : CLS.prototype;");
        out.append("\n}");
        StringBuilder sb = new StringBuilder();
        for (String init : toInitilize.toArray()) {
            sb.append("\n").append(init).append("();");
        }
        return sb.toString();
    }
    private void generateStaticMethod(String prefix, MethodData m, StringArray toInitilize) throws IOException {
        if (javaScriptBody(prefix, m, true)) {
            return;
        }
        StringBuilder argsCnt = new StringBuilder();
        final String mn = findMethodName(m, argsCnt);
        out.append(prefix).append(mn).append(" = function");
        if (mn.equals("classV")) {
            toInitilize.add(className(jc) + "(false)." + mn);
        }
        out.append('(');
        String space = "";
        for (int index = 0, i = 0; i < argsCnt.length(); i++) {
            out.append(space);
            out.append("arg").append(String.valueOf(index));
            space = ",";
            final String desc = null;// XXX findDescriptor(args.get(i).getDescriptor());
            if (argsCnt.charAt(i) == '1') {
                index += 2;
            } else {
                index++;
            }
        }
        out.append(") {").append("\n");
        final byte[] code = m.getCode();
        if (code != null) {
            int len = m.getMaxLocals();
            for (int index = argsCnt.length(), i = argsCnt.length(); i < len; i++) {
                out.append("  var ");
                out.append("arg").append(String.valueOf(i)).append(";\n");
            }
            out.append("  var s = new Array();\n");
            produceCode(code);
        } else {
            out.append("  throw 'no code found for ").append(m.getInternalSig()).append("';\n");
        }
        out.append("};");
    }
    
    private void generateInstanceMethod(String prefix, MethodData m) throws IOException {
        if (javaScriptBody(prefix, m, false)) {
            return;
        }
        StringBuilder argsCnt = new StringBuilder();
        final String mn = findMethodName(m, argsCnt);
        out.append(prefix).append(mn).append(" = function");
        out.append("(arg0");
        String space = ",";
        for (int index = 1, i = 0; i < argsCnt.length(); i++) {
            out.append(space);
            out.append("arg").append(String.valueOf(index));
            if (argsCnt.charAt(i) == '1') {
                index += 2;
            } else {
                index++;
            }
        }
        out.append(") {").append("\n");
        final byte[] code = m.getCode();
        if (code != null) {
            int len = m.getMaxLocals();
            for (int index = argsCnt.length(), i = argsCnt.length(); i < len; i++) {
                out.append("  var ");
                out.append("arg").append(String.valueOf(i + 1)).append(";\n");
            }
            out.append(";\n  var s = new Array();\n");
            produceCode(code);
        } else {
            out.append("  throw 'no code found for ").append(m.getInternalSig()).append("';\n");
        }
        out.append("};");
    }

    private void produceCode(byte[] byteCodes) throws IOException {
        out.append("\n  var gt = 0;\n  for(;;) switch(gt) {\n");
        for (int i = 0; i < byteCodes.length; i++) {
            int prev = i;
            out.append("    case " + i).append(": ");
            final int c = readByte(byteCodes, i);
            switch (c) {
                case opc_aload_0:
                case opc_iload_0:
                case opc_lload_0:
                case opc_fload_0:
                case opc_dload_0:
                    out.append("s.push(arg0);");
                    break;
                case opc_aload_1:
                case opc_iload_1:
                case opc_lload_1:
                case opc_fload_1:
                case opc_dload_1:
                    out.append("s.push(arg1);");
                    break;
                case opc_aload_2:
                case opc_iload_2:
                case opc_lload_2:
                case opc_fload_2:
                case opc_dload_2:
                    out.append("s.push(arg2);");
                    break;
                case opc_aload_3:
                case opc_iload_3:
                case opc_lload_3:
                case opc_fload_3:
                case opc_dload_3:
                    out.append("s.push(arg3);");
                    break;
                case opc_iload:
                case opc_lload:
                case opc_fload:
                case opc_dload:
                case opc_aload: {
                    final int indx = readByte(byteCodes, ++i);
                    out.append("s.push(arg").append(indx + ");");
                    break;
                }
                case opc_istore:
                case opc_lstore:
                case opc_fstore:
                case opc_dstore:
                case opc_astore: {
                    final int indx = readByte(byteCodes, ++i);
                    out.append("arg" + indx).append(" = s.pop();");
                    break;
                }
                case opc_astore_0:
                case opc_istore_0:
                case opc_lstore_0:
                case opc_fstore_0:
                case opc_dstore_0:
                    out.append("arg0 = s.pop();");
                    break;
                case opc_astore_1:
                case opc_istore_1:
                case opc_lstore_1:
                case opc_fstore_1:
                case opc_dstore_1:
                    out.append("arg1 = s.pop();");
                    break;
                case opc_astore_2:
                case opc_istore_2:
                case opc_lstore_2:
                case opc_fstore_2:
                case opc_dstore_2:
                    out.append("arg2 = s.pop();");
                    break;
                case opc_astore_3:
                case opc_istore_3:
                case opc_lstore_3:
                case opc_fstore_3:
                case opc_dstore_3:
                    out.append("arg3 = s.pop();");
                    break;
                case opc_iadd:
                case opc_ladd:
                case opc_fadd:
                case opc_dadd:
                    out.append("s.push(s.pop() + s.pop());");
                    break;
                case opc_isub:
                case opc_lsub:
                case opc_fsub:
                case opc_dsub:
                    out.append("{ var tmp = s.pop(); s.push(s.pop() - tmp); }");
                    break;
                case opc_imul:
                case opc_lmul:
                case opc_fmul:
                case opc_dmul:
                    out.append("s.push(s.pop() * s.pop());");
                    break;
                case opc_idiv:
                case opc_ldiv:
                    out.append("{ var tmp = s.pop(); s.push(Math.floor(s.pop() / tmp)); }");
                    break;
                case opc_fdiv:
                case opc_ddiv:
                    out.append("{ var tmp = s.pop(); s.push(s.pop() / tmp); }");
                    break;
                case opc_irem:
                case opc_lrem:
                case opc_frem:
                case opc_drem:
                    out.append("{ var d = s.pop(); s.push(s.pop() % d); }");
                    break;
                case opc_iand:
                case opc_land:
                    out.append("s.push(s.pop() & s.pop());");
                    break;
                case opc_ior:
                case opc_lor:
                    out.append("s.push(s.pop() | s.pop());");
                    break;
                case opc_ixor:
                case opc_lxor:
                    out.append("s.push(s.pop() ^ s.pop());");
                    break;
                case opc_ineg:
                case opc_lneg:
                case opc_fneg:
                case opc_dneg:
                    out.append("s.push(- s.pop());");
                    break;
                case opc_ishl:
                case opc_lshl:
                    out.append("{ var v = s.pop(); s.push(s.pop() << v); }");
                    break;
                case opc_ishr:
                case opc_lshr:
                    out.append("{ var v = s.pop(); s.push(s.pop() >> v); }");
                    break;
                case opc_iushr:
                case opc_lushr:
                    out.append("{ var v = s.pop(); s.push(s.pop() >>> v); }");
                    break;
                case opc_iinc: {
                    final int varIndx = readByte(byteCodes, ++i);
                    final int incrBy = byteCodes[++i];
                    if (incrBy == 1) {
                        out.append("arg" + varIndx).append("++;");
                    } else {
                        out.append("arg" + varIndx).append(" += " + incrBy).append(";");
                    }
                    break;
                }
                case opc_return:
                    out.append("return;");
                    break;
                case opc_ireturn:
                case opc_lreturn:
                case opc_freturn:
                case opc_dreturn:
                case opc_areturn:
                    out.append("return s.pop();");
                    break;
                case opc_i2l:
                case opc_i2f:
                case opc_i2d:
                case opc_l2i:
                    // max int check?
                case opc_l2f:
                case opc_l2d:
                case opc_f2d:
                case opc_d2f:
                    out.append("/* number conversion */");
                    break;
                case opc_f2i:
                case opc_f2l:
                case opc_d2i:
                case opc_d2l:
                    out.append("s.push(Math.floor(s.pop()));");
                    break;
                case opc_i2b:
                case opc_i2c:
                case opc_i2s:
                    out.append("/* number conversion */");
                    break;
                case opc_aconst_null:
                    out.append("s.push(null);");
                    break;
                case opc_iconst_m1:
                    out.append("s.push(-1);");
                    break;
                case opc_iconst_0:
                case opc_dconst_0:
                case opc_lconst_0:
                case opc_fconst_0:
                    out.append("s.push(0);");
                    break;
                case opc_iconst_1:
                case opc_lconst_1:
                case opc_fconst_1:
                case opc_dconst_1:
                    out.append("s.push(1);");
                    break;
                case opc_iconst_2:
                case opc_fconst_2:
                    out.append("s.push(2);");
                    break;
                case opc_iconst_3:
                    out.append("s.push(3);");
                    break;
                case opc_iconst_4:
                    out.append("s.push(4);");
                    break;
                case opc_iconst_5:
                    out.append("s.push(5);");
                    break;
                case opc_ldc: {
                    int indx = readByte(byteCodes, ++i);
                    String v = encodeConstant(indx);
                    out.append("s.push(").append(v).append(");");
                    break;
                }
                case opc_ldc_w:
                case opc_ldc2_w: {
                    int indx = readIntArg(byteCodes, i);
                    i += 2;
                    String v = encodeConstant(indx);
                    out.append("s.push(").append(v).append(");");
                    break;
                }
                case opc_lcmp:
                case opc_fcmpl:
                case opc_fcmpg:
                case opc_dcmpl:
                case opc_dcmpg: {
                    out.append("{ var delta = s.pop() - s.pop(); s.push(delta < 0 ?-1 : (delta == 0 ? 0 : 1)); }");
                    break;
                }
                case opc_if_acmpeq:
                    i = generateIf(byteCodes, i, "===");
                    break;
                case opc_if_acmpne:
                    i = generateIf(byteCodes, i, "!=");
                    break;
                case opc_if_icmpeq: {
                    i = generateIf(byteCodes, i, "==");
                    break;
                }
                case opc_ifeq: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (s.pop() == 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case opc_ifne: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (s.pop() != 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case opc_iflt: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (s.pop() < 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case opc_ifle: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (s.pop() <= 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case opc_ifgt: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (s.pop() > 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case opc_ifge: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (s.pop() >= 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case opc_ifnonnull: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (s.pop() !== null) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case opc_ifnull: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (s.pop() === null) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case opc_if_icmpne:
                    i = generateIf(byteCodes, i, "!=");
                    break;
                case opc_if_icmplt:
                    i = generateIf(byteCodes, i, ">");
                    break;
                case opc_if_icmple:
                    i = generateIf(byteCodes, i, ">=");
                    break;
                case opc_if_icmpgt:
                    i = generateIf(byteCodes, i, "<");
                    break;
                case opc_if_icmpge:
                    i = generateIf(byteCodes, i, "<=");
                    break;
                case opc_goto: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("gt = " + indx).append("; continue;");
                    i += 2;
                    break;
                }
                case opc_lookupswitch: {
                    int table = i / 4 * 4 + 4;
                    int dflt = i + readInt4(byteCodes, table);
                    table += 4;
                    int n = readInt4(byteCodes, table);
                    table += 4;
                    out.append("switch (s.pop()) {\n");
                    while (n-- > 0) {
                        int cnstnt = readInt4(byteCodes, table);
                        table += 4;
                        int offset = i + readInt4(byteCodes, table);
                        table += 4;
                        out.append("  case " + cnstnt).append(": gt = " + offset).append("; continue;\n");
                    }
                    out.append("  default: gt = " + dflt).append("; continue;\n}");
                    i = table - 1;
                    break;
                }
                case opc_tableswitch: {
                    int table = i / 4 * 4 + 4;
                    int dflt = i + readInt4(byteCodes, table);
                    table += 4;
                    int low = readInt4(byteCodes, table);
                    table += 4;
                    int high = readInt4(byteCodes, table);
                    table += 4;
                    out.append("switch (s.pop()) {\n");
                    while (low <= high) {
                        int offset = i + readInt4(byteCodes, table);
                        table += 4;
                        out.append("  case " + low).append(": gt = " + offset).append("; continue;\n");
                        low++;
                    }
                    out.append("  default: gt = " + dflt).append("; continue;\n}");
                    i = table - 1;
                    break;
                }
                case opc_invokeinterface: {
                    i = invokeVirtualMethod(byteCodes, i) + 2;
                    break;
                }
                case opc_invokevirtual:
                    i = invokeVirtualMethod(byteCodes, i);
                    break;
                case opc_invokespecial:
                    i = invokeStaticMethod(byteCodes, i, false);
                    break;
                case opc_invokestatic:
                    i = invokeStaticMethod(byteCodes, i, true);
                    break;
                case opc_new: {
                    int indx = readIntArg(byteCodes, i);
                    String ci = jc.getClassName(indx);
                    out.append("s.push(new ");
                    out.append(ci.replace('/','_'));
                    out.append("());");
                    addReference(ci);
                    i += 2;
                    break;
                }
                case opc_newarray: {
                    int type = byteCodes[i++];
                    out.append("s.push(new Array(s.pop()).fillNulls());");
                    break;
                }
                case opc_anewarray: {
                    i += 2; // skip type of array
                    out.append("s.push(new Array(s.pop()).fillNulls());");
                    break;
                }
                case opc_multianewarray: {
                    i += 2;
                    int dim = readByte(byteCodes, ++i);
                    out.append("{ var a0 = new Array(s.pop()).fillNulls();");
                    for (int d = 1; d < dim; d++) {
                        out.append("\n  var l" + d).append(" = s.pop();");
                        out.append("\n  for (var i" + d).append (" = 0; i" + d).
                            append(" < a" + (d - 1)).
                            append(".length; i" + d).append("++) {");
                        out.append("\n    var a" + d).
                            append (" = new Array(l" + d).append(").fillNulls();");
                        out.append("\n    a" + (d - 1)).append("[i" + d).append("] = a" + d).
                            append(";");
                    }
                    for (int d = 1; d < dim; d++) {
                        out.append("\n  }");
                    }
                    out.append("\ns.push(a0); }");
                    break;
                }
                case opc_arraylength:
                    out.append("s.push(s.pop().length);");
                    break;
                case opc_iastore:
                case opc_lastore:
                case opc_fastore:
                case opc_dastore:
                case opc_aastore:
                case opc_bastore:
                case opc_castore:
                case opc_sastore: {
                    out.append("{ var value = s.pop(); var indx = s.pop(); s.pop()[indx] = value; }");
                    break;
                }
                case opc_iaload:
                case opc_laload:
                case opc_faload:
                case opc_daload:
                case opc_aaload:
                case opc_baload:
                case opc_caload:
                case opc_saload: {
                    out.append("{ var indx = s.pop(); s.push(s.pop()[indx]); }");
                    break;
                }
                case opc_pop2:
                    out.append("s.pop();");
                case opc_pop:
                    out.append("s.pop();");
                    break;
                case opc_dup:
                    out.append("s.push(s[s.length - 1]);");
                    break;
                case opc_dup_x1:
                    out.append("{ var v1 = s.pop(); var v2 = s.pop(); s.push(v1); s.push(v2); s.push(v1); }");
                    break;
                case opc_dup_x2:
                    out.append("{ var v1 = s.pop(); var v2 = s.pop(); var v3 = s.pop(); s.push(v1); s.push(v3); s.push(v2); s.push(v1); }");
                    break;
                case opc_bipush:
                    out.append("s.push(" + byteCodes[++i] + ");");
                    break;
                case opc_sipush:
                    out.append("s.push(" + readIntArg(byteCodes, i) + ");");
                    i += 2;
                    break;
                case opc_getfield: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    out.append("s.push(s.pop().fld_").
                        append(fi[1]).append(");");
                    i += 2;
                    break;
                }
                case opc_getstatic: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    out.append("s.push(").append(fi[0].replace('/', '_'));
                    out.append('.').append(fi[1]).append(");");
                    i += 2;
                    addReference(fi[0]);
                    break;
                }
                case opc_putstatic: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    out.append(fi[0].replace('/', '_'));
                    out.append('.').append(fi[1]).append(" = s.pop();");
                    i += 2;
                    addReference(fi[0]);
                    break;
                }
                case opc_putfield: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    out.append("{ var v = s.pop(); s.pop().fld_")
                       .append(fi[1]).append(" = v; }");
                    i += 2;
                    break;
                }
                case opc_checkcast: {
                    int indx = readIntArg(byteCodes, i);
                    final String type = jc.getClassName(indx);
                    if (!type.startsWith("[")) {
                        // no way to check arrays right now
                        out.append("if(s[s.length - 1].$instOf_")
                           .append(type.replace('/', '_'))
                           .append(" != 1) throw {};"); // XXX proper exception
                    }
                    i += 2;
                    break;
                }
                case opc_instanceof: {
                    int indx = readIntArg(byteCodes, i);
                    final String type = jc.getClassName(indx);
                    out.append("s.push(s.pop().$instOf_")
                       .append(type.replace('/', '_'))
                       .append(" ? 1 : 0);");
                    i += 2;
                    break;
                }
                case opc_athrow: {
                    out.append("{ var t = s.pop(); s = new Array(1); s[0] = t; throw t; }");
                    break;
                }
                default: {
                    out.append("throw 'unknown bytecode " + c + "';");
                }
                    
            }
            out.append(" //");
            for (int j = prev; j <= i; j++) {
                out.append(" ");
                final int cc = readByte(byteCodes, j);
                out.append(Integer.toString(cc));
            }
            out.append("\n");
        }
        out.append("  }\n");
    }

    private int generateIf(byte[] byteCodes, int i, final String test) throws IOException {
        int indx = i + readIntArg(byteCodes, i);
        out.append("if (s.pop() ").append(test).append(" s.pop()) { gt = " + indx);
        out.append("; continue; }");
        return i + 2;
    }

    private int readIntArg(byte[] byteCodes, int offsetInstruction) {
        final int indxHi = byteCodes[offsetInstruction + 1] << 8;
        final int indxLo = byteCodes[offsetInstruction + 2];
        return (indxHi & 0xffffff00) | (indxLo & 0xff);
    }
    private int readInt4(byte[] byteCodes, int offsetInstruction) {
        final int d = byteCodes[offsetInstruction + 0] << 24;
        final int c = byteCodes[offsetInstruction + 1] << 16;
        final int b = byteCodes[offsetInstruction + 2] << 8;
        final int a = byteCodes[offsetInstruction + 3];
        return (d & 0xff000000) | (c & 0xff0000) | (b & 0xff00) | (a & 0xff);
    }
    private int readByte(byte[] byteCodes, int offsetInstruction) {
        return (byteCodes[offsetInstruction] + 256) % 256;
    }
    
    private static void countArgs(String descriptor, boolean[] hasReturnType, StringBuilder sig, StringBuilder cnt) {
        int i = 0;
        Boolean count = null;
        boolean array = false;
        int firstPos = sig.length();
        while (i < descriptor.length()) {
            char ch = descriptor.charAt(i++);
            switch (ch) {
                case '(':
                    count = true;
                    continue;
                case ')':
                    count = false;
                    continue;
                case 'A':
                    array = true;
                    break;
                case 'B': 
                case 'C': 
                case 'D': 
                case 'F': 
                case 'I': 
                case 'J': 
                case 'S': 
                case 'Z': 
                    if (count) {
                        if (array) {
                            sig.append('A');
                        }
                        sig.append(ch);
                        if (ch == 'J' || ch == 'D') {
                            cnt.append('1');
                        } else {
                            cnt.append('0');
                        }
                    } else {
                        hasReturnType[0] = true;
                        sig.insert(firstPos, ch);
                        if (array) {
                            sig.insert(firstPos, 'A');
                        }
                    }
                    array = false;
                    continue;
                case 'V': 
                    assert !count;
                    hasReturnType[0] = false;
                    sig.insert(firstPos, 'V');
                    continue;
                case 'L':
                    int next = descriptor.indexOf(';', i);
                    if (count) {
                        if (array) {
                            sig.append('A');
                        }
                        sig.append(ch);
                        sig.append(descriptor.substring(i, next).replace('/', '_'));
                        cnt.append('0');
                    } else {
                        sig.insert(firstPos, descriptor.substring(i, next).replace('/', '_'));
                        sig.insert(firstPos, ch);
                        if (array) {
                            sig.insert(firstPos, 'A');
                        }
                        hasReturnType[0] = true;
                    }
                    i = next + 1;
                    continue;
                case '[':
                    //arrays++;
                    continue;
                default:
                    break; // invalid character
            }
        }
    }

    private String findMethodName(MethodData m, StringBuilder cnt) {
        StringBuilder name = new StringBuilder();
        if ("<init>".equals(m.getName())) { // NOI18N
            name.append("cons"); // NOI18N
        } else if ("<clinit>".equals(m.getName())) { // NOI18N
            name.append("class"); // NOI18N
        } else {
            name.append(m.getName());
        } 
        
        boolean hasReturn[] = { false };
        countArgs(findDescriptor(m.getInternalSig()), hasReturn, name, cnt);
        return name.toString();
    }

    private String findMethodName(String[] mi, StringBuilder cnt, boolean[] hasReturn) {
        StringBuilder name = new StringBuilder();
        String descr = mi[2];//mi.getDescriptor();
        String nm= mi[1];
        if ("<init>".equals(nm)) { // NOI18N
            name.append("cons"); // NOI18N
        } else {
            name.append(nm);
        }
        countArgs(findDescriptor(descr), hasReturn, name, cnt);
        return name.toString();
    }

    private int invokeStaticMethod(byte[] byteCodes, int i, boolean isStatic)
    throws IOException {
        int methodIndex = readIntArg(byteCodes, i);
        String[] mi = jc.getFieldInfoName(methodIndex);
        boolean[] hasReturn = { false };
        StringBuilder cnt = new StringBuilder();
        String mn = findMethodName(mi, cnt, hasReturn);
        out.append("{ ");
        for (int j = cnt.length() - 1; j >= 0; j--) {
            out.append("var v" + j).append(" = s.pop(); ");
        }
        
        if (hasReturn[0]) {
            out.append("s.push(");
        }
        final String in = mi[0];
        out.append(in.replace('/', '_'));
        out.append("(false).");
        out.append(mn);
        out.append('(');
        String sep = "";
        if (!isStatic) {
            out.append("s.pop()");
            sep = ", ";
        }
        for (int j = 0; j < cnt.length(); j++) {
            out.append(sep);
            out.append("v" + j);
            sep = ", ";
        }
        out.append(")");
        if (hasReturn[0]) {
            out.append(")");
        }
        out.append("; }");
        i += 2;
        addReference(in);
        return i;
    }
    private int invokeVirtualMethod(byte[] byteCodes, int i)
    throws IOException {
        int methodIndex = readIntArg(byteCodes, i);
        String[] mi = jc.getFieldInfoName(methodIndex);
        boolean[] hasReturn = { false };
        StringBuilder cnt = new StringBuilder();
        String mn = findMethodName(mi, cnt, hasReturn);
        out.append("{ ");
        for (int j = cnt.length() - 1; j >= 0; j--) {
            out.append("var v" + j).append(" = s.pop(); ");
        }
        out.append("var self = s.pop(); ");
        if (hasReturn[0]) {
            out.append("s.push(");
        }
        out.append("self.");
        out.append(mn);
        out.append('(');
        out.append("self");
        for (int j = 0; j < cnt.length(); j++) {
            out.append(", ");
            out.append("v" + j);
        }
        out.append(")");
        if (hasReturn[0]) {
            out.append(")");
        }
        out.append("; }");
        i += 2;
        return i;
    }
    
    private void addReference(String cn) throws IOException {
        if (requireReference(cn)) {
            out.append(" /* needs ").append(cn).append(" */");
        }
    }

    private void outType(String d, StringBuilder out) {
        int arr = 0;
        while (d.charAt(0) == '[') {
            out.append('A');
            d = d.substring(1);
        }
        if (d.charAt(0) == 'L') {
            assert d.charAt(d.length() - 1) == ';';
            out.append(d.replace('/', '_').substring(0, d.length() - 1));
        } else {
            out.append(d);
        }
    }

    private String encodeConstant(int entryIndex) throws IOException {
        String[] classRef = { null };
        String s = jc.stringValue(entryIndex, classRef);
        if (classRef[0] != null) {
            addReference(classRef[0]);
        }
        return s;
    }

    private String findDescriptor(String d) {
        return d.replace('[', 'A');
    }

    private boolean javaScriptBody(String prefix, MethodData m, boolean isStatic) throws IOException {
        byte[] arr = m.findAnnotationData(true);
        if (arr == null) {
            return false;
        }
        final String jvmType = "Lorg/apidesign/bck2brwsr/core/JavaScriptBody;";
        class P extends AnnotationParser {
            int cnt;
            String[] args = new String[30];
            String body;
            
            @Override
            protected void visitAttr(String type, String attr, String value) {
                if (type.equals(jvmType)) {
                    if ("body".equals(attr)) {
                        body = value;
                    } else if ("args".equals(attr)) {
                        args[cnt++] = value;
                    } else {
                        throw new IllegalArgumentException(attr);
                    }
                }
            }
        }
        P p = new P();
        p.parse(arr, jc);
        if (p.body == null) {
            return false;
        }
        StringBuilder cnt = new StringBuilder();
        out.append(prefix).append(findMethodName(m, cnt));
        out.append(" = function(");
        String space;
        int index;
        if (!isStatic) {                
            out.append(p.args[0]);
            space = ",";
            index = 1;
        } else {
            space = "";
            index = 0;
        }
        for (int i = 0; i < cnt.length(); i++) {
            out.append(space);
            out.append(p.args[index]);
            index++;
            space = ",";
        }
        out.append(") {").append("\n");
        out.append(p.body);
        out.append("\n}\n");
        return true;
    }
    private static String className(ClassData jc) {
        //return jc.getName().getInternalName().replace('/', '_');
        return jc.getClassName().replace('/', '_');
    }
    
    private static String[] findAnnotation(
        byte[] arr, ClassData cd, final String className, 
        final String... attrNames
    ) throws IOException {
        if (arr == null) {
            return null;
        }
        final String[] values = new String[attrNames.length];
        final boolean[] found = { false };
        final String jvmType = "L" + className.replace('.', '/') + ";";
        AnnotationParser ap = new AnnotationParser() {
            @Override
            protected void visitAttr(String type, String attr, String value) {
                if (type.equals(jvmType)) {
                    found[0] = true;
                    for (int i = 0; i < attrNames.length; i++) {
                        if (attrNames[i].equals(attr)) {
                            values[i] = value;
                        }
                    }
                }
            }
            
        };
        ap.parse(arr, cd);
        return found[0] ? values : null;
    }

    private CharSequence initField(FieldData v) {
        final String is = v.getInternalSig();
        if (is.length() == 1) {
            switch (is.charAt(0)) {
                case 'S':
                case 'J':
                case 'B':
                case 'Z':
                case 'C':
                case 'I': return " = 0;";
                case 'F': 
                case 'D': return " = 0.0;";
                default:
                    throw new IllegalStateException(is);
            }
        }
        return " = null;";
    }

    private static void generateAnno(ClassData cd, final Appendable out, byte[] data) throws IOException {
        AnnotationParser ap = new AnnotationParser() {
            int cnt;
            
            @Override
            protected void visitAnnotationStart(String type) throws IOException {
                out.append('"').append(type).append("\" : {\n");
                cnt = 0;
            }

            @Override
            protected void visitAnnotationEnd(String type) throws IOException {
                out.append("\n}\n");
            }
            
            @Override
            protected void visitAttr(String type, String attr, String value) 
            throws IOException {
                if (cnt++ > 0) {
                    out.append(",\n");
                }
                out.append(attr).append(" : ").append(value);
            }
        };
        ap.parse(data, cd);
    }
}
