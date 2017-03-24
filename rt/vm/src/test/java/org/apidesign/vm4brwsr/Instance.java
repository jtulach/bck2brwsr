/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
public class Instance {
    private int in;
    protected short s;
    public double d;
    private float f;
    protected byte b = (byte)31;
    
    private Instance() {
    }

    public Instance(int i, double d) {
        this.in = i;
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
        return (i.in - i.b) * i.d;
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
    public static boolean instanceOf(int sub) {
        Instance i = createInstance(sub);
        return isInstanceSubOf(i);
    }
    public static int castsWork(boolean interfc) {
        Instance i = createInstance(2);
        if (interfc) {
            GetByte b = (GetByte)i;
        } else {
            InstanceSub s = (InstanceSub)i;
        }
        return 5;
    }
    
    private static boolean isInstanceSubOf(Instance instance) {
        return instance instanceof InstanceSub;
    }
    private static Instance createInstance(int type) {
        switch (type) {
            case 0: return null;
            case 1: return new Instance();
            case 2: return new InstanceSub(3, 0);
        }
        throw new IllegalArgumentException();
    }
    private static boolean isNull() {
        return createInstance(2) == null;
    }
    
    @JavaScriptBody(args = "obj", body = "return obj.constructor;")
    static Object constructor(Object obj) {
        return obj;
    }
    
    public static boolean sharedConstructor() {
        class X {
        }
        
        X x1 = new X();
        X x2 = new X();
        
        return constructor(x1) == constructor(x2);
    }
    public static boolean differentConstructor() {
        class X {
        }
        class Y {
        }
        
        X x = new X();
        Y y = new Y();
        
        return constructor(x) == constructor(y);
    }
    @JavaScriptBody(args = {}, body = "return {};")
    private static Object jsObj() {
        return null;
    }
    
    public static boolean iofObject() {
        return jsObj() instanceof Object;
    }
    
    public static int jscall() {
        return jsgetbytes(new Instance());
    }
    
    public static int noInstOfExposed() {
        return countInstOf(10);
    }
    
    public static String props() {
        return list(new Object());
    }
    
    @JavaScriptBody(args = "o", body = "var s = ''; for (var p in {}) { s += p; }; return s;")
    private static native String list(Object o);
    
    @JavaScriptBody(args = "o", body = "var i = 0; for (p in o) { if (p.toString().indexOf('instOf') >= 0) i++; } return i;")
    private static native int countInstOf(Object o);
    
    @JavaScriptBody(args = { "instance" }, body = "return instance.getByte__B();")
    private static native int jsgetbytes(Instance instance);

    int sum(int i, int i0) {
        return i + i0;
    }
}
