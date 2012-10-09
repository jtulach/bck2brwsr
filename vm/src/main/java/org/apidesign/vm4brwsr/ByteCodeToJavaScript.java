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
package org.apidesign.vm4brwsr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.netbeans.modules.classfile.Annotation;
import org.netbeans.modules.classfile.AnnotationComponent;
import org.netbeans.modules.classfile.ArrayElementValue;
import static org.netbeans.modules.classfile.ByteCodes.*;
import org.netbeans.modules.classfile.CPClassInfo;
import org.netbeans.modules.classfile.CPEntry;
import org.netbeans.modules.classfile.CPFieldInfo;
import org.netbeans.modules.classfile.CPMethodInfo;
import org.netbeans.modules.classfile.CPStringInfo;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.ClassName;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.ElementValue;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.Parameter;
import org.netbeans.modules.classfile.PrimitiveElementValue;
import org.netbeans.modules.classfile.Variable;

/** Translator of the code inside class files to JavaScript.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ByteCodeToJavaScript {
    private final ClassFile jc;
    private final Appendable out;
    private final Collection<? super String> references;

    private ByteCodeToJavaScript(
        ClassFile jc, Appendable out, Collection<? super String> references
    ) {
        this.jc = jc;
        this.out = out;
        this.references = references;
    }

    /**
     * Converts a given class file to a JavaScript version.
     *
     * @param classFile input stream with code of the .class file
     * @param out a {@link StringBuilder} or similar to generate the output to
     * @param references a write only collection where the system adds list of
     *   other classes that were referenced and should be loaded in order the
     *   generated JavaScript code works properly. The names are in internal 
     *   JVM form so String is <code>java/lang/String</code>. Can be <code>null</code>
     *   if one is not interested in knowing references
     * @param scripts write only collection with names of resources to read
     * 
     * @throws IOException if something goes wrong during read or write or translating
     */
    
    public static void compile(
        InputStream classFile, Appendable out,
        Collection<? super String> references,
        Collection<? super String> scripts
    ) throws IOException {
        ClassFile jc = new ClassFile(classFile, true);
        final ClassName extraAnn = ClassName.getClassName(ExtraJavaScript.class.getName().replace('.', '/'));
        Annotation a = jc.getAnnotation(extraAnn);
        if (a != null) {
            final ElementValue annVal = a.getComponent("resource").getValue();
            String res = ((PrimitiveElementValue)annVal).getValue().getValue().toString();
            scripts.add(res);
            final AnnotationComponent process = a.getComponent("processByteCode");
            if (process != null && "const=0".equals(process.getValue().toString())) {
                return;
            }
        }
        
        ByteCodeToJavaScript compiler = new ByteCodeToJavaScript(
            jc, out, references
        );
        List<String> toInitilize = new ArrayList<String>();
        for (Method m : jc.getMethods()) {
            if (m.isStatic()) {
                compiler.generateStaticMethod(m, toInitilize);
            } else {
                compiler.generateInstanceMethod(m);
            }
        }
        for (Variable v : jc.getVariables()) {
            if (v.isStatic()) {
                compiler.generateStaticField(v);
            }
        }
        
        final String className = jc.getName().getInternalName().replace('/', '_');
        out.append("\nfunction ").append(className);
        out.append("() {");
        for (Variable v : jc.getVariables()) {
            if (!v.isStatic()) {
                out.append("\n  this." + v.getName() + " = 0;");
            }
        }
        out.append("\n}");
        ClassName sc = jc.getSuperClass();
        if (sc != null) {
            out.append("\n").append(className)
               .append(".prototype = new ").append(sc.getInternalName().replace('/', '_')).append(';');
        }
        for (Method m : jc.getMethods()) {
            if (!m.isStatic() && !m.isPrivate() && !m.getName().contains("<init>")) {
                compiler.generateMethodReference("\n" + className + ".prototype.", m);
            }
        }
        out.append("\n" + className + ".prototype.$instOf_").append(className).append(" = true;");
        for (ClassName superInterface : jc.getInterfaces()) {
            out.append("\n" + className + ".prototype.$instOf_").append(superInterface.getInternalName().replace('/', '_')).append(" = true;");
        }
        for (String init : toInitilize) {
            out.append("\n").append(init).append("();");
        }
    }
    private void generateStaticMethod(Method m, List<String> toInitilize) throws IOException {
        if (javaScriptBody(m, true)) {
            return;
        }
        final String mn = findMethodName(m);
        out.append("\nfunction ").append(
            jc.getName().getInternalName().replace('/', '_')
        ).append('_').append(mn);
        if (mn.equals("classV")) {
            toInitilize.add(jc.getName().getInternalName().replace('/', '_') + '_' + mn);
        }
        out.append('(');
        String space = "";
        List<Parameter> args = m.getParameters();
        for (int index = 0, i = 0; i < args.size(); i++) {
            out.append(space);
            out.append("arg").append(String.valueOf(index));
            space = ",";
            final String desc = findDescriptor(args.get(i).getDescriptor());
            if ("D".equals(desc) || "J".equals(desc)) {
                index += 2;
            } else {
                index++;
            }
        }
        out.append(") {").append("\n");
        final Code code = m.getCode();
        if (code != null) {
            int len = code.getMaxLocals();
            for (int index = args.size(), i = args.size(); i < len; i++) {
                out.append("  var ");
                out.append("arg").append(String.valueOf(i)).append(";\n");
            }
            out.append("  var stack = new Array();\n");
            produceCode(code.getByteCodes());
        } else {
            out.append("  /* no code found for ").append(m.getTypeSignature()).append(" */\n");
        }
        out.append("}");
    }
    
    private void generateMethodReference(String prefix, Method m) throws IOException {
        final String name = findMethodName(m);
        out.append(prefix).append(name).append(" = ")
           .append(jc.getName().getInternalName().replace('/', '_'))
           .append('_').append(name).append(";");
    }
    
    private void generateInstanceMethod(Method m) throws IOException {
        if (javaScriptBody(m, false)) {
            return;
        }
        out.append("\nfunction ").append(
            jc.getName().getInternalName().replace('/', '_')
        ).append('_').append(findMethodName(m));
        out.append("(arg0");
        String space = ",";
        List<Parameter> args = m.getParameters();
        for (int index = 1, i = 0; i < args.size(); i++) {
            out.append(space);
            out.append("arg").append(String.valueOf(index));
            final String desc = findDescriptor(args.get(i).getDescriptor());
            if ("D".equals(desc) || "J".equals(desc)) {
                index += 2;
            } else {
                index++;
            }
        }
        out.append(") {").append("\n");
        final Code code = m.getCode();
        if (code != null) {
            int len = code.getMaxLocals();
            for (int index = args.size(), i = args.size(); i < len; i++) {
                out.append("  var ");
                out.append("arg").append(String.valueOf(i + 1)).append(";\n");
            }
            out.append(";\n  var stack = new Array(");
            out.append(Integer.toString(code.getMaxStack()));
            out.append(");\n");
            produceCode(code.getByteCodes());
        } else {
            out.append("  /* no code found for ").append(m.getTypeSignature()).append(" */\n");
        }
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
                case bc_istore:
                case bc_lstore:
                case bc_fstore:
                case bc_dstore:
                case bc_astore: {
                    final int indx = (byteCodes[++i] + 256) % 256;
                    out.append("arg" + indx).append(" = stack.pop()");
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
                case bc_ineg:
                case bc_lneg:
                case bc_fneg:
                case bc_dneg:
                    out.append("stack.push(- stack.pop());");
                    break;
                case bc_ishl:
                case bc_lshl:
                    out.append("{ var v = stack.pop(); stack.push(stack.pop() << v); }");
                    break;
                case bc_ishr:
                case bc_lshr:
                    out.append("{ var v = stack.pop(); stack.push(stack.pop() >> v); }");
                    break;
                case bc_iushr:
                case bc_lushr:
                    out.append("{ var v = stack.pop(); stack.push(stack.pop() >>> v); }");
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
                case bc_aconst_null:
                    out.append("stack.push(null);");
                    break;
                case bc_iconst_m1:
                    out.append("stack.push(-1);");
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
                case bc_ldc: {
                    int indx = byteCodes[++i];
                    CPEntry entry = jc.getConstantPool().get(indx);
                    String v = encodeConstant(entry);
                    out.append("stack.push(").append(v).append(");");
                    break;
                }
                case bc_ldc_w:
                case bc_ldc2_w: {
                    int indx = readIntArg(byteCodes, i);
                    CPEntry entry = jc.getConstantPool().get(indx);
                    i += 2;
                    String v = encodeConstant(entry);
                    out.append("stack.push(").append(v).append(");");
                    break;
                }
                case bc_lcmp:
                case bc_fcmpl:
                case bc_fcmpg:
                case bc_dcmpl:
                case bc_dcmpg: {
                    out.append("{ var delta = stack.pop() - stack.pop(); stack.push(delta < 0 ?-1 : (delta == 0 ? 0 : 1)); }");
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
                case bc_ifne: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (stack.pop() != 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case bc_iflt: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (stack.pop() < 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case bc_ifle: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (stack.pop() <= 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case bc_ifgt: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (stack.pop() > 0) { gt = " + indx);
                    out.append("; continue; }");
                    i += 2;
                    break;
                }
                case bc_ifge: {
                    int indx = i + readIntArg(byteCodes, i);
                    out.append("if (stack.pop() >= 0) { gt = " + indx);
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
                case bc_invokeinterface: {
                    i = invokeVirtualMethod(byteCodes, i) + 2;
                    break;
                }
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
                    out.append("new ").append(ci.getClassName().getInternalName().replace('/','_'));
                    out.append(");");
                    addReference(ci.getClassName().getInternalName());
                    i += 2;
                    break;
                }
                case bc_newarray: {
                    int type = byteCodes[i++];
                    out.append("stack.push(new Array(stack.pop()));");
                    break;
                }
                case bc_anewarray: {
                    i += 2; // skip type of array
                    out.append("stack.push(new Array(stack.pop()));");
                    break;
                }
                case bc_arraylength:
                    out.append("stack.push(stack.pop().length);");
                    break;
                case bc_iastore:
                case bc_lastore:
                case bc_fastore:
                case bc_dastore:
                case bc_aastore:
                case bc_bastore:
                case bc_castore:
                case bc_sastore: {
                    out.append("{ var value = stack.pop(); var indx = stack.pop(); stack.pop()[indx] = value; }");
                    break;
                }
                case bc_iaload:
                case bc_laload:
                case bc_faload:
                case bc_daload:
                case bc_aaload:
                case bc_baload:
                case bc_caload:
                case bc_saload: {
                    out.append("{ var indx = stack.pop(); stack.push(stack.pop()[indx]); }");
                    break;
                }
                case bc_pop2:
                    out.append("stack.pop();");
                case bc_pop:
                    out.append("stack.pop();");
                    break;
                case bc_dup:
                    out.append("stack.push(stack[stack.length - 1]);");
                    break;
                case bc_bipush:
                    out.append("stack.push(" + byteCodes[++i] + ");");
                    break;
                case bc_sipush:
                    out.append("stack.push(" + readIntArg(byteCodes, i) + ");");
                    i += 2;
                    break;
                case bc_getfield: {
                    int indx = readIntArg(byteCodes, i);
                    CPFieldInfo fi = (CPFieldInfo) jc.getConstantPool().get(indx);
                    out.append("stack.push(stack.pop().").append(fi.getFieldName()).append(");");
                    i += 2;
                    break;
                }
                case bc_getstatic: {
                    int indx = readIntArg(byteCodes, i);
                    CPFieldInfo fi = (CPFieldInfo) jc.getConstantPool().get(indx);
                    final String in = fi.getClassName().getInternalName();
                    out.append("stack.push(").append(in.replace('/', '_'));
                    out.append('_').append(fi.getFieldName()).append(");");
                    i += 2;
                    addReference(in);
                    break;
                }
                case bc_putstatic: {
                    int indx = readIntArg(byteCodes, i);
                    CPFieldInfo fi = (CPFieldInfo) jc.getConstantPool().get(indx);
                    final String in = fi.getClassName().getInternalName();
                    out.append(in.replace('/', '_'));
                    out.append('_').append(fi.getFieldName()).append(" = stack.pop();");
                    i += 2;
                    addReference(in);
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
                case bc_checkcast: {
                    int indx = readIntArg(byteCodes, i);
                    CPClassInfo ci = jc.getConstantPool().getClass(indx);
                    final String type = ci.getClassName().getType();
                    if (!type.startsWith("[")) {
                        // no way to check arrays right now
                        out.append("if(stack[stack.length - 1].$instOf_")
                           .append(type.replace('/', '_'))
                           .append(" != 1) throw {};"); // XXX proper exception
                    }
                    i += 2;
                    break;
                }
                case bc_instanceof: {
                    int indx = readIntArg(byteCodes, i);
                    CPClassInfo ci = jc.getConstantPool().getClass(indx);
                    out.append("stack.push(stack.pop().$instOf_")
                       .append(ci.getClassName().getInternalName().replace('/', '_'))
                       .append(" ? 1 : 0);");
                    i += 2;
                    break;
                }
                    
            }
            out.append(" //");
            for (int j = prev; j <= i; j++) {
                out.append(" ");
                final int cc = (byteCodes[j] + 256) % 256;
                out.append(Integer.toString(cc));
            }
            out.append("\n");
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
                        cnt++;
                        if (array) {
                            sig.append('A');
                        }
                        sig.append(ch);
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
                        cnt++;
                        if (array) {
                            sig.append('A');
                        }
                        sig.append(ch);
                        sig.append(descriptor.substring(i, next).replace('/', '_'));
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
        return cnt;
    }

    private void generateStaticField(Variable v) throws IOException {
        out.append("\nvar ")
           .append(jc.getName().getInternalName().replace('/', '_'))
           .append('_').append(v.getName()).append(" = 0;");
    }

    private String findMethodName(Method m) {
        StringBuilder name = new StringBuilder();
        String descr = m.getDescriptor();
        if ("<init>".equals(m.getName())) { // NOI18N
            name.append("cons"); // NOI18N
        } else if ("<clinit>".equals(m.getName())) { // NOI18N
            name.append("class"); // NOI18N
        } else {
            name.append(m.getName());
        } 
        
        boolean hasReturn[] = { false };
        countArgs(findDescriptor(m.getDescriptor()), hasReturn, name);
        return name.toString();
    }

    private String findMethodName(CPMethodInfo mi, int[] cnt, boolean[] hasReturn) {
        StringBuilder name = new StringBuilder();
        String descr = mi.getDescriptor();
        if ("<init>".equals(mi.getName())) { // NOI18N
            name.append("cons"); // NOI18N
        } else {
            name.append(mi.getName());
        }
        cnt[0] = countArgs(findDescriptor(mi.getDescriptor()), hasReturn, name);
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
        final String in = mi.getClassName().getInternalName();
        out.append(in.replace('/', '_'));
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
        addReference(in);
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
    
    private void addReference(String cn) {
        if (references != null) {
            references.add(cn);
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

    private String encodeConstant(CPEntry entry) {
        final String v;
        if (entry instanceof CPClassInfo) {
            v = "new java_lang_Class";
        } else if (entry instanceof CPStringInfo) {
            v = "\"" + entry.getValue().toString().replace("\"", "\\\"") + "\"";
        } else {
            v = entry.getValue().toString();
        }
        return v;
    }

    private String findDescriptor(String d) {
        return d.replace('[', 'A');
    }

    private boolean javaScriptBody(Method m, boolean isStatic) throws IOException {
        final ClassName extraAnn = ClassName.getClassName(JavaScriptBody.class.getName().replace('.', '/'));
        Annotation a = m.getAnnotation(extraAnn);
        if (a != null) {
            final ElementValue annVal = a.getComponent("body").getValue();
            String body = ((PrimitiveElementValue) annVal).getValue().getValue().toString();
            
            final ArrayElementValue arrVal = (ArrayElementValue) a.getComponent("args").getValue();
            final int len = arrVal.getValues().length;
            String[] names = new String[len];
            for (int i = 0; i < len; i++) {
                names[i] = ((PrimitiveElementValue) arrVal.getValues()[i]).getValue().getValue().toString();
            }
            out.append("\nfunction ").append(
                jc.getName().getInternalName().replace('/', '_')).append('_').append(findMethodName(m));
            out.append("(");
            String space;
            int index;
            if (!isStatic) {                
                out.append(names[0]);
                space = ",";
                index = 1;
            } else {
                space = "";
                index = 0;
            }
            List<Parameter> args = m.getParameters();
            for (int i = 0; i < args.size(); i++) {
                out.append(space);
                out.append(names[index]);
                final String desc = findDescriptor(args.get(i).getDescriptor());
                index++;
            }
            out.append(") {").append("\n");
            out.append(body);
            out.append("\n}\n");
            return true;
        }
        return false;
    }
}
