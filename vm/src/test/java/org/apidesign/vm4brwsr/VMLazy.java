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
    private final Object vm;
    private final Object global;
    
    private VMLazy(Object global, Object vm, Appendable out) {
        super(out);
        this.vm = vm;
        this.global = global;
    }
    
    static String toJavaScript(Object global, Object vm, byte[] is) throws IOException {
        StringBuilder sb = new StringBuilder();
        new VMLazy(global, vm, sb).compile(new ByteArrayInputStream(is));
        return sb.toString().toString();
    }

    @JavaScriptBody(args = { "self", "n" }, 
        body=
          "var cls = n.replaceLjava_lang_StringCC(n,'/','_').toString();"
        + "var glb = self.fld_global;"
        + "var vm = self.fld_vm;"
        + "if (glb[cls]) return false;"
        + "glb[cls] = function() {"
        + "  return vm.loadClass(n,cls);"
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
    protected String assignClass(String className) {
        return "arguments[0][arguments[1]]=";
    }
}
