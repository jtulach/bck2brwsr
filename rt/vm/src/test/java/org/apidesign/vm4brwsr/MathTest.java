/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Checks behavior on Math class.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MathTest {
    @Test public void rintNegativeUp() throws Exception {
        final double cnts = -453904.634;
        assertExec("Should round up to end with 5", mathClass(), "rint__DD", 
            -453905.0, cnts
        );
    }

    protected Class<?> mathClass() {
        return Math.class;
    }

    @Test public void rintNegativeDown() throws Exception {
        final double cnts = -453904.434;
        assertExec("Should round up to end with 4", mathClass(), "rint__DD", 
            -453904.0, cnts
        );
    }

    @Test public void rintPositiveUp() throws Exception {
        final double cnts = 453904.634;
        assertExec("Should round up to end with 5", mathClass(), "rint__DD", 
            453905.0, cnts
        );
    }
    @Test public void rintPositiveDown() throws Exception {
        final double cnts = 453904.434;
        assertExec("Should round up to end with 4", mathClass(), "rint__DD", 
            453904.0, cnts
        );
    }
    @Test public void rintOneHalf() throws Exception {
        final double cnts = 1.5;
        assertExec("Should round up to end with 2", mathClass(), "rint__DD", 
            2.0, cnts
        );
    }
    @Test public void rintNegativeOneHalf() throws Exception {
        final double cnts = -1.5;
        assertExec("Should round up to end with 2", mathClass(), "rint__DD", 
            -2.0, cnts
        );
    }
    @Test public void rintTwoAndHalf() throws Exception {
        final double cnts = 2.5;
        assertExec("Should round up to end with 2", mathClass(), "rint__DD", 
            2.0, cnts
        );
    }
    @Test public void rintNegativeTwoOneHalf() throws Exception {
        final double cnts = -2.5;
        assertExec("Should round up to end with 2", mathClass(), "rint__DD", 
            -2.0, cnts
        );
    }

    @Test public void ieeeReminder1() throws Exception {
        assertExec("Same result 1", mathClass(), "IEEEremainder__DDD", 
            Math.IEEEremainder(10.0, 4.5), 10.0, 4.5
        );
    }

    @Test public void ieeeReminder2() throws Exception {
        assertExec("Same result 1", mathClass(), "IEEEremainder__DDD", 
            Math.IEEEremainder(Integer.MAX_VALUE, -4.5), Integer.MAX_VALUE, -4.5
        );
    }
    
    @Test public void isNaN() throws Exception {
        boolean nan = Double.isNaN(Double.NaN); 
        assertExec("Same result 1", Double.class, "isNaN__ZD", 
            1.0, Double.NaN
        );
    }

    @Test public void isInfinite() throws Exception {
        assertExec("Same result 1", Double.class, "isInfinite__ZD", 
            1.0, Double.POSITIVE_INFINITY
        );
    }

    @Test public void isNegInfinite() throws Exception {
        assertExec("Same result 1", Double.class, "isInfinite__ZD", 
            1.0, Double.NEGATIVE_INFINITY
        );
    }


    private static TestVM code;
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = TestVM.compileClass(sb);
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }

    private void assertExec(
        String msg, Class<?> clazz, String method, 
        Object ret, Object... args
    ) throws Exception {
        code.assertExec(msg, clazz, method, ret, args);
    }
    
}
