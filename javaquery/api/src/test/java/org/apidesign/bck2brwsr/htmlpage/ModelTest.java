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
package org.apidesign.bck2brwsr.htmlpage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;
import org.apidesign.bck2brwsr.htmlpage.api.OnFunction;
import org.apidesign.bck2brwsr.htmlpage.api.OnPropertyChange;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
@Page(xhtml = "Empty.html", className = "Modelik", properties = {
    @Property(name = "value", type = int.class),
    @Property(name = "count", type = int.class),
    @Property(name = "unrelated", type = long.class),
    @Property(name = "names", type = String.class, array = true),
    @Property(name = "values", type = int.class, array = true),
    @Property(name = "people", type = PersonImpl.class, array = true),
    @Property(name = "changedProperty", type=String.class)
})
public class ModelTest {
    private Modelik model;
    private static Modelik leakedModel;
    
    @BeforeMethod
    public void createModel() {
        model = new Modelik();
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

    @Test public void derivedArrayProp() {
        MockKnockout my = new MockKnockout();
        MockKnockout.next = my;
        
        model.applyBindings();
        
        model.setCount(10);
        
        List<String> arr = model.getRepeat();
        assertEquals(arr.size(), 10, "Ten items: " + arr);
        
        my.mutated.clear();
        
        model.setCount(5);
        
        arr = model.getRepeat();
        assertEquals(arr.size(), 5, "Five items: " + arr);

        assertEquals(my.mutated.size(), 2, "Two properties changed: " + my.mutated);
        assertTrue(my.mutated.contains("repeat"), "Array is in there: " + my.mutated);
        assertTrue(my.mutated.contains("count"), "Count is in there: " + my.mutated);
    }
    
    @Test public void derivedPropertiesAreNotified() {
        MockKnockout my = new MockKnockout();
        MockKnockout.next = my;
        
        model.applyBindings();
        
        model.setValue(33);
        
        // not interested in change of this property
        my.mutated.remove("changedProperty");
        
        assertEquals(my.mutated.size(), 2, "Two properties changed: " + my.mutated);
        assertTrue(my.mutated.contains("powerValue"), "Power value is in there: " + my.mutated);
        assertTrue(my.mutated.contains("value"), "Simple value is in there: " + my.mutated);
        
        my.mutated.clear();
        
        model.setUnrelated(44);
        
        
        // not interested in change of this property
        my.mutated.remove("changedProperty");
        assertEquals(my.mutated.size(), 1, "One property changed: " + my.mutated);
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
    
    @OnFunction 
    static void doSomething() {
    }
    
    @ComputedProperty
    static int powerValue(int value) {
        return value * value;
    }
    
    @OnPropertyChange({ "powerValue", "unrelated" })
    static void aPropertyChanged(Modelik m, String name) {
        m.setChangedProperty(name);
    }

    @OnPropertyChange({ "values" })
    static void anArrayPropertyChanged(String name, Modelik m) {
        m.setChangedProperty(name);
    }
    
    @Test public void changeAnything() {
        model.setCount(44);
        assertNull(model.getChangedProperty(), "No observed value change");
    }
    @Test public void changeValue() {
        model.setValue(33);
        assertEquals(model.getChangedProperty(), "powerValue", "power property changed");
    }
    @Test public void changeUnrelated() {
        model.setUnrelated(333);
        assertEquals(model.getChangedProperty(), "unrelated", "unrelated changed");
    }

    @Test public void changeInArray() {
        model.getValues().add(10);
        assertEquals(model.getChangedProperty(), "values", "Something added into the array");
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
    
    @ComputedProperty
    static List<String> repeat(int count) {
        return Collections.nCopies(count, "Hello");
    }
    
    static class MockKnockout extends Knockout {
        List<String> mutated = new ArrayList<>();
        
        MockKnockout() {
            super(null);
        }
        
        @Override
        public void valueHasMutated(String prop) {
            mutated.add(prop);
        }
    }
    
    public @Test void hasPersonPropertyAndComputedFullName() {
        List<Person> arr = model.getPeople();
        assertEquals(arr.size(), 0, "By default empty");
        Person p = null;
        if (p != null) {
            String fullNameGenerated = p.getFullName();
            assertNotNull(fullNameGenerated);
        }
    }
}
