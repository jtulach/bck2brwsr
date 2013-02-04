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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

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

    static float deserFloat() throws IOException {
        byte[] arr = {(byte) 71, (byte) 84, (byte) 52, (byte) 83};
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        DataInputStream dis = new DataInputStream(is);
        float r = dis.readFloat();
        return r;
    }
    static double deserDouble() throws IOException {
        byte[] arr = {(byte)64, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0};
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        DataInputStream dis = new DataInputStream(is);
        return dis.readDouble();
    }
    static long deserLong(byte[] arr) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        DataInputStream dis = new DataInputStream(is);
        return dis.readLong();
    }
    static int deserInt() throws IOException {
        byte[] arr = {(byte) 71, (byte) 84, (byte) 52, (byte) 83};
        ByteArrayInputStream is = new ByteArrayInputStream(arr);
        DataInputStream dis = new DataInputStream(is);
        return dis.readInt();
    }

    static String intToString() {
        return new Integer(5).toString().toString();
    }
    static String floatToString() {
        return new Float(7.0).toString().toString();
    }
    
    public static long conversionL() {
        return Long.MAX_VALUE;
    }
    
    public static long negL(byte[] arrValue) throws IOException {
        ByteArrayInputStream isValue = new ByteArrayInputStream(arrValue);
        DataInputStream disValue = new DataInputStream(isValue);
        return (-disValue.readLong());
    }
    
    public static long addL(byte[] arrX, byte[] arrY) throws IOException {
        ByteArrayInputStream isX = new ByteArrayInputStream(arrX);
        DataInputStream disX = new DataInputStream(isX);
        ByteArrayInputStream isY = new ByteArrayInputStream(arrY);
        DataInputStream disY = new DataInputStream(isY);
        return (disX.readLong() + disY.readLong());
    }
    
    public static long subL(byte[] arrX, byte[] arrY) throws IOException {
        ByteArrayInputStream isX = new ByteArrayInputStream(arrX);
        DataInputStream disX = new DataInputStream(isX);
        ByteArrayInputStream isY = new ByteArrayInputStream(arrY);
        DataInputStream disY = new DataInputStream(isY);
        return (disX.readLong() - disY.readLong());
    }
    
    public static long mulL(byte[] arrX, byte[] arrY) throws IOException {
        ByteArrayInputStream isX = new ByteArrayInputStream(arrX);
        DataInputStream disX = new DataInputStream(isX);
        ByteArrayInputStream isY = new ByteArrayInputStream(arrY);
        DataInputStream disY = new DataInputStream(isY);
        return (disX.readLong() * disY.readLong());
    }
    
    public static long divL(long x, long y) {
        return (x / y);
    }
    
    public static long modL(long x, long y) {
        return (x % y);
    }
    
    public static long shlL(byte[] arrValue, int nBits) throws IOException {
        ByteArrayInputStream isValue = new ByteArrayInputStream(arrValue);
        DataInputStream disValue = new DataInputStream(isValue);
        return (disValue.readLong() << nBits);
    }
    
    public static long shrL(byte[] arrValue, int nBits) throws IOException {
        ByteArrayInputStream isValue = new ByteArrayInputStream(arrValue);
        DataInputStream disValue = new DataInputStream(isValue);
        return (disValue.readLong() >> nBits);
    }
    
    public static long ushrL(byte[] arrValue, int nBits) throws IOException {
        ByteArrayInputStream isValue = new ByteArrayInputStream(arrValue);
        DataInputStream disValue = new DataInputStream(isValue);
        return (disValue.readLong() >>> nBits);
    }
    
    public static long andL(byte[] arrX, byte[] arrY) throws IOException {
        ByteArrayInputStream isX = new ByteArrayInputStream(arrX);
        DataInputStream disX = new DataInputStream(isX);
        ByteArrayInputStream isY = new ByteArrayInputStream(arrY);
        DataInputStream disY = new DataInputStream(isY);
        return (disX.readLong() & disY.readLong());
    }
    
    public static long orL(byte[] arrX, byte[] arrY) throws IOException {
        ByteArrayInputStream isX = new ByteArrayInputStream(arrX);
        DataInputStream disX = new DataInputStream(isX);
        ByteArrayInputStream isY = new ByteArrayInputStream(arrY);
        DataInputStream disY = new DataInputStream(isY);
        return (disX.readLong() | disY.readLong());
    }
    
    public static long xorL(byte[] arrX, byte[] arrY) throws IOException {
        ByteArrayInputStream isX = new ByteArrayInputStream(arrX);
        DataInputStream disX = new DataInputStream(isX);
        ByteArrayInputStream isY = new ByteArrayInputStream(arrY);
        DataInputStream disY = new DataInputStream(isY);
        return (disX.readLong() ^ disY.readLong());
    }
}
