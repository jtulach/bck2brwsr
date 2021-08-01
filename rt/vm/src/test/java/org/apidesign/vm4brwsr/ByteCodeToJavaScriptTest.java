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
package org.apidesign.vm4brwsr;

import org.apidesign.bck2brwsr.emul.reflect.Mangling;
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
    public void manglingDash() {
        StringBuilder cnt = new StringBuilder();
        char[] returnType = { 'V' };
        String ret = ByteCodeToJavaScript.findMethodName(new String[] {
            "StringKt", "isSuccess-impl", "(Ljava/lang/Object;)Z"
        }, cnt, returnType);
        assertEquals(cnt.toString(), "0", "One argument");
        assertTrue(returnType[0] != 'V', "Returns string");
        assertEquals(ret, "isSuccess_0002dimpl__ZLjava_lang_Object_2");
    }

    @Test
    public void compareMangleOfChars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4096; i++) {
            sb.append((char)i);
        }
        String mangleEmulMini = Mangling.mangle(sb, true);
        String mangleCompiler = ByteCodeToJavaScript.mangle(sb.toString(), 0, sb.length(), true);

        assertEquals(mangleCompiler, mangleEmulMini);
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

    @Test public void mangleJsCallbackToAType() throws Exception {
        String res = ByteCodeToJavaScript.mangleJsCallbacks(
            "org.apidesign.bck2brwsr.vmtest.impl.HtmlAnnotations",
            "onError", "Ljava/lang/Object;", false
        );
        assertEquals(res,
            "org_1apidesign_1bck2brwsr_1vmtest_1impl_1HtmlAnnotations$onError$"
                + "Ljava_1lang_1Object_12__"
                + "Ljava_lang_Object_2Lorg_apidesign_bck2brwsr_vmtest_impl_HtmlAnnotations_2Ljava_lang_Object_2",
            "Pretty long method name"
        );
    }
    @Test public void mangleJsCallbackToATypeWithString() throws Exception {
        String res = ByteCodeToJavaScript.mangleJsCallbacks(
            "org.apidesign.bck2brwsr.vmtest.impl.HtmlAnnotations",
            "onMessage", "Ljava/lang/String;", false
        );
        assertEquals(res,
            "org_1apidesign_1bck2brwsr_1vmtest_1impl_1HtmlAnnotations$onMessage$"
                + "Ljava_1lang_1String_12__"
                + "Ljava_lang_Object_2Lorg_apidesign_bck2brwsr_vmtest_impl_HtmlAnnotations_2Ljava_lang_String_2",
            "Pretty long method name"
        );
    }
}
