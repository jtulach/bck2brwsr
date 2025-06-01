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
package org.apidesign.bck2brwsr.mini.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class CompareByteArrayTest {
    @Compare public int byteArraySum() {
        byte[] arr = createArray();
        return sumByteArr(arr);
    }
    
    @Compare public int countZeros() {
        int zeros = 0;
        for (Byte b : createArray()) {
            if (b == 0) {
                zeros++;
            }
        }
        return zeros;
    }
    
    private static int sumByteArr(byte[] arr) {
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }
    
    @Compare public String noOutOfBounds() {
        return atIndex(1);
    }

    @Compare public String outOfBounds() {
        return atIndex(5);
    }

    @Compare public String outOfBoundsMinus() {
        return atIndex(-1);
    }

    @Compare public String toOfBounds() {
        return toIndex(5);
    }

    @Compare public String toOfBoundsMinus() {
        return toIndex(-1);
    }
    
    @Compare public int multiArrayLength() {
         int[][] arr = new int[1][0];
         return arr[0].length;
    }

    @Compare public int multiObjectArrayLength() {
         Object[][] arr = new Object[1][0];
         return arr[0].length;
    }

    private static final int[] arr = { 0, 1, 2 };
    public static String atIndex(int at) {
        return "at@" + arr[at];
    }
    public static String toIndex(int at) {
        arr[at] = 10;
        return "ok";
    }
    
    
    @Factory
    public static Object[] create() {
        return VMTest.create(CompareByteArrayTest.class);
    }

    private byte[] createArray() {
        byte[] arr = new byte[10];
        arr[5] = 3;
        arr[7] = 8;
        return arr;
    }
}
