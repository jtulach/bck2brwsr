/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import org.apidesign.bck2brwsr.core.JavaScriptBody;

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
        disableClassForName();
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
        enableClassForName(prevClassForName);
        prevClassForName = null;
        String s = Object[].class.getName();
        disableClassForName();
        return s;
    }
    
    public static boolean instanceOfArray(Object obj) {
        if ("string-array".equals(obj)) {
            obj = new String[] { "Ahoj" };
        }
        return obj instanceof Object[];
    }
    
    public static boolean castArray(int type) {
        try {
            Object orig = new Object();
            Object res = orig;
            if (type == 0) {
                Object[] arr = new Integer[1];
                String[] str = (String[]) arr;
                res = str;
            }
            if (type == 1) {
                Object[] arr = null;
                String[] str = (String[]) arr;
                res = str;
            }
            if (type == 2) {
                Object[] arr = new String[1];
                String[] str = (String[]) arr;
                res = str;
            }
            if (type == 3) {
                Object[] arr = new String[1];
                CharSequence[] str = (CharSequence[]) arr;
                res = str;
            }
            return res != orig;
        } catch (ClassCastException ex) {
            return false;
        }
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
    
    @JavaScriptBody(args = {  }, body = 
        "var prev = vm.java_lang_Class(false).forName__Ljava_lang_Class_2Ljava_lang_String_2;\n"
      + "if (!prev) throw 'forName not defined';\n"
      + "vm.java_lang_Class(false).forName__Ljava_lang_Class_2Ljava_lang_String_2 = function(s) {\n"
      + "  throw 'Do not call me: ' + s;\n"
      + "};\n"
      + "return prev;\n")
    private static Object disableClassForNameImpl() {
        return null;
    }
    
    private static void disableClassForName() {
        if (prevClassForName == null) {
            prevClassForName = disableClassForNameImpl();
        }
    }
    
    @JavaScriptBody(args = { "fn" }, body = 
        "if (fn !== null) vm.java_lang_Class(false).forName__Ljava_lang_Class_2Ljava_lang_String_2 = fn;"
    )
    private static void enableClassForName(Object fn) {
    }
    
    private static Object prevClassForName;
    public static String nameOfClonedComponent() {
        disableClassForName();
        Object[] intArr = new Integer[10];
        intArr = intArr.clone();
        return intArr.getClass().getComponentType().getName();
    }
    
    public static int multiLen() {
        return new int[1][0].length;
    }
    
    @JavaScriptBody(args = { "arr" }, body = 
        "var cnt = '';\n" +
        "if (arr === null) arr = [];\n" +
        "for (var i in arr) { cnt += i; }\n" +
        "return cnt;\n"
    )
    private static native String iterateArray(Object[] arr);
    
    public static String iterateArray(boolean javaArray) {
        return iterateArray(javaArray ? new String[0] : null);
    }
    
    enum ShapeType {
        ZERO, ONE;
    }
    
    private static void addType(int[][] table, ShapeType type1, ShapeType type2, int value) {
        table[type1.ordinal()][type2.ordinal()] = value;
    }
    
    public static int sumTable() {
        int[][] arr = { { 99, 99 }, { 999, 999 }};
        addType(arr, ShapeType.ZERO, ShapeType.ZERO, 0);
        addType(arr, ShapeType.ZERO, ShapeType.ONE, 1);
        addType(arr, ShapeType.ONE, ShapeType.ZERO, 1);
        addType(arr, ShapeType.ONE, ShapeType.ONE, 2);
        
        int sum = 0;
        for (int[] row : arr) {
            for (int i : row) {
                sum += i;
            }
        }
        return sum;
    }
    
    private static int inc(int v) {
        return v + 1;
    }
    
    public static int plusOrd() {
        return inc(ShapeType.ZERO.ordinal()) + inc(ShapeType.ONE.ordinal());
    }
}
