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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Page(xhtml = "Empty.html", className = "Model", properties = {
    @Property(name = "value", type = int.class),
    @Property(name = "unrelated", type = long.class),
    @Property(name = "names", type = String.class, array = true),
    @Property(name = "values", type = int.class, array = true)
})
public class ModelTest {
    private Model model;
    private static Model leakedModel;
    
    @BeforeMethod
    public void createModel() {
        model = new Model();
    }
    
    @Test public void classGeneratedWithSetterGetter() {
        model.setValue(10);
        assertEquals(10, model.getValue(), "Value changed");
    }
    
    @Test public void computedMethod() {
        model.setValue(4);
        assertEquals(16, model.getPowerValue());
    }
    
    @Test public void arrayIsMutable() {
        assertEquals(model.getNames().size(), 0, "Is empty");
        model.getNames().add("Jarda");
        assertEquals(model.getNames().size(), 1, "One element");
    }
    
    @Test public void arrayChangesNotified() {
        MockKnockout my = new MockKnockout();
        MockKnockout.next = my;
        
        model.applyBindings();
        
        model.getNames().add("Hello");
        
        assertFalse(my.mutated.isEmpty(), "There was a change" + my.mutated);
        assertTrue(my.mutated.contains("names"), "Change in names property: " + my.mutated);

        my.mutated.clear();
        
        Iterator<String> it = model.getNames().iterator();
        assertEquals(it.next(), "Hello");
        it.remove();
        
        assertFalse(my.mutated.isEmpty(), "There was a change" + my.mutated);
        assertTrue(my.mutated.contains("names"), "Change in names property: " + my.mutated);

        my.mutated.clear();
        
        ListIterator<String> lit = model.getNames().listIterator();
        lit.add("Jarda");
        
        assertFalse(my.mutated.isEmpty(), "There was a change" + my.mutated);
        assertTrue(my.mutated.contains("names"), "Change in names property: " + my.mutated);
    }
    
    @Test public void autoboxedArray() {
        MockKnockout my = new MockKnockout();
        MockKnockout.next = my;
        
        model.applyBindings();
        
        model.getValues().add(10);
        
        assertEquals(model.getValues().get(0), Integer.valueOf(10), "Really ten");
    }
    
    @Test public void derivedPropertiesAreNotified() {
        MockKnockout my = new MockKnockout();
        MockKnockout.next = my;
        
        model.applyBindings();
        
        model.setValue(33);
        
        assertEquals(my.mutated.size(), 2, "Two properties changed: " + my.mutated);
        assertTrue(my.mutated.contains("powerValue"), "Power value is in there: " + my.mutated);
        assertTrue(my.mutated.contains("value"), "Simple value is in there: " + my.mutated);
        
        my.mutated.clear();
        
        model.setUnrelated(44);
        assertEquals(my.mutated.size(), 1, "One property changed");
        assertTrue(my.mutated.contains("unrelated"), "Its name is unrelated");
    }
    
    @Test public void computedPropertyCannotWriteToModel() {
        leakedModel = model;
        try {
            String res = model.getNotAllowedWrite();
            fail("We should not be allowed to write to the model: " + res);
        } catch (IllegalStateException ex) {
            // OK, we can't read
        }
    }

    @Test public void computedPropertyCannotReadToModel() {
        leakedModel = model;
        try {
            String res = model.getNotAllowedRead();
            fail("We should not be allowed to read from the model: " + res);
        } catch (IllegalStateException ex) {
            // OK, we can't read
        }
    }
    
    @ComputedProperty
    static int powerValue(int value) {
        return value * value;
    }
    
    @ComputedProperty
    static String notAllowedRead() {
        return "Not allowed callback: " + leakedModel.getUnrelated();
    }

    @ComputedProperty
    static String notAllowedWrite() {
        leakedModel.setUnrelated(11);
        return "Not allowed callback!";
    }
    
    static class MockKnockout extends Knockout {
        List<String> mutated = new ArrayList<String>();
        
        @Override
        public void valueHasMutated(String prop) {
            mutated.add(prop);
        }
    }
}
