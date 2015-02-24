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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Numbers {
    private static Double autoboxDbl() {
        return 3.3;
    }
    public static String autoboxDblToString() {
        return autoboxDbl().toString().toString();
    }
    public static int rem(int a, int b) {
        return a % b;
    }

    public static float deserFloat() throws IOException {
        byte[] arr = {(byte) 71, (byte) 84, (byte) 52, (byte) 83};
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        DataInputStream dis = new DataInputStream(is);
        float r = dis.readFloat();
        return r;
    }
    public static double deserDouble() throws IOException {
        byte[] arr = {(byte)64, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0};
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        DataInputStream dis = new DataInputStream(is);
        return dis.readDouble();
    }
    public static long deserLong(byte[] arr) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        DataInputStream dis = new DataInputStream(is);
        return dis.readLong();
    }
    public static long deserLong(byte[] arr, int shift) throws IOException {
        return deserLong(arr) >> shift;
    }
    public static int deserInt() throws IOException {
        byte[] arr = {(byte) 71, (byte) 84, (byte) 52, (byte) 83};
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        DataInputStream dis = new DataInputStream(is);
        return dis.readInt();
    }
    public static long bytesToLong(byte b1, byte b2, int shift) {
        return (((long)b1 << 56) +
                ((long)b2 & 255) << 48) >> shift;
    }

    public static String intToString() {
        return new Integer(5).toString().toString();
    }
    public static String floatToString() {
        return new Float(7.0).toString().toString();
    }
    
    public static double seven(int todo) {
        switch (todo) {
            case 0: return sevenNew().doubleValue();
            case 1: return sevenNew().intValue();
            case 2: return sevenNew().longValue();
            case 3: return sevenNew().shortValue();
            case 4: return sevenNew().byteValue();
            case 8: return valueOf(Double.valueOf(7.0));
            case 9: return valueOf(Long.valueOf(Long.MAX_VALUE / 5));
            case 30: return valueOf(Boolean.FALSE);
            case 31: return valueOf(Boolean.TRUE);
            case 65: return valueOf(Character.valueOf('A'));
            default: throw new IllegalStateException();
        }
    }
    public static boolean bseven(int todo) {
        switch (todo) {
            case 30: return bvalueOf(Boolean.FALSE);
            case 31: return bvalueOf(Boolean.TRUE);
            default: throw new IllegalStateException();
        }
    }
    
    @JavaScriptBody(args = {}, body = "return 7;")
    private static native Number sevenNew();

    @JavaScriptBody(args = { "o" }, body = "return o.valueOf();")
    private static native double valueOf(Object o);
    
    @JavaScriptBody(args = { "o" }, body = "return o.valueOf();")
    private static native boolean bvalueOf(Object o);
    
    public static int around(Object model, int x, int y) {
        return minesAt(model, x - 1, y - 1)
            + minesAt(model, x - 1, y)
            + minesAt(model, x - 1, y + 1)
            + minesAt(model, x, y - 1)
            + minesAt(model, x, y + 1)
            + minesAt(model, x + 1, y - 1)
            + minesAt(model, x + 1, y)
            + minesAt(model, x + 1, y + 1);
    }

    private static int minesAt(Object model, int x, int y) {
        return x + y;
    }    
}
