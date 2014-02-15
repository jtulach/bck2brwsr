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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apidesign.vm4brwsr;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SizeOfAMethodTest {
    private static TestVM code;

    @Test public void sumXYShouldBeSmall() {
        String s = code.codeSeq().toString();
        int beg = s.indexOf("c.sum__III");
        int end = s.indexOf("c.sum__III.access");
        
        assertTrue(beg > 0, "Found sum method in " + code.toString());
        assertTrue(beg < end, "Found end of sum method in " + code.toString());
        
        String method = s.substring(beg, end);
        
        assertEquals(method.indexOf("st"), -1, "There should be no stack operations:\n" + method);
    }
    
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = TestVM.compileClass(sb, "org/apidesign/vm4brwsr/StaticMethod");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    
}
