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

import java.util.List;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;
import org.apidesign.bck2brwsr.htmlpage.api.OnEvent;
import org.apidesign.bck2brwsr.htmlpage.api.OnFunction;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.HtmlFragment;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Page(xhtml="Knockout.xhtml", className="KnockoutModel", properties={
    @Property(name="name", type=String.class),
    @Property(name="results", type=String.class, array = true),
    @Property(name="callbackCount", type=int.class),
    @Property(name="people", type=PersonImpl.class, array = true)
}) 
public class KnockoutTest {
    
    @HtmlFragment(
        "<h1 data-bind=\"text: helloMessage\">Loading Bck2Brwsr's Hello World...</h1>\n" +
        "Your name: <input id='input' data-bind=\"value: name\"></input>\n" +
        "<button id=\"hello\">Say Hello!</button>\n"
    )
    @BrwsrTest public void modifyValueAssertChangeInModel() {
        KnockoutModel m = new KnockoutModel();
        m.setName("Kukuc");
        m.applyBindings();
        assert "Kukuc".equals(m.input.getValue()) : "Value is really kukuc: " + m.input.getValue();
        m.input.setValue("Jardo");
        m.triggerEvent(m.input, OnEvent.CHANGE);
        assert "Jardo".equals(m.getName()) : "Name property updated: " + m.getName();
    }
    
    @HtmlFragment(
        "<ul id='ul' data-bind='foreach: results'>\n"
        + "  <li data-bind='text: $data, click: $root.call'/>\n"
        + "</ul>\n"
    )
    @BrwsrTest public void displayContentOfArray() {
        KnockoutModel m = new KnockoutModel();
        m.getResults().add("Ahoj");
        m.applyBindings();
        
        int cnt = countChildren("ul");
        assert cnt == 1 : "One child, but was " + cnt;
        
        m.getResults().add("Hi");

        cnt = countChildren("ul");
        assert cnt == 2 : "Two children now, but was " + cnt;
        
        triggerChildClick("ul", 1);
        
        assert 1 == m.getCallbackCount() : "One callback " + m.getCallbackCount();
        assert "Hi".equals(m.getName()) : "We got callback from 2nd child " + m.getName();
    }
    
    @HtmlFragment(
        "<ul id='ul' data-bind='foreach: cmpResults'>\n"
        + "  <li><b data-bind='text: $data'></b></li>\n"
        + "</ul>\n"
    )
    @BrwsrTest public void displayContentOfDerivedArray() {
        KnockoutModel m = new KnockoutModel();
        m.getResults().add("Ahoj");
        m.applyBindings();
        
        int cnt = countChildren("ul");
        assert cnt == 1 : "One child, but was " + cnt;
        
        m.getResults().add("hello");

        cnt = countChildren("ul");
        assert cnt == 2 : "Two children now, but was " + cnt;
    }
    
    @HtmlFragment(
        "<ul id='ul' data-bind='foreach: people'>\n"
        + "  <li data-bind='text: $data.firstName(), click: $root.removePerson'></li>\n"
        + "</ul>\n"
    )
    @BrwsrTest public void displayContentOfArrayOfPeople() {
        KnockoutModel m = new KnockoutModel();
        
        final Person first = new Person();
        first.setFirstName("first");
        m.getPeople().add(first);
        
        m.applyBindings();
        
        int cnt = countChildren("ul");
        assert cnt == 1 : "One child, but was " + cnt;
        
        final Person second = new Person();
        second.setFirstName("second");
        m.getPeople().add(second);

        cnt = countChildren("ul");
        assert cnt == 2 : "Two children now, but was " + cnt;

        triggerChildClick("ul", 1);
        
        assert 1 == m.getCallbackCount() : "One callback " + m.getCallbackCount();

        cnt = countChildren("ul");
        assert cnt == 1 : "Again one child, but was " + cnt;
        
        String txt = childText("ul", 0);
        assert "first".equals(txt) : "Expecting 'first': " + txt;
    }
     
    @OnFunction
    static void call(KnockoutModel m, String data) {
        m.setName(data);
        m.setCallbackCount(m.getCallbackCount() + 1);
    }

    @OnFunction
    static void removePerson(KnockoutModel model, Person data) {
        model.setCallbackCount(model.getCallbackCount() + 1);
        model.getPeople().remove(data);
    }
    
    
    @ComputedProperty
    static String helloMessage(String name) {
        return "Hello " + name + "!";
    }
    
    @ComputedProperty
    static List<String> cmpResults(List<String> results) {
        return results;
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(KnockoutTest.class);
    }
    
    @JavaScriptBody(args = { "id" }, body = 
          "var e = window.document.getElementById(id);\n "
        + "if (typeof e === 'undefined') return -2;\n "
        + "return e.children.length;\n "
    )
    private static native int countChildren(String id);

    @JavaScriptBody(args = { "id", "pos" }, body = 
          "var e = window.document.getElementById(id);\n "
        + "var ev = window.document.createEvent('MouseEvents');\n "
        + "ev.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);\n "
        + "e.children[pos].dispatchEvent(ev);\n "
    )
    private static native void triggerChildClick(String id, int pos);

    @JavaScriptBody(args = { "id", "pos" }, body = 
          "var e = window.document.getElementById(id);\n "
        + "var t = e.children[pos].innerHTML;\n "
        + "return t ? t : null;"
    )
    private static native String childText(String id, int pos);
}
