/**
 * Java 4 Browser Bytecode Translator
 * Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
public class Instance {
    private int i;
    protected short s;
    public double d;
    private float f;
    protected byte b = (byte)31;
    
    private Instance() {
    }

    public Instance(int i, double d) {
        this.i = i;
        this.d = d;
    }
    public byte getByte() {
        return b;
    }
    
    public void setByte(byte b) {
        this.b = b;
    }
    public static double defaultDblValue() {
        Instance create = new Instance();
        return create.d;
    }
    
    public static byte assignedByteValue() {
        return new Instance().b;
    }
    public static double magicOne() {
        Instance i = new Instance(10, 3.3d);
        i.b = (byte)0x09;
        return (i.i - i.b) * i.d;
    }
    public static int virtualBytes() {
        Instance i = new InstanceSub(7, 2.2d);
        i.setByte((byte)0x0a);
        Instance i2 = new Instance(3, 333.0d);
        i2.setByte((byte)44);
        return i.getByte() + i2.getByte();
    }
    public static float interfaceBytes() {
        GetByte i = new InstanceSub(7, 2.2d);
        return i.getByte();
    }
    public static boolean instanceOf(boolean sub) {
        Instance i = createInstance(sub);
        return isInstanceSubOf(i);
    }
    private static boolean isInstanceSubOf(Instance instance) {
        return instance instanceof InstanceSub;
    }
    private static Instance createInstance(boolean sub) {
        return sub ? new InstanceSub(3, 0) : new Instance();
    }
    private static boolean isNull() {
        return createInstance(true) == null;
    }
}
