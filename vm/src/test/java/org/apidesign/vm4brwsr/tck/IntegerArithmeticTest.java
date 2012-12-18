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
package org.apidesign.vm4brwsr.tck;

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.vm4brwsr.Compare;
import org.apidesign.vm4brwsr.CompareVMs;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class IntegerArithmeticTest {
    //@JavaScriptBody(args="msg", body="java.lang.System.out.println(msg.toString());")
    //private static native void log(String msg);
    
    @Compare public int overflow() {
        int v = Integer.MAX_VALUE + 1;
        return v;
    }
    
    @Compare public int underflow() {
        int v = Integer.MIN_VALUE - 1;
        return v;
    }
    
    /* @Compare public int convertToInt() {
        long v = Long.MAX_VALUE / 2;
        log("convert: " + v);
        return (int) v;
    } */
    @Compare public int addAndMaxInt() {
        return Integer.MAX_VALUE + Integer.MAX_VALUE;
    }
    
    @Compare public int multiplyMaxInt() {
        return Integer.MAX_VALUE * Integer.MAX_VALUE;
    }
    
    @Factory
    public static Object[] create() {
        return CompareVMs.create(IntegerArithmeticTest.class);
    }
}
