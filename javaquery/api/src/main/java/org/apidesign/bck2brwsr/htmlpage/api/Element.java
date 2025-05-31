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
package org.apidesign.bck2brwsr.htmlpage.api;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Represents a generic HTML element.
 *
 * @author Jaroslav Tulach
 */
public abstract class Element {
    private final String id;
    
    public Element(String id) {
        this.id = id;
    }
    
    /** Id of the element in the document.
     * @return the id for this element
     */
    public String getId() {
        return id;
    }
    
    abstract void dontSubclass();
    
    @JavaScriptBody(
        args={"el", "property", "value"},
        body="var e = window.document.getElementById(el._id());\n"
           + "e[property] = value;\n"
    )
    static native void setAttribute(Element el, String property, Object value);

    @JavaScriptBody(
        args={"el", "property"},
        body="var e = window.document.getElementById(el._id());\n"
           + "return e[property];\n"
    )
    static native Object getAttribute(Element el, String property);
    
    @JavaScriptBody(
        args={"el"},
        body="return window.document.getElementById(el._id());"
    )
    static native Object getElementById(Element el);
    
    /** Executes given runnable when user performs a "click" on the given
     * element.
     * @param data an array of one element to fill with event parameter (if any)
     * @param r the runnable to execute, never null
     */
    @JavaScriptBody(
        args={ "ev", "r" },
        body="var e = window.document.getElementById(this._id());\n"
           + "e[ev._id()] = function(ev) {\n"
        + "  var d = ev ? ev : null;\n"
        + "  r['onEvent__VLjava_lang_Object_2'](d);\n"
        + "};\n"
    )
    final void on(OnEvent ev, OnHandler r) {
    }

    /** Shows alert message dialog in a browser.
     * @param msg the message to show
     */
    @JavaScriptBody(args = "msg", body = "alert(msg);")
    public static native void alert(String msg);

    /** Generic way to query any attribute of this element.
     * @param property name of the attribute
     */
    public final Object getAttribute(String property) {
        return getAttribute(this, property);
    }
    
    /** Generic way to change an attribute of this element.
     * 
     * @param property name of the attribute
     * @param value value to associate with the attribute
     */
    public final void setAttribute(String property, Object value) {
        setAttribute(this, property, value);
    }
}
