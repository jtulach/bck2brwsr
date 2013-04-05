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

import java.util.Iterator;
import org.apidesign.bck2brwsr.htmlpage.api.OnReceive;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.Http;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.Factory;

/** Need to verify that models produce reasonable JSON objects.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Page(xhtml = "Empty.html", className = "JSONik", properties = {
    @Property(name = "fetched", type = PersonImpl.class)
})
public class JSONTest {
    
    @Test public void personToString() throws JSONException {
        Person p = new Person();
        p.setSex(Sex.MALE);
        p.setFirstName("Jarda");
        p.setLastName("Tulach");
        
        JSONTokener t = new JSONTokener(p.toString());
        JSONObject o;
        try {
            o = new JSONObject(t);
        } catch (JSONException ex) {
            throw new AssertionError("Can't parse " + p.toString(), ex);
        }
        
        Iterator it = o.sortedKeys();
        assertEquals(it.next(), "firstName");
        assertEquals(it.next(), "lastName");
        assertEquals(it.next(), "sex");
        
        assertEquals(o.getString("firstName"), "Jarda");
        assertEquals(o.getString("lastName"), "Tulach");
        assertEquals(o.getString("sex"), "MALE");
    }
    
    @Test public void personWithWildCharactersAndNulls() throws JSONException {
        Person p = new Person();
        p.setFirstName("'\"\n");
        p.setLastName("\t\r\u0002");
        
        JSONTokener t = new JSONTokener(p.toString());
        JSONObject o;
        try {
            o = new JSONObject(t);
        } catch (JSONException ex) {
            throw new AssertionError("Can't parse " + p.toString(), ex);
        }
        
        Iterator it = o.sortedKeys();
        assertEquals(it.next(), "firstName");
        assertEquals(it.next(), "lastName");
        assertEquals(it.next(), "sex");
        
        assertEquals(o.getString("firstName"), p.getFirstName());
        assertEquals(o.getString("lastName"), p.getLastName());
        assertEquals(o.get("sex"), JSONObject.NULL);
    }
    
    @Test public void personsInArray() throws JSONException {
        Person p1 = new Person();
        p1.setFirstName("One");

        Person p2 = new Person();
        p2.setFirstName("Two");
        
        People arr = new People();
        arr.getInfo().add(p1);
        arr.getInfo().add(p2);
        arr.getNicknames().add("Prvn\u00ed k\u016f\u0148");
        final String n2 = "Druh\u00fd hlem\u00fd\u017e\u010f, star\u0161\u00ed";
        arr.getNicknames().add(n2);
        arr.getAge().add(33);
        arr.getAge().add(73);
        
        
        final String json = arr.toString();
        
        JSONTokener t = new JSONTokener(json);
        JSONObject o;
        try {
            o = new JSONObject(t);
        } catch (JSONException ex) {
            throw new AssertionError("Can't parse " + json, ex);
        }

        assertEquals(o.getJSONArray("info").getJSONObject(0).getString("firstName"), "One");
        assertEquals(o.getJSONArray("nicknames").getString(1), n2);
        assertEquals(o.getJSONArray("age").getInt(1), 73);
    }
    
    
    @OnReceive(url="/{url}")
    static void fetch(Person p, JSONik model) {
        model.setFetched(p);
        throw new IllegalStateException("Got him: " + p);
    }
    
    @Http(@Http.Resource(
        content = "{'firstName': 'Sitar', 'sex': 'MALE'}", 
        path="/person.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseJSON() {
        JSONik js = new JSONik();
        js.applyBindings();
        
        js.fetch("person.json");
        
        Person p = null;
        for (int i = 0; i < 10000000; i++) {
            if (js.getFetched() != null) {
                p = js.getFetched();
            }
        }
        assert p != null : "We should get our person back: " + p;
        assert "Sitar".equals(p.getFirstName()) : "Expecting Sitar: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
    }
    
    @Http(@Http.Resource(
        content = "[{'firstName': 'Sitar', 'sex': 'MALE'}]", 
        path="/person.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseJSONArray() {
        JSONik js = new JSONik();
        js.applyBindings();
        
        js.fetch("person.json");
        
        Person p = null;
        for (int i = 0; i < 10000000; i++) {
            if (js.getFetched() != null) {
                p = js.getFetched();
            }
        }
        assert p != null : "We should get our person back: " + p;
        assert "Sitar".equals(p.getFirstName()) : "Expecting Sitar: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(JSONTest.class);
    }
    
}
