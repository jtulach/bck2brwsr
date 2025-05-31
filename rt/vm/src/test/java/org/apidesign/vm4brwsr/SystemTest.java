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

import org.testng.annotations.BeforeClass;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class SystemTest {
    private static TestVM code;
    
    @Test public void verifyJSTime() throws Exception {
        long now = System.currentTimeMillis();
        
        Object js = code.execCode("Get js time", 
            org.apidesign.bck2brwsr.emul.lang.System.class, "currentTimeMillisDouble__D",
            null
        );
        
        assertTrue(js instanceof Double, "Double " + js);
        long time = ((Double)js).longValue();
        
        long later = System.currentTimeMillis();
        
        assertTrue(now <= time, "Lower bound is OK: " + now + " <= " + time);
        assertTrue(time <= later, "Upper bound is OK: " + time + " <= " + later);
    }
    
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        code = TestVM.compileClass(
            "org/apidesign/bck2brwsr/emul/lang/System");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    
}

