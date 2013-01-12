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
package org.apidesign.bck2brwsr.htmlpage.api;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Represents a generic HTML element.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class Element {
    private final String id;
    
    public Element(String id) {
        this.id = id;
    }
    
    abstract void dontSubclass();
    
    @JavaScriptBody(
        args={"el", "property", "value"},
        body="var e = window.document.getElementById(el.fld_id);\n"
           + "e[property] = value;\n"
    )
    static native void setAttribute(Element el, String property, Object value);

    @JavaScriptBody(
        args={"el", "property"},
        body="var e = window.document.getElementById(el.fld_id);\n"
           + "return e[property];\n"
    )
    static native Object getAttribute(Element el, String property);
    
    /** Executes given runnable when user performs a "click" on the given
     * element.
     * @param r the runnable to execute, never null
     */
    @JavaScriptBody(
        args={ "self", "ev", "r" },
        body="var e = window.document.getElementById(self.fld_id);\n"
           + "e[ev.fld_id] = function() { r.run__V(r); };\n"
    )
    final native void on(OnEvent ev, Runnable r);

    /** Shows alert message dialog in a browser.
     * @param msg the message to show
     */
    @JavaScriptBody(args = "msg", body = "alert(msg);")
    public static native void alert(String msg);
    
}
