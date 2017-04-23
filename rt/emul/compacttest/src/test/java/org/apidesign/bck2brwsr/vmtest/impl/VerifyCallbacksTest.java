/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.vmtest.impl;

import org.apidesign.vm4brwsr.Bck2Brwsr;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

public class VerifyCallbacksTest {

    @Test
    public void noTryAndCatch() throws Exception {
        StringBuilder sb = new StringBuilder();
        Bck2Brwsr.newCompiler().library().addRootClasses("org/apidesign/bck2brwsr/vmtest/impl/$JsCallbacks$").generate(sb);
        String beginTxt = "java_1lang_1Runnable$run$__Ljava_lang_Object_2Ljava_lang_Runnable_2 = function";
        int begin = sb.indexOf(beginTxt);
        assertNotEquals(-1, begin, "Function def found in\n " + sb);
        int end = sb.indexOf("c['java_1lang_1Runnable$run$__Ljava_lang_Object_2Ljava_lang_Runnable_2']");
        assertNotEquals(-1, end, "Function end found in\n " + sb);

        String body = sb.substring(begin, end);

        assertEquals(body.indexOf("try"), -1, "No try in\n" + body);
        assertEquals(body.indexOf("catch"), -1, "No catch");
        assertEquals(body.indexOf("finally"), -1, "No finally");
    }

}
