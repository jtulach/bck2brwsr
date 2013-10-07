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
    
    static double[][] dbls = new double[1][2];
    public static double twoDoubles() {
        return dbls[0][0] + dbls[0][0];
    }

    static int[][] tints = new int[1][2];
    public static int twoInts() {
        return tints[0][0] + tints[0][0];
    }
    
    private static final Array[] ARR = { new Array(), new Array(), new Array() };
    
    private static Array[][] arr() {
        Array[][] matrix = new Array[3][3];
        for (int i = 0; i < ARR.length; i++) {
            matrix[i][i] = ARR[i];
        }
        return matrix;
    }
    private static <T> T[] filter(T[] in) {
        return in;
    }
    
    public static double sum() {
        double sum = 0.0;
        for (Array[] row : arr()) {
            int indx = -1;
            for (Array a : row) {
                indx++;
                if (a == null) {
                    continue;
                }
                sum += a.bytes();
                sum += a.shorts();
                sum += a.ints()[2];
                sum += a.floats();
                sum += filter(row)[indx].doubles();
            }
        }
        return sum;
    }
    private static final int[] arr = { 0, 1, 2, 3, 4, 5 };
    public static int simple(boolean clone) {
        int[] ar;
        if (clone) {
            ar = arr.clone();
        } else {
            ar = arr;
        }
        
        int sum = 0;
        for (int a : ar) {
            sum += a;
        }
        return sum;
    }
    
    public static String objectArrayClass() {
        return Object[].class.getName();
    }
    
    public static boolean instanceOfArray(Object obj) {
        return obj instanceof Object[];
    }
    
    public static int sum(int size) {
        int[] arr = new int[size];
        return arr[0] + arr[1];
    }
    
    static void arraycopy(char[] value, int srcBegin, char[] dst, int dstBegin, int count) {
        while (count-- > 0) {
            dst[dstBegin++] = value[srcBegin++];
        }
    }

    public static char copyArray() {
        char[] arr = { '0' };
        arraycopy(arr()[0][0].chars, 0, arr, 0, 1);
        return arr[0];
    }
    
    public static int multiLen() {
        return new int[1][0].length;
    }
}
