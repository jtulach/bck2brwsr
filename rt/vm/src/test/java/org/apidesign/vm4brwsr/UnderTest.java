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
package org.apidesign.vm4brwsr;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Checks behavior of classes and methods with underscore.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UnderTest {
    @Test public void one() throws Exception {
        assertExec(
            "Should be one", 
            Under_Score.class, "one__I", 
            Double.valueOf(1)
        );
    }

    @Test public void onePlusOne() throws Exception {
        assertExec(
            "Should be two", 
            Under_Score.class, "one_1plus_1one__I", 
            Double.valueOf(2)
        );
    }

    @Test public void two() throws Exception {
        assertExec(
            "Should be two", 
            Under_Score.class, "two__I", 
            Double.valueOf(2)
        );
    }

    @Test public void staticField() throws Exception {
        assertExec(
            "Should be ten", 
            Under_Score.class, "staticField__I", 
            Double.valueOf(10)
        );
    }

    @Test public void instance() throws Exception {
        assertExec(
            "Should be five", 
            Under_Score.class, "instance__I", 
            Double.valueOf(5)
        );
    }

    
    private static TestVM code;
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = TestVM.compileClass(sb, "org/apidesign/vm4brwsr/Under_Score");
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
