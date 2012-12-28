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
