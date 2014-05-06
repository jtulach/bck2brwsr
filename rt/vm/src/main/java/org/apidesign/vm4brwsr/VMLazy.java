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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class VMLazy {
    private final Object loader;
    private final Object[] args;
    
    private VMLazy(Object loader, Object[] args) {
        this.loader = loader;
        this.args = args;
    }
    
    static void init() {
    }
    
    static Object load(Object loader, String name, Object[] arguments) 
    throws IOException, ClassNotFoundException {
        return new VMLazy(loader, arguments).load(name, false);
    }

    static Object reload(Object loader, String name, Object[] arguments, byte[] arr) 
    throws IOException, ClassNotFoundException {
        return new VMLazy(loader, arguments).defineClass(arr, name, false);
    }
    
    static byte[] loadBytes(Object loader, String name, Object[] arguments, int skip) throws Exception {
        return Zips.loadFromCp(arguments, name, skip);
    }
    
    private Object load(String name, boolean instance)
    throws IOException, ClassNotFoundException {
        String res = name.replace('.', '/') + ".class";
        byte[] arr = Zips.loadFromCp(args, res, 0);
        if (arr == null) {
            throw new ClassNotFoundException(name);
        }
        
        return defineClass(arr, name, instance);
    }

    private Object defineClass(byte[] arr, String name, boolean instance) throws IOException {
        StringBuilder out = new StringBuilder(65535);
        out.append("var loader = arguments[0];\n");
        out.append("var vm = loader.vm;\n");
        int prelude = out.length();
        String initCode = new Gen(this, out).compile(new ByteArrayInputStream(arr));
        String code = out.toString().toString();
        String under = name.replace('.', '_');
        Object fn = applyCode(loader, under, code, instance);
        
        if (!initCode.isEmpty()) {
            out.setLength(prelude);
            out.append(initCode);
            code = out.toString().toString();
            applyCode(loader, null, code, false);
        }            
        
        return fn;
    }

    @JavaScriptBody(args = {"loader", "name", "script", "instance" }, body =
        "try {\n" +
        "  new Function(script)(loader, name);\n" +
        "} catch (ex) {\n" +
        "  throw 'Cannot compile ' + name + ' ' + ex + ' line: ' + ex.lineNumber + ' script:\\n' + script;\n" +
        "}\n" +
        "return name != null ? vm[name](instance) : null;\n"
    )
    private static native Object applyCode(Object loader, String name, String script, boolean instance);
    
    
    private static final class Gen extends ByteCodeToJavaScript {
        private final VMLazy lazy;

        public Gen(VMLazy vm, Appendable out) {
            super(out);
            this.lazy = vm;
        }
        
        @JavaScriptBody(args = {"n"},
        body =
        "var cls = n.replace__Ljava_lang_String_2CC('/','_').toString();"
        + "\nvar dot = n.replace__Ljava_lang_String_2CC('/','.').toString();"
        + "\nvar lazy = this._lazy();"
        + "\nvar loader = lazy._loader();"
        + "\nvar vm = loader.vm;"
        + "\nif (vm[cls]) return false;"
        + "\nvm[cls] = function() {"
        + "\n  var instance = arguments.length == 0 || arguments[0] === true;"
        + "\n  return lazy.load__Ljava_lang_Object_2Ljava_lang_String_2Z(dot, instance);"
        + "\n};"
        + "\nreturn true;")
        @Override
        protected boolean requireReference(String internalClassName) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void requireScript(String resourcePath) throws IOException {
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            String code = readCode(resourcePath);
            applyCode(lazy.loader, null, code, false);
        }

        private String readCode(String resourcePath) throws IOException {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            StringBuilder sb = new StringBuilder();
            for (;;) {
                int ch = is.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char)ch);
            }
            return sb.toString();
        }

        @Override
        String assignClass(String className) {
            return "vm[arguments[1]]=";
        }

        @Override
        String accessClass(String classOperation) {
            return "vm." + classOperation;
        }
    }
}
