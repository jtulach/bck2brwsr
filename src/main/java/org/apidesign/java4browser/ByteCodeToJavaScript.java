/*
Java 4 Browser Bytecode Translator
Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. Look for COPYING file in the top folder.
If not, see http://opensource.org/licenses/GPL-2.0.
*/
package org.apidesign.java4browser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import static org.netbeans.modules.classfile.ByteCodes.*;
import org.netbeans.modules.classfile.CPClassInfo;
import org.netbeans.modules.classfile.CPEntry;
import org.netbeans.modules.classfile.CPFieldInfo;
import org.netbeans.modules.classfile.CPMethodInfo;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.Parameter;
import org.netbeans.modules.classfile.Variable;

/** Translator of the code inside class files to JavaScript.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ByteCodeToJavaScript {
    private final ClassFile jc;
    private final Appendable out;

    private ByteCodeToJavaScript(ClassFile jc, Appendable out) {
        this.jc = jc;
        this.out = out;
    }
    
    /** Converts a given class file to a JavaScript version.
     * @param fileName the name of the file we are reading
     * @param classFile input stream with code of the .class file
     * @param out a {@link StringBuilder} or similar to generate the output to
     * @throws IOException if something goes wrong during read or write or translating
     */
    public static void compile(String fileName, InputStream classFile, Appendable out) throws IOException {
        ClassFile jc = new ClassFile(classFile, true);
        ByteCodeToJavaScript compiler = new ByteCodeToJavaScript(jc, out);
        for (Method m : jc.getMethods()) {
            if (m.isStatic()) {
                compiler.generateStaticMethod(m);
            } else {
                compiler.generateInstanceMethod(m);
            }
        }
        for (Variable v : jc.getVariables()) {
            if (v.isStatic()) {
                compiler.generateStaticField(v);
            }
        }
        out.append("function java_lang_Object(){}\n"); // XXX temporary
        out.append("function java_lang_Object_consV(self){}\n"); // XXX temporary
        
        final String className = jc.getName().getExternalName().replace('.', '_');
        out.append("\nfunction ").append(className);
        out.append("() {");
        for (Method m : jc.getMethods()) {
            if (!m.isStatic()) {
                compiler.generateMethodReference(m);
            }
        }
        for (Variable v : jc.getVariables()) {
            if (!v.isStatic()) {
                out.append("\n  this." + v.getName() + " = 0;");
            }
        }
        out.append("\n  this.$instOf_").append(className).append(" = true;");
        out.append("\n}");
        ClassName sc = jc.getSuperClass();
        if (sc != null) {
            out.append("\n  ").append(className)
               .append(".prototype = new ").append(sc.getExternalName().replace('.', '_'));
        }
    }
    private void generateStaticMethod(Method m) throws IOException {
        out.append("\nfunction ").append(
            jc.getName().getExternalName().replace('.', '_')
        ).append('_').append(findMethodName(m));
        out.append('(');
        String space = "";
        List<Parameter> args = m.getParameters();
        for (int index = 0, i = 0; i < args.size(); i++) {
            out.append(space);
            out.append("arg").append(String.valueOf(index));
            space = ",";
            final String desc = args.get(i).getDescriptor();
            if ("D".equals(desc) || "J".equals(desc)) {
                index += 2;
            } else {
                index++;
            }
        }
        out.append(") {").append("\n");
        final Code code = m.getCode();
        int len = code.getMaxLocals();
        for (int index = args.size(), i = args.size(); i < len; i++) {
            out.append("  var ");
            out.append("arg").append(String.valueOf(i)).append(";\n");
        }
        out.append("  var stack = new Array(");
        out.append(Integer.toString(code.getMaxStack()));
        out.append(");\n");
        produceCode(code.getByteCodes());
        out.append("}");
    }
    
    private void generateMethodReference(Method m) throws IOException {
        final String name = findMethodName(m);
        out.append("\n  this.").append(name).append(" = ")
           .append(jc.getName().getExternalName().replace('.', '_'))
           .append('_').append(name).append(";");
    }
    
    private void generateInstanceMethod(Method m) throws IOException {
        out.append("\nfunction ").append(
            jc.getName().getExternalName().replace('.', '_')
        ).append('_').append(findMethodName(m));
        out.append("(arg0");
        String space = ",";
        List<Parameter> args = m.getParameters();
        for (int index = 1, i = 0; i < args.size(); i++) {
            out.append(space);
            out.append("arg").append(String.valueOf(index));
            final String desc = args.get(i).getDescriptor();
            if ("D".equals(desc) || "J".equals(desc)) {
                index += 2;
            } else {
                index++;
            }
        }
        out.append(") {").append("\n");
        final Code code = m.getCode();
        int len = code.getMaxLocals();
        for (int index = args.size(), i = args.size(); i < len; i++) {
            out.append("  var ");
            out.append("arg").append(String.valueOf(i + 1)).append(";\n");
        }
        out.append(";\n  var stack = new Array(");
        out.append(Integer.toString(code.getMaxStack()));
        out.append(");\n");
        produceCode(code.getByteCodes());
        out.append("}");
    }

    private void produceCode(byte[] byteCodes) throws IOException {
        out.append("\n  var gt = 0;\n  for(;;) switch(gt) {\n");
        for (int i = 0; i < byteCodes.length; i++) {
            int prev = i;
            out.append("    case " + i).append(": ");
            final int c = (byteCodes[i] + 256) % 256;
            switch (c) {
                case bc_aload_0:
                case bc_iload_0:
                case bc_lload_0:
                case bc_fload_0:
                case bc_dload_0:
                    out.append("stack.push(arg0);");
                    break;
                case bc_aload_1:
                case bc_iload_1:
                case bc_lload_1:
                case bc_fload_1:
                case bc_dload_1:
                    out.append("stack.push(arg1);");
                    break;
                case bc_aload_2:
                case bc_iload_2:
                case bc_lload_2:
                case bc_fload_2:
                case bc_dload_2:
                    out.append("stack.push(arg2);");
                    break;
                case bc_aload_3:
                case bc_iload_3:
                case bc_lload_3:
                case bc_fload_3:
                case bc_dload_3:
                    out.append("stack.push(arg3);");
                    break;
                case bc_iload:
                case bc_lload:
                case bc_fload:
                case bc_dload:
                case bc_aload: {
                    final int indx = (byteCodes[++i] + 256) % 256;
                    out.append("stack.push(arg").append(indx + ");");
                    break;
                }
                case bc_astore_0:
                case bc_istore_0:
                case bc_lstore_0:
                case bc_fstore_0:
                case bc_dstore_0:
                    out.append("arg0 = stack.pop();");
                    break;
                case bc_astore_1:
                case bc_istore_1:
                case bc_lstore_1:
                case bc_fstore_1:
                case bc_dstore_1:
                    out.append("arg1 = stack.pop();");
                    break;
                case bc_astore_2:
                case bc_istore_2:
                case bc_lstore_2:
                case bc_fstore_2:
                case bc_dstore_2:
                    out.append("arg2 = stack.pop();");
                    break;
                case bc_astore_3:
                case bc_istore_3:
                case bc_lstore_3:
                case bc_fstore_3:
                case bc_dstore_3:
                    out.append("arg3 = stack.pop();");
                    break;
                case bc_iadd:
                case bc_ladd:
                case bc_fadd:
                case bc_dadd:
                    out.append("stack.push(stack.pop() + stack.pop());");
                    break;
                case bc_isub:
                case bc_lsub:
                case bc_fsub:
                case bc_dsub:
                    out.append("{ var tmp = stack.pop(); stack.push(stack.pop() - tmp); }");
                    break;
                case bc_imul:
                case bc_lmul:
                case bc_fmul:
                case bc_dmul:
                    out.append("stack.push(stack.pop() * stack.pop());");
                    break;
                case bc_idiv:
                case bc_ldiv:
                    out.append("{ var tmp = stack.pop(); stack.push(Math.floor(stack.pop() / tmp)); }");
                    break;
                case bc_fdiv:
                case bc_ddiv:
                    out.append("{ var tmp = stack.pop(); stack.push(stack.pop() / tmp); }");
                    break;
                case bc_iand:
                case bc_land:
                    out.append("stack.push(stack.pop() & stack.pop());");
                    break;
                case bc_ior:
                case bc_lor:
                    out.append("stack.push(stack.pop() | stack.pop());");
                    break;
                case bc_ixor:
                case bc_lxor:
                    out.append("stack.push(stack.pop() ^ stack.pop());");
                    break;
                case bc_iinc: {
                    final int varIndx = (byteCodes[++i] + 256) % 256;
                    final int incrBy = (byteCodes[++i] + 256) % 256;
                    if (incrBy == 1) {
                        out.append("arg" + varIndx).append("++;");
                    } else {
                        out.append("arg" + varIndx).append(" += " + incrBy).append(";");
                    }
                    break;
                }
                case bc_return:
                    out.append("return;");
                    break;
                case bc_ireturn:
                case bc_lreturn:
                case bc_freturn:
                case bc_dreturn:
                case bc_areturn:
                    out.append("return stack.pop();");
                    break;
                case bc_i2l:
                case bc_i2f:
                case bc_i2d:
                case bc_l2i:
                    // max int check?
                case bc_l2f:
                case bc_l2d:
                case bc_f2d:
                case bc_d2f:
                    out.append("/* number conversion */");
                    break;
                case bc_f2i:
                case bc_f2l:
                case bc_d2i:
                case bc_d2l:
                    out.append("stack.push(Math.floor(stack.pop()));");
                    break;
                case bc_i2b:
                case bc_i2c:
                case bc_i2s:
                    out.append("/* number conversion */");
                    break;
                case bc_iconst_0:
                case bc_dconst_0:
                case bc_lconst_0:
                case bc_fconst_0:
                    out.append("stack.push(0);");
                    break;
                case bc_iconst_1:
                case bc_lconst_1:
                case bc_fconst_1:
                case bc_dconst_1:
                    out.append("stack.push(1);");
                    break;
                case bc_iconst_2:
                case bc_fconst_2:
                    out.append("stack.push(2);");
                    break;
                case bc_iconst_3:
                    out.append("stack.push(3);");
                    break;
                case bc_iconst_4:
                    out.append("stack.push(4);");
                    break;
                case bc_iconst_5:
                    out.append("stack.push(5);");
                    break;
                case bc_ldc_w:
                case bc_ldc2_w: {
                    int indx = readIntArg(byteCodes, i);
                    CPEntry entry = jc.getConstantPool().get(indx);
                    i += 2;
                    out.append("stack.push(" + entry.getValue() + ");");
                    break;
                }
                case bc_if_icmpeq: {
                    i = generateIf(byteCodes, i, "==");
                    break;
                }
                case bc_ifeq: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (stack.pop() == 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case bc_ifnonnull: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (stack.pop()) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case bc_ifnull: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (!stack.pop()) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case bc_if_icmpne:
                    i = generateIf(byteCodes, i, "!=");
                    break;
                case bc_if_icmplt:
                    i = generateIf(byteCodes, i, ">");
                    break;
                case bc_if_icmple:
                    i = generateIf(byteCodes, i, ">=");
                    break;
                case bc_if_icmpgt:
                    i = generateIf(byteCodes, i, "<");
                    break;
                case bc_if_icmpge:
                    i = generateIf(byteCodes, i, "<=");
                    break;
                case bc_goto: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("gt = " + indx).append("; continue;");
                    i += 2;
                    break;
                }
                case bc_invokeinterface:
                case bc_invokevirtual:
                    i = invokeVirtualMethod(byteCodes, i);
                    break;
                case bc_invokespecial:
                    i = invokeStaticMethod(byteCodes, i, false);
                    break;
                case bc_invokestatic:
                    i = invokeStaticMethod(byteCodes, i, true);
                    break;
                case bc_new: {
                    int indx = readIntArg(byteCodes, i);
                    CPClassInfo ci = jc.getConstantPool().getClass(indx);
                    out.append("stack.push(");
                    out.append("new ").append(ci.getClassName().getExternalName().replace('.','_'));
                    out.append(");");
                    i += 2;
                    break;
                }
                case bc_dup:
                    out.append("stack.push(stack[stack.length - 1]);");
                    break;
                case bc_bipush:
                    out.append("stack.push(" + byteCodes[++i] + ");");
                    break;
                case bc_getfield: {
                    int indx = readIntArg(byteCodes, i);
                    CPFieldInfo fi = (CPFieldInfo) jc.getConstantPool().get(indx);
                    out.append(" stack.push(stack.pop().").append(fi.getFieldName()).append(");");
                    i += 2;
                    break;
                }
                case bc_getstatic: {
                    int indx = readIntArg(byteCodes, i);
                    CPFieldInfo fi = (CPFieldInfo) jc.getConstantPool().get(indx);
                    out.append("stack.push(").append(fi.getClassName().getExternalName().replace('.', '_'));
                    out.append('_').append(fi.getFieldName()).append(");");
                    i += 2;
                    break;
                }
                case bc_putstatic: {
                    int indx = readIntArg(byteCodes, i);
                    CPFieldInfo fi = (CPFieldInfo) jc.getConstantPool().get(indx);
                    out.append(fi.getClassName().getExternalName().replace('.', '_'));
                    out.append('_').append(fi.getFieldName()).append(" = stack.pop();");
                    i += 2;
                    break;
                }
                case bc_putfield: {
                    int indx = readIntArg(byteCodes, i);
                    CPFieldInfo fi = (CPFieldInfo) jc.getConstantPool().get(indx);
                    out.append("{ var v = stack.pop(); stack.pop().")
                       .append(fi.getFieldName()).append(" = v; }");
                    i += 2;
                    break;
                }
                case bc_instanceof: {
                    int indx = readIntArg(byteCodes, i);
                    CPClassInfo ci = jc.getConstantPool().getClass(indx);
                    out.append("stack.push(stack.pop().$instOf_")
                       .append(ci.getClassName().getExternalName().replace('.', '_'))
                       .append(" ? 1 : 0);");
                    i += 2;
                }
                    
            }
            out.append(" /*");
            for (int j = prev; j <= i; j++) {
                out.append(" ");
                final int cc = (byteCodes[j] + 256) % 256;
                out.append(Integer.toString(cc));
            }
            out.append("*/\n");
        }
        out.append("  }\n");
    }

    private int generateIf(byte[] byteCodes, int i, final String test) throws IOException {
        int indx = i + readIntArg(byteCodes, i);
        out.append("if (stack.pop() ").append(test).append(" stack.pop()) { gt = " + indx);
        out.append("; continue; }");
        return i + 2;
    }

    private int readIntArg(byte[] byteCodes, int offsetInstruction) {
        final int indxHi = byteCodes[offsetInstruction + 1] << 8;
        final int indxLo = byteCodes[offsetInstruction + 2];
        return (indxHi & 0xffffff00) | (indxLo & 0xff);
    }
    
    private static int countArgs(String descriptor, boolean[] hasReturnType, StringBuilder sig) {
        int cnt = 0;
        int i = 0;
        Boolean count = null;
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
                case 'B': 
                case 'C': 
                case 'D': 
                case 'F': 
                case 'I': 
                case 'J': 
                case 'S': 
                case 'Z': 
                    if (count) {
                        cnt++;
                        sig.append(ch);
                    } else {
                        hasReturnType[0] = true;
                        sig.insert(firstPos, ch);
                    }
                    continue;
                case 'V': 
                    assert !count;
                    hasReturnType[0] = false;
                    sig.insert(firstPos, 'V');
                    continue;
                case 'L':
                    int next = descriptor.indexOf(';', i);
                    if (count) {
                        cnt++;
                        sig.append(ch);
                        sig.append(descriptor.substring(i, next).replace('/', '_'));
                    } else {
                        sig.insert(firstPos, descriptor.substring(i, next).replace('/', '_'));
                        sig.insert(firstPos, ch);
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
        return cnt;
    }

    private void generateStaticField(Variable v) throws IOException {
        out.append("\nvar ")
           .append(jc.getName().getExternalName().replace('.', '_'))
           .append('_').append(v.getName()).append(" = 0;");
    }

    private String findMethodName(Method m) {
        StringBuilder out = new StringBuilder();
        if ("<init>".equals(m.getName())) { // NOI18N
            out.append("consV"); // NOI18N
        } else {
            out.append(m.getName());
            outType(m.getReturnType(), out);
        } 
        List<Parameter> args = m.getParameters();
        for (Parameter t : args) {
            outType(t.getDescriptor(), out);
        }
        return out.toString();
    }

    private String findMethodName(CPMethodInfo mi, int[] cnt, boolean[] hasReturn) {
        StringBuilder name = new StringBuilder();
        if ("<init>".equals(mi.getName())) { // NOI18N
            name.append("cons"); // NOI18N
        } else {
            name.append(mi.getName());
        }
        cnt[0] = countArgs(mi.getDescriptor(), hasReturn, name);
        return name.toString();
    }

    private int invokeStaticMethod(byte[] byteCodes, int i, boolean isStatic)
    throws IOException {
        int methodIndex = readIntArg(byteCodes, i);
        CPMethodInfo mi = (CPMethodInfo) jc.getConstantPool().get(methodIndex);
        boolean[] hasReturn = { false };
        int[] cnt = { 0 };
        String mn = findMethodName(mi, cnt, hasReturn);
        out.append("{ ");
        for (int j = cnt[0] - 1; j >= 0; j--) {
            out.append("var v" + j).append(" = stack.pop(); ");
        }
        
        if (hasReturn[0]) {
            out.append("stack.push(");
        }
        out.append(mi.getClassName().getInternalName().replace('/', '_'));
        out.append('_');
        out.append(mn);
        out.append('(');
        String sep = "";
        if (!isStatic) {
            out.append("stack.pop()");
            sep = ", ";
        }
        for (int j = 0; j < cnt[0]; j++) {
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
        return i;
    }
    private int invokeVirtualMethod(byte[] byteCodes, int i)
    throws IOException {
        int methodIndex = readIntArg(byteCodes, i);
        CPMethodInfo mi = (CPMethodInfo) jc.getConstantPool().get(methodIndex);
        boolean[] hasReturn = { false };
        int[] cnt = { 0 };
        String mn = findMethodName(mi, cnt, hasReturn);
        out.append("{ ");
        for (int j = cnt[0] - 1; j >= 0; j--) {
            out.append("var v" + j).append(" = stack.pop(); ");
        }
        out.append("var self = stack.pop(); ");
        if (hasReturn[0]) {
            out.append("stack.push(");
        }
        out.append("self.");
        out.append(mn);
        out.append('(');
        out.append("self");
        for (int j = 0; j < cnt[0]; j++) {
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

    private void outType(final String d, StringBuilder out) {
        if (d.charAt(0) == 'L') {
            assert d.charAt(d.length() - 1) == ';';
            out.append(d.replace('/', '_').substring(0, d.length() - 1));
        } else {
            out.append(d);
        }
    }
}
