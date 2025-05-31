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

/**
 *
 * @author Jaroslav Tulach
 */
public final class Input extends Element {
    public Input(String id) {
        super(id);
    }

    @Override
    void dontSubclass() {
    }
    
    public void setAutocomplete(boolean state) {
        setAttribute(this, "autocomplete", state);
    }
    
    public final String getValue() {
        return (String)getAttribute(this, "value");
    }
    
    public final void setValue(String txt) {
        setAttribute(this, "value", txt);
    }
}
