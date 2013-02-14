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
package org.apidesign.bck2brwsr.ide.editor;

import org.testng.Assert;
import org.testng.annotations.Test;


public class ManglingSinkTest {

    @Test
    public void testMangle_1() {
        Assert.assertEquals(
                "binarySearch__I_3BIIB",
                ManglingSink.mangle("java.util.Arrays", "binarySearch", "[BIIB")
        );
    }

    @Test
    public void testMangle_2() {
        Assert.assertEquals(
                "sort__V_3I",
                ManglingSink.mangle("java.util.Arrays", "sort", "[I")
        );
    }

    @Test
    public void testMangle_3() {
        Assert.assertEquals(
                "binarySearch__I_3Ljava_lang_Object_2IILjava_lang_Object_2",
                ManglingSink.mangle("java.util.Arrays", "binarySearch", "[Ljava/lang/Object;IILjava/lang/Object;")
        );
    }


    @Test
    public void testField() {
        final ManglingSink manglingSink = new ManglingSink();
        manglingSink.field(null, "value");

        Assert.assertEquals("_value()", manglingSink.toString());
    }
}
