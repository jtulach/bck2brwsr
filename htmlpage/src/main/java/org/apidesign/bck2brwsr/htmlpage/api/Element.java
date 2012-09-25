/**
 * Java 4 Browser Bytecode Translator
 * Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
    
    static void setAttribute(Element el, String property, Object value) {
        throw new UnsupportedOperationException("Needs JavaScript!");
    }
    
    /** Executes given runnable when user performs a "click" on the given
     * element.
     * @param r the runnable to execute, never null
     */
    public final void addOnClick(Runnable r) {
        throw new UnsupportedOperationException("Needs JavaScript!");
    }
}
