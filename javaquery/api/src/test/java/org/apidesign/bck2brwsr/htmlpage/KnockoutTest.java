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
import org.apidesign.bck2brwsr.htmlpage.api.OnEvent;
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
    @Property(name="name", type=String.class)
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
        assert "Kukuc".equals(m.INPUT.getValue()) : "Value is really kukuc: " + m.INPUT.getValue();
        m.INPUT.setValue("Jardo");
        m.triggerEvent(m.INPUT, OnEvent.CHANGE);
        assert "Jardo".equals(m.getName()) : "Name property updated: " + m.getName();
    }
    
    @ComputedProperty
    static String helloMessage(String name) {
        return "Hello " + name + "!";
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(KnockoutTest.class);
    }
}
