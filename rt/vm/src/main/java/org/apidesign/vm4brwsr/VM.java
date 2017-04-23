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
    private final StringBuilder invokerMethods;
    private final StringArray asBinary;
    int exportedCount;

    private VM(
        Appendable out, Bck2Brwsr.Resources resources, 
        StringArray explicitlyExported, StringArray asBinary
    ) {
        super(out);
        this.resources = resources;
        this.classDataCache = new ClassDataCache(resources);
        this.exportedSymbols = new ExportedSymbols(resources, explicitlyExported);
        this.invokerMethods = new StringBuilder();
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
    boolean debug(Appendable out, String msg) throws IOException {
        return false;
    }
    
    static void compile(Appendable out, 
        Bck2Brwsr config
    ) throws IOException {
        String[] both = config.classes().toArray();
        
        final StringArray fixedNames = new StringArray();
        boolean addThree = false;
        
        VM vm;
        if (config.isExtension()) {
            fixedNames.add(VM.class.getName().replace('.', '/'));
            vm = new Extension(out, 
                config.getResources(), both, config.exported(),
                config.allResources(), config.classpath()
            );
            addThree = true;
        } else {
            if (config.includeVM()) {
                fixedNames.add(VM.class.getName().replace('.', '/'));
                addThree = true;
            }
            vm = new Standalone(out, 
                config.getResources(), config.exported(),
                config.allResources()
            );
        }            
        if (addThree) {
            fixedNames.add(Object.class.getName().replace('.', '/'));
            fixedNames.add(Class.class.getName().replace('.', '/'));
            fixedNames.add(ArithmeticException.class.getName().replace('.', '/'));
        }
        vm.doCompile(out, fixedNames.addAndNew(both));
    }

    private void doCompile(Appendable out, StringArray names) throws IOException {
        generatePrologue(out);
        out.append("\n  var invoker = {};");
        out.append("\n  function registerClass(vm, name, fn) {");
        out.append("\n    if (!vm[name]) vm[name] = fn;");
        out.append("\n    return vm[name];");
        out.append("\n  }");
        generateBody(out, names);
        out.append(invokerMethods);
        
        for (String r : asBinary.toArray()) {
            out.append("\n  ").append(getExportsObject()).append("['registerResource']('");
            out.append(r).append("', '");
            InputStream is = this.resources.get(r);
            int avail = is.available();
            if (avail <= 0) {
                avail = 4096;
            }
            byte[] arr = new byte[avail];
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
            out.append(btoa(arr));
            out.append("');");
        }
        
        out.append("\n");
        generateEpilogue(out);
    }

    @JavaScriptBody(args = { "arr" }, body = "return btoa(arr);")
    private static String btoa(byte[] arr) {
        return javax.xml.bind.DatatypeConverter.printBase64Binary(arr);
    }

    protected abstract void generatePrologue(Appendable out) throws IOException;

    protected abstract void generateEpilogue(Appendable out) throws IOException;

    protected abstract String getExportsObject();

    protected abstract boolean isExternalClass(String className);

    protected abstract void lazyReference(Appendable out, String n) throws IOException;
    
    @Override
    protected final void declareClass(Appendable out, ClassData classData, String mangledName)
            throws IOException {
        if (exportedSymbols.isExported(classData)) {
            out.append("registerClass(").append(getExportsObject()).append(",'")
                                               .append(mangledName)
                                               .append("',")
                            .append(mangledName)
               .append(")");
            exportedCount++;
        } else {
            out.append(mangledName);
        }
    }

    protected String generateClass(Appendable out, String className) throws IOException {
        ClassData classData = classDataCache.getClassData(className);
        if (classData == null) {
            throw new IOException("Can't find class " + className);
        }
        return compile(classData);
    }

    @Override
    protected void declaredField(Appendable out, FieldData fieldData, String destObject, String mangledName) throws IOException {
        if (exportedSymbols.isExported(fieldData)) {
            exportMember(out, destObject, mangledName);
        }
    }

    @Override
    protected void declaredMethod(Appendable out, MethodData methodData, String destObject, String mangledName) throws IOException {
        if (isHierarchyExported(methodData)) {
            exportMember(out, destObject, mangledName);
        }
    }

    private void exportMember(Appendable out, String destObject, String memberName)
            throws IOException {
        out.append("\n").append(destObject).append("['")
        .append(memberName)
        .append("'] = m;\n");
    }

    private void generateBody(Appendable out, StringArray names) throws IOException {
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
                    lazyReference(out, name);
                    skipClass.add(name);
                    continue;
                }
                try {
                    String ic = generateClass(out, name);
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
                requireResourceImpl(out, false, resource);
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
                        out.append(theCode).append("\n");
                    }
                    initCode.toArray()[indx] = "";
                }
            }
        }
    }

    final void requireResourceImpl(Appendable out, boolean useEval, String resource) throws IOException {
        InputStream emul = resources.get(resource);
        if (emul == null) {
            throw new IOException("Can't find " + resource);
        }
        out.append("\n// resource from ").append(resource).append("\n");
        out.append("\n");
        if (useEval) {
            out.append("(0 || eval)(\"");
        }
        readResource(useEval, emul, out);
        if (useEval) {
            out.append("\");");
        }
        out.append("\n");
    }

    private static void readResource(boolean escape, InputStream emul, Appendable out) throws IOException {
        try {
            for (;;) {
                int ch = emul.read();
                if (ch == -1) {
                    break;
                }
                if (ch < 0 || ch > 255) {
                    throw new IOException("Invalid char in emulation " + ch);
                }
                if (escape) {
                    switch (ch) {
                        case '"':
                            out.append("\\\"");
                            break;
                        case '\\':
                            out.append("\\\\");
                            break;
                        case '\n':
                            out.append("\\n\"\n + \"");
                            break;
                        case '\t':
                            out.append("\\t");
                            break;
                        case '\r':
                            out.append("\\r");
                            break;
                        default:
                            out.append((char)ch);
                    }
                } else {
                    out.append((char)ch);
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
        return references.addIfMissing(cn);
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
    protected FieldData findField(String[] fieldInfoName) throws IOException {
        FieldData field = classDataCache.findField(
            fieldInfoName[0], fieldInfoName[1], fieldInfoName[2]
        );
        return field != null && canAccessDirectly(field.cls) ? field : null;
    }

    @Override
    protected String accessField(String object, FieldData field, String[] fieldInfoName)
    throws IOException {
        if (field != null && !field.isStatic()) {
            return "['fld_" + object + "_" + field.getName() + "']";
        } else {
            String mangledName = "_" + fieldInfoName[1];
           return accessNonVirtualMember(
               object, mangledName, field != null ? field.cls : null
           );
         }
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
            String[] fieldInfoName, 
            int params
    ) throws IOException {
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
            return object + "." + mangledName + '(';
        }

        return accessThroughInvoker(object, mangledName, params);
    }

    private String accessThroughInvoker(String object, String mangledName, int params) 
    throws IOException {
        String def = "\n  invoker." + mangledName + " = function(target";
        if (invokerMethods.indexOf(def) == -1) {
            invokerMethods.append(def);
            for (int j = 1; j < params; j++) {
                invokerMethods.append(", p").append(j);
            }
            invokerMethods.append(") {\n    return target['").
                append(mangledName).append("'](");
            for (int j = 1; j < params; j++) {
                if (j > 1) {
                    invokerMethods.append(",");
                }
                invokerMethods.append("p").append(j);
            }
            invokerMethods.append(");\n  };");
        }
        return "invoker." + mangledName + '(' + object + (params > 1 ? "," : "");
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

    private boolean canAccessDirectly(ClassData declaringClass) {
        if (declaringClass == null) {
            return false;
        }
        final String className = declaringClass.getClassName();
        if (
            "java/lang/Object".equals(className) ||
            "java/lang/reflect/Array".equals(className) ||
            isExternalClass(className)
        ) {
            return false;
        }
        return true;
    }

    private String accessNonVirtualMember(
        String object, String mangledName, ClassData declaringClass
    ) {
        return canAccessDirectly(declaringClass) ?
            object + "." + mangledName :
            object + "['" + mangledName + "']";
    }

    private final class ExportedMethodFinder
            implements ClassDataCache.TraversalCallback<MethodData> {
        private final ExportedSymbols exportedSymbols;
        private MethodData found;

        public ExportedMethodFinder(final ExportedSymbols exportedSymbols) {
            this.exportedSymbols = exportedSymbols;
        }

        @Override
        public boolean traverse(final MethodData methodData) {
            try {
                if (
                    exportedSymbols.isExported(methodData) ||
                    isExternalClass(methodData.cls.getClassName())
                ) {
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
        protected void generatePrologue(Appendable out) throws IOException {
            out.append("(function VM(global) {var fillInVMSkeleton = function(vm) {");
        }

        @Override
        protected void generateEpilogue(Appendable out) throws IOException {
            out.append(
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
                + "  var pending = [];\n"
                + "  var pendingClasses = [];\n"
                + "  function extensionLoaded(ev) {\n"
                + "    var at = pending.indexOf(ev.target);\n"
                + "    pending.splice(at, 1);\n"
                + "    if (pending.length === 0) {\n"
                + "      for (var i = 0; i < pendingClasses.length; i += 3) {\n"
                + "        invokeMethod(pendingClasses[i], pendingClasses[i + 1], pendingClasses[i + 2]);\n"
                + "      }\n"
                + "      pendingClasses = [];\n"
                + "    }\n"
                + "  }\n"
                + "  function invokeMethod(vm, n, args) {\n"
                + "    var clazz = vm.loadClass(n);\n"
                + "    if (args) {\n"
                + "      var seek = args[0];\n"
                + "      var prefix = seek.indexOf('__') == -1 ? seek + '__' : seek;\n"
                + "      args = Array.prototype.slice.call(args, 1);\n"
                + "      var found = '';\n"
                + "      for (var m in clazz) {\n"
                + "        if (m.indexOf(prefix) === 0) {\n"
                + "          return clazz[m].apply(null, args);\n"
                + "        }\n"
                + "        found += m.toString() + '\\n'\n"
                + "      }\n"
                + "      throw 'Cannot find ' + seek + ' in ' + n + ' found:\\n' + found;\n"
                + "    }\n"
                + "  }\n"
                + "  function extensionError(ev) {\n"
                + "    console.log('error loading ' + ev.target.src);\n"
                + "    extensionLoaded(ev);\n"
                + "  }\n"
                + "  function loadExtension(url, registerScript) {\n"
                + "      if (url.substring(url.length - 4) == '.jar')\n"
                + "        url = url.substring(0, url.length - 4) + '.js';\n"
                + "      var script = document.createElement('script');\n"
                + "      script.type = 'text/javascript';\n"
                + "      script.src = url;\n"
                + "      script.onload = extensionLoaded;\n"
                + "      script.onerror = extensionError;\n"
                + "      if (registerScript) document['currentScript'] = script;\n"
                + "      document.getElementsByTagName('head')[0].appendChild(script);\n"
                + "      pending.push(script);\n"
                + "  }\n"
                + "  global.bck2brwsr = function() {\n"
                + "    var args = Array.prototype.slice.apply(arguments);\n"
                + "    var resources = {};\n"
                + "    function registerResource(n, a64) {\n"
                + "      var frm = atob || window.atob;\n"
                + "      var str = frm(a64);\n"
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
                + "    var classPath;\n"
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
                + "          loadExtension(at, document && !document['currentScript']);\n"
                + "          args[i] = null;\n"
                + "        } else if (typeof at === 'function') ret = at(name, skip);\n"
                + "        else {\n"
                + "          if (classPath === undefined) {\n"
                + "             classPath = null;\n"
                + "             try {\n"
                + "               classPath = vm.loadClass('org.apidesign.vm4brwsr.ClassPath');\n"
                + "             } catch (err) {\n"
                + "             }\n"
                + "           }\n"
                + "          if (!classPath) {\n"
                + "            if (name !== 'org/apidesign/vm4brwsr/ClassPath.class') {\n"
                + "              throw 'Core Java library not registered. Cannot load from ' + at;\n"
                + "            }\n"
                + "            ret = null;\n"
                + "          } else {\n"
                + "            ret = classPath['loadBytes___3BLjava_lang_String_2Ljava_lang_Object_2II'](name, args, i, skip);\n"
                + "          }\n"
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
                + "    var reload = function(name, arr, keep) {\n"
                + "      if (!arr) throw 'Cannot find ' + name;\n"
                + "      var err = null;\n"
                + "      try {\n"
                + "        var lazy = loadClass('org.apidesign.vm4brwsr.VMLazy');\n"
                + "      } catch (e) {\n"
                + "        err = e;\n"
                + "      }\n"
                + "      if (!lazy) {\n"
                + "         throw 'No bck2brwsr VM module to compile ' + name + ':\\n' + err;\n"
                + "      }\n"
                + "      if (!keep) {\n"
                + "        var attr = mangleClass(name);\n"
                + "        delete vm[attr];\n"
                + "      }\n"
                + "      return lazy['load__Ljava_lang_Object_2Ljava_lang_Object_2Ljava_lang_String_2_3Ljava_lang_Object_2_3B']\n"
                + "        (vm, name, args, arr);\n"
                + "    };\n"
                + "    var loadClass = function(name) {\n"
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
                + "    vm['loadClass'] = loadClass;\n"
                + "    vm['_reload'] = reload;\n"
                + "    vm['loadBytes'] = loadBytes;\n"
                + "    initVM();\n"
                + "    loader.loadClass = function(name) {\n"
                + "      if (pending.length === 0) {\n"
                + "        try {\n"
                + "          var c = loadClass(name);\n"
                + "          c['invoke'] = function() {\n"
                + "            return invokeMethod(vm, name, arguments);\n"
                + "          };\n"
                + "          return c;\n"
                + "        } catch (err) {\n"
                + "          if (pending.length === 0) throw err;\n"
                + "        }\n"
                + "      }\n"
                + "      pendingClasses.push(vm);\n"
                + "      pendingClasses.push(name);\n"
                + "      pendingClasses.push(null);\n"
                + "      return {\n"
                + "        'invoke' : function() {\n"
                + "          if (pending.length === 0) {\n"
                + "            invokeMethod(vm, name, arguments);\n"
                + "            return;\n"
                + "          }\n"
                + "          pendingClasses.push(vm);\n"
                + "          pendingClasses.push(name);\n"
                + "          pendingClasses.push(arguments);\n"
                + "        }\n"
                + "      };\n"
                + "    }\n"
                + "    return loader;\n"
                + "  };\n");
            out.append(
                  "  global.bck2brwsr.register = function(config, extension) {\n"
                + "    if (!config || config['magic'] !== 'kafčo') {\n"
                + "      console.log('Will not register: ' + extension);\n"
                + "      return false;\n"
                + "    }\n"
                + "    if (typeof document == 'undefined') {\n"
                + "      var cs = null;\n"
                + "    } else {\n"
                + "      var cs = document['currentScript'];\n"
                + "      if (!cs) {\n"
                + "        var all = document.getElementsByTagName('script');\n"
                + "        var last = all.length;\n"
                + "        while (--last >= 0 && !all[last].src) {\n"
                + "        }\n"
                + "        cs = all[last];\n"
                + "      }\n"
                + "    }\n"
                + "    var csUrl = cs ? cs['src'] : null;\n"
                + "    var prefix = csUrl ? csUrl['replace'](/\\/[^\\/]*$/,'/') : '';\n"
                + "    extensions.push(extension);\n"
                + "    var cp = config['classpath'];\n"
                + "    if (cp) for (var i = 0; i < cp.length; i++) {\n"
                + "      loadExtension(prefix + cp[i]);\n"
                + "    }\n"
                + "    return null;\n"
                + "  };\n");
            out.append("}(this));");
        }

        @Override
        protected String getExportsObject() {
            return "vm";
        }

        @Override
        protected boolean isExternalClass(String className) {
            return false;
        }
        
        @Override
        protected void lazyReference(Appendable out, String n) throws IOException {
            String cls = n.replace('/', '_');
            String dot = n.replace('/', '.');

            out.append("\nvm.").append(cls).append(" = function() {");
            out.append("\n  var instance = arguments.length == 0 || arguments[0] === true;");
            out.append("\n  delete vm.").append(cls).append(";");
            out.append("\n  var c = vm.loadClass('").append(dot).append("');");
            out.append("\n  return vm.").append(cls).append("(instance);");
            out.append("\n}");
        }

        @Override
        protected void requireResource(Appendable out, String resourcePath) throws IOException {
            requireResourceImpl(out, true, resourcePath);
            super.asBinary.remove(resourcePath);
        }
    }

    private static final class Extension extends VM {
        private final StringArray extensionClasses;
        private final StringArray classpath;

        private Extension(Appendable out, Bck2Brwsr.Resources resources,
            String[] extClassesArray, StringArray explicitlyExported,
            StringArray asBinary, StringArray classpath
        ) throws IOException {
            super(out, resources, explicitlyExported, asBinary);
            this.extensionClasses = StringArray.asList(extClassesArray);
            this.classpath = classpath;
        }

        @Override
        protected void generatePrologue(Appendable out) throws IOException {
            out.append(
                  "bck2brwsr.register({\n"
                + "  'magic' : 'kafčo'"
            );
            if (classpath != null && classpath.toArray().length > 0) {
                out.append(
                  ",\n  'classpath' : [\n"
                );
                String sep = "    ";
                for (String s : classpath.toArray()) {
                    out.append(sep).append("'").append(s).append("'");
                    sep = ",\n    ";
                }
                out.append(
                  "\n  ]"
                );
            }
            out.append(
                  "\n}, function(exports) {\n"
                + "  var vm = {};\n");
            out.append("  function link(n, assign) {\n"
                + "    function replaceAll(s, o, n) {\n"
                + "      var pos = 0;\n"
                + "      for (;;) {\n"
                + "         var indx = s.indexOf(o, pos);\n"
                + "         if (indx === -1) {\n"
                + "           return s;\n"
                + "         }\n"
                + "         pos = indx + n.length;\n"
                + "         s = s.substring(0, indx) + n + s.substring(indx + o.length);\n"
                + "      }\n"
                + "    }\n"
                + "    return function() {\n"
                + "      var no_ = replaceAll(n, '_', '_1');\n"
                + "      var cls = replaceAll(no_, '/', '_');\n"
                + "      var dot = replaceAll(n, '/', '.');\n"
                + "      exports.loadClass(dot);\n"
                + "      assign(exports[cls]);\n"
                + "      return exports[cls](arguments);\n"
                + "    };\n"
                + "  };\n"
            );
        }

        @Override
        protected void generateEpilogue(Appendable out) throws IOException {
            out.append("});");
            if (exportedCount == 0) {
                throw new IOException("Creating library without any exported symbols is useless!");
            }
        }

        @Override
        String accessClass(String className) {
            if (this.extensionClasses.contains(className.replace('_', '/'))) {
                return className;
            }
            return super.accessClass(className);
        }

        @Override
        protected String generateClass(Appendable out, String className) throws IOException {
            if (isExternalClass(className)) {
                final String cls = className.replace("_", "_1").replace('/', '_');
                out.append("\n").append(assignClass(cls))
                   .append("link('")
                   .append(className)
                   .append("', function(f) { ").append(assignClass(cls)).append(" f; });");

                return null;
            }

            return super.generateClass(out, className);
        }

        @Override
        protected String getExportsObject() {
            return "exports";
        }

        @Override
        protected boolean isExternalClass(String className) {
            return !extensionClasses.contains(className);
        }
        
        @Override
        protected void lazyReference(Appendable out, String n) throws IOException {
            String cls = n.replace('/', '_');

            out.append("\nvm.").append(cls).append(" = function() {");
            out.append("\n  var instance = arguments.length == 0 || arguments[0] === true;");
            out.append("\n  delete vm.").append(cls).append(";");
            out.append("\n  return link('").append(n).append("', function(f) { vm.");
            out.append(cls).append(" = f;})(instance);");
            out.append("\n}");
        }

        @Override
        protected void requireResource(Appendable out, String resourcePath) throws IOException {
            requireResourceImpl(out, true, resourcePath);
            super.asBinary.remove(resourcePath);
        }
    }
}
