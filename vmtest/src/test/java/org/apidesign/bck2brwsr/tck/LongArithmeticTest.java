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
package org.apidesign.bck2brwsr.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LongArithmeticTest {
    
    private static long add(long x, long y) {
        return (x + y);
    }
    
    private static long sub(long x, long y) {
        return (x - y);
    }
    
    private static long mul(long x, long y) {
        return (x * y);
    }
    
    private static long div(long x, long y) {
        return (x / y);
    }
    
    private static long mod(long x, long y) {
        return (x % y);
    }
    
    private static long neg(long x) {
        return -x;
    }
    
    @Compare public long conversion() {
        return Long.MAX_VALUE;
    }
    
    @Compare public long negate1() {
        return neg(0x00fa37d7763e0ca1l);
    }
    
    @Compare public long negate2() {
        return neg(0x80fa37d7763e0ca1l);
    }

    @Compare public long negate3() {
        return neg(0xfffffffffffffeddl);
    }

    @Compare public long addOverflow() {
        return add(Long.MAX_VALUE, 1l);
    }

    @Compare public long subUnderflow() {
        return sub(Long.MIN_VALUE, 1l);
    }

    @Compare public long addMaxLongAndMaxLong() {
        return add(Long.MAX_VALUE, Long.MAX_VALUE);
    }
    
    @Compare public long subMinLongAndMinLong() {
        return sub(Long.MIN_VALUE, Long.MIN_VALUE);
    }
    
    @Compare public long subMinLongAndMaxLong() {
        return sub(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Compare public long multiplyMaxLong() {
        return mul(Long.MAX_VALUE, 2l);
    }
    
    @Compare public long multiplyMaxLongAndMaxLong() {
        return mul(Long.MAX_VALUE, Long.MAX_VALUE);
    }
    
    @Compare public long multiplyMinLong() {
        return mul(Long.MIN_VALUE, 2l);
    }
    
    @Compare public long multiplyMinLongAndMinLong() {
        return mul(Long.MIN_VALUE, Long.MIN_VALUE);
    }
    
    @Compare public long multiplyPrecision() {
        return mul(0x00fa37d7763e0ca1l, 0xa7b3432fff00123el);
    }
    
    @Compare public long divideSmallPositiveNumbers() {
        return div(0xabcdef, 0x123);
    }

    @Compare public long divideSmallNegativeNumbers() {
        return div(-0xabcdef, -0x123);
    }

    @Compare public long divideSmallMixedNumbers() {
        return div(0xabcdef, -0x123);
    }

    @Compare public long dividePositiveNumbersOneDigitDenom() {
        return div(0xabcdef0102ffffl, 0x654);
    }

    @Compare public long divideNegativeNumbersOneDigitDenom() {
        return div(-0xabcdef0102ffffl, -0x654);
    }

    @Compare public long divideMixedNumbersOneDigitDenom() {
        return div(-0xabcdef0102ffffl, 0x654);
    }

    @Compare public long dividePositiveNumbersMultiDigitDenom() {
        return div(0x7ffefc003322aabbl, 0x89ab1000l);
    }

    @Compare public long divideNegativeNumbersMultiDigitDenom() {
        return div(-0x7ffefc003322aabbl, -0x123489ab1001l);
    }

    @Compare public long divideMixedNumbersMultiDigitDenom() {
        return div(0x7ffefc003322aabbl, -0x38f49b0b7574e36l);
    }

    @Compare public long divideWithOverflow() {
        return div(0x8000fffe0000l, 0x8000ffffl);
    }

    @Compare public long divideWithCorrection() {
        return div(0x7fff800000000000l, 0x800000000001l);
    }

    @Compare public long moduloSmallPositiveNumbers() {
        return mod(0xabcdef, 0x123);
    }

    @Compare public long moduloSmallNegativeNumbers() {
        return mod(-0xabcdef, -0x123);
    }

    @Compare public long moduloSmallMixedNumbers() {
        return mod(0xabcdef, -0x123);
    }

    @Compare public long moduloPositiveNumbersOneDigitDenom() {
        return mod(0xabcdef0102ffffl, 0x654);
    }

    @Compare public long moduloNegativeNumbersOneDigitDenom() {
        return mod(-0xabcdef0102ffffl, -0x654);
    }

    @Compare public long moduloMixedNumbersOneDigitDenom() {
        return mod(-0xabcdef0102ffffl, 0x654);
    }

    @Compare public long moduloPositiveNumbersMultiDigitDenom() {
        return mod(0x7ffefc003322aabbl, 0x89ab1000l);
    }

    @Compare public long moduloNegativeNumbersMultiDigitDenom() {
        return mod(-0x7ffefc003322aabbl, -0x123489ab1001l);
    }

    @Compare public long moduloMixedNumbersMultiDigitDenom() {
        return mod(0x7ffefc003322aabbl, -0x38f49b0b7574e36l);
    }

    @Compare public long moduloWithOverflow() {
        return mod(0x8000fffe0000l, 0x8000ffffl);
    }

    @Compare public long moduloWithCorrection() {
        return mod(0x7fff800000000000l, 0x800000000001l);
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(LongArithmeticTest.class);
    }
}
