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

/** Tests whether we can access public field from a class in extension
 * in an underscored package.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Imported_Field_Test {
    @Test public void checkHello() throws Exception {
        code.assertExec("Can access field from extension",
            Implement_Factory.class, "meaning__I",
            42
        );
    }

    private static TestVM code;
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        ScriptEngine[] eng = { null };
        code = TestVM.compileClassesAsExtension(sb, eng, null, null,
            "org/apidesign/vm4brwsr/under_score/ImportedField",
            "org/apidesign/vm4brwsr/under_score/ImportedField$Factory"
        );
        code = TestVM.compileClassesAsExtension(sb, eng, null, null,
            "org/apidesign/vm4brwsr/Implement_Factory",
            "org/apidesign/vm4brwsr/Implement_Factory$Impl"
        );
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
}
