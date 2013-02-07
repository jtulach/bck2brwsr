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
    
    @Compare public long conversion() {
        return Long.MAX_VALUE;
    }
    
    /*
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
        return mul(17638l, 1103l);
    }
    
    @Compare public long division() {
        return div(1l, 2l);
    }
    
    @Compare public long divisionReminder() {
        return mod(1l, 2l);
    }
    */
    
    @Factory
    public static Object[] create() {
        return VMTest.create(LongArithmeticTest.class);
    }
}
