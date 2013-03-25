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

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ConvertTypes {
    ConvertTypes() {
    }
    
    public static String toString(Object object, String property) {
        Object ret = getProperty(object, property);
        return ret == null ? null : ret.toString();
    }

    public static double toDouble(Object object, String property) {
        Object ret = getProperty(object, property);
        return ret instanceof Number ? ((Number)ret).doubleValue() : Double.NaN;
    }

    public static int toInt(Object object, String property) {
        Object ret = getProperty(object, property);
        return ret instanceof Number ? ((Number)ret).intValue() : Integer.MIN_VALUE;
    }
    
    @JavaScriptBody(args = { "object", "property" },
        body = "if (property === null) return object;\n"
        + "var p = object[property]; return p ? p : null;"
    )
    private static Object getProperty(Object object, String property) {
        return null;
    }
}
