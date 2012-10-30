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

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Array {
    byte[] bytes = { 1 };
    short[] shorts = { 2, 3 };
    int[] ints = { 4, 5, 6 };
    float[] floats = { 7, 8, 9, 10 };
    double[][] doubles = { {11}, {12}, {13}, {14}, {15} };
    char[] chars = { 'a', 'b' };
    
    private Array() {
    }
    
    byte bytes() {
        return bytes[0];
    }
    short shorts() {
        return shorts[1];
    }
    
    int[] ints() {
        return ints;
    }
    
    float floats() {
        return floats[3];
    }
    
    double doubles() {
        return doubles[4][0];
    }
    
    private static final Array[] ARR = { new Array(), new Array(), new Array() };
    
    private static Array[] arr() {
        return ARR;
    }
    private static <T> T[] filter(T[] in) {
        return in;
    }
    
    public static double sum() {
        double sum = 0.0;
        for (int i = 0; i < arr().length; i++) {
            sum += arr()[i].bytes();
            sum += arr()[i].shorts();
            sum += arr()[i].ints()[2];
            sum += arr()[i].floats();
            sum += filter(arr())[i].doubles();
        }
        return sum;
    }
    public static int simple() {
        int[] arr = { 0, 1, 2, 3, 4, 5 };
        
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }
    
    static void arraycopy(char[] value, int srcBegin, char[] dst, int dstBegin, int count) {
        while (count-- > 0) {
            dst[dstBegin++] = value[srcBegin++];
        }
    }

    public static char copyArray() {
        char[] arr = { '0' };
        arraycopy(arr()[0].chars, 0, arr, 0, 1);
        return arr[0];
    }
}
