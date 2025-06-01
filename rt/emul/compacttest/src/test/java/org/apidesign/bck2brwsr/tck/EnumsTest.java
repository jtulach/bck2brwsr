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
package org.apidesign.bck2brwsr.tck;

import java.util.EnumMap;
import java.util.EnumSet;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class EnumsTest {
    enum Color {
        B, W;
    }

    @Compare public String enumSet() {
        try { throw new Exception(); } catch (Exception ex) {}
        EnumSet<Color> c = EnumSet.allOf(Color.class);
        return c.toString();
    }

    @Compare public String enumSetOneByOne() {
        EnumSet<Color> c = EnumSet.of(Color.B, Color.W);
        return c.toString();
    }

    @Compare public boolean enumFirstContains() {
        EnumSet<Color> c = EnumSet.of(Color.B);
        return c.contains(Color.B);
    }

    @Compare public boolean enumFirstDoesNotContains() {
        EnumSet<Color> c = EnumSet.of(Color.B);
        return c.contains(Color.W);
    }

    @Compare public boolean enumSndContains() {
        EnumSet<Color> c = EnumSet.of(Color.W);
        return c.contains(Color.W);
    }

    @Compare public boolean enumSecondDoesNotContains() {
        EnumSet<Color> c = EnumSet.of(Color.W);
        return c.contains(Color.B);
    }

    @Compare public String enumMap() {
        EnumMap<Color,String> c = new EnumMap(Color.class);
        c.put(Color.B, "Black");
        c.put(Color.W, "White");
        return c.toString();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(EnumsTest.class);
    }
}
