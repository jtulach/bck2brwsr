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
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;
import org.apidesign.vm4brwsr.ByteCodeParser.FieldData;
import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;

/** Generator of JavaScript from bytecode of classes on classpath of the VM.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
abstract class VM extends ByteCodeToJavaScript {
    protected final Bck2Brwsr.Resources resources;
    private final ExportedSymbols exportedSymbols;

    private VM(Appendable out, Bck2Brwsr.Resources resources) {
        super(out);
        this.resources = resources;
        this.exportedSymbols = new ExportedSymbols(resources);
    }

    static {
        // uses VMLazy to load dynamic classes
        boolean assertsOn = false;
        assert assertsOn = true;
        if (assertsOn) {
            VMLazy.init();
            Zips.init();
        }
    }

    @Override
    boolean debug(String msg) throws IOException {
        return false;
    }
    
    static void compileStandalone(Bck2Brwsr.Resources l, Appendable out, StringArray names) throws IOException {
        VM vm = new Standalone(out, l);
        vm.doCompile(names);
    }

    static void compileExtension(Bck2Brwsr.Resources l, Appendable out, StringArray names) throws IOException {
        VM vm = new Extension(out, l);
        vm.doCompile(names);
    }

    private void doCompile(StringArray names) throws IOException {
        generatePrologue();
        generateBody(names);
        generateEpilogue();
    }

    protected abstract void generatePrologue() throws IOException;

    protected abstract void generateEpilogue() throws IOException;

    protected abstract String generateClass(String className)
            throws IOException;

    protected abstract String getExportsObject();

    @Override
    protected final void declaredClass(ClassData classData, String mangledName)
            throws IOException {
        if (exportedSymbols.isExported(classData)) {
            out.append("\n").append(getExportsObject()).append("['")
                                               .append(mangledName)
                                               .append("'] = ")
                            .append(accessClass(mangledName))
               .append(";\n");
        }
    }

    @Override
    protected void declaredField(FieldData fieldData,
                                 String destObject,
                                 String mangledName) throws IOException {
        if (exportedSymbols.isExported(fieldData)) {
            exportMember(destObject, mangledName);
        }
    }

    @Override
    protected void declaredMethod(MethodData methodData,
                                  String destObject,
                                  String mangledName) throws IOException {
        if (exportedSymbols.isExported(methodData)) {
            exportMember(destObject, mangledName);
        }
    }

    private void exportMember(String destObject, String memberName)
            throws IOException {
        out.append("\n").append(destObject).append("['")
                                           .append(memberName)
                                           .append("'] = ")
                        .append(destObject).append(".").append(memberName)
           .append(";\n");
    }

    private void generateBody(StringArray names) throws IOException {
        StringArray processed = new StringArray();
        StringArray initCode = new StringArray();
        for (String baseClass : names.toArray()) {
            references.add(baseClass);
            for (;;) {
                String name = null;
                for (String n : references.toArray()) {
                    if (processed.contains(n)) {
                        continue;
                    }
                    name = n;
                }
                if (name == null) {
                    break;
                }

                try {
                    String ic = generateClass(name);
                    processed.add(name);
                    initCode.add(ic == null ? "" : ic);
                } catch (RuntimeException ex) {
                    if (out instanceof CharSequence) {
                        CharSequence seq = (CharSequence)out;
                        int lastBlock = seq.length();
                        while (lastBlock-- > 0) {
                            if (seq.charAt(lastBlock) == '{') {
                                break;
                            }
                        }
                        throw new IOException("Error while compiling " + name + "\n"
                            + seq.subSequence(lastBlock + 1, seq.length()), ex
                        );
                    } else {
                        throw new IOException("Error while compiling " + name + "\n"
                            + out, ex
                        );
                    }
                }
            }

            for (String resource : scripts.toArray()) {
                while (resource.startsWith("/")) {
                    resource = resource.substring(1);
                }
                InputStream emul = resources.get(resource);
                if (emul == null) {
                    throw new IOException("Can't find " + resource);
                }
                readResource(emul, out);
            }
            scripts = new StringArray();

            StringArray toInit = StringArray.asList(references.toArray());
            toInit.reverse();

            for (String ic : toInit.toArray()) {
                int indx = processed.indexOf(ic);
                if (indx >= 0) {
                    final String theCode = initCode.toArray()[indx];
                    if (!theCode.isEmpty()) {
                        out.append(theCode).append("\n");
                    }
                    initCode.toArray()[indx] = "";
                }
            }
        }
    }

    private static void readResource(InputStream emul, Appendable out) throws IOException {
        try {
            int state = 0;
            for (;;) {
                int ch = emul.read();
                if (ch == -1) {
                    break;
                }
                if (ch < 0 || ch > 255) {
                    throw new IOException("Invalid char in emulation " + ch);
                }
                switch (state) {
                    case 0: 
                        if (ch == '/') {
                            state = 1;
                        } else {
                            out.append((char)ch);
                        }
                        break;
                    case 1:
                        if (ch == '*') {
                            state = 2;
                        } else {
                            out.append('/').append((char)ch);
                            state = 0;
                        }
                        break;
                    case 2:
                        if (ch == '*') {
                            state = 3;
                        }
                        break;
                    case 3:
                        if (ch == '/') {
                            state = 0;
                        } else {
                            state = 2;
                        }
                        break;
                }
            }
        } finally {
            emul.close();
        }
    }

    private static InputStream loadClass(Bck2Brwsr.Resources l, String name) throws IOException {
        return l.get(name + ".class"); // NOI18N
    }

    static String toString(String name) throws IOException {
        StringBuilder sb = new StringBuilder();
//        compile(sb, name);
        return sb.toString().toString();
    }

    private StringArray scripts = new StringArray();
    private StringArray references = new StringArray();
    
    @Override
    protected boolean requireReference(String cn) {
        if (references.contains(cn)) {
            return false;
        }
        references.add(cn);
        return true;
    }

    @Override
    protected void requireScript(String resourcePath) {
        scripts.add(resourcePath);
    }

    @Override
    String assignClass(String className) {
        return "vm." + className + " = ";
    }
    
    @Override
    String accessClass(String className) {
        return "vm." + className;
    }

    private static final class Standalone extends VM {
        private Standalone(Appendable out, Bck2Brwsr.Resources resources) {
            super(out, resources);
        }

        @Override
        protected void generatePrologue() throws IOException {
            out.append("(function VM(global) {var fillInVMSkeleton = function(vm) {");
        }

        @Override
        protected void generateEpilogue() throws IOException {
            out.append(
                  "  return vm;\n"
                + "  };\n"
                + "  var extensions = [];\n"
                + "  global.bck2brwsr = function() {\n"
                + "    var args = Array.prototype.slice.apply(arguments);\n"
                + "    var vm = fillInVMSkeleton({});\n"
                + "    for (var i = 0; i < extensions.length; ++i) {\n"
                + "      extensions[i](vm);\n"
                + "    }\n"
                + "    var loader = {};\n"
                + "    loader.vm = vm;\n"
                + "    loader.loadClass = function(name) {\n"
                + "      var attr = name.replace__Ljava_lang_String_2CC('.','_');\n"
                + "      var fn = vm[attr];\n"
                + "      if (fn) return fn(false);\n"
                + "      return vm.org_apidesign_vm4brwsr_VMLazy(false).\n"
                + "        load__Ljava_lang_Object_2Ljava_lang_Object_2Ljava_lang_String_2_3Ljava_lang_Object_2(loader, name, args);\n"
                + "    }\n"
                + "    if (vm.loadClass) {\n"
                + "      throw 'Cannot initialize the bck2brwsr VM twice!';\n"
                + "    }\n"
                + "    vm.loadClass = loader.loadClass;\n"
                + "    vm.loadBytes = function(name) {\n"
                + "      return vm.org_apidesign_vm4brwsr_VMLazy(false).\n"
                + "        loadBytes___3BLjava_lang_Object_2Ljava_lang_String_2_3Ljava_lang_Object_2(loader, name, args);\n"
                + "    }\n"
                + "    vm.java_lang_reflect_Array(false);\n"
                + "    vm.org_apidesign_vm4brwsr_VMLazy(false).\n"
                + "      loadBytes___3BLjava_lang_Object_2Ljava_lang_String_2_3Ljava_lang_Object_2(loader, null, args);\n"
                + "    return loader;\n"
                + "  };\n");
            out.append(
                  "  global.bck2brwsr.registerExtension"
                             + " = function(extension) {\n"
                + "    extensions.push(extension);\n"
                + "  };\n");
            out.append("}(this));");
        }

        @Override
        protected String generateClass(String className) throws IOException {
            InputStream is = loadClass(resources, className);
            if (is == null) {
                throw new IOException("Can't find class " + className);
            }
            return compile(is);
        }

        @Override
        protected String getExportsObject() {
            return "vm";
        }
    }

    private static final class Extension extends VM {
        private Extension(Appendable out, Bck2Brwsr.Resources resources) {
            super(out, resources);
        }

        @Override
        protected void generatePrologue() throws IOException {
            out.append("bck2brwsr.registerExtension(function(exports) {\n"
                           + "  var vm = {};\n");
            out.append("  function link(n, inst) {\n"
                           + "    var cls = n.replace__Ljava_lang_String_2CC("
                                                  + "'/', '_').toString();\n"
                           + "    var dot = n.replace__Ljava_lang_String_2CC("
                                                  + "'/', '.').toString();\n"
                           + "    exports.loadClass(dot);\n"
                           + "    vm[cls] = exports[cls];\n"
                           + "    return vm[cls](inst);\n"
                           + "  };\n");
        }

        @Override
        protected void generateEpilogue() throws IOException {
            out.append("});");
        }

        @Override
        protected String generateClass(String className) throws IOException {
            InputStream is = loadClass(resources, className);
            if (is == null) {
                out.append("\n").append(assignClass(
                                            className.replace('/', '_')))
                   .append("function() {\n  return link('")
                   .append(className)
                   .append("', arguments.length == 0 || arguments[0] === true);"
                               + "\n};");

                return null;
            }

            return compile(is);
        }

        @Override
        protected String getExportsObject() {
            return "exports";
        }
    }
}
