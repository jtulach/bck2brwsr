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

import java.util.Arrays;
import java.util.Iterator;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
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
    @Property(name = "fetched", type = PersonImpl.class),
    @Property(name = "fetchedCount", type = int.class),
    @Property(name = "fetchedSex", type = Sex.class, array = true)
})
public class JSONTest {
    private JSONik js;
    private Integer orig;
    
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
    
    @BrwsrTest public void toJSONInABrowser() throws Throwable {
        Person p = new Person();
        p.setSex(Sex.MALE);
        p.setFirstName("Jarda");
        p.setLastName("Tulach");

        Object json;
        try {
            json = parseJSON(p.toString());
        } catch (Throwable ex) {
            throw new IllegalStateException("Can't parse " + p).initCause(ex);
        }
        
        Person p2 = new Person(json);
        
        assert p2.getFirstName().equals(p.getFirstName()) : 
            "Should be the same: " + p.getFirstName() + " != " + p2.getFirstName();
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
    }

    @OnReceive(url="/{url}")
    static void fetchArray(Person[] p, JSONik model) {
        model.setFetchedCount(p.length);
        model.setFetched(p[0]);
    }
    
    @OnReceive(url="/{url}")
    static void fetchPeople(People p, JSONik model) {
        model.setFetchedCount(p.getInfo().size());
        model.setFetched(p.getInfo().get(0));
    }

    @OnReceive(url="/{url}")
    static void fetchPeopleAge(People p, JSONik model) {
        int sum = 0;
        for (int a : p.getAge()) {
            sum += a;
        }
        model.setFetchedCount(sum);
    }
    
    @Http(@Http.Resource(
        content = "{'firstName': 'Sitar', 'sex': 'MALE'}", 
        path="/person.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseJSON() throws InterruptedException {
        if (js == null) {
            js = new JSONik();
            js.applyBindings();

            js.fetch("person.json");
        }
    
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert "Sitar".equals(p.getFirstName()) : "Expecting Sitar: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
    }
    
    @OnReceive(url="/{url}?callme={me}", jsonp = "me")
    static void fetchViaJSONP(Person p, JSONik model) {
        model.setFetched(p);
    }
    
    @Http(@Http.Resource(
        content = "$0({'firstName': 'Mitar', 'sex': 'MALE'})", 
        path="/person.json", 
        mimeType = "application/javascript",
        parameters = { "callme" }
    ))
    @BrwsrTest public void loadAndParseJSONP() throws InterruptedException {
        
        if (js == null) {
            orig = scriptElements();
            assert orig > 0 : "There should be some scripts on the page";
            
            js = new JSONik();
            js.applyBindings();

            js.fetchViaJSONP("person.json");
        }
    
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert "Mitar".equals(p.getFirstName()) : "Unexpected: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
        
        int now = scriptElements();
        
        assert orig == now : "The set of elements is unchanged. Delta: " + (now - orig);
    }
    
    @JavaScriptBody(args = {  }, body = "return window.document.getElementsByTagName('script').length;")
    private static native int scriptElements();

    @JavaScriptBody(args = { "s" }, body = "return window.JSON.parse(s);")
    private static native Object parseJSON(String s);
    
    @Http(@Http.Resource(
        content = "{'firstName': 'Sitar', 'sex': 'MALE'}", 
        path="/person.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseJSONSentToArray() throws InterruptedException {
        if (js == null) {
            js = new JSONik();
            js.applyBindings();

            js.fetchArray("person.json");
        }
        
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert p != null : "We should get our person back: " + p;
        assert "Sitar".equals(p.getFirstName()) : "Expecting Sitar: " + p.getFirstName();
        assert Sex.MALE.equals(p.getSex()) : "Expecting MALE: " + p.getSex();
    }
    
    @Http(@Http.Resource(
        content = "[{'firstName': 'Gitar', 'sex': 'FEMALE'}]", 
        path="/person.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseJSONArraySingle() throws InterruptedException {
        if (js == null) {
            js = new JSONik();
            js.applyBindings();
        
            js.fetch("person.json");
        }
        
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert p != null : "We should get our person back: " + p;
        assert "Gitar".equals(p.getFirstName()) : "Expecting Gitar: " + p.getFirstName();
        assert Sex.FEMALE.equals(p.getSex()) : "Expecting FEMALE: " + p.getSex();
    }
    
    @Http(@Http.Resource(
        content = "{'info':[{'firstName': 'Gitar', 'sex': 'FEMALE'}]}", 
        path="/people.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseArrayInPeople() throws InterruptedException {
        if (js == null) {
            js = new JSONik();
            js.applyBindings();
        
            js.fetchPeople("people.json");
        }
        
        if (0 == js.getFetchedCount()) {
            throw new InterruptedException();
        }

        assert js.getFetchedCount() == 1 : "One person loaded: " + js.getFetchedCount();
        
        Person p = js.getFetched();
        
        assert p != null : "We should get our person back: " + p;
        assert "Gitar".equals(p.getFirstName()) : "Expecting Gitar: " + p.getFirstName();
        assert Sex.FEMALE.equals(p.getSex()) : "Expecting FEMALE: " + p.getSex();
    }
    
    @Http(@Http.Resource(
        content = "{'age':[1, 2, 3]}", 
        path="/people.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseArrayOfIntegers() throws InterruptedException {
        if (js == null) {
            js = new JSONik();
            js.applyBindings();
        
            js.fetchPeopleAge("people.json");
        }
        
        if (0 == js.getFetchedCount()) {
            throw new InterruptedException();
        }

        assert js.getFetchedCount() == 6 : "1 + 2 + 3 is " + js.getFetchedCount();
    }
    
    @OnReceive(url="/{url}")
    static void fetchPeopleSex(People p, JSONik model) {
        model.setFetchedCount(1);
        model.getFetchedSex().addAll(p.getSex());
    }
    
    
    @Http(@Http.Resource(
        content = "{'sex':['FEMALE', 'MALE', 'MALE']}", 
        path="/people.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseArrayOfEnums() throws InterruptedException {
        if (js == null) {
            js = new JSONik();
            js.applyBindings();
        
            js.fetchPeopleSex("people.json");
        }
        
        if (0 == js.getFetchedCount()) {
            throw new InterruptedException();
        }

        assert js.getFetchedCount() == 1 : "Loaded";
        
        assert js.getFetchedSex().size() == 3 : "Three values " + js.getFetchedSex();
        assert js.getFetchedSex().get(0) == Sex.FEMALE : "Female first " + js.getFetchedSex();
        assert js.getFetchedSex().get(1) == Sex.MALE : "male 2nd " + js.getFetchedSex();
        assert js.getFetchedSex().get(2) == Sex.MALE : "male 3rd " + js.getFetchedSex();
    }
    
    @Http(@Http.Resource(
        content = "[{'firstName': 'Gitar', 'sex': 'FEMALE'},"
        + "{'firstName': 'Peter', 'sex': 'MALE'}"
        + "]", 
        path="/person.json", 
        mimeType = "application/json"
    ))
    @BrwsrTest public void loadAndParseJSONArray() throws InterruptedException {
        if (js == null) {
            js = new JSONik();
            js.applyBindings();
            js.fetchArray("person.json");
        }
        
        
        Person p = js.getFetched();
        if (p == null) {
            throw new InterruptedException();
        }
        
        assert js.getFetchedCount() == 2 : "We got two values: " + js.getFetchedCount();
        assert p != null : "We should get our person back: " + p;
        assert "Gitar".equals(p.getFirstName()) : "Expecting Gitar: " + p.getFirstName();
        assert Sex.FEMALE.equals(p.getSex()) : "Expecting FEMALE: " + p.getSex();
    }

    @Factory public static Object[] create() {
        return VMTest.create(JSONTest.class);
    }
    
}
