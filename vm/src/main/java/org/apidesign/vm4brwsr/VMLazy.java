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
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class VMLazy extends ByteCodeToJavaScript {
    private final Object loader;
    
    private VMLazy(Object loader, Appendable out) {
        super(out);
        this.loader = loader;
    }
    
    static void init() {
    }
    
    @JavaScriptBody(args={"res", "args" }, body = "return args[0](res.toString());")
    private static native byte[] read(String res, Object[] args);
    
    static Object load(Object loader, String name, Object[] arguments) 
    throws IOException, ClassNotFoundException {
        String res = name.replace('.', '/') + ".class";
        byte[] arr = read(res, arguments);
        if (arr == null) {
            throw new ClassNotFoundException(name);
        }
        String code = toJavaScript(loader, arr);
        return applyCode(loader, name, code);
    }
    
    @JavaScriptBody(args = {"loader", "name", "script" }, body =
        "try {\n" +
        "  new Function(script)(loader, name);\n" +
        "} catch (ex) {\n" +
        "  throw 'Cannot compile ' + ex + ' script:\\\\n' + script;\n" +
        "}\n" +
        "return vm[name](false);\n"
    )
    private static native Object applyCode(Object loader, String name, String script);
    
    private static String toJavaScript(Object loader, byte[] is) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("var loader = arguments[0];\n");
        sb.append("var vm = loader.vm;\n");
        new VMLazy(loader, sb).compile(new ByteArrayInputStream(is));
        return sb.toString().toString();
    }

    @JavaScriptBody(args = { "self", "n" }, 
        body=
          "var cls = n.replace__Ljava_lang_String_2CC(n,'/','_').toString();"
        + "var loader = self.fld_loader;"
        + "var vm = loader.vm;"
        + "if (vm[cls]) return false;"
        + "vm[cls] = function() {"
        + "  return loader.loadClass(n,cls);"
        + "};"
        + "return true;"
    )
    @Override
    protected boolean requireReference(String internalClassName) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void requireScript(String resourcePath) {
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
