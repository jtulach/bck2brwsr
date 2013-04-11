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
package org.apidesign.bck2brwsr.htmlpage;

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ConvertTypesTest {
    @JavaScriptBody(args = {  }, body = "var json = new Object();"
        + "json.firstName = 'son';\n"
        + "json.lastName = 'dj';\n"
        + "json.sex = 'MALE';\n"
        + "return json;"
    )
    private static native Object createJSON();
    
    @BrwsrTest
    public void testConvertToPeople() {
        final Object o = createJSON();
        
        Person p = new Person(o);
        
        assert "son".equals(p.getFirstName()) : "First name: " + p.getFirstName();
        assert "dj".equals(p.getLastName()) : "Last name: " + p.getLastName();
        assert Sex.MALE.equals(p.getSex()) : "Sex: " + p.getSex();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ConvertTypesTest.class);
    }
}