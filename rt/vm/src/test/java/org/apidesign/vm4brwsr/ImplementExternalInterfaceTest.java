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

import javax.script.ScriptEngine;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Tests whether private impl can implement public interface method
 * from another extension.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ImplementExternalInterfaceTest {
    @Test public void checkHello() throws Exception {
        String exp = "Hello!";
        
        code.assertExec("Can extension implement class from another extension",
            ImplementFactory.class, "hello__Ljava_lang_String_2", 
            exp
        );
    }

    private static TestVM code;
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        ScriptEngine[] eng = { null };
        code = TestVM.compileClassAsExtension(sb, eng, 
            "org/apidesign/vm4brwsr/extrnl/ImplementInterface", null, null
        );
        code = TestVM.compileClassesAsExtension(sb, eng, null, null,
            "org/apidesign/vm4brwsr/ImplementFactory", 
            "org/apidesign/vm4brwsr/ImplementFactory$Impl"
        );
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
}
