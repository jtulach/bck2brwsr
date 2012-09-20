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
    @Test public void verifyInterfaceMethods() throws Exception {
        assertExec(
            "Retruns default value",
            "org_apidesign_java4browser_Instance_interfaceBytesF",
            Double.valueOf(31)
        );
    }

    @Test public void isNull() throws Exception {
        assertExec(
            "Yes, we are instance",
            "org_apidesign_java4browser_Instance_isNullZ",
            Double.valueOf(0.0)
        );
    }

    @Test public void isInstanceOf() throws Exception {
        assertExec(
            "Yes, we are instance",
            "org_apidesign_java4browser_Instance_instanceOfZZ",
            Double.valueOf(1.0), true
        );
    }

    @Test public void notInstanceOf() throws Exception {
        assertExec(
            "No, we are not an instance",
            "org_apidesign_java4browser_Instance_instanceOfZZ",
            Double.valueOf(0.0), false
        );
    }
    
    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        StringBuilder sb = new StringBuilder();
        Invocable i = StaticMethodTest.compileClass(sb, 
            "org/apidesign/java4browser/Instance",
            "org/apidesign/java4browser/InstanceSub"
        );
        
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
