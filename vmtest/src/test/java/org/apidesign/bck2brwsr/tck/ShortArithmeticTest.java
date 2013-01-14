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
public class ShortArithmeticTest {
    
    private static short add(short x, short y) {
        return (short)(x + y);
    }
    
    private static short sub(short x, short y) {
        return (short)(x - y);
    }
    
    private static short mul(short x, short y) {
        return (short)(x * y);
    }
    
    private static short div(short x, short y) {
        return (short)(x / y);
    }
    
    private static short mod(short x, short y) {
        return (short)(x % y);
    }
    
    @Compare public short conversion() {
        return (short)123456;
    }
    
    @Compare public short addOverflow() {
        return add(Short.MAX_VALUE, (short)1);
    }
    
    @Compare public short subUnderflow() {
        return sub(Short.MIN_VALUE, (short)1);
    }
    
    @Compare public short addMaxShortAndMaxShort() {
        return add(Short.MAX_VALUE, Short.MAX_VALUE);
    }
    
    @Compare public short subMinShortAndMinShort() {
        return sub(Short.MIN_VALUE, Short.MIN_VALUE);
    }
    
    @Compare public short multiplyMaxShort() {
        return mul(Short.MAX_VALUE, (short)2);
    }
    
    @Compare public short multiplyMaxShortAndMaxShort() {
        return mul(Short.MAX_VALUE, Short.MAX_VALUE);
    }
    
    @Compare public short multiplyMinShort() {
        return mul(Short.MIN_VALUE, (short)2);
    }
    
    @Compare public short multiplyMinShortAndMinShort() {
        return mul(Short.MIN_VALUE, Short.MIN_VALUE);
    }
    
    @Compare public short multiplyPrecision() {
        return mul((short)17638, (short)1103);
    }
    
    @Compare public short division() {
        return div((short)1, (short)2);
    }
    
    @Compare public short divisionReminder() {
        return mod((short)1, (short)2);
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(ShortArithmeticTest.class);
    }
}
