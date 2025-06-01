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

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class ByteArithmeticTest {
    
    private static byte add(byte x, byte y) {
        return (byte)(x + y);
    }
    
    private static byte sub(byte x, byte y) {
        return (byte)(x - y);
    }
    
    private static byte mul(byte x, byte y) {
        return (byte)(x * y);
    }
    
    private static byte div(byte x, byte y) {
        return (byte)(x / y);
    }
    
    private static byte mod(byte x, byte y) {
        return (byte)(x % y);
    }
    
    @Compare public byte conversion() {
        return (byte)123456;
    }
    
    @Compare public byte addOverflow() {
        return add(Byte.MAX_VALUE, (byte)1);
    }
    
    @Compare public byte subUnderflow() {
        return sub(Byte.MIN_VALUE, (byte)1);
    }
    
    @Compare public byte addMaxByteAndMaxByte() {
        return add(Byte.MAX_VALUE, Byte.MAX_VALUE);
    }
    
    @Compare public byte subMinByteAndMinByte() {
        return sub(Byte.MIN_VALUE, Byte.MIN_VALUE);
    }
    
    @Compare public byte multiplyMaxByte() {
        return mul(Byte.MAX_VALUE, (byte)2);
    }
    
    @Compare public byte multiplyMaxByteAndMaxByte() {
        return mul(Byte.MAX_VALUE, Byte.MAX_VALUE);
    }
    
    @Compare public byte multiplyMinByte() {
        return mul(Byte.MIN_VALUE, (byte)2);
    }
    
    @Compare public byte multiplyMinByteAndMinByte() {
        return mul(Byte.MIN_VALUE, Byte.MIN_VALUE);
    }
    
    @Compare public byte multiplyPrecision() {
        return mul((byte)17638, (byte)1103);
    }
    
    @Compare public byte division() {
        return div((byte)1, (byte)2);
    }
    
    @Compare public byte divisionReminder() {
        return mod((byte)1, (byte)2);
    }

    private static int readShort(byte[] byteCodes, int offset) {
        int signed = byteCodes[offset];
        byte b0 = (byte)signed;
        return (b0 << 8) | (byteCodes[offset + 1] & 0xff);
    }
    
    private static int readShortArg(byte[] byteCodes, int offsetInstruction) {
        return readShort(byteCodes, offsetInstruction + 1);
    }
    
    @Compare public int readIntArgs255and156() {
        final byte[] arr = new byte[] { (byte)0, (byte)255, (byte)156 };
        
        assert arr[1] == -1 : "First byte: " + arr[1];
        assert arr[2] == -100 : "Second byte: " + arr[2];
        final int ret = readShortArg(arr, 0);
        assert ret < 65000: "Value: " + ret;
        return ret;
    }
    
    @JavaScriptBody(args = { "arr" }, body = "arr[1] = 255; arr[2] = 156; return arr;")
    private static byte[] fill255and156(byte[] arr) {
        arr[1] = (byte)255;
        arr[2] = (byte)156;
        return arr;
    }

    @Compare public int readIntArgs255and156JSArray() {
        final byte[] arr = fill255and156(new byte[] { 0, 0, 0 });
        
        final int ret = readShortArg(arr, 0);
        assert ret < 65000: "Value: " + ret;
        return ret;
    }
    
    @Compare public int readIntArgsMinus1andMinus100() {
        final byte[] arr = new byte[] { (byte)0, (byte)-1, (byte)-100 };
        
        assert arr[1] == -1 : "First byte: " + arr[1];
        assert arr[2] == -100 : "Second byte: " + arr[2];
        
        return readShortArg(arr, 0);
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(ByteArithmeticTest.class);
    }
}
