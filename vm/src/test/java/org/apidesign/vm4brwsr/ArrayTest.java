/*
Java 4 Browser Bytecode Translator
Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. Look for COPYING file in the top folder.
If not, see http://opensource.org/licenses/GPL-2.0.
*/
package org.apidesign.vm4brwsr;

import javax.script.Invocable;
import javax.script.ScriptException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ArrayTest {
    @Test public void verifySimpleIntOperation() throws Exception {
            assertExec("CheckTheSum", "org_apidesign_vm4brwsr_Array_simpleI", 
            Double.valueOf(15)
        );
    }
    @Test public void verifyOperationsOnArrays() throws Exception {
        assertExec("The sum is 105", "org_apidesign_vm4brwsr_Array_sumD", 
            Double.valueOf(105)
        );
    }
    
    private static CharSequence codeSeq;
    private static Invocable code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = StaticMethodTest.compileClass(sb, 
            "org/apidesign/vm4brwsr/Array"
        );
        codeSeq = sb;
    }
    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        Object ret = null;
        try {
            ret = code.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in\n" + codeSeq, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in\n" + codeSeq, ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + codeSeq);
    }
}
