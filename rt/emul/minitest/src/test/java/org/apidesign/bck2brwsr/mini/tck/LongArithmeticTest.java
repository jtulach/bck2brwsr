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
package org.apidesign.bck2brwsr.mini.tck;

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
        return (-x);
    }

    private static long shl(long x, int b) {
        return (x << b);
    }

    private static long shr(long x, int b) {
        return (x >> b);
    }

    private static long ushr(long x, int b) {
        return (x >>> b);
    }

    private static long and(long x, long y) {
        return (x & y);
    }

    private static long or(long x, long y) {
        return (x | y);
    }

    private static long xor(long x, long y) {
        return (x ^ y);
    }

    private static float fadd(float x, float y) {
        return x + y;
    }

    private static double dadd(double x, double y) {
        return x + y;
    }
    
    private static long mul64shr64(long base, int mul, int shr) {
        long tmp = base * mul;
        if (shr == -1) {
            return tmp;
        }
        return tmp >> shr;
    }

    public static int compare(long x, long y, int zero) {
        final int xyResult = compareL(x, y, zero);
        final int yxResult = compareL(y, x, zero);

        return ((xyResult + yxResult) == 0) ? xyResult : -2;
    }

    private static int compareL(long x, long y, int zero) {
        int result = -2;
        int trueCount = 0;

        x += zero;
        if (x == y) {
            result = 0;
            ++trueCount;
        }

        x += zero;
        if (x < y) {
            result = -1;
            ++trueCount;
        }

        x += zero;
        if (x > y) {
            result = 1;
            ++trueCount;
        }

        return (trueCount == 1) ? result : -2;
    }
    
    @Compare public int parameterSlotCount() {
        long argCounts = 281479271874563L;
        int x = unpack(argCounts, 2);
        return x;
    }
    private static char unpack(long packed, int word) { // word==0 => return a, ==3 => return d
        assert(word <= 3);
        final long val = packed >> ((3-word) * 16);
        return (char)val;
    }
    @Compare public long conversion() {
        return Long.MAX_VALUE;
    }
    
    @Compare public long thirteen() {
        return add(10, 3);
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

    @Compare public long conversionFromFloatPositive() {
        return (long) fadd(2, 0.6f);
    }

    @Compare public long conversionFromFloatNegative() {
        return (long) fadd(-2, -0.6f);
    }

    @Compare public long conversionFromDoublePositive() {
        return (long) dadd(0x20ffff0000L, 0.6);
    }

    @Compare public long conversionFromDoubleNegative() {
        return (long) dadd(-0x20ffff0000L, -0.6);
    }

    @Compare public boolean divByZeroThrowsArithmeticException() {
        try {
            div(1, 0);
            return false;
        } catch (final ArithmeticException e) {
            return true;
        }
    }

    @Compare public boolean modByZeroThrowsArithmeticException() {
        try {
            mod(1, 0);
            return false;
        } catch (final ArithmeticException e) {
            return true;
        }
    }

    @Compare public long shiftL1() {
        return shl(0x00fa37d7763e0ca1l, 5);
    }

    @Compare public long shiftL2() {
        return shl(0x00fa37d7763e0ca1l, 32);
    }

    @Compare public long shiftL3() {
        return shl(0x00fa37d7763e0ca1l, 45);
    }
    
    @Compare public long shiftL4() {
        return shl(0x00fa37d7763e0ca1l, 0);
    }
    
    @Compare public long shiftL5() {
        return shl(0x00fa37d7763e0ca1l, 70);
    }

    @Compare public long shiftR1() {
        return shr(0x00fa37d7763e0ca1l, 5);
    }

    @Compare public long shiftR2() {
        return shr(0x00fa37d7763e0ca1l, 32);
    }

    @Compare public long shiftR3() {
        return shr(0x00fa37d7763e0ca1l, 45);
    }
    
    @Compare public long shiftR4() {
        return shr(0x00fa37d7763e0ca1l, 0);
    }
    
    @Compare public long shiftR5() {
        return shr(0x00fa37d7763e0ca1l, 70);
    }

    @Compare public long uShiftR1() {
        return ushr(0x00fa37d7763e0ca1l, 5);
    }

    @Compare public long uShiftR2() {
        return ushr(0x00fa37d7763e0ca1l, 45);
    }
    
    @Compare public long uShiftR3() {
        return ushr(0x00fa37d7763e0ca1l, 0);
    }
    
    @Compare public long uShiftR4() {
        return ushr(0x00fa37d7763e0ca1l, 70);
    }

    @Compare public long uShiftR5() {
        return ushr(0xf0fa37d7763e0ca1l, 5);
    }

    @Compare public long uShiftR6() {
        return ushr(0xf0fa37d7763e0ca1l, 45);
    }
    
    @Compare public long uShiftR7() {
        return ushr(0xf0fa37d7763e0ca1l, 0);
    }
    
    @Compare public long uShiftR8() {
        return ushr(0xf0fa37d7763e0ca1l, 70);
    }

    @Compare public long and1() {
        return and(0x00fa37d7763e0ca1l, 0xa7b3432fff00123el);
    }

    @Compare public long or1() {
        return or(0x00fa37d7763e0ca1l, 0xa7b3432fff00123el);
    }

    @Compare public long xor1() {
        return xor(0x00fa37d7763e0ca1l, 0xa7b3432fff00123el);
    }

    @Compare public long xor2() {
        return xor(0x00fa37d7763e0ca1l, 0x00000000ff00123el);
    }

    @Compare public long xor3() {
        return xor(0x00000000763e0ca1l, 0x00000000ff00123el);
    }

    @Compare public int compareSameNumbers() {
        return compare(0x0000000000000000l, 0x0000000000000000l, 0);
    }

    @Compare public int comparePositiveNumbers() {
        return compare(0x0000000000200000l, 0x0000000010000000l, 0);
    }

    @Compare public int compareNegativeNumbers() {
        return compare(0xffffffffffffffffl, 0xffffffff00000000l, 0);
    }

    @Compare public int compareMixedNumbers() {
        return compare(0x8000000000000000l, 0x7fffffffffffffffl, 0);
    }
    
    @Compare public int intConversionProblemInRandom() {
        long res = mul64shr64(16, 1561751147, 31);
        return (int) res;
    }

    @Compare public long shiftProblemInRandom() {
        long res = mul64shr64(16, 1561751147, 31);
        return res;
    }

    @Compare public long multiplyProblemInRandom() {
        long res = mul64shr64(16, 1561751147, -1);
        return res;
    }

    @Compare public double longBitsToDouble() {
        long res = mul64shr64(16, 1561751147, -1);
        double ignore = Double.longBitsToDouble(res);
        return 0.0;
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(LongArithmeticTest.class);
    }
}
