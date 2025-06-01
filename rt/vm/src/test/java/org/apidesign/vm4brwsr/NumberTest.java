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

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class NumberTest {
    @Test public void integerFromString() throws Exception {
        assertExec("Can convert string to integer", Integer.class, "parseInt__ILjava_lang_String_2",
            Double.valueOf(333), "333"
        );
    }

    @Test public void doubleFromString() throws Exception {
        assertExec("Can convert string to double", Double.class, "parseDouble__DLjava_lang_String_2",
            Double.valueOf(33.3), "33.3"
        );
    }

    @Test public void autoboxDouble() throws Exception {
        assertExec("Autoboxing of doubles is OK", Numbers.class, "autoboxDblToString__Ljava_lang_String_2",
            "3.3"
        );
    }
    
    @Test public void javalog1000() throws Exception {
        assertEquals(3.0, Math.log10(1000.0), 0.00003, "log_10(1000) == 3");
    }

    @Test public void jslog1000() throws Exception {
        assertExec("log_10(1000) == 3", Math.class, "log10__DD", 
            Double.valueOf(3.0), 1000.0
        );
    }
    
    @Test public void javaRem() {
        assertEquals(3, Numbers.rem(303, 10));
    }
    @Test public void jsRem() throws Exception {
        assertExec("Should be three", Numbers.class, "rem__III", 
            Double.valueOf(3.0), 303, 10
        );
    }
    
    @Test public void deserializeInt() throws Exception {
        int exp = Numbers.deserInt();
        assertExec("Should be the same", Numbers.class, "deserInt__I", 
            Double.valueOf(exp)
        );
    }

    @Test public void deserializeSimpleLong() throws Exception {
        assertExec("Should be 3454", Numbers.class, "deserLong__J_3B", 
            Double.valueOf(3454), 
            new byte[] { (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)13, (byte)126 }
        );
    }
    @Test public void deserializeMiddleLong() throws Exception {
        final byte[] arr = new byte[] {
            (byte)0, (byte)0, (byte)64, (byte)32, (byte)23, (byte)0, (byte)0, (byte)0
        };
        long exp = Numbers.deserLong(arr, 16);
        assertExec("Should be " + exp, Numbers.class, "deserLong__J_3BI", 
            Double.valueOf(exp), arr, 16);
    }
    @Test public void deserializeLargeLong() throws Exception {
        final byte[] arr = new byte[] {
            (byte)64, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0
        };
        long exp = Numbers.deserLong(arr, 32);
        assertExec("Should be " + exp, Numbers.class, "deserLong__J_3BI", 
            Double.valueOf(exp), arr, 32);
    }
    
    @Test public void deserializeFloatInJava() throws Exception {
        float f = 54324.32423f;
        float r = Numbers.deserFloat();
        assertEquals(r, f, "Floats are the same");
    }
    
    @Test public void deserializeFloatInJS() throws Exception {
        float f = 54324.32423f;
        assertExec("Should be the same", Numbers.class, "deserFloat__F", 
            Double.valueOf(f)
        );
    }

    @Test public void deserializeDoubleInJava() throws Exception {
        double f = 3.0;
        double r = Numbers.deserDouble();
        assertEquals(r, f, 0.001, "Doubles are the same");
    }
    
    @Test public void deserializeDoubleInJS() throws Exception {
        double f = 3.0;
        assertExec("Should be the same", Numbers.class, "deserDouble__D", f);
    }
    
    @Test public void bytesToLong() throws Exception {
        long exp = Numbers.bytesToLong((byte)30, (byte)20, 32);
        assertExec("Should be the same", Numbers.class, "bytesToLong__JBBI", 
            Double.valueOf(exp), 30, 20, 32);
    }
    /*
    @Test public void serDouble() throws IOException {
        double f = 3.0;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(os);
        d.writeLong(3454);
        d.close();
        
        StringBuilder sb = new StringBuilder();
        byte[] arr = os.toByteArray();
        for (int i = 0; i < arr.length; i++) {
            sb.append("(byte)").append(arr[i]).append(", ");
        }
        fail("" + sb);
    }
*/    
    @Test public void fiveInStringJS() throws Exception {
        String s = Numbers.intToString();
        assertExec("Should be the same: " + s, 
            Numbers.class, "intToString__Ljava_lang_String_2", 
            s
        );
    }

    @Test public void sevenInStringJS() throws Exception {
        String s = Numbers.floatToString();
        assertExec("Should be the same: " + s, 
            Numbers.class, "floatToString__Ljava_lang_String_2", 
            s
        );
    }

    @Test public void everyNumberHasJavaLangNumberMethods() throws Exception {
        assertExec("Can we call doubleValue?", 
            Numbers.class, "seven__DI", 
            Double.valueOf(7.0), 0
        );
    }
    @Test public void everyNumberHasJavaLangNumberMethodsInt() throws Exception {
        assertExec("Can we call doubleValue?", 
            Numbers.class, "seven__DI", 
            Double.valueOf(7.0), 1
        );
    }
    @Test public void everyNumberHasJavaLangNumberMethodsLong() throws Exception {
        assertExec("Can we call doubleValue?", 
            Numbers.class, "seven__DI", 
            Double.valueOf(7.0), 2
        );
    }
    @Test public void everyNumberHasJavaLangNumberMethodsShort() throws Exception {
        assertExec("Can we call doubleValue?", 
            Numbers.class, "seven__DI", 
            Double.valueOf(7.0), 3
        );
    }
    @Test public void everyNumberHasJavaLangNumberMethodsByte() throws Exception {
        assertExec("Can we call doubleValue?", 
            Numbers.class, "seven__DI", 
            Double.valueOf(7.0), 4
        );
    }
    @Test public void valueOfNumber() throws Exception {
        assertExec("Can we call JavaScripts valueOf?", 
            Numbers.class, "seven__DI", 
            Double.valueOf(7.0), 8
        );
    }
    @Test public void valueOfLongNumber() throws Exception {
        assertExec("Can we call JavaScripts valueOf?", 
            Numbers.class, "seven__DI", 
            Double.valueOf(Long.MAX_VALUE / 5), 9
        );
    }
    @Test public void valueOfLongCharA() throws Exception {
        assertExec("Can we call JavaScripts valueOf on Character?", 
            Numbers.class, "seven__DI", 
            Double.valueOf('A'), 65
        );
    }

    @Test public void valueOfLongBooleanTrue() throws Exception {
        assertExec("Can we call JavaScripts valueOf on Boolean?", 
            Numbers.class, "bseven__ZI", 
            true, 31
        );
    }
    @Test public void valueOfLongBooleanFalse() throws Exception {
        assertExec("Can we call JavaScripts valueOf on Boolean?", 
            Numbers.class, "bseven__ZI", 
            false, 30
        );
    }
    
    @Test public void computeAround() throws Exception {
        double exp = Numbers.around(new Object(), 5, 8);
        assertExec("Computes the same value", 
            Numbers.class, "around__ILjava_lang_Object_2II", 
            exp, null, 5, 8
        );
    }

    @Test public void stringToLong300() throws Exception {
        int res = Numbers.stringToLong("300");
        assertExec("Gives the same value",
            Numbers.class, "stringToLong__ILjava_lang_String_2",
            res, "300");
    }

    @Test public void stringToLong255() throws Exception {
        int res = Numbers.stringToLong("255");
        assertExec("Gives the same value",
            Numbers.class, "stringToLong__ILjava_lang_String_2",
            res, "255");
    }

    @Test public void stringToLong123() throws Exception {
        int res = Numbers.stringToLong("123");
        assertExec("Gives the same value",
            Numbers.class, "stringToLong__ILjava_lang_String_2",
            res, "123");
    }

    @Test public void stringToLong0() throws Exception {
        int res = Numbers.stringToLong("0");
        assertExec("Gives the same value",
            Numbers.class, "stringToLong__ILjava_lang_String_2",
            res, "0");
    }

    @Test public void longToStringNull() throws Exception {
        String res = Numbers.longToString(null, false);
        assertExec("Gives the same value",
            Numbers.class, "longToString__Ljava_lang_String_2Ljava_lang_Long_2Z",
            res, null, false);
    }

    @Test public void longToString1() throws Exception {
        String res = Numbers.longToString(1L, false);
        assertExec("Gives the same value",
            Numbers.class, "longToString__Ljava_lang_String_2Ljava_lang_Long_2Z",
            res, 1, false);
    }

    @Test public void longToStringNeg1() throws Exception {
        String res = Numbers.longToString(1L, true);
        assertExec("Gives the same value",
            Numbers.class, "longToString__Ljava_lang_String_2Ljava_lang_Long_2Z",
            res, 1, true);
    }

    private static TestVM code;

    @BeforeClass
    public static void compileTheCode() throws Exception {
        code = TestVM.compileClass("org/apidesign/vm4brwsr/Numbers");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }

    private static void assertExec(
        String msg, Class<?> clazz, String method, Object expRes, Object... args) throws Exception
    {
        code.assertExec(msg, clazz, method, expRes, args);
    }
    
}
