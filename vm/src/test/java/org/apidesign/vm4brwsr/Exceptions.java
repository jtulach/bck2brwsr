/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.vm4brwsr;

/**
 *
 * @author tom
 */
public class Exceptions {

    public static int methodWithTryCatchNoThrow() {
        int res = 0;
        try {
            res = 1;
        } catch (IllegalArgumentException e) {
            res = 2;
        }
        //join point
        return res;
    }

    public static int methodWithTryCatchThrow() {
        int res = 0;
        try {
            res = 1;
            throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            res = 2;
        }
        //join point
        return res;
    }

}
