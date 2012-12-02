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
import static org.testng.Assert.*;
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
        assertExec("Classes are equal", Classes.class, "equalsClassesOfExceptionsZ", Double.valueOf(1.0));
    }

    @Test public void classesAreDifferent() {
        assertTrue(Classes.differenceInClasses(), "Classes are not equal");
    }

    @Test public void jsClassesAreDifferent() throws Exception {
        assertExec("Classes are not equal", Classes.class, "differenceInClassesZ", Double.valueOf(1.0));
    }

    @Test public void javaInstanceName() throws Exception {
        assertEquals(Classes.classForInstance(), "java.io.IOException");
    }
    @Test public void jsInstanceName() throws Exception {
        assertExec("I/O name", Classes.class, "classForInstanceLjava_lang_String", "java.io.IOException");
    }
    @Test public void javaName() throws Exception {
        assertEquals(Classes.name(), "java.io.IOException");
    }
    @Test public void jsName() throws Exception {
        assertExec("I/O name", Classes.class, "nameLjava_lang_String", "java.io.IOException");
    }
    @Test public void javaSimpleName() throws Exception {
        assertEquals(Classes.simpleName(), "IOException");
    }
    @Test public void jsGetsSimpleName() throws Exception {
        assertExec("I/O simple name", Classes.class, "simpleNameLjava_lang_String", "IOException");
    }
    @Test public void javaCanonicalName() {
        assertEquals(Classes.canonicalName(), "java.io.IOException");
    }
    @Test public void jsCanonicalName() throws Exception {
        assertExec("I/O simple name", Classes.class, "canonicalNameLjava_lang_String", "java.io.IOException");
    }
    @Test public void javaNewInstance() throws Exception {
        assertTrue(Classes.newInstance());
    }
    @Test public void jsNewInstance() throws Exception {
        assertExec("Check new instance", Classes.class, "newInstanceZ", Double.valueOf(1));
    }
    @Test public void jsAnnotation() throws Exception {
        assertExec("Check class annotation", Classes.class, "getMarkerI", Double.valueOf(10));
    }
    
    private static CharSequence codeSeq;
    private static Invocable code;
    
    @BeforeClass
    public void compileTheCode() throws Exception {
        if (codeSeq == null) {
            StringBuilder sb = new StringBuilder();
            code = StaticMethodTest.compileClass(sb, "org/apidesign/vm4brwsr/Classes");
            codeSeq = sb;
        }
    }
    
    private void assertExec(
        String msg, Class clazz, String method, Object expRes, Object... args
    ) throws Exception {
        StaticMethodTest.assertExec(code, codeSeq, msg, clazz, method, expRes, args);
    }
    
}
