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

import java.lang.reflect.Array;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ReflectionArrayTest {
    @Compare public int lengthOfStringArray() {
        String[] arr = (String[]) Array.newInstance(String.class, 10);
        return arr.length;
    }
    
    @Compare public int reflectiveLengthOfStringArray() {
        Object arr = Array.newInstance(String.class, 10);
        return Array.getLength(arr);
    }

    @Compare public int reflectiveLengthOneNonArray() {
        Object arr = "non-array";
        return Array.getLength(arr);
    }

    @Compare public String compTypeOfStringArray() {
        String[] arr = (String[]) Array.newInstance(String.class, 10);
        return arr.getClass().getComponentType().getName();
    }

    @Compare public Object negativeArrayExcp() {
        return Array.newInstance(String.class, -5);
    }
    
    @Compare public int lengthOfIntArray() {
        int[] arr = (int[]) Array.newInstance(Integer.TYPE, 10);
        return arr.length;
    }

    @Compare public int reflectiveLengthOfIntArray() {
        Object arr = Array.newInstance(Integer.TYPE, 10);
        return Array.getLength(arr);
    }

    @Compare public String compTypeOfIntArray() {
        int[] arr = (int[]) Array.newInstance(int.class, 10);
        return arr.getClass().getComponentType().getName();
    }

    @Compare public Object intNegativeArrayExcp() {
        return Array.newInstance(int.class, -5);
    }

    @Compare public Integer verifyAutobox() {
        int[] arr = (int[]) Array.newInstance(int.class, 5);
        return (Integer) Array.get(arr, 0);
    }
    @Compare public int verifyInt() {
        int[] arr = (int[]) Array.newInstance(int.class, 5);
        return Array.getInt(arr, 0);
    }
    @Compare public long verifyConvertToLong() {
        int[] arr = (int[]) Array.newInstance(int.class, 5);
        return Array.getLong(arr, 0);
    }

    @Compare public Object verifySetIntToObject() {
        Object[] arr = (Object[]) Array.newInstance(Object.class, 5);
        Array.setInt(arr, 0, 10);
        return Array.get(arr, 0);
    }
    @Compare public long verifySetShort() {
        int[] arr = (int[]) Array.newInstance(int.class, 5);
        Array.setShort(arr, 0, (short)10);
        return Array.getLong(arr, 0);
    }
    @Compare public long verifyCantSetLong() {
        int[] arr = (int[]) Array.newInstance(int.class, 5);
        Array.setLong(arr, 0, 10);
        return Array.getLong(arr, 0);
    }

    @Compare public double verifyConvertToDouble() {
        int[] arr = (int[]) Array.newInstance(int.class, 5);
        return Array.getDouble(arr, 0);
    }
    
    @Compare public int multiIntArray() {
        int[][][] arr = (int[][][]) Array.newInstance(int.class, 3, 3, 3);
        return arr[0][1][2] + 5 + arr[2][2][0];
    }

    @Compare public String multiIntArrayCompType() {
        return Array.newInstance(int.class, 3, 3, 3).getClass().getName();
    }
    
    
    @Factory
    public static Object[] create() {
        return VMTest.create(ReflectionArrayTest.class);
    }
}
