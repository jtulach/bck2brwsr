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
package org.apidesign.vm4brwsr.tck;

import org.apidesign.vm4brwsr.Compare;
import org.apidesign.vm4brwsr.CompareVMs;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class IntegerArithmeticTest {
    
    private static int add(int x, int y) {
        return x + y;
    }
    
    private static int sub(int x, int y) {
        return x - y;
    }
    
    private static int mul(int x, int y) {
        return x * y;
    }
    
    private static int div(int x, int y) {
        return x / y;
    }
    
    private static int mod(int x, int y) {
        return x % y;
    }
    
    @Compare public int addOverflow() {
        return add(Integer.MAX_VALUE, 1);
    }
    
    @Compare public int subUnderflow() {
        return sub(Integer.MIN_VALUE, 1);
    }
    
    @Compare public int addMaxIntAndMaxInt() {
        return add(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    @Compare public int subMinIntAndMinInt() {
        return sub(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
    
    @Compare public int multiplyMaxInt() {
        return mul(Integer.MAX_VALUE, 2);
    }
    
    @Compare public int multiplyMaxIntAndMaxInt() {
        return mul(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    @Compare public int multiplyMinInt() {
        return mul(Integer.MIN_VALUE, 2);
    }
    
    @Compare public int multiplyMinIntAndMinInt() {
        return mul(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
    
    @Compare public int multiplyPrecision() {
        return mul(119106029, 1103515245);
    }
    
    @Compare public int division() {
        return div(1, 2);
    }
    
    @Compare public int divisionReminder() {
        return mod(1, 2);
    }
    
    @Factory
    public static Object[] create() {
        return CompareVMs.create(IntegerArithmeticTest.class);
    }
}
