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
package org.apidesign.bck2brwsr.tck;

import java.util.Arrays;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public class RegExpSplitTest {

    public @Compare Object splitSpace() {
        return Arrays.asList("How are you today?".split(" "));
    }

    public @Compare String splitNewline() {
        return Arrays.toString("initializer must be able to complete normally".split("\n"));
    }

    public @Compare Object splitSpaceTrimMinusOne() {
        return Arrays.asList(" How are you today? ".split(" ", -1));
    }

    public @Compare Object splitSpaceTrimZero() {
        return Arrays.asList(" How are you today? ".split(" ", 0));
    }

    public @Compare Object splitSpaceLimit2() {
        return Arrays.asList("How are you today?".split(" ", 2));
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(RegExpSplitTest.class);
    }
}
