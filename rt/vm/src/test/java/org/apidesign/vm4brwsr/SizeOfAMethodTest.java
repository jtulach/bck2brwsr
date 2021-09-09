/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.io.IOException;
import java.io.InputStream;
import static org.testng.Assert.*;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SizeOfAMethodTest {
    private static String code;

    @Test
    public void sumXYShouldBeSmall() {
        String s = code;
        int beg = s.indexOf("c.sum__III");
        int end = s.indexOf(".access", beg);
        
        assertTrue(beg > 0, "Found sum method in " + code);
        assertTrue(beg < end, "Found end of sum method in " + code);
        
        String method = s.substring(beg, end);
    
        assumeNewStackMapper(method);
        
        assertEquals(method.indexOf("st"), -1, "There should be no stack operations:\n" + method);
    }

    @Test public void betterConstructor() {
        String s = code;
        int beg = s.indexOf("c.initInflater__IIZ");
        int end = s.indexOf(".access", beg);
        
        assertTrue(beg > 0, "Found initInflater method in " + code);
        assertTrue(beg < end, "Found end of initInflater method in " + code);
        
        String method = s.substring(beg, end);
        
        assumeNewStackMapper(method);
        
        assertEquals(method.indexOf("stA1"), -1, "No need for stA1 register:\n" + method);
    }

    @Test 
    public void deepConstructor() {
        String s = code;
        int beg = s.indexOf("c.intHolder__I");
        int end = s.indexOf(".access", beg);
        
        assertTrue(beg > 0, "Found intHolder method in " + code);
        assertTrue(beg < end, "Found end of intHolder method in " + code);
        
        String method = s.substring(beg, end);
    
        assumeNewStackMapper(method);
        
        assertEquals(method.indexOf("stA3"), -1, "No need for stA3 register on second constructor:\n" + method);
    }

    @Test 
    public void emptyConstructorRequiresNoStack() {
        String s = code;
        int beg = s.indexOf("CLS.cons__V");
        int end = s.indexOf(".access", beg);
        
        assertTrue(beg > 0, "Found constructor in " + code);
        assertTrue(beg < end, "Found end of constructor in " + code);
        
        String method = s.substring(beg, end);
        method = method.replace("constructor", "CNSTR");
    
        assumeNewStackMapper(method);
        
        assertEquals(method.indexOf("st"), -1, "There should be no stack operations:\n" + method);
        assertEquals(method.indexOf("for"), -1, "There should be no for blocks:\n" + method);
    }
    
    @Test
    public void dontGeneratePrimitiveFinalConstants() {
        assertEquals(code.indexOf("MISSING_CONSTANT"), -1, "MISSING_CONSTANT field should not be generated");
    }
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        final String res = "org/apidesign/vm4brwsr/StaticMethod";
        StringBuilder sb = new StringBuilder();
        class JustStaticMethod implements Bck2Brwsr.Resources {
            @Override
            public InputStream get(String resource) throws IOException {
                final String cn = res + ".class";
                if (resource.equals(cn)) {
                    return getClass().getClassLoader().getResourceAsStream(cn);
                }
                return null;
            }
        }
        Bck2Brwsr.generate(sb, new JustStaticMethod(), res);
        code = sb.toString();
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }

    private static void assumeNewStackMapper(String code) {
        int stackArray = code.indexOf("var stack = [];");
        if (stackArray >= 0) {
            throw new SkipException("Skipping the text as code contains stack = []");
        }
    }
    
}
