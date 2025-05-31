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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
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
    @Test public void noInstOfExposed() throws Exception {
        assertExec(
            "No instOf properties found",
            Instance.class, "noInstOfExposed__I",
            Double.valueOf(0)
        );
    }
    @Test public void noIterablePropsOnObject() throws Exception {
        assertExec(
            "No instOf properties found",
            Instance.class, "props__Ljava_lang_String_2",
            ""
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

    @Test public void hiInstance() throws Exception {
        assertExec(
            "Calls Instance private method",
            Instance.class, "hiInstance__Ljava_lang_String_2",
            "Hi Instance!Instance"
        );
    }

    @Test public void hiInstanceSub() throws Exception {
        assertExec(
            "Calls Instance private method",
            Instance.class, "hiInstanceSub__Ljava_lang_String_2",
            "Hi Instance!InstanceSub"
        );
    }
    
    protected String startCompilationWith() {
        return "org/apidesign/vm4brwsr/Instance";
    }
    
    private static TestVM code;
    
    @BeforeClass
    public void compileTheCode() throws Exception {
        code = TestVM.compileClass(startCompilationWith());
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    
    private void assertExec(
        String msg, Class clazz, String method, Object expRes, Object... args
    ) throws Exception {
        code.assertExec(msg, clazz, method, expRes, args);
    }
    
}
