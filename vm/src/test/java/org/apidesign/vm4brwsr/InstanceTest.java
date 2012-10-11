/**
 * Java 4 Browser Bytecode Translator
 * Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import javax.script.Invocable;
import javax.script.ScriptException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstanceTest {
    @Test public void verifyDefaultDoubleValue() throws Exception {
        assertExec(
            "Will be zero",
            "org_apidesign_vm4brwsr_Instance_defaultDblValueD",
            Double.valueOf(0)
        );
    }
    @Test public void verifyStaticMethodCall() throws Exception {
        assertExec(
            "Will be zero",
            "org_apidesign_vm4brwsr_InstanceSub_recallDblD",
            Double.valueOf(0)
        );
    }
    @Test public void verifyAssignedByteValue() throws Exception {
        assertExec(
            "Will one thirty one",
            "org_apidesign_vm4brwsr_Instance_assignedByteValueB",
            Double.valueOf(31)
        );
    }
    @Test public void verifyMagicOne() throws Exception {
        assertExec(
            "Should be three and something",
            "org_apidesign_vm4brwsr_Instance_magicOneD",
            Double.valueOf(3.3)
        );
    }
    @Test public void verifyInstanceMethods() throws Exception {
        assertExec(
            "Should be eleven as we invoke overwritten method, plus 44",
            "org_apidesign_vm4brwsr_Instance_virtualBytesI",
            Double.valueOf(55)
        );
    }
    @Test public void verifyInterfaceMethods() throws Exception {
        assertExec(
            "Retruns default value",
            "org_apidesign_vm4brwsr_Instance_interfaceBytesF",
            Double.valueOf(31)
        );
    }

    @Test public void isNull() throws Exception {
        assertExec(
            "Yes, we are instance",
            "org_apidesign_vm4brwsr_Instance_isNullZ",
            Double.valueOf(0.0)
        );
    }

    @Test public void isInstanceOf() throws Exception {
        assertExec(
            "Yes, we are instance",
            "org_apidesign_vm4brwsr_Instance_instanceOfZZ",
            Double.valueOf(1.0), true
        );
    }

    @Test public void notInstanceOf() throws Exception {
        assertExec(
            "No, we are not an instance",
            "org_apidesign_vm4brwsr_Instance_instanceOfZZ",
            Double.valueOf(0.0), false
        );
    }
    
    @Test public void verifyCastToClass() throws Exception {
        assertExec(
            "Five signals all is good",
            "org_apidesign_vm4brwsr_Instance_castsWorkIZ",
            Double.valueOf(5.0), false
        );
    }
    @Test public void verifyCastToInterface() throws Exception {
        assertExec(
            "Five signals all is good",
            "org_apidesign_vm4brwsr_Instance_castsWorkIZ",
            Double.valueOf(5.0), true
        );
    }
    
    protected String startCompilationWith() {
        return "org/apidesign/vm4brwsr/Instance";
    }
    
    private static CharSequence codeSeq;
    private static Invocable code;
    
    @BeforeTest 
    public void compileTheCode() throws Exception {
        if (codeSeq == null) {
            StringBuilder sb = new StringBuilder();
            code = StaticMethodTest.compileClass(sb, startCompilationWith());
            codeSeq = sb;
        }
    }
    
    private void assertExec(
        String msg, String methodName, Object expRes, Object... args
    ) throws Exception {

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
