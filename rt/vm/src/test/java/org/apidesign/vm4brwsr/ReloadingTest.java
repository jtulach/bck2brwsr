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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class ReloadingTest {
    private static TestVM code;
    
    @Test public void verifyUsageOf() throws Exception {
        code.execCode("First hello", 
            Hello.class, "hello__Ljava_lang_String_2",
            "Hello World!"
        );

        byte[] arr = BytesLoader.readClass("org/apidesign/vm4brwsr/Hello.class");
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == 'H' && arr[i + 1] == 'e' && arr[i + 2] == 'l') {
                arr[i] = 'A';
                arr[i + 1] = 'h';
                arr[i + 2] = 'o';
                arr[i + 3] = 'y';
                arr[i + 4] = ' ';
                break;
            }
        }
        
        code.execCode("Redefine class",
            Hello.class, "reloadYourSelf__Ljava_lang_Object_2_3B",
            null, arr
        );

        code.execCode("Second hello", 
            Hello.class, "hello__Ljava_lang_String_2",
            "Ahoy  World!"
        );
    }
    
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        code = TestVM.compileClass(
            "org/apidesign/vm4brwsr/Hello");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    
}

