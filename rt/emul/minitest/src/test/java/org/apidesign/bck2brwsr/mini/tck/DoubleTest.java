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
package org.apidesign.bck2brwsr.mini.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DoubleTest {
    @Compare public boolean parsedDoubleIsDouble() {
        return Double.valueOf("1.1") instanceof Double;
    }

    @Compare public boolean parsedFloatIsFloat() {
        return Float.valueOf("1.1") instanceof Float;
    }
    
    @Compare public String integerToString() {
        return toStr(1);
    }

    @Compare public String integerAndHalfToString() {
        return toStr(1.5);
    }

    @Compare public double longToAndBack() {
        return Double.parseDouble(toStr(Long.MAX_VALUE / 10));
    }

    @Compare public String negativeIntToString() {
        return toStr(-10);
    }

    @Compare public String negativeIntAndHalfToString() {
        return toStr(-10.5);
    }

    @Compare public double negativeLongAndBack() {
        return Double.parseDouble(toStr(Long.MIN_VALUE / 10));
    }
    
    @Compare public double canParseExp() {
        return Double.parseDouble(toStr(1.7976931348623157e+308));
    }
    
    private static String toStr(double d) {
        return Double.toString(d);
    }
    
    @Factory 
    public static Object[] create() {
        return VMTest.create(DoubleTest.class);
    }
}
