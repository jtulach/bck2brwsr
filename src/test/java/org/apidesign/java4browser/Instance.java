/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.java4browser;

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
}
