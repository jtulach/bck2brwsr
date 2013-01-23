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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static org.testng.Assert.*;
import javax.script.Invocable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class VMinVMTest {

    private static CharSequence codeSeq;
    private static Invocable code;
    
    @Test public void compareGeneratedCodeForArrayClass() throws Exception {
        compareCode("org/apidesign/vm4brwsr/Array.class");
    }

    @Test public void compareGeneratedCodeForClassesClass() throws Exception {
        compareCode("org/apidesign/vm4brwsr/Classes.class");
    }

    @BeforeClass
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = StaticMethodTest.compileClass(sb, 
            "org/apidesign/vm4brwsr/VMinVM"
        );
        codeSeq = sb;
    }
    
    private void compareCode(final String nm) throws Exception, IOException {
        byte[] arr = BytesLoader.readClass(nm);
        String ret1 = VMinVM.toJavaScript(arr);
        
        Object ret;
        try {
            ret = code.invokeFunction("bck2brwsr");
            ret = code.invokeMethod(ret, "loadClass", VMinVM.class.getName());
            ret = code.invokeMethod(ret, "toJavaScript__Ljava_lang_String_2_3B", arr);
        } catch (Exception ex) {
            File f = File.createTempFile("execution", ".js");
            FileWriter w = new FileWriter(f);
            w.append("var byteCode = [\n  ");
            String sep = "";
            for (int i = 0; i < arr.length; i++) {
                w.append(sep).append(Integer.toString((arr[i] + 256) % 256));
                sep = ", ";
                if (i % 20 == 0) {
                    w.append("\n  ");
                }
            }
            w.append("\n];\n");
            w.append(codeSeq);
            w.close();
            throw new Exception(ex.getMessage() + " file: " + f, ex);
        }

        
        assertTrue(ret instanceof String, "It is string: " + ret);
        
        if (!ret1.toString().equals(ret)) {
            StringBuilder msg = new StringBuilder("Difference found between ");
            msg.append(StaticMethodTest.dumpJS(ret1));
            msg.append(" ");
            msg.append(StaticMethodTest.dumpJS((CharSequence) ret));
            msg.append(" compiled by ");
            msg.append(StaticMethodTest.dumpJS(codeSeq));
            fail(msg.toString());
        }
    }
}
