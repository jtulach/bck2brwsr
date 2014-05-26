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
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;
import org.apidesign.vm4brwsr.ByteCodeParser.FieldData;
import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;

/** Generator of JavaScript from bytecode of classes on classpath of the VM.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
abstract class VM extends ByteCodeToJavaScript {
    protected final ClassDataCache classDataCache;

    private final Bck2Brwsr.Resources resources;
    private final ExportedSymbols exportedSymbols;
    private final StringArray invokerMethods;
    private final StringArray asBinary;

    private VM(
        Appendable out, Bck2Brwsr.Resources resources, 
        StringArray explicitlyExported, StringArray asBinary
    ) {
        super(out);
        this.resources = resources;
        this.classDataCache = new ClassDataCache(resources);
        this.exportedSymbols = new ExportedSymbols(resources, explicitlyExported);
        this.invokerMethods = new StringArray();
        this.asBinary = asBinary;
    }

    static {
        // uses VMLazy to load dynamic classes
        boolean assertsOn = false;
        assert assertsOn = true;
        if (assertsOn) {
            VMLazy.init();
            ClassPath.init();
        }
    }

    @Override
    boolean debug(String msg) throws IOException {
        return false;
    }
    
    static void compile(Appendable out, 
        Bck2Brwsr config
    ) throws IOException {
        String[] both = config.classes().toArray();
        
        final StringArray fixedNames = new StringArray();
        fixedNames.add(Class.class.getName().replace('.', '/'));
        fixedNames.add(ArithmeticException.class.getName().replace('.', '/'));
        
        VM vm;
        if (config.isExtension()) {
            fixedNames.add(VM.class.getName().replace('.', '/'));
            vm = new Extension(out, 
                config.getResources(), both, config.exported(),
                config.allResources()
            );
        } else {
            if (config.includeVM()) {
                fixedNames.add(VM.class.getName().replace('.', '/'));
            }
            vm = new Standalone(out, 
                config.getResources(), config.exported(),
                config.allResources()
            );
        }            
        vm.doCompile(fixedNames.addAndNew(both));
    }

    private void doCompile(StringArray names) throws IOException {
        generatePrologue();
        append(
                "\n  var invoker = {};");
        generateBody(names);
        for (String invokerMethod: invokerMethods.toArray()) {
            append("\n  invoker." + invokerMethod + " = function(target) {"
                + "\n    return function() {"
                + "\n      return target['" + invokerMethod + "'].apply(target, arguments);"
                + "\n    };"
                + "\n  };"
            );
        }
        
        for (String r : asBinary.toArray()) {
            append("\n  ").append(getExportsObject()).append("['registerResource']('");
            append(r).append("', '");
            InputStream is = this.resources.get(r);
            byte[] arr = new byte[is.available()];
            int offset = 0;
            for (;;) {
                if (offset == arr.length) {
                    byte[] tmp = new byte[arr.length * 2];
                    System.arraycopy(arr, 0, tmp, 0, arr.length);
                    arr = tmp;
                }
                int len = is.read(arr, offset, arr.length - offset);
                if (len == -1) {
                    break;
                }
                offset += len;
            }
            if (offset != arr.length) {
                byte[] tmp = new byte[offset];
                System.arraycopy(arr, 0, tmp, 0, offset);
                arr = tmp;
            }
            append(btoa(arr));
            append("');");
        }
        
        append("\n");
        generateEpilogue();
    }

    @JavaScriptBody(args = { "arr" }, body = "return btoa(arr);")
    private static String btoa(byte[] arr) {
        return javax.xml.bind.DatatypeConverter.printBase64Binary(arr);
    }

    protected abstract void generatePrologue() throws IOException;

    protected abstract void generateEpilogue() throws IOException;

    protected abstract String getExportsObject();

    protected abstract boolean isExternalClass(String className);

    @Override
    protected final void declaredClass(ClassData classData, String mangledName)
            throws IOException {
        if (exportedSymbols.isExported(classData)) {
            append("\n").append(getExportsObject()).append("['")
                                               .append(mangledName)
                                               .append("'] = ")
                            .append(accessClass(mangledName))
               .append(";\n");
        }
    }

    protected String generateClass(String className) throws IOException {
        ClassData classData = classDataCache.getClassData(className);
        if (classData == null) {
            throw new IOException("Can't find class " + className);
        }
        return compile(classData);
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
        if (isHierarchyExported(methodData)) {
            exportMember(destObject, mangledName);
        }
    }

    private void exportMember(String destObject, String memberName)
            throws IOException {
        append("\n").append(destObject).append("['")
                                           .append(memberName)
                                           .append("'] = ")
                        .append(destObject).append(".").append(memberName)
           .append(";\n");
    }

    private void generateBody(StringArray names) throws IOException {
        StringArray processed = new StringArray();
        StringArray initCode = new StringArray();
        StringArray skipClass = new StringArray();
        for (String baseClass : names.toArray()) {
            references.add(baseClass);
            for (;;) {
                String name = null;
                for (String n : references.toArray()) {
                    if (skipClass.contains(n)) {
                        continue;
                    }
                    if (processed.contains(n)) {
                        continue;
                    }
                    name = n;
                }
                if (name == null) {
                    break;
                }
                InputStream is = resources.get(name + ".class");
                if (is == null) {
                    lazyReference(this, name);
                    skipClass.add(name);
                    continue;
                }
                try {
                    String ic = generateClass(name);
                    processed.add(name);
                    initCode.add(ic == null ? "" : ic);
                } catch (RuntimeException ex) {
                    throw new IOException("Error while compiling " + name + "\n", ex);
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
                readResource(emul, this);
                asBinary.remove(resource);
            }
            scripts = new StringArray();

            StringArray toInit = StringArray.asList(references.toArray());
            toInit.reverse();

            for (String ic : toInit.toArray()) {
                int indx = processed.indexOf(ic);
                if (indx >= 0) {
                    final String theCode = initCode.toArray()[indx];
                    if (!theCode.isEmpty()) {
                        append(theCode).append("\n");
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

    @Override
    protected String accessField(String object, String mangledName,
                                 String[] fieldInfoName) throws IOException {
        final FieldData field =
                classDataCache.findField(fieldInfoName[0],
                                         fieldInfoName[1],
                                         fieldInfoName[2]);
        return accessNonVirtualMember(object, mangledName,
                                      (field != null) ? field.cls : null);
    }

    @Override
    protected String accessStaticMethod(
                             String object,
                             String mangledName,
                             String[] fieldInfoName) throws IOException {
        final MethodData method =
                classDataCache.findMethod(fieldInfoName[0],
                                          fieldInfoName[1],
                                          fieldInfoName[2]);
        return accessNonVirtualMember(object, mangledName,
                                      (method != null) ? method.cls : null);
    }

    @Override
    protected String accessVirtualMethod(
                             String object,
                             String mangledName,
                             String[] fieldInfoName) throws IOException {
        final ClassData referencedClass =
                classDataCache.getClassData(fieldInfoName[0]);
        final MethodData method =
                classDataCache.findMethod(referencedClass,
                                          fieldInfoName[1],
                                          fieldInfoName[2]);

        if ((method != null)
                && !isExternalClass(method.cls.getClassName())
                && (((method.access & ByteCodeParser.ACC_FINAL) != 0)
                        || ((referencedClass.getAccessFlags()
                                 & ByteCodeParser.ACC_FINAL) != 0)
                        || !isHierarchyExported(method))) {
            return object + "." + mangledName;
        }

        return accessThroughInvoker(object, mangledName);
    }

    private String accessThroughInvoker(String object, String mangledName) {
        if (!invokerMethods.contains(mangledName)) {
            invokerMethods.add(mangledName);
        }
        return "invoker." + mangledName + '(' + object + ')';
    }

    private boolean isHierarchyExported(final MethodData methodData)
            throws IOException {
        if (exportedSymbols.isExported(methodData)) {
            return true;
        }
        if ((methodData.access & (ByteCodeParser.ACC_PRIVATE
                                      | ByteCodeParser.ACC_STATIC)) != 0) {
            return false;
        }

        final ExportedMethodFinder exportedMethodFinder =
                new ExportedMethodFinder(exportedSymbols);

        classDataCache.findMethods(
                methodData.cls,
                methodData.getName(),
                methodData.getInternalSig(),
                exportedMethodFinder);

        return (exportedMethodFinder.getFound() != null);
    }

    private String accessNonVirtualMember(String object,
                                          String mangledName,
                                          ClassData declaringClass) {
        return ((declaringClass != null)
                    && !isExternalClass(declaringClass.getClassName()))
                            ? object + "." + mangledName
                            : object + "['" + mangledName + "']";
    }

    private static final class ExportedMethodFinder
            implements ClassDataCache.TraversalCallback<MethodData> {
        private final ExportedSymbols exportedSymbols;
        private MethodData found;

        public ExportedMethodFinder(final ExportedSymbols exportedSymbols) {
            this.exportedSymbols = exportedSymbols;
        }

        @Override
        public boolean traverse(final MethodData methodData) {
            try {
                if (exportedSymbols.isExported(methodData)) {
                    found = methodData;
                    return false;
                }
            } catch (final IOException e) {
            }

            return true;
        }

        public MethodData getFound() {
            return found;
        }
    }

    private static final class Standalone extends VM {
        private Standalone(Appendable out,
            Bck2Brwsr.Resources resources, 
            StringArray explicitlyExported, StringArray asBinary
        ) {
            super(out, resources, explicitlyExported, asBinary);
        }

        @Override
        protected void generatePrologue() throws IOException {
            append("(function VM(global) {var fillInVMSkeleton = function(vm) {");
        }

        @Override
        protected void generateEpilogue() throws IOException {
            append(
                  "  return vm;\n"
                + "  };\n"
                + "  var extensions = [];\n"
                + "  function replaceAll(s, target, replacement) {\n"
                + "    var pos = 0;\n"
                + "    for (;;) {\n"
                + "      var indx = s.indexOf(target, pos);\n"
                + "      if (indx === -1) {\n"
                + "        return s;\n"
                + "      }\n"
                + "      pos = indx + replacement.length;\n"
                + "      s = s.substring(0, indx) + replacement + s.substring(indx + target.length);\n"
                + "    }\n"
                + "  }\n"
                + "  function mangleClass(name) {\n"
                + "    name = replaceAll(name, '_', '_1');\n"
                + "    name = replaceAll(name, '.', '_');\n"
                + "    return name;\n"
                + "  };\n"
                + "  global.bck2brwsr = function() {\n"
                + "    var args = Array.prototype.slice.apply(arguments);\n"
                + "    var resources = {};\n"
                + "    function loadExtension(url) {\n"
                + "      var xhr = new XMLHttpRequest();\n"
                + "      xhr.open('GET', url, false);\n"
                + "      xhr.send();\n"
                + "      var script = document.createElement('script');\n"
                + "      script.type = 'text/javascript';\n"
                + "      script.text = xhr.responseText;\n"
                + "      document.getElementsByTagName('head')[0].appendChild(script);\n"
                + "    }\n"
                + "    function registerResource(n, a64) {\n"
                + "      var str = atob(a64);\n"
                + "      var arr = [];\n"
                + "      for (var i = 0; i < str.length; i++) {\n"
                + "        var ch = str.charCodeAt(i) & 0xff;\n"
                + "        if (ch > 127) ch -= 256;\n"
                + "        arr.push(ch);\n"
                + "      }\n"
                + "      if (!resources[n]) resources[n] = [arr];\n"
                + "      else resources[n].push(arr);\n"
                + "    }\n"
                + "    var vm = fillInVMSkeleton({ 'registerResource' : registerResource });\n"
                + "    function initVM() {\n"
                + "      var clsArray = vm['java_lang_reflect_Array'];\n"
                + "      if (clsArray) clsArray(false);\n"
                + "    }\n"
                + "    for (var i = 0; i < extensions.length; ++i) {\n"
                + "      extensions[i](vm);\n"
                + "    }\n"
                + "    vm['registerResource'] = null;\n"
                + "    var knownExtensions = extensions.length;\n"
                + "    var loader = {};\n"
                + "    var loadBytes = function(name, skip) {\n"
                + "      skip = typeof skip == 'number' ? skip : 0;\n"
                + "      var arr = resources[name];\n"
                + "      if (arr) {\n"
                + "        var arrSize = arr.length;\n"
                + "        if (skip < arrSize) return arr[skip];\n"
                + "        skip -= arrSize;\n"
                + "      } else {\n"
                + "        var arrSize = 0;\n"
                + "      };\n"
                + "      for (var i = 0; i < args.length; i++) {\n"
                + "        var at = args[i];\n"
                + "        if(!at) continue;\n"
                + "        var ret;\n"
                + "        if (typeof at === 'string' && at.substring(at.length - 3) === '.js') {\n"
                + "          loadExtension(at);\n"
                + "          args[i] = null;\n"
                + "        } else if (typeof at === 'function') ret = at(name, skip);\n"
                + "        else {\n"
                + "          var cp = vm['org_apidesign_vm4brwsr_ClassPath'];\n"
                + "          if (!cp) throw 'Core Java library not registered. Cannot load from ' + at;\n"
                + "          ret = cp(false).\n"
                + "            loadBytes___3BLjava_lang_String_2Ljava_lang_Object_2II(name, args, i, skip);\n"
                + "        }\n"
                + "        if (ret) return ret;\n"
                + "      }\n"
                + "      while (knownExtensions < extensions.length) {\n"
                + "        vm['registerResource'] = registerResource;\n"
                + "        extensions[knownExtensions++](vm);\n"
                + "        vm['registerResource'] = null;\n"
                + "        initVM();\n"
                + "      }\n"
                + "      var arr = resources[name];\n"
                + "      return (arr && arr.length > arrSize) ? arr[arrSize] : null;\n"
                + "    }\n"
                + "    var reload = function(name, arr, keep) {;\n"
                + "      if (!keep) {\n"
                + "        var attr = mangleClass(name);\n"
                + "        delete vm[attr];\n"
                + "      }\n"
                + "      return vm['org_apidesign_vm4brwsr_VMLazy'](false)\n"
                + "        ['load__Ljava_lang_Object_2Ljava_lang_Object_2Ljava_lang_String_2_3Ljava_lang_Object_2_3B']\n"
                + "        (vm, name, args, arr);\n"
                + "    };\n"
                + "    loader.loadClass = function(name) {\n"
                + "      var attr = mangleClass(name);\n"
                + "      var fn = vm[attr];\n"
                + "      if (fn) return fn(false);\n"
                + "      try {\n"
                + "        var arr = loadBytes(replaceAll(name, '.', '/') + '.class');\n"
                + "        return reload(name, arr, true);\n"
                + "      } catch (err) {\n"
                + "        fn = vm[attr];\n"
                + "        if (fn) return fn(false);\n"
                + "        throw err;\n"
                + "      }\n"
                + "    }\n"
                + "    if (vm['loadClass']) {\n"
                + "      throw 'Cannot initialize the bck2brwsr VM twice!';\n"
                + "    }\n"
                + "    vm['loadClass'] = loader.loadClass;\n"
                + "    vm['_reload'] = reload;\n"
                + "    vm['loadBytes'] = loadBytes;\n"
                + "    initVM();\n"
                + "    return loader;\n"
                + "  };\n");
            append(
                  "  global.bck2brwsr.register = function(config, extension) {\n"
                + "    if (!config || config['magic'] !== 'kafíčko') {\n"
                + "      console.log('Will not register: ' + extension);\n"
                + "      return false;\n"
                + "    }\n"
                + "    extensions.push(extension);\n"
                + "    return null;\n"
                + "  };\n");
            append("}(this));");
        }

        @Override
        protected String getExportsObject() {
            return "vm";
        }

        @Override
        protected boolean isExternalClass(String className) {
            return false;
        }
    }

    private static final class Extension extends VM {
        private final StringArray extensionClasses;

        private Extension(Appendable out, Bck2Brwsr.Resources resources,
            String[] extClassesArray, StringArray explicitlyExported,
            StringArray asBinary
        ) {
            super(out, resources, explicitlyExported, asBinary);
            this.extensionClasses = StringArray.asList(extClassesArray);
        }

        @Override
        protected void generatePrologue() throws IOException {
            append("bck2brwsr.register({\n"
                    + "'magic' : 'kafíčko'\n"
                + "}, function(exports) {\n"
                           + "  var vm = {};\n");
            append("  function link(n) {\n"
                + "    return function() {\n"
                + "      var cls = n['replace__Ljava_lang_String_2CC']"
                                       + "('/', '_').toString();\n"
                + "      var dot = n['replace__Ljava_lang_String_2CC']"
                                       + "('/', '.').toString();\n"
                + "      exports.loadClass(dot);\n"
                + "      vm[cls] = exports[cls];\n"
                + "      return vm[cls](arguments);\n"
                + "    };\n"
                + "  };\n"
            );
        }

        @Override
        protected void generateEpilogue() throws IOException {
            append("});");
        }

        @Override
        protected String generateClass(String className) throws IOException {
            if (isExternalClass(className)) {
                append("\n").append(assignClass(
                                            className.replace('/', '_')))
                   .append("link('")
                   .append(className)
                   .append("');");

                return null;
            }

            return super.generateClass(className);
        }

        @Override
        protected String getExportsObject() {
            return "exports";
        }

        @Override
        protected boolean isExternalClass(String className) {
            return !extensionClasses.contains(className);
        }
    }
    
    private static void lazyReference(Appendable out, String n) throws IOException {
        String cls = n.replace('/', '_');
        String dot = n.replace('/', '.');
        
        out.append("\nvm.").append(cls).append(" = function() {");
        out.append("\n  var instance = arguments.length == 0 || arguments[0] === true;");
        out.append("\n  delete vm.").append(cls).append(";");
        out.append("\n  var c = vm.loadClass('").append(dot).append("');");
        out.append("\n  return vm.").append(cls).append("(instance);");
        out.append("\n}");
    }
}
