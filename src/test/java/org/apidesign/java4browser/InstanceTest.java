/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.java4browser;

import javax.script.Invocable;
import javax.script.ScriptException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstanceTest {
    @Test public void verifyDefaultDoubleValue() throws Exception {
        assertExec(
            "Will be zero",
            "org_apidesign_java4browser_Instance_defaultDblValueD",
            Double.valueOf(0)
        );
    }
    @Test public void verifyAssignedByteValue() throws Exception {
        assertExec(
            "Will one thirty one",
            "org_apidesign_java4browser_Instance_assignedByteValueB",
            Double.valueOf(31)
        );
    }
    @Test public void verifyMagicOne() throws Exception {
        assertExec(
            "Should be three and something",
            "org_apidesign_java4browser_Instance_magicOneD",
            Double.valueOf(3.3)
        );
    }
    @Test public void verifyInstanceMethods() throws Exception {
        assertExec(
            "Should be eleven as we invoke overwritten method, plus 44",
            "org_apidesign_java4browser_Instance_virtualBytesI",
            Double.valueOf(55)
        );
    }
    
    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        StringBuilder sb = new StringBuilder();
        Invocable i = StaticMethodTest.compileClass(sb, "Instance.class", "InstanceSub.class");
        
        Object ret = null;
        try {
            ret = i.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in " + sb, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in " + sb, ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + sb);
        
    }
    
}
