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
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ByteCodeToJavaScriptTest {
    
    public ByteCodeToJavaScriptTest() {
    }

    @Test
    public void findMethodNameManglesObjectsCorrectly() {
        StringBuilder cnt = new StringBuilder();
        char[] returnType = { 'V' };
        String ret = ByteCodeToJavaScript.findMethodName(new String[] { 
            "StringTest", "replace", "(Ljava/lang/String;CC)Ljava/lang/String;"
        }, cnt, returnType);
        assertEquals(cnt.toString(), "000", "No doubles or longs");
        assertTrue(returnType[0] != 'V', "Returns string");
        assertEquals(ret, "replace__Ljava_lang_String_2Ljava_lang_String_2CC");
    }

    @Test
    public void manglingArrays() {
        StringBuilder cnt = new StringBuilder();
        char[] returnType = { 'V' };
        String ret = ByteCodeToJavaScript.findMethodName(new String[] { 
            "VMinVM", "toJavaScript", "([B)Ljava/lang/String;"
        }, cnt, returnType);
        assertEquals(cnt.toString(), "0", "No doubles or longs");
        assertTrue(returnType[0] != 'V', "Returns string");
        assertEquals(ret, "toJavaScript__Ljava_lang_String_2_3B");
    }
}
