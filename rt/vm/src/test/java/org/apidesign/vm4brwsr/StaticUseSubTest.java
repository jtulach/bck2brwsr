/**
 * Back 2 Browser Bytecode Translator Copyright (C) 2012 Jaroslav Tulach
 * <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. Look for COPYING file in the top folder. If not, see
 * http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.vm4brwsr;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StaticUseSubTest {
    private static TestVM code;

    @BeforeClass
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = TestVM.compileClass(sb, "org/apidesign/vm4brwsr/StaticUseSub");
    }
    
    @Test public void getInheritedStaticField() throws Exception {
        code.assertExec(
            "Obtains non-null", StaticUseSub.class, 
            "getNonNull__Ljava_lang_String_2",
            "java.lang.Object"
        );
    }
}
