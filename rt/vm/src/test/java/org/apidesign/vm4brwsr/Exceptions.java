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
package org.apidesign.vm4brwsr;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author tom
 */
public class Exceptions {
    private Exceptions() {
    }

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
    
    @JavaScriptBody(args = "msg", body = "throw msg;")
    public static void thrw(String msg) {}
    
    public static String catchThrowableCatchesAll() {
        try {
            thrw("Hello!");
            return "Not here!";
        } catch (Throwable ex) {
            return ex.getMessage();
        }
    }

    public static String newInstance(String n) {
        try {
            Class c;
            try {
                c = Class.forName(n);
            } catch (ClassNotFoundException ex) {
                return ("CNFE:" + ex.getMessage()).toString();
            }
            return c.newInstance().getClass().getName();
        } catch (InstantiationException  ex) {
            return ex.getMessage();
        } catch (IllegalAccessException ex) {
            return ex.getMessage();
        }
    }
    
    private static int counter;
    public static int readCounter(String n) throws ClassNotFoundException {
        try {
            Class.forName(n);
        } finally {
            counter++;
        }
        return counter;
    }
}
