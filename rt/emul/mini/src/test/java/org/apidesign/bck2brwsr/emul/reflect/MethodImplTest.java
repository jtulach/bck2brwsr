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
package org.apidesign.bck2brwsr.emul.reflect;

import java.lang.reflect.Method;
import java.util.Enumeration;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MethodImplTest {
    
    public MethodImplTest() {
    }
    
    public static String[] arr(String... arr) {
        return arr;
    }

    @Test
    public void testSignatureForMethodWithAnArray() throws NoSuchMethodException {
        Method m = MethodImplTest.class.getMethod("arr", String[].class);
        String sig = MethodImpl.toSignature(m);
        int sep = sig.indexOf("__");
        assert sep > 0 : "Separator found " + sig;
        
        Enumeration<Class> en = MethodImpl.signatureParser(sig.substring(sep + 2));
        
        assert en.nextElement() == m.getReturnType() : "Return type is the same";
        assert en.nextElement() == m.getParameterTypes()[0] : "1st param type is the same";
    }
}