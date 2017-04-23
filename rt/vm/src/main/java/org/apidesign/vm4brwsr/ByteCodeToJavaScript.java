/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import static org.apidesign.vm4brwsr.ByteCodeParser.*;
import org.apidesign.vm4brwsr.ByteCodeParser.AnnotationParser;
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;
import org.apidesign.vm4brwsr.ByteCodeParser.FieldData;
import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;
import org.apidesign.vm4brwsr.ByteCodeParser.StackMapIterator;
import org.apidesign.vm4brwsr.ByteCodeParser.TrapData;
import org.apidesign.vm4brwsr.ByteCodeParser.TrapDataIterator;

/** Translator of the code inside class files to JavaScript.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
abstract class ByteCodeToJavaScript {
    private ClassData jc;
    private final StringArray classRefs = new StringArray();
    private final NumberOperations numbers = new NumberOperations();
    private final Appendable output;
    private boolean callbacks;

    protected ByteCodeToJavaScript(final Appendable out) {
        this.output = out;
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
    
    protected abstract void requireResource(Appendable out, String resourcePath) throws IOException;
    
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
    
    final String accessClassFalse(String classOperation) {
        if (mangleClassName(jc.getClassName()).equals(classOperation)) {
            return "c";
        }
        classRefs.addIfMissing(classOperation);
        return "(refs_" + classOperation + " || (refs_" + classOperation + " = " + accessClass(classOperation) + "(false)))";
    }

    protected FieldData findField(String[] fieldInfoName) throws IOException {
        return null;
    }

    protected String accessField(String object, FieldData data, String[] fieldInfoName)
    throws IOException {
        String mangledName = "_" + fieldInfoName[1];
        return object + "." + mangledName;
    }

    protected String accessStaticMethod(
                             String object,
                             String mangledName,
                             String[] fieldInfoName) throws IOException {
        return object + "." + mangledName;
    }

    protected String accessVirtualMethod(
            String object, 
            String mangledName, 
            String[] fieldInfoName, 
            int params
    ) throws IOException {
        return object + "." + mangledName + '(';
    }

    protected void declareClass(Appendable out, ClassData classData, String mangledName)
    throws IOException {
        out.append(mangledName);
    }

    protected void declaredField(Appendable out, FieldData fieldData, String destObject, String mangledName) throws IOException {
    }

    protected void declaredMethod(Appendable out, MethodData methodData, String destObject, String mangledName) throws IOException {
    }

    /** Prints out a debug message. 
     * 
     * @param msg the message
     * @return true if the message has been printed
     * @throws IOException 
     */
    boolean debug(Appendable out, String msg) throws IOException {
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
        return compile(new ClassData(classFile));
    }

    protected String compile(ClassData classData) throws IOException {
        this.jc = classData;
        final String cn = this.jc.getClassName();
        try {
            return compileImpl(this.output, cn);
        } catch (IOException ex) {
            throw new IOException("Cannot compile " + cn + ":", ex);
        }
    }

    private String compileImpl(Appendable out, final String cn) throws IOException {
        this.numbers.reset();
        this.callbacks = cn.endsWith("/$JsCallbacks$");
        if (jc.getMajor_version() < 50 && !cn.endsWith("/package-info")) {
            throw new IOException("Can't compile " + cn + ". Class file version " + jc.getMajor_version() + "."
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
        final String jsResource;
        {
            String[] arr = findAnnotation(arrData, jc,
                "net.java.html.js.JavaScriptResource", 
                "value"
            );
            if (arr != null) {
                if (arr[0].startsWith("/")) {
                    jsResource = arr[0];
                } else {
                    int last = cn.lastIndexOf('/');
                    jsResource = cn.substring(0, last + 1).replace('.', '/') + arr[0];
                }
            } else {
                jsResource = null;
            }
        }
        String[] proto = findAnnotation(arrData, jc,
            "org.apidesign.bck2brwsr.core.JavaScriptPrototype", 
            "container", "prototype"
        );
        StringArray toInitilize = new StringArray();
        final String className = className(jc);
        out.append("\n\n");
        out.append("function ").append(className).append("() {");
        out.append("\n  var m;");
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
                if ((v.access & ACC_FINAL) != 0 && v.hasConstantValue()) {
                    if (v.getInternalSig().length() == 1 || v.getInternalSig().equals("Ljava/lang/String;")) {
                        continue;
                    }
                }
                out.append("\n  CLS['fld_").append(v.getName()).append("']").append(initField(v));
                out.append("\n  m = c._").append(v.getName()).append(" = function (v) {")
                    .append("  if (arguments.length == 1) CLS['fld_").append(v.getName())
                    .append("'] = v; return CLS['fld_")
                    .append(v.getName()).append("']; };");
            } else {
                out.append("\n  m = c._").append(v.getName()).append(" = function (v) {")
                    .append("  if (arguments.length == 1) this['fld_")
                    .append(className).append('_').append(v.getName())
                    .append("'] = v; return this['fld_")
                    .append(className).append('_').append(v.getName())
                    .append("']; };");
            }

            declaredField(out, v, "c", "_" + v.getName());
        }
        for (MethodData m : jc.getMethods()) {
            byte[] onlyArr = m.findAnnotationData(true);
            if (javaScriptOnly(out, onlyArr)) continue;
            String destObject;
            String mn;
            out.append("\n    ");
            if (m.isStatic()) {
                destObject = "c";
                mn = generateStaticMethod(out, destObject, m, toInitilize);
            } else {
                if (m.isConstructor()) {
                    destObject = "CLS";
                    mn = generateInstanceMethod(out, destObject, m);
                } else {
                    destObject = "c";
                    mn = generateInstanceMethod(out, destObject, m);
                }
            }
            declaredMethod(out, m, destObject, mn);
            byte[] runAnno = m.findAnnotationData(false);
            if (runAnno != null) {
                out.append("\n    m.anno = {");
                AnnotationParser ap = new GenerateAnno(out, true, false);
                ap.parse(runAnno, jc);
                out.append("\n    };");
            }
            out.append("\n    m.access = " + m.getAccess()).append(";");
            out.append("\n    m.cls = CLS;");
        }
        out.append(numbers.generate());
        out.append("\n    c.constructor = CLS;");
        out.append("\n    function ").append(className).append("fillInstOf(x) {");
        String instOfName = "$instOf_" + className;
        out.append("\n        Object.defineProperty(x, '").append(instOfName).append("', { value : true });");
        if (jc.isInterface()) {
            for (MethodData m : jc.getMethods()) {
                if ((m.getAccess() & ACC_ABSTRACT) == 0
                    && (m.getAccess() & ACC_STATIC) == 0
                    && (m.getAccess() & ACC_PRIVATE) == 0) {
                    final String mn = findMethodName(m, new StringBuilder());
                    out.append("\n        if (!x['").append(mn).append("']) Object.defineProperty(x, '").append(mn).append("', { value : c['").append(mn).append("']});");
                }
            }
        }
        for (String superInterface : jc.getSuperInterfaces()) {
            String intrfc = mangleClassName(superInterface);
            out.append("\n      vm.").append(intrfc).append("(false)['fillInstOf'](x);");
            requireReference(superInterface);
        }
        out.append("\n    }");
        out.append("\n    if (!c.hasOwnProperty('fillInstOf')) Object.defineProperty(c, 'fillInstOf', { value: ").append(className).append("fillInstOf });");
        out.append("\n    ").append(className).append("fillInstOf(c);");
//        obfuscationDelegate.exportJSProperty(this, "c", instOfName);
        out.append("\n    CLS.$class = 'temp';");
        out.append("\n    CLS.$class = ");
        out.append(accessClass("java_lang_Class")).append("(true);");
        out.append("\n    CLS.$class.jvmName = '").append(cn).append("';");
        out.append("\n    CLS.$class.superclass = sprcls;");
        out.append("\n    CLS.$class.interfaces = function() { return [");
        {
            boolean first = true;
            for (String intrfc : jc.getSuperInterfaces()) {
                if (!first) {
                    out.append(",");
                }
                requireReference(intrfc);
                String mangledIface = mangleClassName(intrfc);
                out.append("\n        ");
                out.append(accessClass(mangledIface)).append("(false).constructor.$class");
                first = false;
            }
        }
        out.append("\n    ]; };");
        int flags = jc.getAccessFlags();
        if (jc.hasEnclosingMethod()) {
            flags |= 0x10000;
        }
        out.append("\n    CLS.$class.access = ").append(flags+";");
        out.append("\n    CLS.$class.cnstr = CLS;");
        byte[] classAnno = jc.findAnnotationData(false);
        if (classAnno != null) {
            out.append("\n    CLS.$class.anno = {");
            AnnotationParser ap = new GenerateAnno(out, true, false);
            ap.parse(classAnno, jc);
            out.append("\n    };");
        }
        for (String init : toInitilize.toArray()) {
            out.append("\n    ").append(init).append("();");
        }
        for (String ref : classRefs.toArray()) {
            out.append("\n    var refs_").append(ref).append(";");
        }
        classRefs.clear();
        
        if (jsResource != null) {
            requireResource(out, jsResource);
        }
        
        out.append("\n  }");
        out.append("\n  if (arguments.length === 0) {");
        out.append("\n    if (!(this instanceof CLS)) {");
        out.append("\n      return new CLS();");
        out.append("\n    }");
        for (FieldData v : jc.getFields()) {
            byte[] onlyArr = v.findAnnotationData(true);
            if (javaScriptOnly(out, onlyArr)) continue;
            if (!v.isStatic()) {
                out.append("\n    this['fld_").
                    append(className).append('_').
                    append(v.getName()).append("']").append(initField(v));
            }
        }
        out.append("\n    return this;");
        out.append("\n  }");
        out.append("\n  return arguments[0] ? new CLS() : CLS.prototype;");
        out.append("\n};");

        out.append("\n").append(assignClass(className));
        declareClass(out, jc, className);
        out.append(";\n");

//        StringBuilder sb = new StringBuilder();
//        for (String init : toInitilize.toArray()) {
//            sb.append("\n").append(init).append("();");
//        }
        return "";
    }

    private boolean javaScriptOnly(Appendable out, byte[] anno) throws IOException {
        String[] only = findAnnotation(anno, jc,
            "org.apidesign.bck2brwsr.core.JavaScriptOnly",
            "name", "value"
        );
        if (only != null) {
            if (only[0] != null && only[1] != null) {
                out.append("\n    p.").append(only[0]).append(" = ")
                    .append(only[1]).append(";");
            }
            if (ExportedSymbols.isMarkedAsExported(anno, jc)) {
                out.append("\n    p['").append(only[0]).append("'] = p.")
                    .append(only[0]).append(";");
            }
            return true;
        }
        return false;
    }
    private String generateStaticMethod(Appendable out, String destObject, MethodData m, StringArray toInitilize) throws IOException {
        String jsb = javaScriptBody(out, destObject, m, true);
        if (jsb != null) {
            return jsb;
        }
        final String mn = findMethodName(m, new StringBuilder());
        boolean defineProp = generateMethod(out, destObject, mn, m);
        if (mn.equals("class__V")) {
            if (defineProp) {
                toInitilize.add(accessClassFalse(className(jc)) + "['" + mn + "']");
            } else {
                toInitilize.add(accessClassFalse(className(jc)) + "." + mn);
            }
        }
        return mn;
    }

    private String generateInstanceMethod(Appendable out, String destObject, MethodData m) throws IOException {
        String jsb = javaScriptBody(out, destObject, m, false);
        if (jsb != null) {
            return jsb;
        }
        final String mn = findMethodName(m, new StringBuilder());
        generateMethod(out, destObject, mn, m);
        return mn;
    }

    private boolean generateMethod(Appendable out, String destObject, String name, MethodData m)
            throws IOException {
        final StackMapIterator stackMapIterator = m.createStackMapIterator();
        TrapDataIterator trap = m.getTrapDataIterator();
        final LocalsMapper lmapper =
                new LocalsMapper(stackMapIterator.getArguments());

        boolean defineProp = 
            "java/lang/Object".equals(jc.getClassName()) ||
            "java/lang/reflect/Array".equals(jc.getClassName());
        
        if (defineProp) {
            out.append("Object.defineProperty(").append(destObject).
                append(", '").append(name).append("', { configurable: true, writable: true, value: m = function(");
        } else {
            out.append("m = ").append(destObject).append(".").append(name).append(" = function(");
        }
        lmapper.outputArguments(out, m.isStatic());
        out.append(") {").append("\n");

        final byte[] byteCodes = m.getCode();
        if (byteCodes == null) {
            byte[] defaultAttr = m.getDefaultAttribute();
            if (defaultAttr != null) {
                out.append("  return ");
                AnnotationParser ap = new GenerateAnno(out, true, false);
                ap.parseDefault(defaultAttr, jc);
                out.append(";\n");
            } else {
                if (debug(out, "  throw 'no code found for ")) {
                   out.append(jc.getClassName()).append('.')
                   .append(m.getName()).append("';\n");
                }
            }
            if (defineProp) {
                out.append("}});");
            } else {
                out.append("};");
            }
            return defineProp;
        }

        final StackMapper smapper = new StackMapper();

        if (!m.isStatic()) {
            out.append("  var ").append(" lcA0 = this;\n");
        }

        LoopCode loop;
        if (this.callbacks && !name.equals("class__V")) {
            lmapper.outputUndefinedCheck(out);
            loop = new JsCallbackCode(this, out, numbers, jc);
        } else {
            loop = new LoopCode(this, output, numbers, jc);
        }

        loop.loopCode(stackMapIterator, byteCodes, trap, smapper, lmapper);

        if (defineProp) {
            out.append("\n}});");
        } else {
            out.append("\n};");
        }
        return defineProp;
    }

    static int generateIf(Appendable out, StackMapper mapper, byte[] byteCodes,
        int i, final CharSequence v2, final CharSequence v1, 
        final String test, int topMostLabel
    ) throws IOException {
        mapper.flush(out);
        int indx = i + readShortArg(byteCodes, i);
        out.append("if ((").append(v1)
           .append(") ").append(test).append(" (")
           .append(v2).append(")) ");
        goTo(out, i, indx, topMostLabel);
        return i + 2;
    }
    
    int readInt4(byte[] byteCodes, int offset) {
        final int d = byteCodes[offset + 0] << 24;
        final int c = byteCodes[offset + 1] << 16;
        final int b = byteCodes[offset + 2] << 8;
        final int a = byteCodes[offset + 3];
        return (d & 0xff000000) | (c & 0xff0000) | (b & 0xff00) | (a & 0xff);
    }
    static int readUByte(byte[] byteCodes, int offset) {
        return byteCodes[offset] & 0xff;
    }

    static int readUShort(byte[] byteCodes, int offset) {
        return ((byteCodes[offset] & 0xff) << 8)
                    | (byteCodes[offset + 1] & 0xff);
    }
    static int readUShortArg(byte[] byteCodes, int offsetInstruction) {
        return readUShort(byteCodes, offsetInstruction + 1);
    }

    static int readShort(byte[] byteCodes, int offset) {
        int signed = byteCodes[offset];
        byte b0 = (byte)signed;
        return (b0 << 8) | (byteCodes[offset + 1] & 0xff);
    }
    static int readShortArg(byte[] byteCodes, int offsetInstruction) {
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
                default: 
                    if (Character.isJavaIdentifierPart(ch)) {
                        sb.append(ch);
                    } else {
                        sb.append("_0");
                        String hex = Integer.toHexString(ch).toLowerCase();
                        for (int m = hex.length(); m < 4; m++) {
                            sb.append("0");
                        }
                        sb.append(hex);
                    }
                break;
            }
        }
        return sb.toString();
    }
    
    static String mangleClassName(String name) {
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

    void addReference(Appendable out, String cn) throws IOException {
        if (requireReference(cn)) {
            debug(out, " /* needs " + cn + " */");
        }
    }

    void outType(String d, StringBuilder out) {
        while (d.charAt(0) == '[') {
            out.append('A');
            d = d.substring(1);
        }
        if (d.charAt(0) == 'L') {
            assert d.charAt(d.length() - 1) == ';';
            out.append(mangleClassName(d).substring(0, d.length() - 1));
        } else {
            out.append(d);
        }
    }

    String encodeConstant(Appendable out, int entryIndex) throws IOException {
        String[] classRef = { null };
        String s = jc.stringValue(entryIndex, classRef);
        if (classRef[0] != null) {
            if (classRef[0].startsWith("[")) {
                s = accessClass("java_lang_Class") + "(false)['forName__Ljava_lang_Class_2Ljava_lang_String_2']('" + classRef[0] + "')";
            } else {
                addReference(out, classRef[0]);
                s = accessClassFalse(mangleClassName(s)) + ".constructor.$class";
            }
        }
        return s;
    }

    private String javaScriptBody(Appendable out, String destObject, MethodData m, boolean isStatic) throws IOException {
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
                    } else if ("wait4js".equals(attr)) {
                        // ignore, we always invoke synchronously
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
        out.append("m = ").append(destObject).append(".").append(mn);
        out.append(" = function(");
        if (p.html4j) {
            out.append(") {").append("\n");
            if (p.html4j) {
                out.append("  var r = (function(");
            }
        }
        String space = "";
        int index = 0;
        StringBuilder toValue = new StringBuilder();
        for (int i = 0; i < cnt.length(); i++) {
            out.append(space);
            space = outputArg(out, p.args, index);
            if (p.html4j && space.length() > 0) {
                toValue.append("\n  ").append(p.args[index]).append(" = ")
                    .append(accessClass("java_lang_Class")).append("(false).toJS(").
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
        if (p.html4j) {
            out.append("\n}).apply(this, arguments");
            out.append(");\n  return r === undefined ? null : r;\n");
        }
        out.append("\n}\n");
        return mn;
    }
    
    private CharSequence mangleCallbacks(String pkgName, String body) {
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
            int paramEnd = closingParenthesis(body, paramBeg);

            sb.append(accessClass("java_lang_Class")).append("(false).toJS(");
            sb.append("vm.").append(mangleClassName(pkgName)).append("_$JsCallbacks$(false)._VM().");
            sb.append(mangleJsCallbacks(fqn, method, params, false));
            sb.append("(").append(refId);
            if (body.charAt(paramBeg + 1) != ')') {
                sb.append(",");
            }
            sb.append(body.substring(paramBeg + 1, paramEnd));
            sb.append(")");
            pos = paramEnd;
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
            int paramEnd = closingParenthesis(body, paramBeg);

            sb.append(accessClass("java_lang_Class")).append("(false).toJS(");
            sb.append("vm.").append(mangleClassName(pkgName)).append("_$JsCallbacks$(false)._VM().");
            sb.append(mangleJsCallbacks(fqn, method, params, true));
            sb.append("(");
            sb.append(body.substring(paramBeg + 1, paramEnd));
            sb.append(")");
            pos = paramEnd;
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
                    int column = params.indexOf(';', indx) + 1;
                    if (column > indx) {
                        String real = params.substring(indx, column);
                        if ("Ljava/lang/String;".equals(real)) {
                            pb.append("Ljava/lang/String;");
                            indx = column;
                            continue;
                        }
                    }
                    pb.append("Ljava/lang/Object;");
                    indx = column;
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

    final void emitNoFlush(
        Appendable out, StackMapper sm,
        final String format, final CharSequence... params
    ) throws IOException {
        emitImpl(out, format, params);
    }
    static final void emit(
        final Appendable out, StackMapper sm, final String format, final CharSequence... params
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

    void generateCatch(Appendable out, TrapData[] traps, int current, int topMostLabel) throws IOException {
        out.append("} catch (e) {\n");
        int finallyPC = -1;
        for (TrapData e : traps) {
            if (e == null) {
                break;
            }
            if (e.catch_cpx != 0) { //not finally
                final String classInternalName = jc.getClassName(e.catch_cpx);
                addReference(out, classInternalName);
                out.append("e = vm.java_lang_Class(false).bck2BrwsrThrwrbl(e);");
                out.append("if (e['$instOf_" + mangleClassName(classInternalName) + "']) {");
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

    static void goTo(Appendable out, int current, int to, int canBack) throws IOException {
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

    static void emitIf(
        Appendable out, StackMapper sm, String pattern, CharSequence param, int current, int to, int canBack
    ) throws IOException {
        sm.flush(out);
        emitImpl(out, pattern, param);
        goTo(out, current, to, canBack);
    }

    void generateNewArray(Appendable out, int atype, final StackMapper smapper) throws IOException, IllegalStateException {
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
        emit(out, smapper,
            "var @2 = Array.prototype['newArray__Ljava_lang_Object_2ZLjava_lang_String_2Ljava_lang_Object_2I'](true, '@3', null, @1);",
             smapper.popI(), smapper.pushA(), jvmType);
    }

    void generateANewArray(Appendable out, int type, final StackMapper smapper) throws IOException {
        String typeName = jc.getClassName(type);
        String ref = "null";
        if (typeName.startsWith("[")) {
            typeName = "'[" + typeName + "'";
        } else {
            ref = "vm." + mangleClassName(typeName);
            typeName = "'[L" + typeName + ";'";
        }
        emit(out, smapper,
            "var @2 = Array.prototype['newArray__Ljava_lang_Object_2ZLjava_lang_String_2Ljava_lang_Object_2I'](false, @3, @4, @1);",
             smapper.popI(), smapper.pushA(), typeName, ref);
    }

    int generateMultiANewArray(Appendable out, int type, final byte[] byteCodes, int i, final StackMapper smapper) throws IOException {
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
        String fn = "null";
        if (typeName.charAt(dim) == 'L') {
            fn = "vm." + mangleClassName(typeName.substring(dim + 1, typeName.length() - 1));
        }
        emit(out, smapper,
            "var @2 = Array.prototype['multiNewArray__Ljava_lang_Object_2Ljava_lang_String_2_3ILjava_lang_Object_2']('@3', @1, @4);",
             dims.toString(), smapper.pushA(), typeName, fn
        );
        return i;
    }

    int generateTableSwitch(Appendable out, int i, final byte[] byteCodes, final StackMapper smapper, int topMostLabel) throws IOException {
        int table = i / 4 * 4 + 4;
        int dflt = i + readInt4(byteCodes, table);
        table += 4;
        int low = readInt4(byteCodes, table);
        table += 4;
        int high = readInt4(byteCodes, table);
        table += 4;
        final CharSequence swVar = smapper.popValue();
        smapper.flush(out);
        out.append("switch (").append(swVar).append(") {\n");
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

    int generateLookupSwitch(Appendable out, int i, final byte[] byteCodes, final StackMapper smapper, int topMostLabel) throws IOException {
        int table = i / 4 * 4 + 4;
        int dflt = i + readInt4(byteCodes, table);
        table += 4;
        int n = readInt4(byteCodes, table);
        table += 4;
        final CharSequence swVar = smapper.popValue();
        smapper.flush(out);
        out.append("switch (").append(swVar).append(") {\n");
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

    void generateInstanceOf(Appendable out, int indx, final StackMapper smapper) throws IOException {
        String type = jc.getClassName(indx);
        if (!type.startsWith("[")) {
            emit(out, smapper,
                    "var @2 = @1 != null && @1['$instOf_@3'] ? 1 : 0;",
                 smapper.popA(), smapper.pushI(),
                 mangleClassName(type));
        } else {
            int cnt = 0;
            while (type.charAt(cnt) == '[') {
                cnt++;
            }
            if (type.charAt(cnt) == 'L') {
                String component = type.substring(cnt + 1, type.length() - 1);
                requireReference(component);
                type = "vm." + mangleClassName(component);
                emit(out, smapper,
                    "var @2 = Array.prototype['isInstance__ZLjava_lang_Object_2ILjava_lang_Object_2'](@1, @4, @3);",
                    smapper.popA(), smapper.pushI(),
                    type, "" + cnt
                );
            } else {
                emit(out, smapper,
                    "var @2 = Array.prototype['isInstance__ZLjava_lang_Object_2Ljava_lang_String_2'](@1, '@3');",
                    smapper.popA(), smapper.pushI(), type
                );
            }
        }
    }

    void generateCheckcast(Appendable out, int indx, final StackMapper smapper) throws IOException {
        String type = jc.getClassName(indx);
        if (!type.startsWith("[")) {
            emitNoFlush(out, smapper,
                 "if (@1 !== null && !@1['$instOf_@2']) vm.java_lang_Class(false).castEx(@1, '@3');",
                 smapper.getT(0, VarType.REFERENCE, false), mangleClassName(type), type.replace('/', '.'));
        } else {
            int cnt = 0;
            while (type.charAt(cnt) == '[') {
                cnt++;
            }
            if (type.charAt(cnt) == 'L') {
                String component = type.substring(cnt + 1, type.length() - 1);
                requireReference(component);
                type = "vm." + mangleClassName(component);
                emitNoFlush(out, smapper,
                    "if (@1 !== null && !Array.prototype['isInstance__ZLjava_lang_Object_2ILjava_lang_Object_2'](@1, @3, @2)) vm.java_lang_Class(false).castEx(@1, '');",
                     smapper.getT(0, VarType.REFERENCE, false), type, "" + cnt
                );
            } else {
                emitNoFlush(out, smapper,
                    "if (@1 !== null && !Array.prototype['isInstance__ZLjava_lang_Object_2Ljava_lang_String_2'](@1, '@2')) vm.java_lang_Class(false).castEx(@1, '');",
                     smapper.getT(0, VarType.REFERENCE, false), type
                );
            }
        }
    }

    void generateByteCodeComment(Appendable out, int prev, int i, final byte[] byteCodes) throws IOException {
        for (int j = prev; j <= i; j++) {
            out.append(" ");
            final int cc = readUByte(byteCodes, j);
            out.append(Integer.toString(cc));
        }
    }
    
    @JavaScriptBody(args = "msg", body = "")
    static void println(String msg) {
        System.err.println(msg);
    }

    private static int closingParenthesis(String body, int at) {
        int cnt = 0;
        for (;;) {
            switch (body.charAt(at++)) {
                case '(': cnt++; break;
                case ')': cnt--; break;
            }
            if (cnt == 0) {
                return at;
            }
        }
    }

    private class GenerateAnno extends AnnotationParser {
        private final Appendable out;

        public GenerateAnno(Appendable out, boolean textual, boolean iterateArray) {
            super(textual, iterateArray);
            this.out = out;
        }

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
                out.append('"').append(attrName).append("\" : ");
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

            final String cn = mangleClassName(slashType);
            out.append(accessClassFalse(cn))
                .append("['valueOf__L").
                append(cn).
                append("_2Ljava_lang_String_2']('").
                append(value).
                append("')");
        }

        @Override
        protected void visitClassAttr(String annoType, String attr, String className) throws IOException {
            if (className.startsWith("L")) {
                final String slashType = className.substring(1, className.length() - 1);
                requireReference(slashType);

                final String cn = mangleClassName(slashType);
                out.append(accessClassFalse(cn)).append(".constructor.$class");
            } else {
                String primitiveType = null;
                switch (className.charAt(0)) {
                    case 'J':
                        primitiveType = "java_lang_Long";
                        break;
                    case 'I':
                        primitiveType = "java_lang_Integer";
                        break;
                    case 'S':
                        primitiveType = "java_lang_Short";
                        break;
                    case 'B':
                        primitiveType = "java_lang_Byte";
                        break;
                    case 'F':
                        primitiveType = "java_lang_Float";
                        break;
                    case 'D':
                        primitiveType = "java_lang_Double";
                        break;
                    case 'C':
                        primitiveType = "java_lang_Character";
                        break;
                    case 'Z':
                        primitiveType = "java_lang_Boolean";
                        break;
                    case '[':
                        String ac = accessClassFalse("java_lang_Class");
                        String af = accessStaticMethod(ac, "forName__Ljava_lang_Class_2Ljava_lang_String_2", new String[] {
                            "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;"
                        });
                        out.append(af).append("(\"").append(className).append("\")");
                        break;
                    default:
                        out.append(accessClassFalse("java_lang_Object")).append(".constructor.$class");

                }
                if (primitiveType != null) {
                    String ac = accessClassFalse(primitiveType);
                    String af = accessField(ac, null, new String[] { null, "TYPE" });
                    out.append(af).append("()");
                }
            }
        }
    }
}
