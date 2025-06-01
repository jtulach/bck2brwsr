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

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class ArrayTest {
    @Test public void intArrayShouldBeFilledWithZeroes() throws Exception {
            assertExec("0 + 0", Array.class, "sum__II", 
            Double.valueOf(0), 2
        );
    }
    @Test public void verifySimpleIntOperation() throws Exception {
            assertExec("CheckTheSum", Array.class, "simple__IZ", 
            Double.valueOf(15), false
        );
    }
    
    @Test public void cloneOnArray() throws Exception {
            assertExec("CheckTheSum on clone", Array.class, "simple__IZ", 
            Double.valueOf(15), true
        );
    }

    @Test public void cloneOnArrayAndComponentType() throws Exception {
        String exp = Array.nameOfClonedComponent();
        assertExec("getComponentType on clone", Array.class, "nameOfClonedComponent__Ljava_lang_String_2", 
            exp
        );
    }
    
    @Test public void realOperationOnArrays() throws Exception {
        assertEquals(Array.sum(), 105.0, "Computes to 105");
    }
    
    @Test public void verifyOperationsOnArrays() throws Exception {
        assertExec("The sum is 105", Array.class, "sum__D", 
            Double.valueOf(105)
        );
    }

    @Test public void twoDoubles() throws Exception {
        assertExec("Elements are initialized", Array.class, "twoDoubles__D", 
            Double.valueOf(0)
        );
    }
    @Test public void twoInts() throws Exception {
        assertExec("Elements are initialized", Array.class, "twoInts__I", 
            Double.valueOf(0)
        );
    }

    @Test public void iterateEmptyArray() throws Exception {
        assertExec("No elements in empty array", Array.class, "iterateArray__Ljava_lang_String_2Z", 
            "", false
        );
    }

    @Test public void iterateEmptyJavaArray() throws Exception {
        assertExec("No elements in empty array", Array.class, "iterateArray__Ljava_lang_String_2Z", 
            "", true
        );
    }
    
    @Test public void doesCopyArrayWork() throws Exception {
        assertExec("Returns 'a'", Array.class, "copyArray__C", Double.valueOf('a'));
    }

    @Test public void verifyObjectArrayClass() throws Exception {
        assertExec("Returns 'Object[]'", Array.class, "objectArrayClass__Ljava_lang_String_2", Array.objectArrayClass());
    }
    @Test public void verifyInstanceOfArray() throws Exception {
        assertExec("Returns 'false'", Array.class, "instanceOfArray__ZLjava_lang_Object_2", Double.valueOf(0), "non-array");
    }
    @Test public void verifyInstanceOfArrayOnNull() throws Exception {
        assertFalse(Array.instanceOfArray(null), "Null is not an instance of array");
        assertExec("Returns 'false'", Array.class, "instanceOfArray__ZLjava_lang_Object_2", Double.valueOf(0), (Object) null);
    }
    @Test public void verifyInstanceOfArrayStrings() throws Exception {
        assertExec("Returns 'false'", Array.class, "instanceOfArray__ZLjava_lang_Object_2", Double.valueOf(1), "string-array");
    }
    @Test public void verifyMultiLen() throws Exception {
        assertExec("Multi len is one", Array.class, "multiLen__I", Double.valueOf(1));
    }
    @Test public void upCastAnArray() throws Exception {
        assertExec("Cannot cast int to string array", Array.class, "castArray__ZI", Double.valueOf(0), Double.valueOf(0));
    }
    @Test public void upCastANullArray() throws Exception {
        assertExec("Can cast null to string array", Array.class, "castArray__ZI", Double.valueOf(1), Double.valueOf(1));
    }
    @Test public void upCastOK() throws Exception {
        assertExec("Can cast string to string array", Array.class, "castArray__ZI", Double.valueOf(1), Double.valueOf(2));
    }
    @Test public void upCastOK2() throws Exception {
        assertExec("Can cast string to char sequence array", Array.class, "castArray__ZI", Double.valueOf(1), Double.valueOf(3));
    }
    @Test public void twoDimensionalArrayAccess() throws Exception {
        int exp = Array.sumTable();
        assertExec("Is 2d access working OK?", Array.class, "sumTable__I", exp);
    }
    @Test public void ordAccess() throws Exception {
        int exp = Array.plusOrd();
        assertExec("Is int allocated OK?", Array.class, "plusOrd__I", exp);
    }
    
    private static TestVM code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        code = TestVM.compileClass("org/apidesign/vm4brwsr/Array");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    private static void assertExec(String msg, Class clazz, String method, Object expRes, Object... args) throws Exception {
        code.assertExec(msg, clazz, method, expRes, args);
    }
}
