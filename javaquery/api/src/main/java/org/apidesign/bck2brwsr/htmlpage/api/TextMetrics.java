/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

/**
 *
 * @author Anton Epple <toni.epple@eppleton.de>
 */
public class TextMetrics {

    private Object textMetrics;

    TextMetrics(Object measureTextImpl) {
        this.textMetrics = measureTextImpl;
    }

    @JavaScriptBody(args = {"textMetrics"}, body = "return textMetrics.width;")
    private native double getWidth(Object textMetrics);

    @JavaScriptBody(args = {"textMetrics"}, body = "return textMetrics.height;")
    private native double getHeight(Object textMetrics);

    public double getWidth() {
        return getWidth(textMetrics);
    }

    public double getHeight() {
        return getHeight(textMetrics);

    }
}
