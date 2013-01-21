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

import java.util.ArrayList;
import java.util.List;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Page(xhtml = "Empty.html", className = "Model", properties = {
    @Property(name = "value", type = int.class),
    @Property(name = "unrelated", type = long.class)
})
public class ModelTest {
    @Test public void classGeneratedWithSetterGetter() {
        Class<?> c = Model.class;
        assertNotNull(c, "Class for empty page generated");
        Model.setValue(10);
        assertEquals(10, Model.getValue(), "Value changed");
    }
    
    @Test public void computedMethod() {
        Model.setValue(4);
        assertEquals(16, Model.getPowerValue());
    }
    
    @Test public void derivedPropertiesAreNotified() {
        MockKnockout my = new MockKnockout();
        MockKnockout.next = my;
        
        Model.applyBindings();
        
        Model.setValue(33);
        
        assertEquals(my.mutated.size(), 2, "Two properties changed: " + my.mutated);
        assertTrue(my.mutated.contains("powerValue"), "Power value is in there: " + my.mutated);
        assertTrue(my.mutated.contains("value"), "Simple value is in there: " + my.mutated);
        
        my.mutated.clear();
        
        Model.setUnrelated(44);
        assertEquals(my.mutated.size(), 1, "One property changed");
        assertTrue(my.mutated.contains("unrelated"), "Its name is unrelated");
    }
    
    @ComputedProperty
    static int powerValue(int value) {
        return value * value;
    }
    
    static class MockKnockout extends Knockout {
        List<String> mutated = new ArrayList<String>();
        
        @Override
        public void valueHasMutated(String prop) {
            mutated.add(prop);
        }
    }
}
