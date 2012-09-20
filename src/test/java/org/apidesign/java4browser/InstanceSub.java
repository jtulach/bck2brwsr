/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.java4browser;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstanceSub extends Instance implements GetByte {
    public InstanceSub(int i, double d) {
        super(i, d);
    }
    
    @Override
    public void setByte(byte b) {
        super.setByte((byte) (b + 1));
    }
}
