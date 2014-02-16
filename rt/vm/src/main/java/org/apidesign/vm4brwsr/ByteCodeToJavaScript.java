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
    final ObfuscationDelegate obfuscationDelegate;

    protected ByteCodeToJavaScript(Appendable out) {
        this(out, ObfuscationDelegate.NULL);
    }

    protected ByteCodeToJavaScript(
            Appendable out, ObfuscationDelegate obfuscationDelegate) {
        this.out = out;
        this.obfuscationDelegate = obfuscationDelegate;
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

    abstract String getVMObject();

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
        {
            String[] arr = findAnnotation(arrData, jc, 
                "org.apidesign.bck2brwsr.core.ExtraJavaScript", 
                "resource", "processByteCode"
            );
            if (arr != null) {
                if (!arr[0].isEmpty()) {
                    requireScript(arr[0]);
                }
                if ("0".equals(arr[1])) {
                    return null;
                }
            }
        }
        {
            String[] arr = findAnnotation(arrData, jc, 
                "net.java.html.js.JavaScriptResource", 
                "value"
            );
            if (arr != null) {
                if (arr[0].startsWith("/")) {
                    requireScript(arr[0]);
                } else {
                    int last = jc.getClassName().lastIndexOf('/');
                    requireScript(
                        jc.getClassName().substring(0, last + 1).replace('.', '/') + arr[0]
                    );
                }
            }
        }
        String[] proto = findAnnotation(arrData, jc, 
            "org.apidesign.bck2brwsr.core.JavaScriptPrototype", 
            "container", "prototype"
        );
        StringArray toInitilize = new StringArray();
        final String className = className(jc);
        out.append("\n\n").append(assignClass(className));
        out.append("function ").append(className).append("() {");
        out.append("\n  var CLS = ").append(className).append(';');
        out.append("\n  if (!CLS.$class) {");
        if (proto == null) {
            String sc = jc.getSuperClassName(); // with _
            out.append("\n    var pp = ").
                append(accessClass(mangleClassName(sc))).append("(true);");
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
                out.append("\n  CLS.fld_").append(v.getName()).append(initField(v));
                out.append("\n  c._").append(v.getName()).append(" = function (v) {")
                   .append("  if (arguments.length == 1) CLS.fld_").append(v.getName())
                   .append(" = v; return CLS.fld_").
                    append(v.getName()).append("; };");
            } else {
                out.append("\n  c._").append(v.getName()).append(" = function (v) {")
                   .append("  if (arguments.length == 1) this.fld_").
                    append(className).append('_').append(v.getName())
                   .append(" = v; return this.fld_").
                    append(className).append('_').append(v.getName())
                   .append("; };");
            }

            obfuscationDelegate.exportField(out, "c", "_" + v.getName(), v);
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
            String destObject;
            String mn;
            out.append("\n    ");
            if (m.isStatic()) {
                destObject = "c";
                mn = generateStaticMethod(destObject, m, toInitilize);
            } else {
                if (m.isConstructor()) {
                    destObject = "CLS";
                    mn = generateInstanceMethod(destObject, m);
                } else {
                    destObject = "c";
                    mn = generateInstanceMethod(destObject, m);
                }
            }
            obfuscationDelegate.exportMethod(out, destObject, mn, m);
            byte[] runAnno = m.findAnnotationData(false);
            if (runAnno != null) {
                out.append("\n    ").append(destObject).append(".").append(mn).append(".anno = {");
                generateAnno(jc, out, runAnno);
                out.append("\n    };");
            }
            out.append("\n    ").append(destObject).append(".").append(mn).append(".access = " + m.getAccess()).append(";");
            out.append("\n    ").append(destObject).append(".").append(mn).append(".cls = CLS;");
        }
        out.append("\n    c.constructor = CLS;");
        out.append("\n    function fillInstOf(x) {");
        String instOfName = "$instOf_" + className;
        out.append("\n        x.").append(instOfName).append(" = true;");
        for (String superInterface : jc.getSuperInterfaces()) {
            String intrfc = superInterface.replace('/', '_');
            out.append("\n      vm.").append(intrfc).append("(false).fillInstOf(x);");
            requireReference(superInterface);
        }
        out.append("\n    }");
        out.append("\n    c.fillInstOf = fillInstOf;");
        out.append("\n    fillInstOf(c);");
        obfuscationDelegate.exportJSProperty(out, "c", instOfName);
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
        for (String init : toInitilize.toArray()) {
            out.append("\n    ").append(init).append("();");
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

        obfuscationDelegate.exportClass(out, getVMObject(), className, jc);

//        StringBuilder sb = new StringBuilder();
//        for (String init : toInitilize.toArray()) {
//            sb.append("\n").append(init).append("();");
//        }
        return "";
    }
    private String generateStaticMethod(String destObject, MethodData m, StringArray toInitilize) throws IOException {
        String jsb = javaScriptBody(destObject, m, true);
        if (jsb != null) {
            return jsb;
        }
        final String mn = findMethodName(m, new StringBuilder());
        if (mn.equals("class__V")) {
            toInitilize.add(accessClass(className(jc)) + "(false)." + mn);
        }
        generateMethod(destObject, mn, m);
        return mn;
    }

    private String generateInstanceMethod(String destObject, MethodData m) throws IOException {
        String jsb = javaScriptBody(destObject, m, false);
        if (jsb != null) {
            return jsb;
        }
        final String mn = findMethodName(m, new StringBuilder());
        generateMethod(destObject, mn, m);
        return mn;
    }

    private void generateMethod(String destObject, String name, MethodData m)
            throws IOException {
        final StackMapIterator stackMapIterator = m.createStackMapIterator();
        TrapDataIterator trap = m.getTrapDataIterator();
        final LocalsMapper lmapper =
                new LocalsMapper(stackMapIterator.getArguments());

        out.append(destObject).append(".").append(name).append(" = function(");
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
        
        out.append("\n  var gt = 0;\n");
        int openBraces = 0;
        int topMostLabel = 0;
        for (int i = 0; i < byteCodes.length; i++) {
            int prev = i;
            stackMapIterator.advanceTo(i);
            boolean changeInCatch = trap.advanceTo(i);
            if (changeInCatch || lastStackFrame != stackMapIterator.getFrameIndex()) {
                if (previousTrap != null) {
                    generateCatch(previousTrap, i, topMostLabel);
                    previousTrap = null;
                }
            }
            if (lastStackFrame != stackMapIterator.getFrameIndex()) {
                smapper.flush(out);
                if (i != 0) {
                    out.append("    }\n");
                }
                if (openBraces > 64) {
                    for (int c = 0; c < 64; c++) {
                        out.append("break;}\n");
                    }
                    openBraces = 1;
                    topMostLabel = i;
                }
                
                lastStackFrame = stackMapIterator.getFrameIndex();
                lmapper.syncWithFrameLocals(stackMapIterator.getFrameLocals());
                smapper.syncWithFrameStack(stackMapIterator.getFrameStack());
                out.append("    X_" + i).append(": for (;;) { IF: if (gt <= " + i + ") {\n");
                openBraces++;
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
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(0));
                    break;
                case opc_iload_0:
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(0));
                    break;
                case opc_lload_0:
                    smapper.assign(out, VarType.LONG, lmapper.getL(0));
                    break;
                case opc_fload_0:
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(0));
                    break;
                case opc_dload_0:
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(0));
                    break;
                case opc_aload_1:
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(1));
                    break;
                case opc_iload_1:
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(1));
                    break;
                case opc_lload_1:
                    smapper.assign(out, VarType.LONG, lmapper.getL(1));
                    break;
                case opc_fload_1:
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(1));
                    break;
                case opc_dload_1:
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(1));
                    break;
                case opc_aload_2:
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(2));
                    break;
                case opc_iload_2:
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(2));
                    break;
                case opc_lload_2:
                    smapper.assign(out, VarType.LONG, lmapper.getL(2));
                    break;
                case opc_fload_2:
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(2));
                    break;
                case opc_dload_2:
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(2));
                    break;
                case opc_aload_3:
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(3));
                    break;
                case opc_iload_3:
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(3));
                    break;
                case opc_lload_3:
                    smapper.assign(out, VarType.LONG, lmapper.getL(3));
                    break;
                case opc_fload_3:
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(3));
                    break;
                case opc_dload_3:
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(3));
                    break;
                case opc_iload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    smapper.assign(out, VarType.INTEGER, lmapper.getI(indx));
                    break;
                }
                case opc_lload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    smapper.assign(out, VarType.LONG, lmapper.getL(indx));
                    break;
                }
                case opc_fload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    smapper.assign(out, VarType.FLOAT, lmapper.getF(indx));
                    break;
                }
                case opc_dload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    smapper.assign(out, VarType.DOUBLE, lmapper.getD(indx));
                    break;
                }
                case opc_aload: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    smapper.assign(out, VarType.REFERENCE, lmapper.getA(indx));
                    break;
                }
                case opc_istore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(smapper, out, "var @1 = @2;",
                         lmapper.setI(indx), smapper.popI());
                    break;
                }
                case opc_lstore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(smapper, out, "var @1 = @2;",
                         lmapper.setL(indx), smapper.popL());
                    break;
                }
                case opc_fstore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(smapper, out, "var @1 = @2;",
                         lmapper.setF(indx), smapper.popF());
                    break;
                }
                case opc_dstore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(smapper, out, "var @1 = @2;",
                         lmapper.setD(indx), smapper.popD());
                    break;
                }
                case opc_astore: {
                    ++i;
                    final int indx = wide ? readUShort(byteCodes, i++)
                                          : readUByte(byteCodes, i);
                    wide = false;
                    emit(smapper, out, "var @1 = @2;",
                         lmapper.setA(indx), smapper.popA());
                    break;
                }
                case opc_astore_0:
                    emit(smapper, out, "var @1 = @2;", lmapper.setA(0), smapper.popA());
                    break;
                case opc_istore_0:
                    emit(smapper, out, "var @1 = @2;", lmapper.setI(0), smapper.popI());
                    break;
                case opc_lstore_0:
                    emit(smapper, out, "var @1 = @2;", lmapper.setL(0), smapper.popL());
                    break;
                case opc_fstore_0:
                    emit(smapper, out, "var @1 = @2;", lmapper.setF(0), smapper.popF());
                    break;
                case opc_dstore_0:
                    emit(smapper, out, "var @1 = @2;", lmapper.setD(0), smapper.popD());
                    break;
                case opc_astore_1:
                    emit(smapper, out, "var @1 = @2;", lmapper.setA(1), smapper.popA());
                    break;
                case opc_istore_1:
                    emit(smapper, out, "var @1 = @2;", lmapper.setI(1), smapper.popI());
                    break;
                case opc_lstore_1:
                    emit(smapper, out, "var @1 = @2;", lmapper.setL(1), smapper.popL());
                    break;
                case opc_fstore_1:
                    emit(smapper, out, "var @1 = @2;", lmapper.setF(1), smapper.popF());
                    break;
                case opc_dstore_1:
                    emit(smapper, out, "var @1 = @2;", lmapper.setD(1), smapper.popD());
                    break;
                case opc_astore_2:
                    emit(smapper, out, "var @1 = @2;", lmapper.setA(2), smapper.popA());
                    break;
                case opc_istore_2:
                    emit(smapper, out, "var @1 = @2;", lmapper.setI(2), smapper.popI());
                    break;
                case opc_lstore_2:
                    emit(smapper, out, "var @1 = @2;", lmapper.setL(2), smapper.popL());
                    break;
                case opc_fstore_2:
                    emit(smapper, out, "var @1 = @2;", lmapper.setF(2), smapper.popF());
                    break;
                case opc_dstore_2:
                    emit(smapper, out, "var @1 = @2;", lmapper.setD(2), smapper.popD());
                    break;
                case opc_astore_3:
                    emit(smapper, out, "var @1 = @2;", lmapper.setA(3), smapper.popA());
                    break;
                case opc_istore_3:
                    emit(smapper, out, "var @1 = @2;", lmapper.setI(3), smapper.popI());
                    break;
                case opc_lstore_3:
                    emit(smapper, out, "var @1 = @2;", lmapper.setL(3), smapper.popL());
                    break;
                case opc_fstore_3:
                    emit(smapper, out, "var @1 = @2;", lmapper.setF(3), smapper.popF());
                    break;
                case opc_dstore_3:
                    emit(smapper, out, "var @1 = @2;", lmapper.setD(3), smapper.popD());
                    break;
                case opc_iadd:
                    smapper.replace(out, VarType.INTEGER, "(@1).add32(@2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_ladd:
                    smapper.replace(out, VarType.LONG, "(@1).add64(@2)", smapper.getL(1), smapper.popL());
                    break;
                case opc_fadd:
                    smapper.replace(out, VarType.FLOAT, "(@1 + @2)", smapper.getF(1), smapper.popF());
                    break;
                case opc_dadd:
                    smapper.replace(out, VarType.DOUBLE, "(@1 + @2)", smapper.getD(1), smapper.popD());
                    break;
                case opc_isub:
                    smapper.replace(out, VarType.INTEGER, "(@1).sub32(@2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_lsub:
                    smapper.replace(out, VarType.LONG, "(@1).sub64(@2)", smapper.getL(1), smapper.popL());
                    break;
                case opc_fsub:
                    smapper.replace(out, VarType.FLOAT, "(@1 - @2)", smapper.getF(1), smapper.popF());
                    break;
                case opc_dsub:
                    smapper.replace(out, VarType.DOUBLE, "(@1 - @2)", smapper.getD(1), smapper.popD());
                    break;
                case opc_imul:
                    smapper.replace(out, VarType.INTEGER, "(@1).mul32(@2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_lmul:
                    smapper.replace(out, VarType.LONG, "(@1).mul64(@2)", smapper.getL(1), smapper.popL());
                    break;
                case opc_fmul:
                    smapper.replace(out, VarType.FLOAT, "(@1 * @2)", smapper.getF(1), smapper.popF());
                    break;
                case opc_dmul:
                    smapper.replace(out, VarType.DOUBLE, "(@1 * @2)", smapper.getD(1), smapper.popD());
                    break;
                case opc_idiv:
                    smapper.replace(out, VarType.INTEGER, "(@1).div32(@2)",
                         smapper.getI(1), smapper.popI());
                    break;
                case opc_ldiv:
                    smapper.replace(out, VarType.LONG, "(@1).div64(@2)",
                         smapper.getL(1), smapper.popL());
                    break;
                case opc_fdiv:
                    smapper.replace(out, VarType.FLOAT, "(@1 / @2)", smapper.getF(1), smapper.popF());
                    break;
                case opc_ddiv:
                    smapper.replace(out, VarType.DOUBLE, "(@1 / @2)", smapper.getD(1), smapper.popD());
                    break;
                case opc_irem:
                    smapper.replace(out, VarType.INTEGER, "(@1).mod32(@2)",
                         smapper.getI(1), smapper.popI());
                    break;
                case opc_lrem:
                    smapper.replace(out, VarType.LONG, "(@1).mod64(@2)",
                         smapper.getL(1), smapper.popL());
                    break;
                case opc_frem:
                    smapper.replace(out, VarType.FLOAT, "(@1 % @2)", smapper.getF(1), smapper.popF());
                    break;
                case opc_drem:
                    smapper.replace(out, VarType.DOUBLE, "(@1 % @2)", smapper.getD(1), smapper.popD());
                    break;
                case opc_iand:
                    smapper.replace(out, VarType.INTEGER, "(@1 & @2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_land:
                    smapper.replace(out, VarType.LONG, "(@1).and64(@2)", smapper.getL(1), smapper.popL());
                    break;
                case opc_ior:
                    smapper.replace(out, VarType.INTEGER, "(@1 | @2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_lor:
                    smapper.replace(out, VarType.LONG, "(@1).or64(@2)", smapper.getL(1), smapper.popL());
                    break;
                case opc_ixor:
                    smapper.replace(out, VarType.INTEGER, "(@1 ^ @2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_lxor:
                    smapper.replace(out, VarType.LONG, "(@1).xor64(@2)", smapper.getL(1), smapper.popL());
                    break;
                case opc_ineg:
                    smapper.replace(out, VarType.INTEGER, "(@1).neg32()", smapper.getI(0));
                    break;
                case opc_lneg:
                    smapper.replace(out, VarType.LONG, "(@1).neg64()", smapper.getL(0));
                    break;
                case opc_fneg:
                    smapper.replace(out, VarType.FLOAT, "(-@1)", smapper.getF(0));
                    break;
                case opc_dneg:
                    smapper.replace(out, VarType.DOUBLE, "(-@1)", smapper.getD(0));
                    break;
                case opc_ishl:
                    smapper.replace(out, VarType.INTEGER, "(@1 << @2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_lshl:
                    smapper.replace(out, VarType.LONG, "(@1).shl64(@2)", smapper.getL(1), smapper.popI());
                    break;
                case opc_ishr:
                    smapper.replace(out, VarType.INTEGER, "(@1 >> @2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_lshr:
                    smapper.replace(out, VarType.LONG, "(@1).shr64(@2)", smapper.getL(1), smapper.popI());
                    break;
                case opc_iushr:
                    smapper.replace(out, VarType.INTEGER, "(@1 >>> @2)", smapper.getI(1), smapper.popI());
                    break;
                case opc_lushr:
                    smapper.replace(out, VarType.LONG, "(@1).ushr64(@2)", smapper.getL(1), smapper.popI());
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
                        emit(smapper, out, "@1++;", lmapper.getI(varIndx));
                    } else {
                        emit(smapper, out, "@1 += @2;",
                             lmapper.getI(varIndx),
                             Integer.toString(incrBy));
                    }
                    break;
                }
                case opc_return:
                    emit(smapper, out, "return;");
                    break;
                case opc_ireturn:
                    emit(smapper, out, "return @1;", smapper.popI());
                    break;
                case opc_lreturn:
                    emit(smapper, out, "return @1;", smapper.popL());
                    break;
                case opc_freturn:
                    emit(smapper, out, "return @1;", smapper.popF());
                    break;
                case opc_dreturn:
                    emit(smapper, out, "return @1;", smapper.popD());
                    break;
                case opc_areturn:
                    emit(smapper, out, "return @1;", smapper.popA());
                    break;
                case opc_i2l:
                    smapper.replace(out, VarType.LONG, "@1", smapper.getI(0));
                    break;
                case opc_i2f:
                    smapper.replace(out, VarType.FLOAT, "@1", smapper.getI(0));
                    break;
                case opc_i2d:
                    smapper.replace(out, VarType.DOUBLE, "@1", smapper.getI(0));
                    break;
                case opc_l2i:
                    smapper.replace(out, VarType.INTEGER, "(@1).toInt32()", smapper.getL(0));
                    break;
                    // max int check?
                case opc_l2f:
                    smapper.replace(out, VarType.FLOAT, "(@1).toFP()", smapper.getL(0));
                    break;
                case opc_l2d:
                    smapper.replace(out, VarType.DOUBLE, "(@1).toFP()", smapper.getL(0));
                    break;
                case opc_f2d:
                    smapper.replace(out, VarType.DOUBLE, "@1",
                         smapper.getF(0));
                    break;
                case opc_d2f:
                    smapper.replace(out, VarType.FLOAT, "@1",
                         smapper.getD(0));
                    break;
                case opc_f2i:
                    smapper.replace(out, VarType.INTEGER, "(@1).toInt32()",
                         smapper.getF(0));
                    break;
                case opc_f2l:
                    smapper.replace(out, VarType.LONG, "(@1).toLong()",
                         smapper.getF(0));
                    break;
                case opc_d2i:
                    smapper.replace(out, VarType.INTEGER, "(@1).toInt32()",
                         smapper.getD(0));
                    break;
                case opc_d2l:
                    smapper.replace(out, VarType.LONG, "(@1).toLong()", smapper.getD(0));
                    break;
                case opc_i2b:
                    smapper.replace(out, VarType.INTEGER, "(@1).toInt8()", smapper.getI(0));
                    break;
                case opc_i2c:
                    break;
                case opc_i2s:
                    smapper.replace(out, VarType.INTEGER, "(@1).toInt16()", smapper.getI(0));
                    break;
                case opc_aconst_null:
                    smapper.assign(out, VarType.REFERENCE, "null");
                    break;
                case opc_iconst_m1:
                    smapper.assign(out, VarType.INTEGER, "-1");
                    break;
                case opc_iconst_0:
                    smapper.assign(out, VarType.INTEGER, "0");
                    break;
                case opc_dconst_0:
                    smapper.assign(out, VarType.DOUBLE, "0");
                    break;
                case opc_lconst_0:
                    smapper.assign(out, VarType.LONG, "0");
                    break;
                case opc_fconst_0:
                    smapper.assign(out, VarType.FLOAT, "0");
                    break;
                case opc_iconst_1:
                    smapper.assign(out, VarType.INTEGER, "1");
                    break;
                case opc_lconst_1:
                    smapper.assign(out, VarType.LONG, "1");
                    break;
                case opc_fconst_1:
                    smapper.assign(out, VarType.FLOAT, "1");
                    break;
                case opc_dconst_1:
                    smapper.assign(out, VarType.DOUBLE, "1");
                    break;
                case opc_iconst_2:
                    smapper.assign(out, VarType.INTEGER, "2");
                    break;
                case opc_fconst_2:
                    smapper.assign(out, VarType.FLOAT, "2");
                    break;
                case opc_iconst_3:
                    smapper.assign(out, VarType.INTEGER, "3");
                    break;
                case opc_iconst_4:
                    smapper.assign(out, VarType.INTEGER, "4");
                    break;
                case opc_iconst_5:
                    smapper.assign(out, VarType.INTEGER, "5");
                    break;
                case opc_ldc: {
                    int indx = readUByte(byteCodes, ++i);
                    String v = encodeConstant(indx);
                    int type = VarType.fromConstantType(jc.getTag(indx));
                    emit(smapper, out, "var @1 = @2;", smapper.pushT(type), v);
                    break;
                }
                case opc_ldc_w:
                case opc_ldc2_w: {
                    int indx = readUShortArg(byteCodes, i);
                    i += 2;
                    String v = encodeConstant(indx);
                    int type = VarType.fromConstantType(jc.getTag(indx));
                    if (type == VarType.LONG) {
                        final Long lv = new Long(v);
                        final int low = (int)(lv.longValue() & 0xFFFFFFFF);
                        final int hi = (int)(lv.longValue() >> 32);
                        if (hi == 0) {
                            smapper.assign(out, VarType.LONG, "0x" + Integer.toHexString(low));
                        } else {
                            smapper.assign(out, VarType.LONG,
                                "0x" + Integer.toHexString(hi) + ".next32(0x" + 
                                    Integer.toHexString(low) + ")"
                            );
                        }
                    } else {
                        smapper.assign(out, type, v);
                    }
                    break;
                }
                case opc_lcmp:
                    emit(smapper, out, "var @3 = (@2).compare64(@1);",
                         smapper.popL(), smapper.popL(), smapper.pushI());
                    break;
                case opc_fcmpl:
                case opc_fcmpg:
                    emit(smapper, out, "var @3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);",
                         smapper.popF(), smapper.popF(), smapper.pushI());
                    break;
                case opc_dcmpl:
                case opc_dcmpg:
                    emit(smapper, out, "var @3 = (@2 == @1) ? 0 : ((@2 < @1) ? -1 : 1);",
                         smapper.popD(), smapper.popD(), smapper.pushI());
                    break;
                case opc_if_acmpeq:
                    i = generateIf(byteCodes, i, smapper.popA(), smapper.popA(),
                                   "===", topMostLabel);
                    break;
                case opc_if_acmpne:
                    i = generateIf(byteCodes, i, smapper.popA(), smapper.popA(),
                                   "!==", topMostLabel);
                    break;
                case opc_if_icmpeq:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   "==", topMostLabel);
                    break;
                case opc_ifeq: {
                    int indx = i + readShortArg(byteCodes, i);
                    emitIf(smapper, out, "if (@1 == 0) ",
                         smapper.popI(), i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_ifne: {
                    int indx = i + readShortArg(byteCodes, i);
                    emitIf(smapper, out, "if (@1 != 0) ",
                         smapper.popI(), i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_iflt: {
                    int indx = i + readShortArg(byteCodes, i);
                    emitIf(smapper, out, "if (@1 < 0) ",
                         smapper.popI(), i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_ifle: {
                    int indx = i + readShortArg(byteCodes, i);
                    emitIf(smapper, out, "if (@1 <= 0) ",
                         smapper.popI(), i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_ifgt: {
                    int indx = i + readShortArg(byteCodes, i);
                    emitIf(smapper, out, "if (@1 > 0) ",
                         smapper.popI(), i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_ifge: {
                    int indx = i + readShortArg(byteCodes, i);
                    emitIf(smapper, out, "if (@1 >= 0) ",
                         smapper.popI(), i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_ifnonnull: {
                    int indx = i + readShortArg(byteCodes, i);
                    emitIf(smapper, out, "if (@1 !== null) ",
                         smapper.popA(), i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_ifnull: {
                    int indx = i + readShortArg(byteCodes, i);
                    emitIf(smapper, out, "if (@1 === null) ",
                         smapper.popA(), i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_if_icmpne:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   "!=", topMostLabel);
                    break;
                case opc_if_icmplt:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   "<", topMostLabel);
                    break;
                case opc_if_icmple:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   "<=", topMostLabel);
                    break;
                case opc_if_icmpgt:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   ">", topMostLabel);
                    break;
                case opc_if_icmpge:
                    i = generateIf(byteCodes, i, smapper.popI(), smapper.popI(),
                                   ">=", topMostLabel);
                    break;
                case opc_goto: {
                    smapper.flush(out);
                    int indx = i + readShortArg(byteCodes, i);
                    goTo(out, i, indx, topMostLabel);
                    i += 2;
                    break;
                }
                case opc_lookupswitch: {
                    i = generateLookupSwitch(i, byteCodes, smapper, topMostLabel);
                    break;
                }
                case opc_tableswitch: {
                    i = generateTableSwitch(i, byteCodes, smapper, topMostLabel);
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
                    int indx = readUShortArg(byteCodes, i);
                    String ci = jc.getClassName(indx);
                    emit(smapper, out, "var @1 = new @2;",
                         smapper.pushA(), accessClass(mangleClassName(ci)));
                    addReference(ci);
                    i += 2;
                    break;
                }
                case opc_newarray:
                    int atype = readUByte(byteCodes, ++i);
                    generateNewArray(atype, smapper);
                    break;
                case opc_anewarray: {
                    int type = readUShortArg(byteCodes, i);
                    i += 2;
                    generateANewArray(type, smapper);
                    break;
                }
                case opc_multianewarray: {
                    int type = readUShortArg(byteCodes, i);
                    i += 2;
                    i = generateMultiANewArray(type, byteCodes, i, smapper);
                    break;
                }
                case opc_arraylength:
                    emit(smapper, out, "var @2 = @1.length;",
                         smapper.popA(), smapper.pushI());
                    break;
                case opc_lastore:
                    emit(smapper, out, "Array.at(@3, @2, @1);",
                         smapper.popL(), smapper.popI(), smapper.popA());
                    break;
                case opc_fastore:
                    emit(smapper, out, "Array.at(@3, @2, @1);",
                         smapper.popF(), smapper.popI(), smapper.popA());
                    break;
                case opc_dastore:
                    emit(smapper, out, "Array.at(@3, @2, @1);",
                         smapper.popD(), smapper.popI(), smapper.popA());
                    break;
                case opc_aastore:
                    emit(smapper, out, "Array.at(@3, @2, @1);",
                         smapper.popA(), smapper.popI(), smapper.popA());
                    break;
                case opc_iastore:
                case opc_bastore:
                case opc_castore:
                case opc_sastore:
                    emit(smapper, out, "Array.at(@3, @2, @1);",
                         smapper.popI(), smapper.popI(), smapper.popA());
                    break;
                case opc_laload:
                    emit(smapper, out, "var @3 = Array.at(@2, @1);",
                         smapper.popI(), smapper.popA(), smapper.pushL());
                    break;
                case opc_faload:
                    emit(smapper, out, "var @3 = Array.at(@2, @1);",
                         smapper.popI(), smapper.popA(), smapper.pushF());
                    break;
                case opc_daload:
                    emit(smapper, out, "var @3 = Array.at(@2, @1);",
                         smapper.popI(), smapper.popA(), smapper.pushD());
                    break;
                case opc_aaload:
                    emit(smapper, out, "var @3 = Array.at(@2, @1);",
                         smapper.popI(), smapper.popA(), smapper.pushA());
                    break;
                case opc_iaload:
                case opc_baload:
                case opc_caload:
                case opc_saload:
                    emit(smapper, out, "var @3 = Array.at(@2, @1);",
                         smapper.popI(), smapper.popA(), smapper.pushI());
                    break;
                case opc_pop:
                case opc_pop2:
                    smapper.pop(1);
                    debug("/* pop */");
                    break;
                case opc_dup: {
                    final Variable v = smapper.get(0);
                    emit(smapper, out, "var @1 = @2;", smapper.pushT(v.getType()), v);
                    break;
                }
                case opc_dup2: {
                    final Variable vi1 = smapper.get(0);

                    if (vi1.isCategory2()) {
                        emit(smapper, out, "var @1 = @2;",
                             smapper.pushT(vi1.getType()), vi1);
                    } else {
                        final Variable vi2 = smapper.get(1);
                        emit(smapper, out, "var @1 = @2, @3 = @4;",
                             smapper.pushT(vi2.getType()), vi2,
                             smapper.pushT(vi1.getType()), vi1);
                    }
                    break;
                }
                case opc_dup_x1: {
                    final Variable vi1 = smapper.pop(out);
                    final Variable vi2 = smapper.pop(out);
                    final Variable vo3 = smapper.pushT(vi1.getType());
                    final Variable vo2 = smapper.pushT(vi2.getType());
                    final Variable vo1 = smapper.pushT(vi1.getType());

                    emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6;",
                         vo1, vi1, vo2, vi2, vo3, vo1);
                    break;
                }
                case opc_dup2_x1: {
                    final Variable vi1 = smapper.pop(out);
                    final Variable vi2 = smapper.pop(out);

                    if (vi1.isCategory2()) {
                        final Variable vo3 = smapper.pushT(vi1.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());

                        emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6;",
                             vo1, vi1, vo2, vi2, vo3, vo1);
                    } else {
                        final Variable vi3 = smapper.pop(out);
                        final Variable vo5 = smapper.pushT(vi2.getType());
                        final Variable vo4 = smapper.pushT(vi1.getType());
                        final Variable vo3 = smapper.pushT(vi3.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());

                        emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6,",
                             vo1, vi1, vo2, vi2, vo3, vi3);
                        emit(smapper, out, " @1 = @2, @3 = @4;",
                             vo4, vo1, vo5, vo2);
                    }
                    break;
                }
                case opc_dup_x2: {
                    final Variable vi1 = smapper.pop(out);
                    final Variable vi2 = smapper.pop(out);

                    if (vi2.isCategory2()) {
                        final Variable vo3 = smapper.pushT(vi1.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());

                        emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6;",
                             vo1, vi1, vo2, vi2, vo3, vo1);
                    } else {
                        final Variable vi3 = smapper.pop(out);
                        final Variable vo4 = smapper.pushT(vi1.getType());
                        final Variable vo3 = smapper.pushT(vi3.getType());
                        final Variable vo2 = smapper.pushT(vi2.getType());
                        final Variable vo1 = smapper.pushT(vi1.getType());

                        emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8;",
                             vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                    }
                    break;
                }
                case opc_dup2_x2: {
                    final Variable vi1 = smapper.pop(out);
                    final Variable vi2 = smapper.pop(out);

                    if (vi1.isCategory2()) {
                        if (vi2.isCategory2()) {
                            final Variable vo3 = smapper.pushT(vi1.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());

                            emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6;",
                                 vo1, vi1, vo2, vi2, vo3, vo1);
                        } else {
                            final Variable vi3 = smapper.pop(out);
                            final Variable vo4 = smapper.pushT(vi1.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());

                            emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8;",
                                 vo1, vi1, vo2, vi2, vo3, vi3, vo4, vo1);
                        }
                    } else {
                        final Variable vi3 = smapper.pop(out);

                        if (vi3.isCategory2()) {
                            final Variable vo5 = smapper.pushT(vi2.getType());
                            final Variable vo4 = smapper.pushT(vi1.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());

                            emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6,",
                                 vo1, vi1, vo2, vi2, vo3, vi3);
                            emit(smapper, out, " @1 = @2, @3 = @4;",
                                 vo4, vo1, vo5, vo2);
                        } else {
                            final Variable vi4 = smapper.pop(out);
                            final Variable vo6 = smapper.pushT(vi2.getType());
                            final Variable vo5 = smapper.pushT(vi1.getType());
                            final Variable vo4 = smapper.pushT(vi4.getType());
                            final Variable vo3 = smapper.pushT(vi3.getType());
                            final Variable vo2 = smapper.pushT(vi2.getType());
                            final Variable vo1 = smapper.pushT(vi1.getType());
                            
                            emit(smapper, out, "var @1 = @2, @3 = @4, @5 = @6, @7 = @8,",
                                 vo1, vi1, vo2, vi2, vo3, vi3, vo4, vi4);
                            emit(smapper, out, " @1 = @2, @3 = @4;",
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

                        emit(smapper, out, "var @1 = @2, @2 = @3, @3 = @1;",
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
                    smapper.assign(out, VarType.INTEGER, 
                        "(" + Integer.toString(byteCodes[++i]) + ")");
                    break;
                case opc_sipush:
                    smapper.assign(out, VarType.INTEGER, 
                        "(" + Integer.toString(readShortArg(byteCodes, i)) + ")"
                    );
                    i += 2;
                    break;
                case opc_getfield: {
                    int indx = readUShortArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = VarType.fromFieldType(fi[2].charAt(0));
                    final String mangleClass = mangleClassName(fi[0]);
                    final String mangleClassAccess = accessClass(mangleClass);
                    emit(smapper, out, "var @2 = @4(false)._@3.call(@1);",
                         smapper.popA(),
                         smapper.pushT(type), fi[1], mangleClassAccess
                    );
                    i += 2;
                    break;
                }
                case opc_putfield: {
                    int indx = readUShortArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = VarType.fromFieldType(fi[2].charAt(0));
                    final String mangleClass = mangleClassName(fi[0]);
                    final String mangleClassAccess = accessClass(mangleClass);
                    emit(smapper, out, "@4(false)._@3.call(@2, @1);",
                         smapper.popT(type),
                         smapper.popA(), fi[1], 
                         mangleClassAccess
                    );
                    i += 2;
                    break;
                }
                case opc_getstatic: {
                    int indx = readUShortArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = VarType.fromFieldType(fi[2].charAt(0));
                    emit(smapper, out, "var @1 = @2(false)._@3();",
                         smapper.pushT(type),
                         accessClass(mangleClassName(fi[0])), fi[1]);
                    i += 2;
                    addReference(fi[0]);
                    break;
                }
                case opc_putstatic: {
                    int indx = readUShortArg(byteCodes, i);
                    String[] fi = jc.getFieldInfoName(indx);
                    final int type = VarType.fromFieldType(fi[2].charAt(0));
                    emit(smapper, out, "@1(false)._@2(@3);",
                         accessClass(mangleClassName(fi[0])), fi[1],
                         smapper.popT(type));
                    i += 2;
                    addReference(fi[0]);
                    break;
                }
                case opc_checkcast: {
                    int indx = readUShortArg(byteCodes, i);
                    generateCheckcast(indx, smapper);
                    i += 2;
                    break;
                }
                case opc_instanceof: {
                    int indx = readUShortArg(byteCodes, i);
                    generateInstanceOf(indx, smapper);
                    i += 2;
                    break;
                }
                case opc_athrow: {
                    final CharSequence v = smapper.popA();
                    smapper.clear();

                    emit(smapper, out, "{ var @1 = @2; throw @2; }",
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
                    emit(smapper, out, "throw 'unknown bytecode @1';",
                         Integer.toString(c));
                }
            }
            if (debug(" //")) {
                generateByteCodeComment(prev, i, byteCodes);
            }
            out.append("\n");            
        }
        if (previousTrap != null) {
            generateCatch(previousTrap, byteCodes.length, topMostLabel);
        }
        out.append("\n    }\n");
        while (openBraces-- > 0) {
            out.append('}');
        }
        out.append("\n};");
    }

    private int generateIf(byte[] byteCodes, int i, final CharSequence v2, final CharSequence v1, final String test, int topMostLabel) throws IOException {
        int indx = i + readShortArg(byteCodes, i);
        out.append("if (").append(v1)
           .append(' ').append(test).append(' ')
           .append(v2).append(") ");
        goTo(out, i, indx, topMostLabel);
        return i + 2;
    }
    
    private int readInt4(byte[] byteCodes, int offset) {
        final int d = byteCodes[offset + 0] << 24;
        final int c = byteCodes[offset + 1] << 16;
        final int b = byteCodes[offset + 2] << 8;
        final int a = byteCodes[offset + 3];
        return (d & 0xff000000) | (c & 0xff0000) | (b & 0xff00) | (a & 0xff);
    }
    private static int readUByte(byte[] byteCodes, int offset) {
        return byteCodes[offset] & 0xff;
    }

    private static int readUShort(byte[] byteCodes, int offset) {
        return ((byteCodes[offset] & 0xff) << 8)
                    | (byteCodes[offset + 1] & 0xff);
    }
    private static int readUShortArg(byte[] byteCodes, int offsetInstruction) {
        return readUShort(byteCodes, offsetInstruction + 1);
    }

    private static int readShort(byte[] byteCodes, int offset) {
        int signed = byteCodes[offset];
        byte b0 = (byte)signed;
        return (b0 << 8) | (byteCodes[offset + 1] & 0xff);
    }
    private static int readShortArg(byte[] byteCodes, int offsetInstruction) {
        return readShort(byteCodes, offsetInstruction + 1);
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
    
    private static String mangleMethodName(String name) {
        StringBuilder sb = new StringBuilder(name.length() * 2);
        int last = name.length();
        for (int i = 0; i < last; i++) {
            final char ch = name.charAt(i);
            switch (ch) {
                case '_': sb.append("_1"); break;
                default: sb.append(ch); break;
            }
        }
        return sb.toString();
    }
    private static String mangleSig(String txt, int first, int last) {
        StringBuilder sb = new StringBuilder((last - first) * 2);
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
    
    private static String mangleClassName(String name) {
        return mangleSig(name);
    }

    private static String findMethodName(MethodData m, StringBuilder cnt) {
        StringBuilder name = new StringBuilder();
        if ("<init>".equals(m.getName())) { // NOI18N
            name.append("cons"); // NOI18N
        } else if ("<clinit>".equals(m.getName())) { // NOI18N
            name.append("class"); // NOI18N
        } else {
            name.append(mangleMethodName(m.getName()));
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
            name.append(mangleMethodName(nm));
        }
        countArgs(descr, returnType, name, cnt);
        return name.toString();
    }

    private int invokeStaticMethod(byte[] byteCodes, int i, final StackMapper mapper, boolean isStatic)
    throws IOException {
        int methodIndex = readUShortArg(byteCodes, i);
        String[] mi = jc.getFieldInfoName(methodIndex);
        char[] returnType = { 'V' };
        StringBuilder cnt = new StringBuilder();
        String mn = findMethodName(mi, cnt, returnType);

        final int numArguments = isStatic ? cnt.length() : cnt.length() + 1;
        final CharSequence[] vars = new CharSequence[numArguments];

        for (int j = numArguments - 1; j >= 0; --j) {
            vars[j] = mapper.popValue();
        }

        if (returnType[0] != 'V') {
            out.append("var ")
               .append(mapper.pushT(VarType.fromFieldType(returnType[0])))
               .append(" = ");
        }

        final String in = mi[0];
        out.append(accessClass(mangleClassName(in)));
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
        int methodIndex = readUShortArg(byteCodes, i);
        String[] mi = jc.getFieldInfoName(methodIndex);
        char[] returnType = { 'V' };
        StringBuilder cnt = new StringBuilder();
        String mn = findMethodName(mi, cnt, returnType);

        final int numArguments = cnt.length() + 1;
        final CharSequence[] vars = new CharSequence[numArguments];

        for (int j = numArguments - 1; j >= 0; --j) {
            vars[j] = mapper.popValue();
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
                s = accessClass(mangleClassName(s)) + "(false).constructor.$class";
            }
        }
        return s;
    }

    private String javaScriptBody(String destObject, MethodData m, boolean isStatic) throws IOException {
        byte[] arr = m.findAnnotationData(true);
        if (arr == null) {
            return null;
        }
        final String jvmType = "Lorg/apidesign/bck2brwsr/core/JavaScriptBody;";
        final String htmlType = "Lnet/java/html/js/JavaScriptBody;";
        class P extends AnnotationParser {
            public P() {
                super(false, true);
            }
            
            int cnt;
            String[] args = new String[30];
            String body;
            boolean javacall;
            boolean html4j;
            
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
                if (type.equals(htmlType)) {
                    html4j = true;
                    if ("body".equals(attr)) {
                        body = value;
                    } else if ("args".equals(attr)) {
                        args[cnt++] = value;
                    } else if ("javacall".equals(attr)) {
                        javacall = "1".equals(value);
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
        out.append(destObject).append(".").append(mn);
        out.append(" = function(");
        String space = "";
        int index = 0;
        StringBuilder toValue = new StringBuilder();
        for (int i = 0; i < cnt.length(); i++) {
            out.append(space);
            space = outputArg(out, p.args, index);
            if (p.html4j && space.length() > 0) {
                toValue.append("\n  ").append(p.args[index]).append(" = vm.org_apidesign_bck2brwsr_emul_lang_System(false).toJS(").
                    append(p.args[index]).append(");");
            }
            index++;
        }
        out.append(") {").append("\n");
        out.append(toValue.toString());
        if (p.javacall) {
            int lastSlash = jc.getClassName().lastIndexOf('/');
            final String pkg = jc.getClassName().substring(0, lastSlash);
            out.append(mangleCallbacks(pkg, p.body));
            requireReference(pkg + "/$JsCallbacks$");
        } else {
            out.append(p.body);
        }
        out.append("\n}\n");
        return mn;
    }
    
    private static CharSequence mangleCallbacks(String pkgName, String body) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (;;) {
            int next = body.indexOf(".@", pos);
            if (next == -1) {
                sb.append(body.substring(pos));
                body = sb.toString();
                break;
            }
            int ident = next;
            while (ident > 0) {
                if (!Character.isJavaIdentifierPart(body.charAt(--ident))) {
                    ident++;
                    break;
                }
            }
            String refId = body.substring(ident, next);

            sb.append(body.substring(pos, ident));

            int sigBeg = body.indexOf('(', next);
            int sigEnd = body.indexOf(')', sigBeg);
            int colon4 = body.indexOf("::", next);
            if (sigBeg == -1 || sigEnd == -1 || colon4 == -1) {
                throw new IllegalStateException("Malformed body " + body);
            }
            String fqn = body.substring(next + 2, colon4);
            String method = body.substring(colon4 + 2, sigBeg);
            String params = body.substring(sigBeg, sigEnd + 1);

            int paramBeg = body.indexOf('(', sigEnd + 1);
            
            sb.append("vm.").append(pkgName.replace('/', '_')).append("_$JsCallbacks$(false)._VM().");
            sb.append(mangleJsCallbacks(fqn, method, params, false));
            sb.append("(").append(refId);
            if (body.charAt(paramBeg + 1) != ')') {
                sb.append(",");
            }
            pos = paramBeg + 1;
        }
        sb = null;
        pos = 0;
        for (;;) {
            int next = body.indexOf("@", pos);
            if (next == -1) {
                if (sb == null) {
                    return body;
                }
                sb.append(body.substring(pos));
                return sb;
            }
            if (sb == null) {
                sb = new StringBuilder();
            }

            sb.append(body.substring(pos, next));

            int sigBeg = body.indexOf('(', next);
            int sigEnd = body.indexOf(')', sigBeg);
            int colon4 = body.indexOf("::", next);
            if (sigBeg == -1 || sigEnd == -1 || colon4 == -1) {
                throw new IllegalStateException("Malformed body " + body);
            }
            String fqn = body.substring(next + 1, colon4);
            String method = body.substring(colon4 + 2, sigBeg);
            String params = body.substring(sigBeg, sigEnd + 1);

            int paramBeg = body.indexOf('(', sigEnd + 1);
            
            sb.append("vm.").append(pkgName.replace('/', '_')).append("_$JsCallbacks$(false)._VM().");
            sb.append(mangleJsCallbacks(fqn, method, params, true));
            sb.append("(");
            pos = paramBeg + 1;
        }
    }

    static String mangleJsCallbacks(String fqn, String method, String params, boolean isStatic) {
        if (params.startsWith("(")) {
            params = params.substring(1);
        }
        if (params.endsWith(")")) {
            params = params.substring(0, params.length() - 1);
        }
        StringBuilder sb = new StringBuilder();
        final String fqnu = fqn.replace('.', '_');
        final String rfqn = mangleClassName(fqnu);
        final String rm = mangleMethodName(method);
        final String srp;
        {
            StringBuilder pb = new StringBuilder();
            int len = params.length();
            int indx = 0;
            while (indx < len) {
                char ch = params.charAt(indx);
                if (ch == '[' || ch == 'L') {
                    pb.append("Ljava/lang/Object;");
                    indx = params.indexOf(';', indx) + 1;
                } else {
                    pb.append(ch);
                    indx++;
                }
            }
            srp = mangleSig(pb.toString());
        }
        final String rp = mangleSig(params);
        final String mrp = mangleMethodName(rp);
        sb.append(rfqn).append("$").append(rm).
            append('$').append(mrp).append("__Ljava_lang_Object_2");
        if (!isStatic) {
            sb.append('L').append(fqnu).append("_2");
        }
        sb.append(srp);
        return sb.toString();
    }

    private static String className(ClassData jc) {
        //return jc.getName().getInternalName().replace('/', '_');
        return mangleClassName(jc.getClassName());
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
                
                out.append(accessClass(mangleClassName(slashType)))
                   .append("(false).constructor.fld_").append(value);
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

    final void emit(
        StackMapper sm, 
        final Appendable out, 
        final String format, final CharSequence... params
    ) throws IOException {
        sm.flush(out);
        emitImpl(out, format, params);
    }
    static void emitImpl(final Appendable out,
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

    private void generateCatch(TrapData[] traps, int current, int topMostLabel) throws IOException {
        out.append("} catch (e) {\n");
        int finallyPC = -1;
        for (TrapData e : traps) {
            if (e == null) {
                break;
            }
            if (e.catch_cpx != 0) { //not finally
                final String classInternalName = jc.getClassName(e.catch_cpx);
                addReference(classInternalName);
                out.append("e = vm.java_lang_Throwable(false).bck2BrwsrCnvrt(e);");
                out.append("if (e.$instOf_" + classInternalName.replace('/', '_') + ") {");
                out.append("var stA0 = e;");
                goTo(out, current, e.handler_pc, topMostLabel);
                out.append("}\n");
            } else {
                finallyPC = e.handler_pc;
            }
        }
        if (finallyPC == -1) {
            out.append("throw e;");
        } else {
            out.append("var stA0 = e;");
            goTo(out, current, finallyPC, topMostLabel);
        }
        out.append("\n}");
    }

    private static void goTo(Appendable out, int current, int to, int canBack) throws IOException {
        if (to < current) {
            if (canBack < to) {
                out.append("{ gt = 0; continue X_" + to + "; }");
            } else {
                out.append("{ gt = " + to + "; continue X_0; }");
            }
        } else {
            out.append("{ gt = " + to + "; break IF; }");
        }
    }

    private static void emitIf(
        StackMapper sm, 
        Appendable out, String pattern, 
        CharSequence param, 
        int current, int to, int canBack
    ) throws IOException {
        sm.flush(out);
        emitImpl(out, pattern, param);
        goTo(out, current, to, canBack);
    }

    private void generateNewArray(int atype, final StackMapper smapper) throws IOException, IllegalStateException {
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
        emit(smapper, out, "var @2 = Array.prototype.newArray__Ljava_lang_Object_2ZLjava_lang_String_2I(true, '@3', @1);",
             smapper.popI(), smapper.pushA(), jvmType);
    }

    private void generateANewArray(int type, final StackMapper smapper) throws IOException {
        String typeName = jc.getClassName(type);
        if (typeName.startsWith("[")) {
            typeName = "[" + typeName;
        } else {
            typeName = "[L" + typeName + ";";
        }
        emit(smapper, out, "var @2 = Array.prototype.newArray__Ljava_lang_Object_2ZLjava_lang_String_2I(false, '@3', @1);",
             smapper.popI(), smapper.pushA(), typeName);
    }

    private int generateMultiANewArray(int type, final byte[] byteCodes, int i, final StackMapper smapper) throws IOException {
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
        emit(smapper, out, "var @2 = Array.prototype.multiNewArray__Ljava_lang_Object_2Ljava_lang_String_2_3II('@3', @1, 0);",
             dims.toString(), smapper.pushA(), typeName);
        return i;
    }

    private int generateTableSwitch(int i, final byte[] byteCodes, final StackMapper smapper, int topMostLabel) throws IOException {
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
            out.append("  case " + low).append(":"); goTo(out, i, offset, topMostLabel); out.append('\n');
            low++;
        }
        out.append("  default: ");
        goTo(out, i, dflt, topMostLabel);
        out.append("\n}");
        i = table - 1;
        return i;
    }

    private int generateLookupSwitch(int i, final byte[] byteCodes, final StackMapper smapper, int topMostLabel) throws IOException {
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
            out.append("  case " + cnstnt).append(": "); goTo(out, i, offset, topMostLabel); out.append('\n');
        }
        out.append("  default: ");
        goTo(out, i, dflt, topMostLabel);
        out.append("\n}");
        i = table - 1;
        return i;
    }

    private void generateInstanceOf(int indx, final StackMapper smapper) throws IOException {
        final String type = jc.getClassName(indx);
        if (!type.startsWith("[")) {
            emit(smapper, out, "var @2 = @1 != null && @1.$instOf_@3 ? 1 : 0;",
                 smapper.popA(), smapper.pushI(),
                 type.replace('/', '_'));
        } else {
            emit(smapper, out, "var @2 = vm.java_lang_Class(false).forName__Ljava_lang_Class_2Ljava_lang_String_2('@3').isInstance__ZLjava_lang_Object_2(@1);",
                smapper.popA(), smapper.pushI(),
                type
            );
        }
    }

    private void generateCheckcast(int indx, final StackMapper smapper) throws IOException {
        final String type = jc.getClassName(indx);
        if (!type.startsWith("[")) {
            emit(smapper, out,
                 "if (@1 !== null && !@1.$instOf_@2) throw vm.java_lang_ClassCastException(true);",
                 smapper.getA(0), type.replace('/', '_'));
        } else {
            emit(smapper, out, "vm.java_lang_Class(false).forName__Ljava_lang_Class_2Ljava_lang_String_2('@2').cast__Ljava_lang_Object_2Ljava_lang_Object_2(@1);",
                 smapper.getA(0), type
            );
        }
    }

    private void generateByteCodeComment(int prev, int i, final byte[] byteCodes) throws IOException {
        for (int j = prev; j <= i; j++) {
            out.append(" ");
            final int cc = readUByte(byteCodes, j);
            out.append(Integer.toString(cc));
        }
    }
}
