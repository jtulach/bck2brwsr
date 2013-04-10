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

import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;
import org.apidesign.bck2brwsr.htmlpage.api.Model;
import org.apidesign.bck2brwsr.htmlpage.api.OnFunction;
import org.apidesign.bck2brwsr.htmlpage.api.Property;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Model(className = "Person", properties = {
    @Property(name = "firstName", type = String.class),
    @Property(name = "lastName", type = String.class),
    @Property(name = "sex", type = Sex.class)
})
final class PersonImpl {
    @ComputedProperty 
    public static String fullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
    
    @ComputedProperty
    public static String sexType(Sex sex) {
        return sex == null ? "unknown" : sex.toString();
    }
    
    @OnFunction
    static void changeSex(Person p) {
        if (p.getSex() == Sex.MALE) {
            p.setSex(Sex.FEMALE);
        } else {
            p.setSex(Sex.MALE);
        }
    }
    
    @Model(className = "People", properties = {
        @Property(array = true, name = "info", type = Person.class),
        @Property(array = true, name = "nicknames", type = String.class),
        @Property(array = true, name = "age", type = int.class),
        @Property(array = true, name = "sex", type = Sex.class)
    })
    public class PeopleImpl {
    }
}
