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
package org.apidesign.bck2brwsr.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CompareHashTest {
    @Compare public int hashOfString() {
        return "Ahoj".hashCode();
    }
    
    @Compare public boolean hashOfIntegerDifferentToOwnHash() {
        Integer i = 120;
        return System.identityHashCode(i) != i.hashCode();
    }

    @Compare public int hashOfObjectSameAsOwnHash() {
        Object o = new Object();
        return System.identityHashCode(o) - o.hashCode();
    }
    
    @Compare public int hashRemainsYieldsZero() {
        Object o = new Object();
        return o.hashCode() - o.hashCode();
    }
    
    @Compare public int initializeInStatic() {
        return StaticUse.NON_NULL.hashCode() - StaticUse.NON_NULL.hashCode();
    }
    
    @Compare public int hashOfInt() {
        return Integer.valueOf(Integer.MAX_VALUE).hashCode();
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(CompareHashTest.class);
    }
}
