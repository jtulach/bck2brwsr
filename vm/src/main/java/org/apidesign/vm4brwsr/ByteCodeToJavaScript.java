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
import org.apidesign.javap.StackMapIterator;
import static org.apidesign.javap.RuntimeConstants.*;

/** Translator of the code inside class files to JavaScript.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class ByteCodeToJavaScript {
    private ClassData jc;
    final Appendable out;

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
        String[] proto = findAnnotation(arrData, jc, 
            "org.apidesign.bck2brwsr.core.JavaScriptPrototype", 
            "container", "prototype"
        );
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
        if (proto == null) {
            String sc = jc.getSuperClassName(); // with _
            out.append("\n    var pp = ").
                append(sc.replace('/', '_')).append("(true);");
            out.append("\n    var p = CLS.prototype = pp;");
            out.append("\n    var c = p;");
            out.append("\n    var sprcls = pp.constructor.$class;");
        } else {
            out.append("\n    var p = CLS.prototype = ").append(proto[1]).append(";");
            out.append("\n    var c = ").append(proto[0]).append(";");
            out.append("\n    var sprcls = null;");
        }
        for (MethodData m : jc.getMethods()) {
            byte[] onlyArr = m.findAnnotationData(true);
            String[] only = findAnnotation(onlyArr, jc, 
                "org.apidesign.bck2brwsr.core.JavaScriptOnly", 
                "name", "value"
            );
            if (only != null) {
                if (only[0] != null && only[1] != null) {
                    out.append("\n    p.").append(only[0]).append(" = ")
                        .append(only[1]).append(";");
                }
                continue;
            }
            String mn;
            if (m.isStatic()) {
                mn = generateStaticMethod("\n    c.", m, toInitilize);
            } else {
                mn = generateInstanceMethod("\n    c.", m);
            }
            byte[] runAnno = m.findAnnotationData(false);
            if (runAnno != null) {
                out.append("\n    c.").append(mn).append(".anno = {");
                generateAnno(jc, out, runAnno);
                out.append("\n    };");
            }
        }
        out.append("\n    c.constructor = CLS;");
        out.append("\n    c.$instOf_").append(className).append(" = true;");
        for (String superInterface : jc.getSuperInterfaces()) {
            out.append("\n    c.$instOf_").append(superInterface.replace('/', '_')).append(" = true;");
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
            byte[] onlyArr = v.findAnnotationData(true);
            String[] only = findAnnotation(onlyArr, jc, 
                "org.apidesign.bck2brwsr.core.JavaScriptOnly", 
                "name", "value"
            );
            if (only != null) {
                if (only[0] != null && only[1] != null) {
                    out.append("\n    p.").append(only[0]).append(" = ")
                        .append(only[1]).append(";");
                }
                continue;
            }
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
    private String generateStaticMethod(String prefix, MethodData m, StringArray toInitilize) throws IOException {
        String jsb = javaScriptBody(prefix, m, true);
        if (jsb != null) {
            return jsb;
        }
        StringBuilder argsCnt = new StringBuilder();
        final String mn = findMethodName(m, argsCnt);
        out.append(prefix).append(mn).append(" = function");
        if (mn.equals("class__V")) {
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
        if (m.getCode() != null) {
            int len = m.getMaxLocals();
            for (int i = argsCnt.length(); i < len; i++) {
                out.append("  var ");
                out.append("arg").append(String.valueOf(i)).append(";\n");
            }
            produceCode(m);
        } else {
            out.append("  throw 'no code found for ").append(m.getInternalSig()).append("';\n");
        }
        out.append("};");
        return mn;
    }
    
    private String generateInstanceMethod(String prefix, MethodData m) throws IOException {
        String jsb = javaScriptBody(prefix, m, false);
        if (jsb != null) {
            return jsb;
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
        if (m.getCode() != null) {
            int len = m.getMaxLocals();
            for (int i = argsCnt.length(); i < len; i++) {
                out.append("  var ");
                out.append("arg").append(String.valueOf(i + 1)).append(";\n");
            }
            produceCode(m);
        } else {
            out.append("  throw 'no code found for ").append(m.getInternalSig()).append("';\n");
        }
        out.append("};");
        return mn;
    }

    private void produceCode(MethodData m) throws IOException {
        final byte[] byteCodes = m.getCode();
        final StackMapIterator stackMapIterator = m.createStackMapIterator();
        final StackToVariableMapper mapper = new StackToVariableMapper();

        // maxStack includes two stack positions for every pushed long / double
        // so this might generate more stack variables than we need
        final int maxStack = m.getMaxStack();
        if (maxStack > 0) {
            // TODO: generate only used stack variables
            for (int j = 0; j <= Variable.LAST_TYPE; ++j) {
                out.append("\n  var ").append(Variable.getStackVariable(j, 0));
                for (int i = 1; i < maxStack; ++i) {
                    out.append(", ");
                    out.append(Variable.getStackVariable(j, i));
                }
                out.append(';');
            }
        }

        int lastStackFrame = -1;

        out.append("\n  var gt = 0;\n  for(;;) switch(gt) {\n");
        for (int i = 0; i < byteCodes.length; i++) {
            int prev = i;
            stackMapIterator.advanceTo(i);
            if (lastStackFrame != stackMapIterator.getFrameIndex()) {
                lastStackFrame = stackMapIterator.getFrameIndex();
                mapper.syncWithFrameStack(stackMapIterator.getFrameStack());
                out.append("    case " + i).append(": ");
            } else {
                out.append("    /* " + i).append(" */ ");
            }
            final int c = readByte(byteCodes, i);
            switch (c) {
                case opc_aload_0:
                    emit(out, "@1 = arg0;", mapper.pushA());
                    break;
                case opc_iload_0:
                    emit(out, "@1 = arg0;", mapper.pushI());
                    break;
                case opc_lload_0:
                    emit(out, "@1 = arg0;", mapper.pushL());
                    break;
                case opc_fload_0:
                    emit(out, "@1 = arg0;", mapper.pushF());
                    break;
                case opc_dload_0:
                    emit(out, "@1 = arg0;", mapper.pushD());
                    break;
                case opc_aload_1:
                    emit(out, "@1 = arg1;", mapper.pushA());
                    break;
                case opc_iload_1:
                    emit(out, "@1 = arg1;", mapper.pushI());
                    break;
                case opc_lload_1:
                    emit(out, "@1 = arg1;", mapper.pushL());
                    break;
                case opc_fload_1:
                    emit(out, "@1 = arg1;", mapper.pushF());
                    break;
                case opc_dload_1:
                    emit(out, "@1 = arg1;", mapper.pushD());
                    break;
                case opc_aload_2:
                    emit(out, "@1 = arg2;", mapper.pushA());
                    break;
                case opc_iload_2:
                    emit(out, "@1 = arg2;", mapper.pushI());
                    break;
                case opc_lload_2:
                    emit(out, "@1 = arg2;", mapper.pushL());
                    break;
                case opc_fload_2:
                    emit(out, "@1 = arg2;", mapper.pushF());
                    break;
                case opc_dload_2:
                    emit(out, "@1 = arg2;", mapper.pushD());
                    break;
                case opc_aload_3:
                    emit(out, "@1 = arg3;", mapper.pushA());
                    break;
                case opc_iload_3:
                    emit(out, "@1 = arg3;", mapper.pushI());
                    break;
                case opc_lload_3:
                    emit(out, "@1 = arg3;", mapper.pushL());
                    break;
                case opc_fload_3:
                    emit(out, "@1 = arg3;", mapper.pushF());
                    break;
                case opc_dload_3:
                    emit(out, "@1 = arg3;", mapper.pushD());
                    break;
                case opc_iload: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "@1 = arg@2;",
                         mapper.pushI(), Integer.toString(indx));
                    break;
                }
                case opc_lload: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "@1 = arg@2;",
                         mapper.pushL(), Integer.toString(indx));
                    break;
                }
                case opc_fload: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "@1 = arg@2;",
                         mapper.pushF(), Integer.toString(indx));
                    break;
                }
                case opc_dload: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "@1 = arg@2;",
                         mapper.pushD(), Integer.toString(indx));
                    break;
                }
                case opc_aload: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "@1 = arg@2;",
                         mapper.pushA(), Integer.toString(indx));
                    break;
                }
                case opc_istore: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "arg@1 = @2;",
                         Integer.toString(indx), mapper.popI());
                    break;
                }
                case opc_lstore: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "arg@1 = @2;",
                         Integer.toString(indx), mapper.popL());
                    break;
                }
                case opc_fstore: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "arg@1 = @2;",
                         Integer.toString(indx), mapper.popF());
                    break;
                }
                case opc_dstore: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "arg@1 = @2;",
                         Integer.toString(indx), mapper.popD());
                    break;
                }
                case opc_astore: {
                    final int indx = readByte(byteCodes, ++i);
                    emit(out, "arg@1 = @2;",
                         Integer.toString(indx), mapper.popA());
                    break;
                }
                case opc_astore_0:
                    emit(out, "arg0 = @1;", mapper.popA());
                    break;
                case opc_istore_0:
                    emit(out, "arg0 = @1;", mapper.popI());
                    break;
                case opc_lstore_0:
                    emit(out, "arg0 = @1;", mapper.popL());
                    break;
                case opc_fstore_0:
                    emit(out, "arg0 = @1;", mapper.popF());
                    break;
                case opc_dstore_0:
                    emit(out, "arg0 = @1;", mapper.popD());
                    break;
                case opc_astore_1:
                    emit(out, "arg1 = @1;", mapper.popA());
                    break;
                case opc_istore_1:
                    emit(out, "arg1 = @1;", mapper.popI());
                    break;
                case opc_lstore_1:
                    emit(out, "arg1 = @1;", mapper.popL());
                    break;
                case opc_fstore_1:
                    emit(out, "arg1 = @1;", mapper.popF());
                    break;
                case opc_dstore_1:
                    emit(out, "arg1 = @1;", mapper.popD());
                    break;
                case opc_astore_2:
                    emit(out, "arg2 = @1;", mapper.popA());
                    break;
                case opc_istore_2:
                    emit(out, "arg2 = @1;", mapper.popI());
                    break;
                case opc_lstore_2:
                    emit(out, "arg2 = @1;", mapper.popL());
                    break;
                case opc_fstore_2:
                    emit(out, "arg2 = @1;", mapper.popF());
                    break;
                case opc_dstore_2:
                    emit(out, "arg2 = @1;", mapper.popD());
                    break;
                case opc_astore_3:
                    emit(out, "arg3 = @1;", mapper.popA());
                    break;
                case opc_istore_3:
                    emit(out, "arg3 = @1;", mapper.popI());
                    break;
                case opc_lstore_3:
                    emit(out, "arg3 = @1;", mapper.popL());
                    break;
                case opc_fstore_3:
                    emit(out, "arg3 = @1;", mapper.popF());
                    break;
                case opc_dstore_3:
                    emit(out, "arg3 = @1;", mapper.popD());
                    break;
                case opc_iadd:
                    emit(out, "@1 += @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_ladd:
                    emit(out, "@1 += @2;", mapper.getL(1), mapper.popL());
                    break;
                case opc_fadd:
                    emit(out, "@1 += @2;", mapper.getF(1), mapper.popF());
                    break;
                case opc_dadd:
                    emit(out, "@1 += @2;", mapper.getD(1), mapper.popD());
                    break;
                case opc_isub:
                    emit(out, "@1 -= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_lsub:
                    emit(out, "@1 -= @2;", mapper.getL(1), mapper.popL());
                    break;
                case opc_fsub:
                    emit(out, "@1 -= @2;", mapper.getF(1), mapper.popF());
                    break;
                case opc_dsub:
                    emit(out, "@1 -= @2;", mapper.getD(1), mapper.popD());
                    break;
                case opc_imul:
                    emit(out, "@1 *= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_lmul:
                    emit(out, "@1 *= @2;", mapper.getL(1), mapper.popL());
                    break;
                case opc_fmul:
                    emit(out, "@1 *= @2;", mapper.getF(1), mapper.popF());
                    break;
                case opc_dmul:
                    emit(out, "@1 *= @2;", mapper.getD(1), mapper.popD());
                    break;
                case opc_idiv:
                    emit(out, "@1 = Math.floor(@1 / @2);",
                         mapper.getI(1), mapper.popI());
                    break;
                case opc_ldiv:
                    emit(out, "@1 = Math.floor(@1 / @2);",
                         mapper.getL(1), mapper.popL());
                    break;
                case opc_fdiv:
                    emit(out, "@1 /= @2;", mapper.getF(1), mapper.popF());
                    break;
                case opc_ddiv:
                    emit(out, "@1 /= @2;", mapper.getD(1), mapper.popD());
                    break;
                case opc_irem:
                    emit(out, "@1 %= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_lrem:
                    emit(out, "@1 %= @2;", mapper.getL(1), mapper.popL());
                    break;
                case opc_frem:
                    emit(out, "@1 %= @2;", mapper.getF(1), mapper.popF());
                    break;
                case opc_drem:
                    emit(out, "@1 %= @2;", mapper.getD(1), mapper.popD());
                    break;
                case opc_iand:
                    emit(out, "@1 &= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_land:
                    emit(out, "@1 &= @2;", mapper.getL(1), mapper.popL());
                    break;
                case opc_ior:
                    emit(out, "@1 |= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_lor:
                    emit(out, "@1 |= @2;", mapper.getL(1), mapper.popL());
                    break;
                case opc_ixor:
                    emit(out, "@1 ^= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_lxor:
                    emit(out, "@1 ^= @2;", mapper.getL(1), mapper.popL());
                    break;
                case opc_ineg:
                    emit(out, "@1 = -@1;", mapper.getI(0));
                    break;
                case opc_lneg:
                    emit(out, "@1 = -@1;", mapper.getL(0));
                    break;
                case opc_fneg:
                    emit(out, "@1 = -@1;", mapper.getF(0));
                    break;
                case opc_dneg:
                    emit(out, "@1 = -@1;", mapper.getD(0));
                    break;
                case opc_ishl:
                    emit(out, "@1 <<= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_lshl:
                    emit(out, "@1 <<= @2;", mapper.getL(1), mapper.popI());
                    break;
                case opc_ishr:
                    emit(out, "@1 >>= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_lshr:
                    emit(out, "@1 >>= @2;", mapper.getL(1), mapper.popI());
                    break;
                case opc_iushr:
                    emit(out, "@1 >>>= @2;", mapper.getI(1), mapper.popI());
                    break;
                case opc_lushr:
                    emit(out, "@1 >>>= @2;", mapper.getL(1), mapper.popI());
                    break;
                case opc_iinc: {
                    final int varIndx = readByte(byteCodes, ++i);
                    final int incrBy = byteCodes[++i];
                    if (incrBy == 1) {
                        emit(out, "arg@1++;", Integer.toString(varIndx));
                    } else {
                        emit(out, "arg@1 += @2;",
                             Integer.toString(varIndx),
                             Integer.toString(incrBy));
                    }
                    break;
                }
                case opc_return:
                    emit(out, "return;");
                    break;
                case opc_ireturn:
                    emit(out, "return @1;", mapper.popI());
                    break;
                case opc_lreturn:
                    emit(out, "return @1;", mapper.popL());
                    break;
                case opc_freturn:
                    emit(out, "return @1;", mapper.popF());
                    break;
                case opc_dreturn:
                    emit(out, "return @1;", mapper.popD());
                    break;
                case opc_areturn:
                    emit(out, "return @1;", mapper.popA());
                    break;
                case opc_i2l:
                    emit(out, "@2 = @1;", mapper.popI(), mapper.pushL());
                    break;
                case opc_i2f:
                    emit(out, "@2 = @1;", mapper.popI(), mapper.pushF());
                    break;
                case opc_i2d:
                    emit(out, "@2 = @1;", mapper.popI(), mapper.pushD());
                    break;
                case opc_l2i:
                    emit(out, "@2 = @1;", mapper.popL(), mapper.pushI());
                    break;
                    // max int check?
                case opc_l2f:
                    emit(out, "@2 = @1;", mapper.popL(), mapper.pushF());
                    break;
                case opc_l2d:
                    emit(out, "@2 = @1;", mapper.popL(), mapper.pushD());
                    break;
                case opc_f2d:
                    emit(out, "@2 = @1;", mapper.popF(), mapper.pushD());
                    break;
                case opc_d2f:
                    emit(out, "@2 = @1;", mapper.popD(), mapper.pushF());
                    break;
                case opc_f2i:
                    emit(out, "@2 = Math.floor(@1);",
                         mapper.popF(), mapper.pushI());
                    break;
                case opc_f2l:
                    emit(out, "@2 = Math.floor(@1);",
                         mapper.popF(), mapper.pushL());
                    break;
                case opc_d2i:
                    emit(out, "@2 = Math.floor(@1);",
                         mapper.popD(), mapper.pushI());
                    break;
                case opc_d2l:
                    emit(out, "@2 = Math.floor(@1);",
                         mapper.popD(), mapper.pushL());
                    break;
                case opc_i2b:
                case opc_i2c:
                case opc_i2s:
                    out.append("/* number conversion */");
                    break;
                case opc_aconst_null:
                    emit(out, "@1 = null;", mapper.pushA());
                    break;
                case opc_iconst_m1:
                    emit(out, "@1 = -1;", mapper.pushI());
                    break;
                case opc_iconst_0:
                    emit(out, "@1 = 0;", mapper.pushI());
                    break;
                case opc_dconst_0:
                    emit(out, "@1 = 0;", mapper.pushD());
                    break;
                case opc_lconst_0:
                    emit(out, "@1 = 0;", mapper.pushL());
                    break;
                case opc_fconst_0:
                    emit(out, "@1 = 0;", mapper.pushF());
                    break;
                case opc_iconst_1:
                    emit(out, "@1 = 1;", mapper.pushI());
                    break;
                case opc_lconst_1:
                    emit(out, "@1 = 1;", mapper.pushL());
                    break;
                case opc_fconst_1:
                    emit(out, "@1 = 1;", mapper.pushF());
                    break;
                case opc_dconst_1:
                    emit(out, "@1 = 1;", mapper.pushD());
                    break;
                case opc_iconst_2:
                    emit(out, "@1 = 2;", mapper.pushI());
                    break;
                case opc_fconst_2:
                    emit(out, "@1 = 2;", mapper.pushF());
                    break;
                case opc_iconst_3:
                    emit(out, "@1 = 3;", mapper.pushI());
                    break;
                case opc_iconst_4:
                    emit(out, "@1 = 4;", mapper.pushI());
                    break;
                case opc_iconst_5:
                    emit(out, "@1 = 5;", mapper.pushI());
                    break;
                case opc_ldc: {
                    int indx = readByte(byteCodes, ++i);
                    String v = encodeConstant(indx);
                    int type = constantToVariableType(jc.getTag(indx));
                    emit(out, "@1 = @2;", mapper.pushT(type), v);
                    break;
                }
                case opc_ldc_w:
                case opc_ldc2_w: {
                    int indx = readIntArg(byteCodes, i);
                    i += 2;
                    String v = encodeConstant(indx);
                    int type = constantToVariableType(jc.getTag(indx));
                    emit(out, "@1 = @2;", mapper.pushT(type), v);
                    break;
                }
                case opc_lcmp:
                    emit(out, "@3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);",
                         mapper.popL(), mapper.popL(), mapper.pushI());
                    break;
                case opc_fcmpl:
                case opc_fcmpg:
                    emit(out, "@3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);",
                         mapper.popF(), mapper.popF(), mapper.pushI());
                    break;
                case opc_dcmpl:
                case opc_dcmpg:
                    emit(out, "@3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);",
                         mapper.popD(), mapper.popD(), mapper.pushI());
                    break;
                case opc_if_acmpeq:
                    i = generateIf(byteCodes, i, mapper.popA(), mapper.popA(),
                                   "===");
                    break;
                case opc_if_acmpne:
                    i = generateIf(byteCodes, i, mapper.popA(), mapper.popA(),
                                   "!=");
                    break;
                case opc_if_icmpeq:
                    i = generateIf(byteCodes, i, mapper.popI(), mapper.popI(),
                                   "==");
                    break;
                case opc_ifeq: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 == 0) { gt = @2; continue; }",
                         mapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifne: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 != 0) { gt = @2; continue; }",
                         mapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_iflt: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 < 0) { gt = @2; continue; }",
                         mapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifle: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 <= 0) { gt = @2; continue; }",
                         mapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifgt: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 > 0) { gt = @2; continue; }",
                         mapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifge: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 >= 0) { gt = @2; continue; }",
                         mapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifnonnull: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 !== null) { gt = @2; continue; }",
                         mapper.popA(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifnull: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 === null) { gt = @2; continue; }",
                         mapper.popA(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_if_icmpne:
                    i = generateIf(byteCodes, i, mapper.popI(), mapper.popI(),
                                   "!=");
                    break;
                case opc_if_icmplt:
                    i = generateIf(byteCodes, i, mapper.popI(), mapper.popI(),
                                   "<");
                    break;
                case opc_if_icmple:
                    i = generateIf(byteCodes, i, mapper.popI(), mapper.popI(),
                                   "<=");
                    break;
                case opc_if_icmpgt:
                    i = generateIf(byteCodes, i, mapper.popI(), mapper.popI(),
                                   ">");
                    break;
                case opc_if_icmpge:
                    i = generateIf(byteCodes, i, mapper.popI(), mapper.popI(),
                                   ">=");
                    break;
                case opc_goto: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "gt = @1; continue;", Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_lookupswitch: {
                    int table = i / 4 * 4 + 4;
                    int dflt = i + readInt4(byteCodes, table);
                    table += 4;
                    int n = readInt4(byteCodes, table);
                    table += 4;
                    out.append("switch (").append(mapper.popI()).append(") {\n");
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
                    out.append("switch (").append(mapper.popI()).append(") {\n");
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
                    i = invokeVirtualMethod(byteCodes, i, mapper) + 2;
                    break;
                }
                case opc_invokevirtual:
                    i = invokeVirtualMethod(byteCodes, i, mapper);
                    break;
                case opc_invokespecial:
                    i = invokeStaticMethod(byteCodes, i, mapper, false);
                    break;
                case opc_invokestatic:
                    i = invokeStaticMethod(byteCodes, i, mapper, true);
                    break;
                case opc_new: {
                    int indx = readIntArg(byteCodes, i);
                    String ci = jc.getClassName(indx);
                    emit(out, "@1 = new @2;",
                         mapper.pushA(), ci.replace('/', '_'));
                    addReference(ci);
                    i += 2;
                    break;
                }
                case opc_newarray:
                    ++i; // skip type of array
                    emit(out, "@2 = new Array(@1).fillNulls();",
                         mapper.popI(), mapper.pushA());
                    break;
                case opc_anewarray:
                    i += 2; // skip type of array
                    emit(out, "@2 = new Array(@1).fillNulls();",
                         mapper.popI(), mapper.pushA());
                    break;
                case opc_multianewarray: {
                    i += 2;
                    int dim = readByte(byteCodes, ++i);
                    out.append("{ var a0 = new Array(").append(mapper.popI())
                       .append(").fillNulls();");
                    for (int d = 1; d < dim; d++) {
                        out.append("\n  var l" + d).append(" = ")
                           .append(mapper.popI()).append(';');
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
                    out.append("\n").append(mapper.pushA()).append(" = a0; }");
                    break;
                }
                case opc_arraylength:
                    emit(out, "@2 = @1.length;", mapper.popA(), mapper.pushI());
                    break;
                case opc_lastore:
                    emit(out, "@3[@2] = @1;",
                         mapper.popL(), mapper.popI(), mapper.popA());
                    break;
                case opc_fastore:
                    emit(out, "@3[@2] = @1;",
                         mapper.popF(), mapper.popI(), mapper.popA());
                    break;
                case opc_dastore:
                    emit(out, "@3[@2] = @1;",
                         mapper.popD(), mapper.popI(), mapper.popA());
                    break;
                case opc_aastore:
                    emit(out, "@3[@2] = @1;",
                         mapper.popA(), mapper.popI(), mapper.popA());
                    break;
                case opc_iastore:
                case opc_bastore:
                case opc_castore:
                case opc_sastore:
                    emit(out, "@3[@2] = @1;",
                         mapper.popI(), mapper.popI(), mapper.popA());
                    break;
                case opc_laload:
                    emit(out, "@3 = @2[@1];",
                         mapper.popI(), mapper.popA(), mapper.pushL());
                    break;
                case opc_faload:
                    emit(out, "@3 = @2[@1];",
                         mapper.popI(), mapper.popA(), mapper.pushF());
                    break;
                case opc_daload:
                    emit(out, "@3 = @2[@1];",
                         mapper.popI(), mapper.popA(), mapper.pushD());
                    break;
                case opc_aaload:
                    emit(out, "@3 = @2[@1];",
                         mapper.popI(), mapper.popA(), mapper.pushA());
                    break;
                case opc_iaload:
                case opc_baload:
                case opc_caload:
                case opc_saload:
                    emit(out, "@3 = @2[@1];",
                         mapper.popI(), mapper.popA(), mapper.pushI());
                    break;
                case opc_pop:
                case opc_pop2:
                    mapper.pop(1);
                    out.append("/* pop */");
                    break;
                case opc_dup: {
                    final Variable v = mapper.get(0);
                    emit(out, "@1 = @2;", mapper.pushT(v.getType()), v);
                    break;
                }
                case opc_dup2: {
                    if (mapper.get(0).isCategory2()) {
                        final Variable v = mapper.get(0);
                        emit(out, "@1 = @2;", mapper.pushT(v.getType()), v);
                    } else {
                        final Variable v1 = mapper.get(0);
                        final Variable v2 = mapper.get(1);
                        emit(out, "{ @1 = @2; @3 = @4; }",
                             mapper.pushT(v2.getType()), v2,
                             mapper.pushT(v1.getType()), v1);
                    }
                    break;
                }
                case opc_dup_x1: {
                    final Variable vi1 = mapper.pop();
                    final Variable vi2 = mapper.pop();
                    final Variable vo3 = mapper.pushT(vi1.getType());
                    final Variable vo2 = mapper.pushT(vi2.getType());
                    final Variable vo1 = mapper.pushT(vi1.getType());

                    emit(out, "{ @1 = @2; @3 = @4; @5 = @6; }",
                         vo1, vi1, vo2, vi2, vo3, vo1);
                    break;
                }
                case opc_dup_x2: {
                    if (mapper.get(1).isCategory2()) {
                        final Variable vi1 = mapper.pop();
                        final Variable vi2 = mapper.pop();
                        final Variable vo3 = mapper.pushT(vi1.getType());
                        final Variable vo2 = mapper.pushT(vi2.getType());
                        final Variable vo1 = mapper.pushT(vi1.getType());

                        emit(out, "{ @1 = @2; @3 = @4; @5 = @6; }",
                             vo1, vi1, vo2, vi2, vo3, vo1);
                    } else {
                        final Variable vi1 = mapper.pop();
                        final Variable vi2 = mapper.pop();
                        final Variable vi3 = mapper.pop();
                        final Variable vo4 = mapper.pushT(vi1.getType());
                        final Variable vo3 = mapper.pushT(vi3.getType());
                        final Variable vo2 = mapper.pushT(vi2.getType());
                        final Variable vo1 = mapper.pushT(vi1.getType());

                        emit(out, "{ @1 = @2; @3 = @4; @5 = @6; @7 = @8; }",
                             vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                    }
                    break;
                }
                case opc_bipush:
                    emit(out, "@1 = @2;",
                         mapper.pushI(), Integer.toString(byteCodes[++i]));
                    break;
                case opc_sipush:
                    emit(out, "@1 = @2;",
                         mapper.pushI(),
                         Integer.toString(readIntArg(byteCodes, i)));
                    i += 2;
                    break;
                case opc_getfield: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = fieldToVariableType(fi[2].charAt(0));
                    emit(out, "@2 = @1.fld_@3;",
                         mapper.popA(), mapper.pushT(type), fi[1]);
                    i += 2;
                    break;
                }
                case opc_getstatic: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = fieldToVariableType(fi[2].charAt(0));
                    emit(out, "@1 = @2.@3;",
                         mapper.pushT(type), fi[0].replace('/', '_'), fi[1]);
                    i += 2;
                    addReference(fi[0]);
                    break;
                }
                case opc_putfield: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = fieldToVariableType(fi[2].charAt(0));
                    emit(out, "@2.fld_@3 = @1;",
                         mapper.popT(type), mapper.popA(), fi[1]);
                    i += 2;
                    break;
                }
                case opc_putstatic: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = fieldToVariableType(fi[2].charAt(0));
                    emit(out, "@1.@2 = @3;",
                         fi[0].replace('/', '_'), fi[1], mapper.popT(type));
                    i += 2;
                    addReference(fi[0]);
                    break;
                }
                case opc_checkcast: {
                    int indx = readIntArg(byteCodes, i);
                    final String type = jc.getClassName(indx);
                    if (!type.startsWith("[")) {
                        // no way to check arrays right now
                        // XXX proper exception
                        emit(out, "if (@1.$instOf_@2 != 1) throw {};",
                             mapper.getA(0), type.replace('/', '_'));
                    }
                    i += 2;
                    break;
                }
                case opc_instanceof: {
                    int indx = readIntArg(byteCodes, i);
                    final String type = jc.getClassName(indx);
                    emit(out, "@2 = @1.$instOf_@3 ? 1 : 0;",
                         mapper.popA(), mapper.pushI(), type.replace('/', '_'));
                    i += 2;
                    break;
                }
                case opc_athrow: {
                    final Variable v = mapper.popA();
                    mapper.clear();

                    emit(out, "{ @1 = @2; throw @2; }",
                         mapper.pushA(), v);
                    break;
                }

                case opc_monitorenter: {
                    out.append("/* monitor enter */");
                    mapper.popA();
                    break;
                }

                case opc_monitorexit: {
                    out.append("/* monitor exit */");
                    mapper.popA();
                    break;
                }

                default: {
                    emit(out, "throw 'unknown bytecode @1';",
                         Integer.toString(c));
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

    private int generateIf(byte[] byteCodes, int i,
                           final Variable v2, final Variable v1,
                           final String test) throws IOException {
        int indx = i + readIntArg(byteCodes, i);
        out.append("if (").append(v1)
           .append(' ').append(test).append(' ')
           .append(v2).append(") { gt = " + indx)
           .append("; continue; }");
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
        return byteCodes[offsetInstruction] & 0xff;
    }
    
    private static void countArgs(String descriptor, char[] returnType, StringBuilder sig, StringBuilder cnt) {
        int i = 0;
        Boolean count = null;
        boolean array = false;
        sig.append("__");
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
                        if (array) {
                            sig.append("_3");
                        }
                        sig.append(ch);
                        if (ch == 'J' || ch == 'D') {
                            cnt.append('1');
                        } else {
                            cnt.append('0');
                        }
                    } else {
                        sig.insert(firstPos, ch);
                        if (array) {
                            returnType[0] = '[';
                            sig.insert(firstPos, "_3");
                        } else {
                            returnType[0] = ch;
                        }
                    }
                    array = false;
                    continue;
                case 'V': 
                    assert !count;
                    returnType[0] = 'V';
                    sig.insert(firstPos, 'V');
                    continue;
                case 'L':
                    int next = descriptor.indexOf(';', i);
                    String realSig = mangleSig(descriptor, i - 1, next + 1);
                    if (count) {
                        if (array) {
                            sig.append("_3");
                        }
                        sig.append(realSig);
                        cnt.append('0');
                    } else {
                        sig.insert(firstPos, realSig);
                        if (array) {
                            sig.insert(firstPos, "_3");
                        }
                        returnType[0] = 'L';
                    }
                    i = next + 1;
                    continue;
                case '[':
                    array = true;
                    continue;
                default:
                    throw new IllegalStateException("Invalid char: " + ch);
            }
        }
    }
    
    private static String mangleSig(String txt, int first, int last) {
        StringBuilder sb = new StringBuilder();
        for (int i = first; i < last; i++) {
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

    private static String findMethodName(MethodData m, StringBuilder cnt) {
        StringBuilder name = new StringBuilder();
        if ("<init>".equals(m.getName())) { // NOI18N
            name.append("cons"); // NOI18N
        } else if ("<clinit>".equals(m.getName())) { // NOI18N
            name.append("class"); // NOI18N
        } else {
            name.append(m.getName());
        } 
        
        countArgs(m.getInternalSig(), new char[1], name, cnt);
        return name.toString();
    }

    static String findMethodName(String[] mi, StringBuilder cnt, char[] returnType) {
        StringBuilder name = new StringBuilder();
        String descr = mi[2];//mi.getDescriptor();
        String nm= mi[1];
        if ("<init>".equals(nm)) { // NOI18N
            name.append("cons"); // NOI18N
        } else {
            name.append(nm);
        }
        countArgs(descr, returnType, name, cnt);
        return name.toString();
    }

    private int invokeStaticMethod(byte[] byteCodes, int i, final StackToVariableMapper mapper, boolean isStatic)
    throws IOException {
        int methodIndex = readIntArg(byteCodes, i);
        String[] mi = jc.getFieldInfoName(methodIndex);
        char[] returnType = { 'V' };
        StringBuilder cnt = new StringBuilder();
        String mn = findMethodName(mi, cnt, returnType);

        final int numArguments = isStatic ? cnt.length() : cnt.length() + 1;
        final Variable[] vars = new Variable[numArguments];

        for (int j = numArguments - 1; j >= 0; --j) {
            vars[j] = mapper.pop();
        }

        if (returnType[0] != 'V') {
            out.append(mapper.pushT(fieldToVariableType(returnType[0])))
               .append(" = ");
        }

        final String in = mi[0];
        out.append(in.replace('/', '_'));
        out.append("(false).");
        out.append(mn);
        out.append('(');
        if (numArguments > 0) {
            out.append(vars[0]);
            for (int j = 1; j < numArguments; ++j) {
                out.append(", ");
                out.append(vars[j]);
            }
        }
        out.append(");");
        i += 2;
        addReference(in);
        return i;
    }
    private int invokeVirtualMethod(byte[] byteCodes, int i, final StackToVariableMapper mapper)
    throws IOException {
        int methodIndex = readIntArg(byteCodes, i);
        String[] mi = jc.getFieldInfoName(methodIndex);
        char[] returnType = { 'V' };
        StringBuilder cnt = new StringBuilder();
        String mn = findMethodName(mi, cnt, returnType);

        final int numArguments = cnt.length() + 1;
        final Variable[] vars = new Variable[numArguments];

        for (int j = numArguments - 1; j >= 0; --j) {
            vars[j] = mapper.pop();
        }

        if (returnType[0] != 'V') {
            out.append(mapper.pushT(fieldToVariableType(returnType[0])))
               .append(" = ");
        }

        out.append(vars[0]).append('.');
        out.append(mn);
        out.append('(');
        out.append(vars[0]);
        for (int j = 1; j < numArguments; ++j) {
            out.append(", ");
            out.append(vars[j]);
        }
        out.append(");");
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

    private String javaScriptBody(String prefix, MethodData m, boolean isStatic) throws IOException {
        byte[] arr = m.findAnnotationData(true);
        if (arr == null) {
            return null;
        }
        final String jvmType = "Lorg/apidesign/bck2brwsr/core/JavaScriptBody;";
        class P extends AnnotationParser {
            public P() {
                super(false);
            }
            
            int cnt;
            String[] args = new String[30];
            String body;
            
            @Override
            protected void visitAttr(String type, String attr, String at, String value) {
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
            return null;
        }
        StringBuilder cnt = new StringBuilder();
        final String mn = findMethodName(m, cnt);
        out.append(prefix).append(mn);
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
        return mn;
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
        AnnotationParser ap = new AnnotationParser(false) {
            @Override
            protected void visitAttr(String type, String attr, String at, String value) {
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
        AnnotationParser ap = new AnnotationParser(true) {
            int anno;
            int cnt;
            
            @Override
            protected void visitAnnotationStart(String type) throws IOException {
                if (anno++ > 0) {
                    out.append(",");
                }
                out.append('"').append(type).append("\" : {\n");
                cnt = 0;
            }

            @Override
            protected void visitAnnotationEnd(String type) throws IOException {
                out.append("\n}\n");
            }
            
            @Override
            protected void visitAttr(String type, String attr, String attrType, String value) 
            throws IOException {
                if (attr == null) {
                    return;
                }
                if (cnt++ > 0) {
                    out.append(",\n");
                }
                out.append(attr).append("__").append(attrType).append(" : ").append(value);
            }
        };
        ap.parse(data, cd);
    }

    private static int constantToVariableType(final byte constantTag) {
        switch (constantTag) {
            case CONSTANT_INTEGER:
                return Variable.TYPE_INT;
            case CONSTANT_FLOAT:
                return Variable.TYPE_FLOAT;
            case CONSTANT_LONG:
                return Variable.TYPE_LONG;
            case CONSTANT_DOUBLE:
                return Variable.TYPE_DOUBLE;

            case CONSTANT_CLASS:
            case CONSTANT_UTF8:
            case CONSTANT_UNICODE:
            case CONSTANT_STRING:
                return Variable.TYPE_REF;

            case CONSTANT_FIELD:
            case CONSTANT_METHOD:
            case CONSTANT_INTERFACEMETHOD:
            case CONSTANT_NAMEANDTYPE:
                /* unclear how to handle for now */
            default:
                throw new IllegalStateException("Unhandled constant tag");
        }
    }

    private static int fieldToVariableType(final char fieldType) {
        switch (fieldType) {
            case 'B':
            case 'C':
            case 'S':
            case 'Z':
            case 'I':
                return Variable.TYPE_INT;
            case 'J':
                return Variable.TYPE_LONG;
            case 'F':
                return Variable.TYPE_FLOAT;
            case 'D':
                return Variable.TYPE_DOUBLE;
            case 'L':
            case '[':
                return Variable.TYPE_REF;

            default:
                throw new IllegalStateException("Unhandled field type");
        }
    }

    private static void emit(final Appendable out,
                             final String format,
                             final CharSequence... params) throws IOException {
        final int length = format.length();

        int processed = 0;
        int paramOffset = format.indexOf('@');
        while ((paramOffset != -1) && (paramOffset < (length - 1))) {
            final char paramChar = format.charAt(paramOffset + 1);
            if ((paramChar >= '1') && (paramChar <= '9')) {
                final int paramIndex = paramChar - '0' - 1;

                out.append(format, processed, paramOffset);
                out.append(params[paramIndex]);

                ++paramOffset;
                processed = paramOffset + 1;
            }

            paramOffset = format.indexOf('@', paramOffset + 1);
        }

        out.append(format, processed, length);
    }

}
