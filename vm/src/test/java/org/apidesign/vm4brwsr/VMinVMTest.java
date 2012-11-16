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
        StringBuilder hotspot = new StringBuilder();
        GenJS.compile(hotspot, "org/apidesign/vm4brwsr/Array");
        
        Object ret = code.invokeFunction(
            "org_apidesign_vm4brwsr_GenJS_toStringLjava_lang_StringLjava_lang_String",
            "org/apidesign/vm4brwsr/Array"
        );
        assertTrue(ret instanceof String, "It is string: " + ret);
        
        assertEquals((String)ret, hotspot.toString(), "The code is the same");
    }
    
    @BeforeClass
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = StaticMethodTest.compileClass(sb, 
            "org/apidesign/vm4brwsr/GenJS"
        );
        codeSeq = sb;
    }
}
