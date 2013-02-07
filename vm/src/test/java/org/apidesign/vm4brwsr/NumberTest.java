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

import javax.script.Invocable;
import javax.script.ScriptException;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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
    /* XXX: JavaScript cannot represent as big longs as Java. 
    @Test public void deserializeLargeLong() throws Exception {
        final byte[] arr = new byte[] {
            (byte)64, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0
        };
        long exp = Numbers.deserLong(arr);
        assertExec("Should be " + exp, "org_apidesign_vm4brwsr_Numbers_deserLong__JAB", 
            Double.valueOf(exp), arr);
    }
    */
    
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
    
    @Test public void longConversion() throws Exception {
        assertExec("Long from cPool",
            Numbers.class, "conversionL__J", 
            Double.valueOf(Long.MAX_VALUE)
        );
    }
    
    @Test public void longNegate1() throws Exception {
        final long res = -0x00fa37d7763e0ca1l;
        assertExec("Long negate",
            Numbers.class, "negL__J_3B", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 }
        );
    }
    
    @Test public void longNegate2() throws Exception {
        final long res = -0x80fa37d7763e0ca1l;
        assertExec("Long negate",
            Numbers.class, "negL__J_3B", 
            Double.valueOf(res),
                new byte[] { (byte)0x80, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 }
        );
    }

    @Test public void longNegate3() throws Exception {
        final long res = -0xfffffffffffffeddl;
        assertExec("Long negate",
            Numbers.class, "negL__J_3B",
            Double.valueOf(res),
                new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfe, (byte)0xdd }
        );
    }

    @Test public void longAddOverflow() throws Exception {
        final long res = Long.MAX_VALUE + 1l;
        assertExec("Addition 1+MAX",
            Numbers.class, "addL__J_3B_3B", 
            Double.valueOf(res),
                new byte[] { (byte)0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff },
                new byte[] { (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)1 }
        );
    }
    
    @Test public void longAddMaxAndMax() throws Exception {
        final long res = Long.MAX_VALUE + Long.MAX_VALUE;
        assertExec("Addition MAX+MAX",
            Numbers.class, "addL__J_3B_3B", 
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff },
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff }
        );
    }
    
    @Test public void longSubUnderflow() throws Exception {
        final long res = Long.MIN_VALUE - 1l;
        assertExec("Subtraction MIN-1",
            Numbers.class, "subL__J_3B_3B", 
            Double.valueOf(res),
                new byte[] { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
                new byte[] { (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)1 }
        );
    }
    
    @Test public void longSubMinAndMin() throws Exception {
        final long res = Long.MIN_VALUE - Long.MIN_VALUE;
        assertExec("Subtraction MIN-MIN",
            Numbers.class, "subL__J_3B_3B", 
            Double.valueOf(res),
            new byte[] { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 }
        );
    }
    
    @Test public void longSubMinAndMax() throws Exception {
        final long res = Long.MIN_VALUE - Long.MAX_VALUE;
        assertExec("Subtraction MIN-MAX",
            Numbers.class, "subL__J_3B_3B", 
            Double.valueOf(res),
            new byte[] { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff }
        );
    }
    
    @Test public void longMultiplyMax() throws Exception {
        final long res = Long.MAX_VALUE * 2l;
        assertExec("Multiplication MAX*2",
            Numbers.class, "mulL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02 }
        );
    }
    
    @Test public void longMultiplyMaxAndMax() throws Exception {
        final long res = Long.MAX_VALUE * Long.MAX_VALUE;
        assertExec("Multiplication MAX*MAX",
            Numbers.class, "mulL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff },
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff }
        );
    }
    
    @Test public void longMultiplyMin() throws Exception {
        final long res = Long.MIN_VALUE * 2l;
        assertExec("Multiplication MIN*2",
            Numbers.class, "mulL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02 }
        );
    }
    
    @Test public void longMultiplyMinAndMin() throws Exception {
        final long res = Long.MIN_VALUE * Long.MIN_VALUE;
        assertExec("Multiplication MIN*2",
            Numbers.class, "mulL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 }
        );
    }
    
    @Test public void longMultiplyPrecision() throws Exception {
        final long res = 0x00fa37d7763e0ca1l * 0xa7b3432fff00123el;
        assertExec("Multiplication",
            Numbers.class, "mulL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
            new byte[] { (byte)0xa7, (byte)0xb3, (byte)0x43, (byte)0x2f, (byte)0xff, (byte)0x00, (byte)0x12, (byte)0x3e }
        );
    }

    @Test public void longDivideSmallPositiveNumbers() throws Exception {
        final long res = 0xabcdef / 0x123;
        assertExec("Division Small Positive Numbers",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xab, (byte)0xcd, (byte)0xef },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x23 }
        );
    }

    @Test public void longDivideSmallNegativeNumbers() throws Exception {
        final long res = -0xabcdef / -0x123;
        assertExec("Division Small Negative Numbers",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x54, (byte)0x32, (byte)0x11 },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfe, (byte)0xdd }
        );
    }

    @Test public void longDivideSmallMixedNumbers() throws Exception {
        final long res = 0xabcdef / -0x123;
        assertExec("Division Small Mixed Numbers",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xab, (byte)0xcd, (byte)0xef },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfe, (byte)0xdd }
        );
    }

    @Test public void longDividePositiveNumbersOneDigitDenom()
            throws Exception {
        final long res = 0xabcdef0102ffffL / 0x654;
        assertExec("Division Positive Numbers One Digit Denom",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0xab, (byte)0xcd, (byte)0xef, (byte)0x01, (byte)0x02, (byte)0xff, (byte)0xff },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x54 }
        );
    }

    @Test public void longDivideNegativeNumbersOneDigitDenom()
            throws Exception {
        final long res = -0xabcdef0102ffffL / -0x654;
        assertExec("Division Negative Numbers One Digit Denom",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0xff, (byte)0x54, (byte)0x32, (byte)0x10, (byte)0xfe, (byte)0xfd, (byte)0x00, (byte)0x01 },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xf9, (byte)0xac }
        );
    }

    @Test public void longDivideMixedNumbersOneDigitDenom()
            throws Exception {
        final long res = -0xabcdef0102ffffL / 0x654;
        assertExec("Division Mixed Numbers One Digit Denom",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0xff, (byte)0x54, (byte)0x32, (byte)0x10, (byte)0xfe, (byte)0xfd, (byte)0x00, (byte)0x01 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x54 }
        );
    }

    @Test public void longDividePositiveNumbersMultiDigitDenom()
            throws Exception {
        final long res = 0x7ffefc003322aabbL / 0x89ab1000L;
        assertExec("Division Positive Numbers Multi Digit Denom",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xfe, (byte)0xfc, (byte)0x00, (byte)0x33, (byte)0x22, (byte)0xaa, (byte)0xbb },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x89, (byte)0xab, (byte)0x10, (byte)0x00 }
        );
    }

    @Test public void longDivideNegativeNumbersMultiDigitDenom()
            throws Exception {
        final long res = -0x7ffefc003322aabbL / -0x123489ab1001L;
        assertExec("Division Negative Numbers Multi Digit Denom",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x80, (byte)0x01, (byte)0x03, (byte)0xff, (byte)0xcc, (byte)0xdd, (byte)0x55, (byte)0x45 },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xed, (byte)0xcb, (byte)0x76, (byte)0x54, (byte)0xef, (byte)0xff }
        );
    }

    @Test public void longDivideMixedNumbersMultiDigitDenom()
            throws Exception {
        final long res = 0x7ffefc003322aabbL / -0x38f49b0b7574e36L;
        assertExec("Division Mixed Numbers Multi Digit Denom",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xfe, (byte)0xfc, (byte)0x00, (byte)0x33, (byte)0x22, (byte)0xaa, (byte)0xbb },
            new byte[] { (byte)0xfc, (byte)0x70, (byte)0xb6, (byte)0x4f, (byte)0x48, (byte)0xa8, (byte)0xb1, (byte)0xca }
        );
    }

    @Test public void longDivideWithOverflow() throws Exception {
        final long res = 0x8000fffe0000L / 0x8000ffffL;
        assertExec("Division With Overflow",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0xff, (byte)0xfe, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0xff, (byte)0xff }
        );
    }

    @Test public void longDivideWithCorrection() throws Exception {
        final long res = 0x7fff800000000000L / 0x800000000001L;
        assertExec("Division With Correction",
            Numbers.class, "divL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01 }
        );
    }

    @Test public void longModuloSmallPositiveNumbers() throws Exception {
        final long res = 0xabcdef % 0x123;
        assertExec("Modulo Small Positive Numbers",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xab, (byte)0xcd, (byte)0xef },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x23 }
        );
    }

    @Test public void longModuloSmallNegativeNumbers() throws Exception {
        final long res = -0xabcdef % -0x123;
        assertExec("Modulo Small Negative Numbers",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x54, (byte)0x32, (byte)0x11 },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfe, (byte)0xdd }
        );
    }

    @Test public void longModuloSmallMixedNumbers() throws Exception {
        final long res = 0xabcdef % -0x123;
        assertExec("Modulo Small Mixed Numbers",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xab, (byte)0xcd, (byte)0xef },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xfe, (byte)0xdd }
        );
    }

    @Test public void longModuloPositiveNumbersOneDigitDenom()
            throws Exception {
        final long res = 0xabcdef0102ffffL % 0x654;
        assertExec("Modulo Positive Numbers One Digit Denom",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0xab, (byte)0xcd, (byte)0xef, (byte)0x01, (byte)0x02, (byte)0xff, (byte)0xff },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x54 }
        );
    }

    @Test public void longModuloNegativeNumbersOneDigitDenom()
            throws Exception {
        final long res = -0xabcdef0102ffffL % -0x654;
        assertExec("Modulo Negative Numbers One Digit Denom",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0xff, (byte)0x54, (byte)0x32, (byte)0x10, (byte)0xfe, (byte)0xfd, (byte)0x00, (byte)0x01 },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xf9, (byte)0xac }
        );
    }

    @Test public void longModuloMixedNumbersOneDigitDenom()
            throws Exception {
        final long res = -0xabcdef0102ffffL % 0x654;
        assertExec("Modulo Mixed Numbers One Digit Denom",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0xff, (byte)0x54, (byte)0x32, (byte)0x10, (byte)0xfe, (byte)0xfd, (byte)0x00, (byte)0x01 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x54 }
        );
    }

    @Test public void longModuloPositiveNumbersMultiDigitDenom()
            throws Exception {
        final long res = 0x7ffefc003322aabbL % 0x89ab1000L;
        assertExec("Modulo Positive Numbers Multi Digit Denom",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xfe, (byte)0xfc, (byte)0x00, (byte)0x33, (byte)0x22, (byte)0xaa, (byte)0xbb },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x89, (byte)0xab, (byte)0x10, (byte)0x00 }
        );
    }

    @Test public void longModuloNegativeNumbersMultiDigitDenom()
            throws Exception {
        final long res = -0x7ffefc003322aabbL % -0x123489ab1001L;
        assertExec("Modulo Negative Numbers Multi Digit Denom",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x80, (byte)0x01, (byte)0x03, (byte)0xff, (byte)0xcc, (byte)0xdd, (byte)0x55, (byte)0x45 },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xed, (byte)0xcb, (byte)0x76, (byte)0x54, (byte)0xef, (byte)0xff }
        );
    }

    @Test public void longModuloMixedNumbersMultiDigitDenom()
            throws Exception {
        final long res = 0x7ffefc003322aabbL % -0x38f49b0b7574e36L;
        assertExec("Modulo Mixed Numbers Multi Digit Denom",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xfe, (byte)0xfc, (byte)0x00, (byte)0x33, (byte)0x22, (byte)0xaa, (byte)0xbb },
            new byte[] { (byte)0xfc, (byte)0x70, (byte)0xb6, (byte)0x4f, (byte)0x48, (byte)0xa8, (byte)0xb1, (byte)0xca }
        );
    }

    @Test public void longModuloWithOverflow() throws Exception {
        final long res = 0x8000fffe0000L % 0x8000ffffL;
        assertExec("Modulo With Overflow",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0xff, (byte)0xfe, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0xff, (byte)0xff }
        );
    }

    @Test public void longModuloWithCorrection() throws Exception {
        final long res = 0x7fff800000000000L % 0x800000000001L;
        assertExec("Modulo With Correction",
            Numbers.class, "modL__J_3B_3B",
            Double.valueOf(res),
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01 }
        );
    }

    @Test public void longShiftL1() throws Exception {
        final long res = 0x00fa37d7763e0ca1l << 5;
        assertExec("Long << 5",
            Numbers.class, "shlL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                5);
    }
    
    @Test public void longShiftL2() throws Exception {
        final long res = 0x00fa37d7763e0ca1l << 32;
        assertExec("Long << 32",
            Numbers.class, "shlL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                32);
    }
    
    @Test public void longShiftL3() throws Exception {
        final long res = 0x00fa37d7763e0ca1l << 45;
        assertExec("Long << 45",
            Numbers.class, "shlL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                45);
    }
    
    @Test public void longShiftR1() throws Exception {
        final long res = 0x00fa37d7763e0ca1l >> 5;
        assertExec("Long >> 5",
            Numbers.class, "shrL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                5);
    }
    
    @Test public void longShiftR2() throws Exception {
        final long res = 0x00fa37d7763e0ca1l >> 32;
        assertExec("Long >> 32",
            Numbers.class, "shrL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                32);
    }
    
    @Test public void longShiftR3() throws Exception {
        final long res = 0x00fa37d7763e0ca1l >> 45;
        assertExec("Long >> 45",
            Numbers.class, "shrL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                45);
    }
    
    @Test public void longUShiftR1() throws Exception {
        final long res = 0x00fa37d7763e0ca1l >>> 5;
        assertExec("Long >>> 5",
            Numbers.class, "ushrL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                5);
    }
    
    @Test public void longUShiftR2() throws Exception {
        final long res = 0x00fa37d7763e0ca1l >>> 45;
        assertExec("Long >>> 45",
            Numbers.class, "ushrL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                45);
    }
    
    @Test public void longUShiftR3() throws Exception {
        final long res = 0xf0fa37d7763e0ca1l >>> 5;
        assertExec("Long >>> 5",
            Numbers.class, "ushrL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0xf0, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                5);
    }
    
    @Test public void longUShiftR4() throws Exception {
        final long res = 0xf0fa37d7763e0ca1l >>> 45;
        assertExec("Long >>> 45",
            Numbers.class, "ushrL__J_3BI", 
            Double.valueOf(res),
                new byte[] { (byte)0xf0, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
                45);
    }
    
    @Test public void longAnd() throws Exception {
        final long res = 0x00fa37d7763e0ca1l & 0xa7b3432fff00123el;
        assertExec("LOng binary AND",
            Numbers.class, "andL__J_3B_3B", 
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
            new byte[] { (byte)0xa7, (byte)0xb3, (byte)0x43, (byte)0x2f, (byte)0xff, (byte)0x00, (byte)0x12, (byte)0x3e }
        );
    }
    
    @Test public void longOr() throws Exception {
        final long res = 0x00fa37d7763e0ca1l | 0xa7b3432fff00123el;
        assertExec("Long binary OR",
            Numbers.class, "orL__J_3B_3B", 
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
            new byte[] { (byte)0xa7, (byte)0xb3, (byte)0x43, (byte)0x2f, (byte)0xff, (byte)0x00, (byte)0x12, (byte)0x3e }
        );
    }
    
    @Test public void longXor1() throws Exception {
        final long res = 0x00fa37d7763e0ca1l ^ 0xa7b3432fff00123el;
        assertExec("Long binary XOR",
            Numbers.class, "xorL__J_3B_3B", 
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
            new byte[] { (byte)0xa7, (byte)0xb3, (byte)0x43, (byte)0x2f, (byte)0xff, (byte)0x00, (byte)0x12, (byte)0x3e }
        );
    }
    
    @Test public void longXor2() throws Exception {
        final long res = 0x00fa37d7763e0ca1l ^ 0x00000000ff00123el;
        assertExec("Long binary XOR",
            Numbers.class, "xorL__J_3B_3B", 
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0xfa, (byte)0x37, (byte)0xd7, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x00, (byte)0x12, (byte)0x3e }
        );
    }
    
    @Test public void longXor3() throws Exception {
        final long res = 0x00000000763e0ca1l ^ 0x00000000ff00123el;
        assertExec("Long binary XOR",
            Numbers.class, "xorL__J_3B_3B", 
            Double.valueOf(res),
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x76, (byte)0x3e, (byte)0x0c, (byte)0xa1 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xff, (byte)0x00, (byte)0x12, (byte)0x3e }
        );
    }

    @Test public void longCompareSameNumbers() throws Exception {
        assertExec("Long compare same numbers",
            Numbers.class, "compareL__I_3B_3BI",
            0.0,
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            0
        );
    }

    @Test public void longComparePositiveNumbers() throws Exception {
        assertExec("Long compare positive numbers",
            Numbers.class, "compareL__I_3B_3BI",
            -1.0,
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00 },
            0
        );
    }

    @Test public void longCompareNegativeNumbers() throws Exception {
        assertExec("Long compare negative numbers",
            Numbers.class, "compareL__I_3B_3BI",
            1.0,
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff },
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            0
        );
    }

    @Test public void longCompareMixedNumbers() throws Exception {
        assertExec("Long compare mixed numbers",
            Numbers.class, "compareL__I_3B_3BI",
            -1.0,
            new byte[] { (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 },
            new byte[] { (byte)0x7f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff },
            0
        );
    }

    private static CharSequence codeSeq;
    private static Invocable code;

    @BeforeClass
    public void compileTheCode() throws Exception {
        if (codeSeq == null) {
            StringBuilder sb = new StringBuilder();
            code = StaticMethodTest.compileClass(sb, "org/apidesign/vm4brwsr/Numbers");
            codeSeq = sb;
        }
    }

    private static void assertExec(
        String msg, Class<?> clazz, String method, Object expRes, Object... args) throws Exception
    {
        Object ret = TestUtils.execCode(code, codeSeq, msg, clazz, method, expRes, args);
        if (ret == null) {
            return;
        }
        if (expRes instanceof Double && ret instanceof Double) {
            double expD = ((Double)expRes).doubleValue();
            double retD = ((Double)ret).doubleValue();
            assertEquals(retD, expD, 0.000004, msg + " "
                    + StaticMethodTest.dumpJS(codeSeq));
            return;
        }
        assertEquals(ret, expRes, msg + " " + StaticMethodTest.dumpJS(codeSeq));
    }
    
}
