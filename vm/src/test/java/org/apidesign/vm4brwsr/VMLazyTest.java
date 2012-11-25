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
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/** Implements loading class by class.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class VMLazyTest {

    
    private static CharSequence codeSeq;
    private static Invocable code;

    @BeforeClass
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\nfunction test(clazz, as, method) {");
        sb.append("\n  var l = new lazyVM();");
        sb.append("\n  var c = l.loadClass(clazz, as);");
        sb.append("\n  return c[method]();");
        sb.append("\n}");
        
        
        sb.append("\nfunction lazyVM() {");
        sb.append("\n  var self = this;");
        sb.append("\n  this.constructor.prototype.Identity = function(value) {");
        sb.append("\n    var self = this;");
        sb.append("\n    self.value = value;");
        sb.append("\n    self.call = function() { return self.value; };");
        sb.append("\n  };");
        sb.append("\n");
        sb.append("\n  this.constructor.prototype.loadClass = function(res, name) {");
        sb.append("\n    var script = org_apidesign_vm4brwsr_VMLazy_toJavaScriptLjava_lang_StringAB(loader.get(res + '.class'));");
//        sb.append("\n    throw script.toString();");
        sb.append("\n    new Function(");
        sb.append("\n      'arguments[0][arguments[1]]=new lazyVM.prototype.Identity(' + script + ').call'");
        sb.append("\n    )(self, name);");
        sb.append("\n  };");
        sb.append("\n");
        sb.append("\n}\n");
        
        ScriptEngine[] arr = { null };
        code = StaticMethodTest.compileClass(sb, arr,
            "org/apidesign/vm4brwsr/VMLazy"
        );
        arr[0].getContext().setAttribute("loader", new FindBytes(), ScriptContext.ENGINE_SCOPE);
        codeSeq = sb;
    }
    
    @Test public void invokeStaticMethod() throws Exception {
        assertExec("Trying to get -1", "test", Double.valueOf(-1),
            "org/apidesign/vm4brwsr/StaticMethod", "org_apidesign_vm4brwsr_StaticMethod", "minusOne"
        );
    }
    

    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        Object ret = null;
        try {
            ret = code.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in\n" + StaticMethodTest.dumpJS(codeSeq), ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in\n" + StaticMethodTest.dumpJS(codeSeq), ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + codeSeq);
    }

    public static final class FindBytes {
        public byte[] get(String name) throws IOException {
            InputStream is = VMLazyTest.class.getClassLoader().getResourceAsStream(name);
            if (is == null) {
                throw new IOException("Can't find " + name);
            }
            byte[] arr = new byte[is.available()];
            int len = is.read(arr);
            if (len != arr.length) {
                throw new IOException("Read only " + len + " wanting " + arr.length);
            }
            System.err.print("loader['" + name + "'] = [");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) {
                    System.err.print(", ");
                }
                System.err.print(arr[i]);
            }
            System.err.println("]");
            return arr;
        }
    }
}
