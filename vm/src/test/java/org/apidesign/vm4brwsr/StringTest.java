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

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StringTest {
    @Test public void firstChar() throws Exception {
        assertExec(
            "First char in Hello is H",
            StringSample.class, "sayHello__CI",
            72, 0
        );
    }

    @Test public void fromChars() throws Exception {
        assertExec(
            "First char in Hello is ABC",
            StringSample.class, "fromChars__Ljava_lang_String_2CCC",
            "ABC", 'A', 'B', 'C'
        );
    }

    @Test public void concatChars() throws Exception {
        assertExec(
            "Composing yields ABC",
            StringSample.class, "chars__Ljava_lang_String_2CCC",
            "ABC", 'A', 'B', 'C'
        );
    }

    @Test public void concatCharsFromInts() throws Exception {
        assertExec(
            "Composing yields ABC",
            StringSample.class, "charsFromNumbers__Ljava_lang_String_2",
            "ABC"
        );
    }

    @Test public void concatCharsFromChars() throws Exception {
        assertExec(
            "Composing yields ABC",
            StringSample.class, "charsFromChars__Ljava_lang_String_2",
            "ABC"
        );
    }

    @Test public void instanceOfWorks() throws Exception {
        assertExec(
            "It is string",
            StringSample.class, "isStringInstance__Z",
            Double.valueOf(1.0)
        );
    }

    @Test public void getBytes() throws Exception {
        final String horse = "Žluťoučký kůň";
        final String expected = StringSample.getBytes(horse);
        assertExec(
            "Bytes look simplar",
            StringSample.class, "getBytes__Ljava_lang_String_2Ljava_lang_String_2",
            expected, horse
        );
    }

    @Test(timeOut=10000) public void toStringConcatenation() throws Exception {
        assertExec(
            "Five executions should generate 5Hello World!",
            StringSample.class, "toStringTest__Ljava_lang_String_2I",
            "Hello World!5", 5
        );
    }
    @Test public void toStringConcatenationJava() throws Exception {
        assertEquals("Hello World!5", StringSample.toStringTest(5));
    }
    
    @Test(timeOut=10000) public void stringStringConcat() throws Exception {
        assertExec(
            "Composes strings OK",
            StringSample.class, "concatStrings__Ljava_lang_String_2",
            "Hello World!1" + "\\\n\r\t"
        );
    }

    @Test public void equalsAndSubstring() throws Exception {
        assertExec(
            "Composes are OK",
            StringSample.class, "equalToHello__ZII",
            true, 0, 5
        );
    }
    @Test public void replaceChars() throws Exception {
        assertExec(
            "Can replace slashes by underscores",
            StringSample.class, "replace__Ljava_lang_String_2Ljava_lang_String_2CC",
            "x_y_z", "x/y/z", '/', '_'
        );
    }
    @Test public void replaceIntChars() throws Exception {
        assertExec(
            "Can replace slashes by underscores",
            StringSample.class, "replace__Ljava_lang_String_2Ljava_lang_String_2CC",
            "x_y_z", "x/y/z", (int)'/', (int)'_'
        );
    }

    @Test public void insertBuilder() throws Exception {
        assertExec(
            "Can insert something into a buffer?",
            StringSample.class, "insertBuffer__Ljava_lang_String_2",
            "Ahojdo!"
        );
    }
    
    @Test public void compareHashCodeHi() throws Exception {
        String j = "Hi";
        int jh = StringSample.hashCode(j);
        assertExec(
            "Hashcode is the same " +jh,
            StringSample.class, "hashCode__ILjava_lang_String_2",
            Double.valueOf(jh), j
        );
    }
    @Test public void compareHashCode1() throws Exception {
        String j = "Hello Java!";
        int jh = StringSample.hashCode(j);
        assertExec(
            "Hashcode is the same " + jh,
            StringSample.class, "hashCode__ILjava_lang_String_2",
            Double.valueOf(jh), j
        );
    }
    @Test public void stringSwitch1() throws Exception {
        assertExec(
            "Get one",
            StringSample.class, "stringSwitch__ILjava_lang_String_2",
            Double.valueOf(1), "jedna"
        );
    }
    @Test public void stringSwitch2() throws Exception {
        assertExec(
            "Get two",
            StringSample.class, "stringSwitch__ILjava_lang_String_2",
            Double.valueOf(2), "dve"
        );
    }
    @Test public void stringSwitchDefault() throws Exception {
        assertExec(
            "Get -1",
            StringSample.class, "stringSwitch__ILjava_lang_String_2",
            Double.valueOf(-1), "none"
        );
    }
    
    @Test public void countAB() throws Exception {
        assertEquals(StringSample.countAB("Ahoj Bedo!"), 3, "Verify Java code is sane");
        assertExec(
            "One A and one B adds to 3",
            StringSample.class, "countAB__ILjava_lang_String_2",
            Double.valueOf(3), "Ahoj Bedo!"
        );
        
    }

    @Test public void compareStrings() throws Exception {
        int res = StringSample.compare("Saab", "Volvo");
        assertExec(
            "Saab finished sooner than Volvo",
            StringSample.class, "compare__ILjava_lang_String_2Ljava_lang_String_2",
            Double.valueOf(res), "Saab", "Volvo"
        );
        
    }
    
    private static TestVM code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        code = TestVM.compileClass(
            "org/apidesign/vm4brwsr/StringSample",
            "java/lang/String"
        );
    }
    
    private static void assertExec(String msg, 
        Class<?> clazz, String method, Object expRes, Object... args
    ) throws Exception {
        code.assertExec(msg, clazz, method, expRes, args);
    }
    
}
