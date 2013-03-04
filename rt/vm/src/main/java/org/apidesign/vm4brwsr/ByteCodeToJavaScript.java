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
import static org.apidesign.vm4brwsr.ByteCodeParser.*;

/** Translator of the code inside class files to JavaScript.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
abstract class ByteCodeToJavaScript {
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
    protected abstract void requireScript(String resourcePath) throws IOException;
    
    /** Allows subclasses to redefine what field a function representing a
     * class gets assigned. By default it returns the suggested name followed
     * by <code>" = "</code>;
     * 
     * @param className suggested name of the class
     */
    /* protected */ String assignClass(String className) {
        return className + " = ";
    }
    /* protected */ String accessClass(String classOperation) {
        return classOperation;
    }
    
    /** Prints out a debug message. 
     * 
     * @param msg the message
     * @return true if the message has been printed
     * @throws IOException 
     */
    boolean debug(String msg) throws IOException {
        out.append(msg);
        return true;
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
        if (jc.getMajor_version() < 50) {
            throw new IOException("Can't compile " + jc.getClassName() + ". Class file version " + jc.getMajor_version() + "."
                + jc.getMinor_version() + " - recompile with -target 1.6 (at least)."
            );
        }
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
        out.append("\n  if (!CLS.$class) {");
        if (proto == null) {
            String sc = jc.getSuperClassName(); // with _
            out.append("\n    var pp = ").
                append(accessClass(sc.replace('/', '_'))).append("(true);");
            out.append("\n    var p = CLS.prototype = pp;");
            out.append("\n    var c = p;");
            out.append("\n    var sprcls = pp.constructor.$class;");
        } else {
            out.append("\n    var p = CLS.prototype = ").append(proto[1]).append(";");
            if (proto[0] == null) {
                proto[0] = "p";
            }
            out.append("\n    var c = ").append(proto[0]).append(";");
            out.append("\n    var sprcls = null;");
        }
        for (FieldData v : jc.getFields()) {
            if (v.isStatic()) {
                out.append("\n  CLS.").append(v.getName()).append(initField(v));
                out.append("\n  c._").append(v.getName()).append(" = function (v) {")
                   .append("  if (arguments.length == 1) CLS.").append(v.getName())
                   .append(" = v; return CLS.").
                    append(v.getName()).append("; };");
            } else {
                out.append("\n  c._").append(v.getName()).append(" = function (v) {")
                   .append("  if (arguments.length == 1) this.fld_").
                    append(className).append('_').append(v.getName())
                   .append(" = v; return this.fld_").
                    append(className).append('_').append(v.getName())
                   .append("; };");
            }
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
            String prefix;
            String mn;
            if (m.isStatic()) {
                prefix = "\n    c.";
                mn = generateStaticMethod(prefix, m, toInitilize);
            } else {
                if (m.isConstructor()) {
                    prefix = "\n    CLS.";
                    mn = generateInstanceMethod(prefix, m);
                } else {
                    prefix = "\n    c.";
                    mn = generateInstanceMethod(prefix, m);
                }
            }
            byte[] runAnno = m.findAnnotationData(false);
            if (runAnno != null) {
                out.append(prefix).append(mn).append(".anno = {");
                generateAnno(jc, out, runAnno);
                out.append("\n    };");
            }
            out.append(prefix).append(mn).append(".access = " + m.getAccess()).append(";");
            out.append(prefix).append(mn).append(".cls = CLS;");
        }
        out.append("\n    c.constructor = CLS;");
        out.append("\n    c.$instOf_").append(className).append(" = true;");
        for (String superInterface : jc.getSuperInterfaces()) {
            out.append("\n    c.$instOf_").append(superInterface.replace('/', '_')).append(" = true;");
        }
        out.append("\n    CLS.$class = 'temp';");
        out.append("\n    CLS.$class = ");
        out.append(accessClass("java_lang_Class(true);"));
        out.append("\n    CLS.$class.jvmName = '").append(jc.getClassName()).append("';");
        out.append("\n    CLS.$class.superclass = sprcls;");
        out.append("\n    CLS.$class.access = ").append(jc.getAccessFlags()+";");
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
                    append(className).append('_').
                    append(v.getName()).append(initField(v));
            }
        }
        out.append("\n    return this;");
        out.append("\n  }");
        out.append("\n  return arguments[0] ? new CLS() : CLS.prototype;");
        out.append("\n};");
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
        final String mn = findMethodName(m, new StringBuilder());
        if (mn.equals("class__V")) {
            toInitilize.add(accessClass(className(jc)) + "(false)." + mn);
        }
        generateMethod(prefix, mn, m);
        return mn;
    }

    private String generateInstanceMethod(String prefix, MethodData m) throws IOException {
        String jsb = javaScriptBody(prefix, m, false);
        if (jsb != null) {
            return jsb;
        }
        final String mn = findMethodName(m, new StringBuilder());
        generateMethod(prefix, mn, m);
        return mn;
    }

    private void generateMethod(String prefix, String name, MethodData m)
            throws IOException {
        final StackMapIterator stackMapIterator = m.createStackMapIterator();
        TrapDataIterator trap = m.getTrapDataIterator();
        final LocalsMapper lmapper =
                new LocalsMapper(stackMapIterator.getArguments());

        out.append(prefix).append(name).append(" = function(");
        lmapper.outputArguments(out, m.isStatic());
        out.append(") {").append("\n");

        final byte[] byteCodes = m.getCode();
        if (byteCodes == null) {
            out.append("  throw 'no code found for ")
               .append(jc.getClassName()).append('.')
               .append(m.getName()).append("';\n");
            out.append("};");
            return;
        }

        final StackMapper smapper = new StackMapper();

        if (!m.isStatic()) {
            out.append("  var ").append(" lcA0 = this;\n");
        }

        int lastStackFrame = -1;
        TrapData[] previousTrap = null;
        boolean wide = false;
        
        out.append("\n  var gt = 0;\n  for(;;) switch(gt) {\n");
        for (int i = 0; i < byteCodes.length; i++) {
            int prev = i;
            stackMapIterator.advanceTo(i);
            boolean changeInCatch = trap.advanceTo(i);
            if (changeInCatch || lastStackFrame != stackMapIterator.getFrameIndex()) {
                if (previousTrap != null) {
                    generateCatch(previousTrap);
                    previousTrap = null;
                }
            }
            if (lastStackFrame != stackMapIterator.getFrameIndex()) {
                lastStackFrame = stackMapIterator.getFrameIndex();
                lmapper.syncWithFrameLocals(stackMapIterator.getFrameLocals());
                smapper.syncWithFrameStack(stackMapIterator.getFrameStack());
                out.append("    case " + i).append(": ");            
                changeInCatch = true;
            } else {
                debug("    /* " + i + " */ ");
            }
            if (changeInCatch && trap.useTry()) {
                out.append("try {");
                previousTrap = trap.current();
            }
            final int c = readUByte(byteCodes, i);
            switch (c) {
                case opc_aload_0:
                    emit(out, "var @1 = @2;", smapper.pushA(), lmapper.getA(0));
                    break;
                case opc_iload_0:
                    emit(out, "var @1 = @2;", smapper.pushI(), lmapper.getI(0));
                    break;
                case opc_lload_0:
                    emit(out, "var @1 = @2;", smapper.pushL(), lmapper.getL(0));
                    break;
                case opc_fload_0:
                    emit(out, "var @1 = @2;", smapper.pushF(), lmapper.getF(0));
                    break;
                case opc_dload_0:
                    emit(out, "var @1 = @2;", smapper.pushD(), lmapper.getD(0));
                    break;
                case opc_aload_1:
                    emit(out, "var @1 = @2;", smapper.pushA(), lmapper.getA(1));
                    break;
                case opc_iload_1:
                    emit(out, "var @1 = @2;", smapper.pushI(), lmapper.getI(1));
                    break;
                case opc_lload_1:
                    emit(out, "var @1 = @2;", smapper.pushL(), lmapper.getL(1));
                    break;
                case opc_fload_1:
                    emit(out, "var @1 = @2;", smapper.pushF(), lmapper.getF(1));
                    break;
                case opc_dload_1:
                    emit(out, "var @1 = @2;", smapper.pushD(), lmapper.getD(1));
                    break;
                case opc_aload_2:
                    emit(out, "var @1 = @2;", smapper.pushA(), lmapper.getA(2));
                    break;
                case opc_iload_2:
                    emit(out, "var @1 = @2;", smapper.pushI(), lmapper.getI(2));
                    break;
                case opc_lload_2:
                    emit(out, "var @1 = @2;", smapper.pushL(), lmapper.getL(2));
                    break;
                case opc_fload_2:
                    emit(out, "var @1 = @2;", smapper.pushF(), lmapper.getF(2));
                    break;
                case opc_dload_2:
                    emit(out, "var @1 = @2;", smapper.pushD(), lmapper.getD(2));
                    break;
                case opc_aload_3:
                    emit(out, "var @1 = @2;", smapper.pushA(), lmapper.getA(3));
                    break;
                case opc_iload_3:
                    emit(out, "var @1 = @2;", smapper.pushI(), lmapper.getI(3));
                    break;
                case opc_lload_3:
                    emit(out, "var @1 = @2;", smapper.pushL(), lmapper.getL(3));
                    break;
                case opc_fload_3:
                    emit(out, "var @1 = @2;", smapper.pushF(), lmapper.getF(3));
                    break;
                case opc_dload_3:
                    emit(out, "var @1 = @2;", smapper.pushD(), lmapper.getD(3));
                    break;
                case opc_iload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         smapper.pushI(), lmapper.getI(indx));
                    break;
                }
                case opc_lload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         smapper.pushL(), lmapper.getL(indx));
                    break;
                }
                case opc_fload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         smapper.pushF(), lmapper.getF(indx));
                    break;
                }
                case opc_dload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         smapper.pushD(), lmapper.getD(indx));
                    break;
                }
                case opc_aload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         smapper.pushA(), lmapper.getA(indx));
                    break;
                }
                case opc_istore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         lmapper.setI(indx), smapper.popI());
                    break;
                }
                case opc_lstore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         lmapper.setL(indx), smapper.popL());
                    break;
                }
                case opc_fstore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         lmapper.setF(indx), smapper.popF());
                    break;
                }
                case opc_dstore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         lmapper.setD(indx), smapper.popD());
                    break;
                }
                case opc_astore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(out, "var @1 = @2;",
                         lmapper.setA(indx), smapper.popA());
                    break;
                }
                case opc_astore_0:
                    emit(out, "var @1 = @2;", lmapper.setA(0), smapper.popA());
                    break;
                case opc_istore_0:
                    emit(out, "var @1 = @2;", lmapper.setI(0), smapper.popI());
                    break;
                case opc_lstore_0:
                    emit(out, "var @1 = @2;", lmapper.setL(0), smapper.popL());
                    break;
                case opc_fstore_0:
                    emit(out, "var @1 = @2;", lmapper.setF(0), smapper.popF());
                    break;
                case opc_dstore_0:
                    emit(out, "var @1 = @2;", lmapper.setD(0), smapper.popD());
                    break;
                case opc_astore_1:
                    emit(out, "var @1 = @2;", lmapper.setA(1), smapper.popA());
                    break;
                case opc_istore_1:
                    emit(out, "var @1 = @2;", lmapper.setI(1), smapper.popI());
                    break;
                case opc_lstore_1:
                    emit(out, "var @1 = @2;", lmapper.setL(1), smapper.popL());
                    break;
                case opc_fstore_1:
                    emit(out, "var @1 = @2;", lmapper.setF(1), smapper.popF());
                    break;
                case opc_dstore_1:
                    emit(out, "var @1 = @2;", lmapper.setD(1), smapper.popD());
                    break;
                case opc_astore_2:
                    emit(out, "var @1 = @2;", lmapper.setA(2), smapper.popA());
                    break;
                case opc_istore_2:
                    emit(out, "var @1 = @2;", lmapper.setI(2), smapper.popI());
                    break;
                case opc_lstore_2:
                    emit(out, "var @1 = @2;", lmapper.setL(2), smapper.popL());
                    break;
                case opc_fstore_2:
                    emit(out, "var @1 = @2;", lmapper.setF(2), smapper.popF());
                    break;
                case opc_dstore_2:
                    emit(out, "var @1 = @2;", lmapper.setD(2), smapper.popD());
                    break;
                case opc_astore_3:
                    emit(out, "var @1 = @2;", lmapper.setA(3), smapper.popA());
                    break;
                case opc_istore_3:
                    emit(out, "var @1 = @2;", lmapper.setI(3), smapper.popI());
                    break;
                case opc_lstore_3:
                    emit(out, "var @1 = @2;", lmapper.setL(3), smapper.popL());
                    break;
                case opc_fstore_3:
                    emit(out, "var @1 = @2;", lmapper.setF(3), smapper.popF());
                    break;
                case opc_dstore_3:
                    emit(out, "var @1 = @2;", lmapper.setD(3), smapper.popD());
                    break;
                case opc_iadd:
                    emit(out, "@1 = @1.add32(@2);", smapper.getI(1), smapper.popI());
                    break;
                case opc_ladd:
                    emit(out, "@1 = @1.add64(@2);", smapper.getL(1), smapper.popL());
                    break;
                case opc_fadd:
                    emit(out, "@1 += @2;", smapper.getF(1), smapper.popF());
                    break;
                case opc_dadd:
                    emit(out, "@1 += @2;", smapper.getD(1), smapper.popD());
                    break;
                case opc_isub:
                    emit(out, "@1 = @1.sub32(@2);", smapper.getI(1), smapper.popI());
                    break;
                case opc_lsub:
                    emit(out, "@1 = @1.sub64(@2);", smapper.getL(1), smapper.popL());
                    break;
                case opc_fsub:
                    emit(out, "@1 -= @2;", smapper.getF(1), smapper.popF());
                    break;
                case opc_dsub:
                    emit(out, "@1 -= @2;", smapper.getD(1), smapper.popD());
                    break;
                case opc_imul:
                    emit(out, "@1 = @1.mul32(@2);", smapper.getI(1), smapper.popI());
                    break;
                case opc_lmul:
                    emit(out, "@1 = @1.mul64(@2);", smapper.getL(1), smapper.popL());
                    break;
                case opc_fmul:
                    emit(out, "@1 *= @2;", smapper.getF(1), smapper.popF());
                    break;
                case opc_dmul:
                    emit(out, "@1 *= @2;", smapper.getD(1), smapper.popD());
                    break;
                case opc_idiv:
                    emit(out, "@1 = @1.div32(@2);",
                         smapper.getI(1), smapper.popI());
                    break;
                case opc_ldiv:
                    emit(out, "@1 = @1.div64(@2);",
                         smapper.getL(1), smapper.popL());
                    break;
                case opc_fdiv:
                    emit(out, "@1 /= @2;", smapper.getF(1), smapper.popF());
                    break;
                case opc_ddiv:
                    emit(out, "@1 /= @2;", smapper.getD(1), smapper.popD());
                    break;
                case opc_irem:
                    emit(out, "@1 = @1.mod32(@2);",
                         smapper.getI(1), smapper.popI());
                    break;
                case opc_lrem:
                    emit(out, "@1 = @1.mod64(@2);",
                         smapper.getL(1), smapper.popL());
                    break;
                case opc_frem:
                    emit(out, "@1 %= @2;", smapper.getF(1), smapper.popF());
                    break;
                case opc_drem:
                    emit(out, "@1 %= @2;", smapper.getD(1), smapper.popD());
                    break;
                case opc_iand:
                    emit(out, "@1 &= @2;", smapper.getI(1), smapper.popI());
                    break;
                case opc_land:
                    emit(out, "@1 = @1.and64(@2);", smapper.getL(1), smapper.popL());
                    break;
                case opc_ior:
                    emit(out, "@1 |= @2;", smapper.getI(1), smapper.popI());
                    break;
                case opc_lor:
                    emit(out, "@1 = @1.or64(@2);", smapper.getL(1), smapper.popL());
                    break;
                case opc_ixor:
                    emit(out, "@1 ^= @2;", smapper.getI(1), smapper.popI());
                    break;
                case opc_lxor:
                    emit(out, "@1 = @1.xor64(@2);", smapper.getL(1), smapper.popL());
                    break;
                case opc_ineg:
                    emit(out, "@1 = @1.neg32();", smapper.getI(0));
                    break;
                case opc_lneg:
                    emit(out, "@1 = @1.neg64();", smapper.getL(0));
                    break;
                case opc_fneg:
                    emit(out, "@1 = -@1;", smapper.getF(0));
                    break;
                case opc_dneg:
                    emit(out, "@1 = -@1;", smapper.getD(0));
                    break;
                case opc_ishl:
                    emit(out, "@1 <<= @2;", smapper.getI(1), smapper.popI());
                    break;
                case opc_lshl:
                    emit(out, "@1 = @1.shl64(@2);", smapper.getL(1), smapper.popI());
                    break;
                case opc_ishr:
                    emit(out, "@1 >>= @2;", smapper.getI(1), smapper.popI());
                    break;
                case opc_lshr:
                    emit(out, "@1 = @1.shr64(@2);", smapper.getL(1), smapper.popI());
                    break;
                case opc_iushr:
                    emit(out, "@1 >>>= @2;", smapper.getI(1), smapper.popI());
                    break;
                case opc_lushr:
                    emit(out, "@1 = @1.ushr64(@2);", smapper.getL(1), smapper.popI());
                    break;
                case opc_iinc: {
                    ++i;
                    final int varIndx = wide ? readUShort(byteCodes, i++)
                                             : readUByte(byteCodes, i);
                    ++i;
                    final int incrBy = wide ? readShort(byteCodes, i++)
                                            : byteCodes[i];
                    wide = false;
                    if (incrBy == 1) {
                        emit(out, "@1++;", lmapper.getI(varIndx));
                    } else {
                        emit(out, "@1 += @2;",
                             lmapper.getI(varIndx),
                             Integer.toString(incrBy));
                    }
                    break;
                }
                case opc_return:
                    emit(out, "return;");
                    break;
                case opc_ireturn:
                    emit(out, "return @1;", smapper.popI());
                    break;
                case opc_lreturn:
                    emit(out, "return @1;", smapper.popL());
                    break;
                case opc_freturn:
                    emit(out, "return @1;", smapper.popF());
                    break;
                case opc_dreturn:
                    emit(out, "return @1;", smapper.popD());
                    break;
                case opc_areturn:
                    emit(out, "return @1;", smapper.popA());
                    break;
                case opc_i2l:
                    emit(out, "var @2 = @1;", smapper.popI(), smapper.pushL());
                    break;
                case opc_i2f:
                    emit(out, "var @2 = @1;", smapper.popI(), smapper.pushF());
                    break;
                case opc_i2d:
                    emit(out, "var @2 = @1;", smapper.popI(), smapper.pushD());
                    break;
                case opc_l2i:
                    emit(out, "var @2 = @1.toInt32();", smapper.popL(), smapper.pushI());
                    break;
                    // max int check?
                case opc_l2f:
                    emit(out, "var @2 = @1.toFP();", smapper.popL(), smapper.pushF());
                    break;
                case opc_l2d:
                    emit(out, "var @2 = @1.toFP();", smapper.popL(), smapper.pushD());
                    break;
                case opc_f2d:
                    emit(out, "var @2 = @1;", smapper.popF(), smapper.pushD());
                    break;
                case opc_d2f:
                    emit(out, "var @2 = @1;", smapper.popD(), smapper.pushF());
                    break;
                case opc_f2i:
                    emit(out, "var @2 = @1.toInt32();",
                         smapper.popF(), smapper.pushI());
                    break;
                case opc_f2l:
                    emit(out, "var @2 = @1.toLong();",
                         smapper.popF(), smapper.pushL());
                    break;
                case opc_d2i:
                    emit(out, "var @2 = @1.toInt32();",
                         smapper.popD(), smapper.pushI());
                    break;
                case opc_d2l:
                    emit(out, "var @2 = @1.toLong();",
                         smapper.popD(), smapper.pushL());
                    break;
                case opc_i2b:
                    emit(out, "var @1 = @1.toInt8();", smapper.getI(0));
                    break;
                case opc_i2c:
                    out.append("{ /* number conversion */ }");
                    break;
                case opc_i2s:
                    emit(out, "var @1 = @1.toInt16();", smapper.getI(0));
                    break;
                case opc_aconst_null:
                    emit(out, "var @1 = null;", smapper.pushA());
                    break;
                case opc_iconst_m1:
                    emit(out, "var @1 = -1;", smapper.pushI());
                    break;
                case opc_iconst_0:
                    emit(out, "var @1 = 0;", smapper.pushI());
                    break;
                case opc_dconst_0:
                    emit(out, "var @1 = 0;", smapper.pushD());
                    break;
                case opc_lconst_0:
                    emit(out, "var @1 = 0;", smapper.pushL());
                    break;
                case opc_fconst_0:
                    emit(out, "var @1 = 0;", smapper.pushF());
                    break;
                case opc_iconst_1:
                    emit(out, "var @1 = 1;", smapper.pushI());
                    break;
                case opc_lconst_1:
                    emit(out, "var @1 = 1;", smapper.pushL());
                    break;
                case opc_fconst_1:
                    emit(out, "var @1 = 1;", smapper.pushF());
                    break;
                case opc_dconst_1:
                    emit(out, "var @1 = 1;", smapper.pushD());
                    break;
                case opc_iconst_2:
                    emit(out, "var @1 = 2;", smapper.pushI());
                    break;
                case opc_fconst_2:
                    emit(out, "var @1 = 2;", smapper.pushF());
                    break;
                case opc_iconst_3:
                    emit(out, "var @1 = 3;", smapper.pushI());
                    break;
                case opc_iconst_4:
                    emit(out, "var @1 = 4;", smapper.pushI());
                    break;
                case opc_iconst_5:
                    emit(out, "var @1 = 5;", smapper.pushI());
                    break;
                case opc_ldc: {
                    int indx = readUByte(byteCodes, ++i);
                    String v = encodeConstant(indx);
                    int type = VarType.fromConstantType(jc.getTag(indx));
                    emit(out, "var @1 = @2;", smapper.pushT(type), v);
                    break;
                }
                case opc_ldc_w:
                case opc_ldc2_w: {
                    int indx = readIntArg(byteCodes, i);
                    i += 2;
                    String v = encodeConstant(indx);
                    int type = VarType.fromConstantType(jc.getTag(indx));
                    if (type == VarType.LONG) {
                        final Long lv = new Long(v);
                        final int low = (int)(lv.longValue() & 0xFFFFFFFF);
                        final int hi = (int)(lv.longValue() >> 32);
                        emit(out, "var @1 = 0x@3.next32(0x@2);", smapper.pushL(), 
                                Integer.toHexString(low), Integer.toHexString(hi));
                    } else {
                        emit(out, "var @1 = @2;", smapper.pushT(type), v);
                    }
                    break;
                }
                case opc_lcmp:
                    emit(out, "var @3 = @2.compare64(@1);",
                         smapper.popL(), smapper.popL(), smapper.pushI());
                    break;
                case opc_fcmpl:
                case opc_fcmpg:
                    emit(out, "var @3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);",
                         smapper.popF(), smapper.popF(), smapper.pushI());
                    break;
                case opc_dcmpl:
                case opc_dcmpg:
                    emit(out, "var @3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);",
                         smapper.popD(), smapper.popD(), smapper.pushI());
                    break;
                case opc_if_acmpeq:
                    i = generateIf(byteCodes, i, smapper.popA(), smapper.popA(),
                                   "===");
                    break;
                case opc_if_acmpne:
                    i = generateIf(byteCodes, i, smapper.popA(), smapper.popA(),
                                   "!==");
                    break;
                case opc_if_icmpeq:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   "==");
                    break;
                case opc_ifeq: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 == 0) { gt = @2; continue; }",
                         smapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifne: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 != 0) { gt = @2; continue; }",
                         smapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_iflt: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 < 0) { gt = @2; continue; }",
                         smapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifle: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 <= 0) { gt = @2; continue; }",
                         smapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifgt: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 > 0) { gt = @2; continue; }",
                         smapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifge: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 >= 0) { gt = @2; continue; }",
                         smapper.popI(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifnonnull: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 !== null) { gt = @2; continue; }",
                         smapper.popA(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_ifnull: {
                    int indx = i + readIntArg(byteCodes, i);
                    emit(out, "if (@1 === null) { gt = @2; continue; }",
                         smapper.popA(), Integer.toString(indx));
                    i += 2;
                    break;
                }
                case opc_if_icmpne:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   "!=");
                    break;
                case opc_if_icmplt:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   "<");
                    break;
                case opc_if_icmple:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   "<=");
                    break;
                case opc_if_icmpgt:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   ">");
                    break;
                case opc_if_icmpge:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
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
                    out.append("switch (").append(smapper.popI()).append(") {\n");
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
                    out.append("switch (").append(smapper.popI()).append(") {\n");
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
                    i = invokeVirtualMethod(byteCodes, i, smapper) + 2;
                    break;
                }
                case opc_invokevirtual:
                    i = invokeVirtualMethod(byteCodes, i, smapper);
                    break;
                case opc_invokespecial:
                    i = invokeStaticMethod(byteCodes, i, smapper, false);
                    break;
                case opc_invokestatic:
                    i = invokeStaticMethod(byteCodes, i, smapper, true);
                    break;
                case opc_new: {
                    int indx = readIntArg(byteCodes, i);
                    String ci = jc.getClassName(indx);
                    emit(out, "var @1 = new @2;",
                         smapper.pushA(), accessClass(ci.replace('/', '_')));
                    addReference(ci);
                    i += 2;
                    break;
                }
                case opc_newarray:
                    int atype = readUByte(byteCodes, ++i);
                    String jvmType;
                    switch (atype) {
                        case 4: jvmType = "[Z"; break;
                        case 5: jvmType = "[C"; break;
                        case 6: jvmType = "[F"; break;
                        case 7: jvmType = "[D"; break;
                        case 8: jvmType = "[B"; break;
                        case 9: jvmType = "[S"; break;
                        case 10: jvmType = "[I"; break;
                        case 11: jvmType = "[J"; break;
                        default: throw new IllegalStateException("Array type: " + atype);
                    }
                    emit(out, "var @2 = Array.prototype.newArray__Ljava_lang_Object_2ZLjava_lang_String_2I(true, '@3', @1);",
                         smapper.popI(), smapper.pushA(), jvmType);
                    break;
                case opc_anewarray: {
                    int type = readIntArg(byteCodes, i);
                    i += 2;
                    String typeName = jc.getClassName(type);
                    if (typeName.startsWith("[")) {
                        typeName = "[" + typeName;
                    } else {
                        typeName = "[L" + typeName + ";";
                    }
                    emit(out, "var @2 = Array.prototype.newArray__Ljava_lang_Object_2ZLjava_lang_String_2I(false, '@3', @1);",
                         smapper.popI(), smapper.pushA(), typeName);
                    break;
                }
                case opc_multianewarray: {
                    int type = readIntArg(byteCodes, i);
                    i += 2;
                    String typeName = jc.getClassName(type);
                    int dim = readUByte(byteCodes, ++i);
                    StringBuilder dims = new StringBuilder();
                    dims.append('[');
                    for (int d = 0; d < dim; d++) {
                        if (d != 0) {
                            dims.insert(1, ",");
                        }
                        dims.insert(1, smapper.popI());
                    }
                    dims.append(']');
                    emit(out, "var @2 = Array.prototype.multiNewArray__Ljava_lang_Object_2Ljava_lang_String_2_3II('@3', @1, 0);",
                         dims.toString(), smapper.pushA(), typeName);
                    break;
                }
                case opc_arraylength:
                    emit(out, "var @2 = @1.length;",
                         smapper.popA(), smapper.pushI());
                    break;
                case opc_lastore:
                    emit(out, "@3.at(@2, @1);",
                         smapper.popL(), smapper.popI(), smapper.popA());
                    break;
                case opc_fastore:
                    emit(out, "@3.at(@2, @1);",
                         smapper.popF(), smapper.popI(), smapper.popA());
                    break;
                case opc_dastore:
                    emit(out, "@3.at(@2, @1);",
                         smapper.popD(), smapper.popI(), smapper.popA());
                    break;
                case opc_aastore:
                    emit(out, "@3.at(@2, @1);",
                         smapper.popA(), smapper.popI(), smapper.popA());
                    break;
                case opc_iastore:
                case opc_bastore:
                case opc_castore:
                case opc_sastore:
                    emit(out, "@3.at(@2, @1);",
                         smapper.popI(), smapper.popI(), smapper.popA());
                    break;
                case opc_laload:
                    emit(out, "var @3 = @2.at(@1);",
                         smapper.popI(), smapper.popA(), smapper.pushL());
                    break;
                case opc_faload:
                    emit(out, "var @3 = @2.at(@1);",
                         smapper.popI(), smapper.popA(), smapper.pushF());
                    break;
                case opc_daload:
                    emit(out, "var @3 = @2.at(@1);",
                         smapper.popI(), smapper.popA(), smapper.pushD());
                    break;
                case opc_aaload:
                    emit(out, "var @3 = @2.at(@1);",
                         smapper.popI(), smapper.popA(), smapper.pushA());
                    break;
                case opc_iaload:
                case opc_baload:
                case opc_caload:
                case opc_saload:
                    emit(out, "var @3 = @2.at(@1);",
                         smapper.popI(), smapper.popA(), smapper.pushI());
                    break;
                case opc_pop:
                case opc_pop2:
                    smapper.pop(1);
                    debug("/* pop */");
                    break;
                case opc_dup: {
                    final Variable v = smapper.get(0);
                    emit(out, "var @1 = @2;", smapper.pushT(v.getType()), v);
                    break;
                }
                case opc_dup2: {
                    final Variable vi1 = smapper.get(0);

                    if (vi1.isCategory2()) {
                        emit(out, "var @1 = @2;",
                             smapper.pushT(vi1.getType()), vi1);
                    } else {
                        final Variable vi2 = smapper.get(1);
                        emit(out, "var @1 = @2, @3 = @4;",
                             smapper.pushT(vi2.getType()), vi2,
                             smapper.pushT(vi1.getType()), vi1);
                    }
                    break;
                }
                case opc_dup_x1: {
                    final Variable vi1 = smapper.pop();
                    final Variable vi2 = smapper.pop();
                    final Variable vo3 = smapper.pushT(vi1.getType());
                    final Variable vo2 = smapper.pushT(vi2.getType());
                    final Variable vo1 = smapper.pushT(vi1.getType());

                    emit(out, "var @1 = @2, @3 = @4, @5 = @6;",
                         vo1, vi1, vo2, vi2, vo3, vo1);
                    break;
                }
                case opc_dup2_x1: {
                    final Variable vi1 = smapper.pop();
                    final Variable vi2 = smapper.pop();

                    if (vi1.isCategory2()) {
                        final Variable vo3 = smapper.pushT(vi1.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());

                        emit(out, "var @1 = @2, @3 = @4, @5 = @6;",
                             vo1, vi1, vo2, vi2, vo3, vo1);
                    } else {
                        final Variable vi3 = smapper.pop();
                        final Variable vo5 = smapper.pushT(vi2.getType());
                        final Variable vo4 = smapper.pushT(vi1.getType());
                        final Variable vo3 = smapper.pushT(vi3.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());

                        emit(out, "var @1 = @2, @3 = @4, @5 = @6,",
                             vo1, vi1, vo2, vi2, vo3, vi3);
                        emit(out, " @1 = @2, @3 = @4;",
                             vo4, vo1, vo5, vo2);
                    }
                    break;
                }
                case opc_dup_x2: {
                    final Variable vi1 = smapper.pop();
                    final Variable vi2 = smapper.pop();

                    if (vi2.isCategory2()) {
                        final Variable vo3 = smapper.pushT(vi1.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());

                        emit(out, "var @1 = @2, @3 = @4, @5 = @6;",
                             vo1, vi1, vo2, vi2, vo3, vo1);
                    } else {
                        final Variable vi3 = smapper.pop();
                        final Variable vo4 = smapper.pushT(vi1.getType());
                        final Variable vo3 = smapper.pushT(vi3.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());

                        emit(out, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8;",
                             vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                    }
                    break;
                }
                case opc_dup2_x2: {
                    final Variable vi1 = smapper.pop();
                    final Variable vi2 = smapper.pop();

                    if (vi1.isCategory2()) {
                        if (vi2.isCategory2()) {
                            final Variable vo3 = smapper.pushT(vi1.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());

                            emit(out, "var @1 = @2, @3 = @4, @5 = @6;",
                                 vo1, vi1, vo2, vi2, vo3, vo1);
                        } else {
                            final Variable vi3 = smapper.pop();
                            final Variable vo4 = smapper.pushT(vi1.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());

                            emit(out, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8;",
                                 vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                        }
                    } else {
                        final Variable vi3 = smapper.pop();

                        if (vi3.isCategory2()) {
                            final Variable vo5 = smapper.pushT(vi2.getType());
                            final Variable vo4 = smapper.pushT(vi1.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());

                            emit(out, "var @1 = @2, @3 = @4, @5 = @6,",
                                 vo1, vi1, vo2, vi2, vo3, vi3);
                            emit(out, " @1 = @2, @3 = @4;",
                                 vo4, vo1, vo5, vo2);
                        } else {
                            final Variable vi4 = smapper.pop();
                            final Variable vo6 = smapper.pushT(vi2.getType());
                            final Variable vo5 = smapper.pushT(vi1.getType());
                            final Variable vo4 = smapper.pushT(vi4.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            
                            emit(out, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8,",
                                 vo1, vi1, vo2, vi2, vo3, vi3, vo4, vi4);
                            emit(out, " @1 = @2, @3 = @4;",
                                 vo5, vo1, vo6, vo2);
                        }
                    }
                    break;
                }
                case opc_swap: {
                    final Variable vi1 = smapper.get(0);
                    final Variable vi2 = smapper.get(1);

                    if (vi1.getType() == vi2.getType()) {
                        final Variable tmp = smapper.pushT(vi1.getType());

                        emit(out, "var @1 = @2, @2 = @3, @3 = @1;",
                             tmp, vi1, vi2);
                        smapper.pop(1);
                    } else {
                        smapper.pop(2);
                        smapper.pushT(vi1.getType());
                        smapper.pushT(vi2.getType());
                    }
                    break;
                }
                case opc_bipush:
                    emit(out, "var @1 = @2;",
                         smapper.pushI(), Integer.toString(byteCodes[++i]));
                    break;
                case opc_sipush:
                    emit(out, "var @1 = @2;",
                         smapper.pushI(),
                         Integer.toString(readIntArg(byteCodes, i)));
                    i += 2;
                    break;
                case opc_getfield: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = VarType.fromFieldType(fi[2].charAt(0));
                    final String mangleClass = mangleSig(fi[0]);
                    final String mangleClassAccess = accessClass(mangleClass);
                    emit(out, "var @2 = @4(false)._@3.call(@1);",
                         smapper.popA(),
                         smapper.pushT(type), fi[1], mangleClassAccess
                    );
                    i += 2;
                    break;
                }
                case opc_putfield: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = VarType.fromFieldType(fi[2].charAt(0));
                    final String mangleClass = mangleSig(fi[0]);
                    final String mangleClassAccess = accessClass(mangleClass);
                    emit(out, "@4(false)._@3.call(@2, @1);",
                         smapper.popT(type),
                         smapper.popA(), fi[1], 
                         mangleClassAccess
                    );
                    i += 2;
                    break;
                }
                case opc_getstatic: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = VarType.fromFieldType(fi[2].charAt(0));
                    emit(out, "var @1 = @2(false)._@3();",
                         smapper.pushT(type),
                         accessClass(fi[0].replace('/', '_')), fi[1]);
                    i += 2;
                    addReference(fi[0]);
                    break;
                }
                case opc_putstatic: {
                    int indx = readIntArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = VarType.fromFieldType(fi[2].charAt(0));
                    emit(out, "@1(false)._@2(@3);",
                         accessClass(fi[0].replace('/', '_')), fi[1],
                         smapper.popT(type));
                    i += 2;
                    addReference(fi[0]);
                    break;
                }
                case opc_checkcast: {
                    int indx = readIntArg(byteCodes, i);
                    final String type = jc.getClassName(indx);
                    if (!type.startsWith("[")) {
                        emit(out,
                             "if (@1 !== null && !@1.$instOf_@2) throw {};",
                             smapper.getA(0), type.replace('/', '_'));
                    } else {
                        emit(out, "vm.java_lang_Class(false).forName__Ljava_lang_Class_2Ljava_lang_String_2('@2').cast__Ljava_lang_Object_2Ljava_lang_Object_2(@1);",
                             smapper.getA(0), type
                        );
                    }
                    i += 2;
                    break;
                }
                case opc_instanceof: {
                    int indx = readIntArg(byteCodes, i);
                    final String type = jc.getClassName(indx);
                    if (!type.startsWith("[")) {
                        emit(out, "var @2 = @1 != null && @1.$instOf_@3 ? 1 : 0;",
                             smapper.popA(), smapper.pushI(),
                             type.replace('/', '_'));
                    } else {
                        emit(out, "var @2 = vm.java_lang_Class(false).forName__Ljava_lang_Class_2Ljava_lang_String_2('@3').isInstance__ZLjava_lang_Object_2(@1);",
                            smapper.popA(), smapper.pushI(),
                            type
                        );
                    }
                    i += 2;
                    break;
                }
                case opc_athrow: {
                    final Variable v = smapper.popA();
                    smapper.clear();

                    emit(out, "{ var @1 = @2; throw @2; }",
                         smapper.pushA(), v);
                    break;
                }

                case opc_monitorenter: {
                    out.append("/* monitor enter */");
                    smapper.popA();
                    break;
                }

                case opc_monitorexit: {
                    out.append("/* monitor exit */");
                    smapper.popA();
                    break;
                }

                case opc_wide:
                    wide = true;
                    break;

                default: {
                    wide = false;
                    emit(out, "throw 'unknown bytecode @1';",
                         Integer.toString(c));
                }
            }
            if (debug(" //")) {
                for (int j = prev; j <= i; j++) {
                    out.append(" ");
                    final int cc = readUByte(byteCodes, j);
                    out.append(Integer.toString(cc));
                }
            }
            out.append("\n");            
        }
        if (previousTrap != null) {
            generateCatch(previousTrap);
        }
        out.append("  }\n");
        out.append("};");
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
    private int readInt4(byte[] byteCodes, int offset) {
        final int d = byteCodes[offset + 0] << 24;
        final int c = byteCodes[offset + 1] << 16;
        final int b = byteCodes[offset + 2] << 8;
        final int a = byteCodes[offset + 3];
        return (d & 0xff000000) | (c & 0xff0000) | (b & 0xff00) | (a & 0xff);
    }
    private int readUByte(byte[] byteCodes, int offset) {
        return byteCodes[offset] & 0xff;
    }

    private int readUShort(byte[] byteCodes, int offset) {
        return ((byteCodes[offset] & 0xff) << 8)
                    | (byteCodes[offset + 1] & 0xff);
    }

    private int readShort(byte[] byteCodes, int offset) {
        return (byteCodes[offset] << 8)
                    | (byteCodes[offset + 1] & 0xff);
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
                    array = false;
                    continue;
                case '[':
                    array = true;
                    continue;
                default:
                    throw new IllegalStateException("Invalid char: " + ch);
            }
        }
    }
    
    static String mangleSig(String sig) {
        return mangleSig(sig, 0, sig.length());
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

    private int invokeStaticMethod(byte[] byteCodes, int i, final StackMapper mapper, boolean isStatic)
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
            out.append("var ")
               .append(mapper.pushT(VarType.fromFieldType(returnType[0])))
               .append(" = ");
        }

        final String in = mi[0];
        out.append(accessClass(in.replace('/', '_')));
        out.append("(false).");
        if (mn.startsWith("cons_")) {
            out.append("constructor.");
        }
        out.append(mn);
        if (isStatic) {
            out.append('(');
        } else {
            out.append(".call(");
        }
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
    private int invokeVirtualMethod(byte[] byteCodes, int i, final StackMapper mapper)
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
            out.append("var ")
               .append(mapper.pushT(VarType.fromFieldType(returnType[0])))
               .append(" = ");
        }

        out.append(vars[0]).append('.');
        out.append(mn);
        out.append('(');
        String sep = "";
        for (int j = 1; j < numArguments; ++j) {
            out.append(sep);
            out.append(vars[j]);
            sep = ", ";
        }
        out.append(");");
        i += 2;
        return i;
    }

    private void addReference(String cn) throws IOException {
        if (requireReference(cn)) {
            debug(" /* needs " + cn + " */");
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
            if (classRef[0].startsWith("[")) {
                s = accessClass("java_lang_Class") + "(false).forName__Ljava_lang_Class_2Ljava_lang_String_2('" + classRef[0] + "');";
            } else {
                addReference(classRef[0]);
                s = accessClass(s.replace('/', '_')) + "(false).constructor.$class";
            }
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
                super(false, true);
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
        String space = "";
        int index = 0;
        for (int i = 0; i < cnt.length(); i++) {
            out.append(space);
            space = outputArg(out, p.args, index);
            index++;
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
        AnnotationParser ap = new AnnotationParser(false, true) {
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

    private void generateAnno(ClassData cd, final Appendable out, byte[] data) throws IOException {
        AnnotationParser ap = new AnnotationParser(true, false) {
            int[] cnt = new int[32];
            int depth;
            
            @Override
            protected void visitAnnotationStart(String attrType, boolean top) throws IOException {
                final String slashType = attrType.substring(1, attrType.length() - 1);
                requireReference(slashType);
                
                if (cnt[depth]++ > 0) {
                    out.append(",");
                }
                if (top) {
                    out.append('"').append(attrType).append("\" : ");
                }
                out.append("{\n");
                cnt[++depth] = 0;
            }

            @Override
            protected void visitAnnotationEnd(String type, boolean top) throws IOException {
                out.append("\n}\n");
                depth--;
            }

            @Override
            protected void visitValueStart(String attrName, char type) throws IOException {
                if (cnt[depth]++ > 0) {
                    out.append(",\n");
                }
                cnt[++depth] = 0;
                if (attrName != null) {
                    out.append(attrName).append(" : ");
                }
                if (type == '[') {
                    out.append("[");
                }
            }

            @Override
            protected void visitValueEnd(String attrName, char type) throws IOException {
                if (type == '[') {
                    out.append("]");
                }
                depth--;
            }
            
            @Override
            protected void visitAttr(String type, String attr, String attrType, String value) 
            throws IOException {
                if (attr == null && value == null) {
                    return;
                }
                out.append(value);
            }

            @Override
            protected void visitEnumAttr(String type, String attr, String attrType, String value) 
            throws IOException {
                final String slashType = attrType.substring(1, attrType.length() - 1);
                requireReference(slashType);
                
                out.append(accessClass(slashType.replace('/', '_')))
                   .append("(false).constructor.").append(value);
            }
        };
        ap.parse(data, cd);
    }

    private static String outputArg(Appendable out, String[] args, int indx) throws IOException {
        final String name = args[indx];
        if (name == null) {
            return "";
        }
        if (name.contains(",")) {
            throw new IOException("Wrong parameter with ',': " + name);
        }
        out.append(name);
        return ",";
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

    private void generateCatch(TrapData[] traps) throws IOException {
        out.append("} catch (e) {\n");
        int finallyPC = -1;
        for (TrapData e : traps) {
            if (e == null) {
                break;
            }
            if (e.catch_cpx != 0) { //not finally
                final String classInternalName = jc.getClassName(e.catch_cpx);
                addReference(classInternalName);
                if ("java/lang/Throwable".equals(classInternalName)) {
                    out.append("if (e.$instOf_java_lang_Throwable) {");
                    out.append("  var stA0 = e;");
                    out.append("} else {");
                    out.append("  var stA0 = vm.java_lang_Throwable(true);");
                    out.append("  vm.java_lang_Throwable.cons__VLjava_lang_String_2.call(stA0, e.toString());");
                    out.append("}");
                    out.append("gt=" + e.handler_pc + "; continue;");
                } else {
                    out.append("if (e.$instOf_" + classInternalName.replace('/', '_') + ") {");
                    out.append("gt=" + e.handler_pc + "; var stA0 = e; continue;");
                    out.append("}\n");
                }
            } else {
                finallyPC = e.handler_pc;
            }
        }
        if (finallyPC == -1) {
            out.append("throw e;");
        } else {
            out.append("gt=" + finallyPC + "; var stA0 = e; continue;");
        }
        out.append("\n}");
    }
}
