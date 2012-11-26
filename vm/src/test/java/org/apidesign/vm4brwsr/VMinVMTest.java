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
import java.io.InputStream;
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
    
    @Test public void compareTheGeneratedCode() throws Exception {
        byte[] arr = readClass("/org/apidesign/vm4brwsr/Array.class");
        String ret1 = VMinVM.toJavaScript(arr);
        
        Object ret;
        try {
            ret = code.invokeFunction(VMinVM.class.getName().replace('.', '_'), true);
            ret = code.invokeMethod(ret, "toJavaScriptLjava_lang_StringAB", arr);
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
        
        assertEquals((String)ret, ret1.toString(), "The code is the same");
    }
    
    @BeforeClass
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = StaticMethodTest.compileClass(sb, 
            "org/apidesign/vm4brwsr/VMinVM"
        );
        codeSeq = sb;
    }
    
    private static byte[] readClass(String res) throws IOException {
        InputStream is1 = VMinVMTest.class.getResourceAsStream(res);
        assertNotNull(is1, "Stream found");
        byte[] arr = new byte[is1.available()];
        int len = is1.read(arr);
        is1.close();
        if (len != arr.length) {
            throw new IOException("Wrong len " + len + " for arr: " + arr.length);
        }
        return arr;
    }
}
