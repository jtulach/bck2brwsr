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
import static org.apidesign.bck2brwsr.htmlpage.api.Element.getAttribute;

/**
 *
 * @author Anton Epple <toni.epple@eppleton.de>
 */
public class Canvas extends Element {

    public Canvas(String id) {
        super(id);
    }

    public void setHeight(int height) {
        setAttribute(this, "height", height);
    }

    public int getHeight() {
       Object ret =  getAttribute(this, "height");
       return (ret instanceof Number) ? ((Number)ret).intValue(): Integer.MIN_VALUE;
    }
    
    public void setWidth(int width) {
        setAttribute(this, "width", width);
    }

    public int getWidth() {
       Object ret =  getAttribute(this, "width");
       return (ret instanceof Number) ? ((Number)ret).intValue(): Integer.MIN_VALUE;
    }

    @JavaScriptBody(
            args = {"el"},
            body = "var e = window.document.getElementById(el._id());\n"
            + "return e['getContext']('2d');\n")
    private native static Object getContextImpl(Canvas el);

    public GraphicsContext getContext() {
        return new GraphicsContext(getContextImpl(this));
    }

    @Override
    void dontSubclass() {
    }
}
