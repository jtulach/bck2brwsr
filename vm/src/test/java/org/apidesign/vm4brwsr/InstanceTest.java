/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InstanceTest {
    @Test public void verifyDefaultDoubleValue() throws Exception {
        assertExec(
            "Will be zero",
            Instance.class, "defaultDblValue__D",
            Double.valueOf(0)
        );
    }
    @Test public void verifyStaticMethodCall() throws Exception {
        assertExec(
            "Will be zero",
            InstanceSub.class, "recallDbl__D",
            Double.valueOf(0)
        );
    }
    @Test public void verifyAssignedByteValue() throws Exception {
        assertExec(
            "Will one thirty one",
            Instance.class, "assignedByteValue__B",
            Double.valueOf(31)
        );
    }
    @Test public void verifyMagicOne() throws Exception {
        assertExec(
            "Should be three and something",
            Instance.class, "magicOne__D",
            Double.valueOf(3.3)
        );
    }
    @Test public void verifyInstanceMethods() throws Exception {
        assertExec(
            "Should be eleven as we invoke overwritten method, plus 44",
            Instance.class, "virtualBytes__I",
            Double.valueOf(55)
        );
    }
    @Test public void verifyInterfaceMethods() throws Exception {
        assertExec(
            "Retruns default value",
            Instance.class, "interfaceBytes__F",
            Double.valueOf(31)
        );
    }

    @Test public void isNull() throws Exception {
        assertExec(
            "Yes, we are instance",
            Instance.class, "isNull__Z",
            Double.valueOf(0.0)
        );
    }

    @Test public void isInstanceOf() throws Exception {
        assertExec(
            "Yes, we are instance",
            Instance.class, "instanceOf__ZI",
            Double.valueOf(1.0), 2
        );
    }

    @Test public void notInstanceOf() throws Exception {
        assertExec(
            "No, we are not an instance",
            Instance.class, "instanceOf__ZI",
            Double.valueOf(0.0), 1
        );
    }
    @Test public void nullInstanceOf() throws Exception {
        assertExec(
            "No, null is not an instance",
            Instance.class, "instanceOf__ZI",
            Double.valueOf(0.0), 0
        );
    }
    
    @Test public void verifyCastToClass() throws Exception {
        assertExec(
            "Five signals all is good",
            Instance.class, "castsWork__IZ",
            Double.valueOf(5.0), false
        );
    }
    @Test public void verifyCastToInterface() throws Exception {
        assertExec(
            "Five signals all is good",
            Instance.class, "castsWork__IZ",
            Double.valueOf(5.0), true
        );
    }
    
    @Test public void sharedConstructor() throws Exception {
        assertExec(
            "Constructor of first and 2nd instance should be the same",
            Instance.class, "sharedConstructor__Z",
            Double.valueOf(1.0)
        );
    }

    @Test public void differentConstructor() throws Exception {
        assertExec(
            "Constructor of X and Y should be the different",
            Instance.class, "differentConstructor__Z",
            Double.valueOf(0)
        );
    }

    @Test public void jsObjectIsLikeJavaObject() throws Exception {
        assertExec(
            "JavaScript object is instance of Java Object",
            Instance.class, "iofObject__Z",
            Double.valueOf(1)
        );
    }

    @Test public void jsCallingConvention() throws Exception {
        assertExec(
            "Pointer to 'this' is passed automatically (and not as a first argument)",
            Instance.class, "jscall__I",
            Double.valueOf(31)
        );
    }
    
    protected String startCompilationWith() {
        return "org/apidesign/vm4brwsr/Instance";
    }
    
    private static CharSequence codeSeq;
    private static Invocable code;
    
    @BeforeClass
    public void compileTheCode() throws Exception {
        if (codeSeq == null) {
            StringBuilder sb = new StringBuilder();
            code = StaticMethodTest.compileClass(sb, startCompilationWith());
            codeSeq = sb;
        }
    }
    
    private void assertExec(
        String msg, Class clazz, String method, Object expRes, Object... args
    ) throws Exception {
        StaticMethodTest.assertExec(code, codeSeq, msg, clazz, method, expRes, args);
    }
    
}
