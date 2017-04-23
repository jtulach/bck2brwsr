/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ClassTest {

    @Test public void superClassEqualsGetSuperclass() {
        assertTrue(Classes.equalsClassesOfExceptions(), "Classes are equal");
    }

    @Test public void jsSuperClassEqualsGetSuperclass() throws Exception {
        assertExec("Classes are equal", Classes.class, "equalsClassesOfExceptions__Z", Double.valueOf(1.0));
    }

    @Test public void classesAreDifferent() {
        assertTrue(Classes.differenceInClasses(), "Classes are not equal");
    }

    @Test public void jsClassesAreDifferent() throws Exception {
        assertExec("Classes are not equal", Classes.class, "differenceInClasses__Z", Double.valueOf(1.0));
    }

    @Test public void javaInstanceName() throws Exception {
        assertEquals(Classes.classForInstance(), "java.io.IOException");
    }
    @Test public void jsInstanceName() throws Exception {
        assertExec("I/O name", Classes.class, "classForInstance__Ljava_lang_String_2", "java.io.IOException");
    }
    @Test public void javaName() throws Exception {
        assertEquals(Classes.name(), "java.io.IOException");
    }
    @Test public void jsName() throws Exception {
        assertExec("I/O name", Classes.class, "name__Ljava_lang_String_2", "java.io.IOException");
    }
    @Test public void javaSimpleName() throws Exception {
        assertEquals(Classes.simpleName(), "IOException");
    }
    @Test public void jsGetsSimpleName() throws Exception {
        assertExec("I/O simple name", Classes.class, "simpleName__Ljava_lang_String_2", "IOException");
    }
    @Test public void javaCanonicalName() {
        assertEquals(Classes.canonicalName(), "java.io.IOException");
    }
    @Test public void jsCanonicalName() throws Exception {
        assertExec("I/O simple name", Classes.class, "canonicalName__Ljava_lang_String_2", "java.io.IOException");
    }
    @Test public void javaNewInstance() throws Exception {
        assertTrue(Classes.newInstance());
    }
    @Test public void jsNewInstance() throws Exception {
        assertExec("Check new instance", Classes.class, "newInstance__Z", Double.valueOf(1));
    }
    @Test public void javaNoNewInstance() throws Exception {
        assertEquals("java.lang.InstantiationException:java.lang.Float", 
            Classes.newInstanceNoPubConstructor()
        );
    }
    @Test public void jsNoNewInstance() throws Exception {
        assertExec("Check problems with new instance", Classes.class, "newInstanceNoPubConstructor__Ljava_lang_String_2", 
            "java.lang.InstantiationException:java.lang.Float"
        );
    }
    @Test public void jsAnnotation() throws Exception {
        assertExec("Check class annotation", Classes.class, "getMarker__I", Double.valueOf(10));
    }
    @Test public void jsAnnotationDefaultValue() throws Exception {
        assertExec("Check class annotation", Classes.class, "getMarkerDefault__I", Double.valueOf(42));
    }
    @Test public void jsArrayAnnotation() throws Exception {
        assertExec("Check array annotation", Classes.class, "getMarkerNicknames__Ljava_lang_String_2", Classes.getMarkerNicknames());
    }
    @Test public void jsEnumAnnotation() throws Exception {
        assertExec("Check enum annotation", Classes.class, "getMarkerE__Ljava_lang_String_2", Classes.getMarkerE());
    }
    @Test public void jsEnumAnnotationDefault() throws Exception {
        assertExec("Check enum annotation", Classes.class, "getMarkerED__Ljava_lang_String_2", Classes.getMarkerED());
    }
    @Test public void jsRetentionAnnotation() throws Exception {
        assertExec("Check enum annotation", Classes.class, "getRetention__Ljava_lang_String_2", Classes.getRetention());
    }
    @Test public void jsStringAnnotation() throws Exception {
        assertExec("Check class annotation", Classes.class, "getNamer__Ljava_lang_String_2Z", "my text", true);
    }
    @Test public void jsStringAnnotationFromArray() throws Exception {
        assertExec("Check class annotation", Classes.class, "getNamer__Ljava_lang_String_2Z", "my text", false);
    }
    @Test public void jsInnerAnnotation() throws Exception {
        assertExec("Check inner annotation", Classes.class, "getInnerNamer__I", Double.valueOf(Classes.getInnerNamer()));
    }
    @Test public void jsInnerAnnotationFromArray() throws Exception {
        assertExec("Check inner annotation", Classes.class, "getInnerNamers__I", Double.valueOf(Classes.getInnerNamers()));
    }
    @Test public void jsAnnotationClassAttr() throws Exception {
        assertExec("Check annotation with class attribute", Classes.class, "self__I", 1);
    }
    @Test public void jsAnnotationDefaultClassAttr() throws Exception {
        assertExec("Check annotation with class attribute", Classes.class, "defaultSelf__I", 1);
    }
    @Test public void javaInvokeMethod() throws Exception {
        assertEquals(Classes.reflectiveMethodCall(true, "name"), "java.io.IOException", "Calls the name() method via reflection");
    }
    @Test public void jsInvokeMethod() throws Exception {
        assertExec("Calls the name() method via reflection", Classes.class, 
            "reflectiveMethodCall__Ljava_lang_Object_2ZLjava_lang_String_2", 
            "java.io.IOException", true, "name"
        );
    }
    
    @Test public void jsMethodDeclaredInObject() throws Exception {
        assertExec("Defined in Object", Classes.class, 
            "objectName__Ljava_lang_String_2", 
            "java.lang.Object"
        );
    }

    @Test public void jsListMethodsInObject() throws Exception {
        String methods = Classes.listObject(false, "");
        
        assertExec("Methods defined in Object", Classes.class, 
            "listObject__Ljava_lang_String_2ZLjava_lang_String_2", 
            methods, false, ""
        );
    }

    @Test public void jsListMethodsInString() throws Exception {
        String methods = Classes.listObject(true, "toStr");
        
        assertExec("Methods defined in Object", Classes.class, 
            "listObject__Ljava_lang_String_2ZLjava_lang_String_2", 
            methods, true, "toStr"
        );
    }
    
    @Test public void jsInvokeParamMethod() throws Exception {
        assertExec("sums two numbers via reflection", Classes.class, 
            "reflectiveSum__III", Double.valueOf(5), 2, 3
        );
    }
    @Test public void javaFindMethod() throws Exception {
        assertEquals(Classes.reflectiveMethodCall(false, "name"), "java.io.IOException", "Calls the name() method via reflection");
    }
    @Test public void jsFindMethod() throws Exception {
        assertExec("Calls the name() method via reflection", Classes.class, 
            "reflectiveMethodCall__Ljava_lang_Object_2ZLjava_lang_String_2", 
            "java.io.IOException", false, "name"
        );
    }
    @Test public void primitiveReturnType() throws Exception {
        assertExec("Tries to get an integer via reflection", Classes.class, 
            "primitiveType__Ljava_lang_String_2Ljava_lang_String_2", 
            Classes.primitiveType("primitive"), "primitive"
        );
    }
    @Test public void primitiveBoolReturnType() throws Exception {
        assertExec("Tries to get an integer via reflection", Classes.class, 
            "primitiveType__Ljava_lang_String_2Ljava_lang_String_2", 
            Classes.primitiveType("primitiveB"), "primitiveB"
        );
    }
    @Test public void javaAnnotatedMethod() throws Exception {
        assertEquals(Classes.reflectiveMethodCall(false, null), "java.io.IOException", "Calls the name() method via reflection");
    }
    @Test public void jsAnnotatedMethod() throws Exception {
        assertExec("Calls the name() method via reflection", Classes.class, 
            "reflectiveMethodCall__Ljava_lang_Object_2ZLjava_lang_String_2", 
            "java.io.IOException", false, null
        );
    }
    @Test public void jsClassParam() throws Exception {
        assertExec("Calls the nameOfIO()", Classes.class, 
            "nameOfIO__Ljava_lang_String_2", 
            "java.io.IOException"
        );
    }
    @Test public void noInterface() throws Exception {
        assertExec("Calls Class.isInterface", Classes.class, 
            "isInterface__ZLjava_lang_String_2", 
            0.0, "java.lang.String"
        );
    }
    @Test public void isInterface() throws Exception {
        assertExec("Calls Class.isInterface", Classes.class, 
            "isInterface__ZLjava_lang_String_2", 
            1.0, "java.lang.Runnable"
        );
    }
    @Test public void integerType() throws Exception {
        assertExec("Computes the type", Classes.class, 
            "intType__Ljava_lang_String_2", 
            Classes.intType()
        );
    }
    
    private static TestVM code;
    
    @BeforeClass
    public void compileTheCode() throws Exception {
        code = TestVM.compileClass("org/apidesign/vm4brwsr/Classes");
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
    @Test public void isClassAssignable() throws Exception {
        assertExec("isAssignable works on subclasses", Classes.class, 
            "isClassAssignable__Z", 
            true
        );
    }
    
    @Test public void valueOfEnum() throws Exception {
        assertExec("can get value of enum", Classes.class,
            "valueEnum__Ljava_lang_String_2Ljava_lang_String_2",
            "TWO", "TWO"
        );
    }

    @Test public void typeOfFn() throws Exception {
        assertExec("Type of function is Object", Classes.class,
            "typeOfFn__Ljava_lang_String_2",
            "java.lang.Object"
        );
    }
    
    @Test public void instanceOfSuperInterface() throws Exception {
        assertExec("Is iof super interface", Classes.class,
            "instanceOfSuperInterface__Z",
            1.0
        );
    }

    @Test public void defaultInt() throws Exception {
        assertExec("Int attr", Classes.class,
            "defaultInt__I",
            42
        );
    }

    @Test public void defaultCls() throws Exception {
        String exp = Classes.defaultCls();
        assertExec("Cls attr", Classes.class,
            "defaultCls__Ljava_lang_String_2", exp
        );
    }

    @Test public void nameOfSuperInterface() throws Exception {
        String exp = Classes.superInterface();
        assertExec("Can get name of superinterface?", Classes.class,
            "superInterface__Ljava_lang_String_2",
            exp
        );
    }
}
