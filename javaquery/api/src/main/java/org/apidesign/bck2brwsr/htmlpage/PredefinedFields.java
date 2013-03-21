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

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jan Horvath <jhorvath@netbeans.org>
 */
public class PredefinedFields {
    
    private static final Map<String, String> IMPORTS = new HashMap<String, String>() {
        {
            put("canvas", "import org.apidesign.bck2brwsr.core.JavaScriptBody;");
        }
    };
    
    private static final Map<String, String> FIELDS = new HashMap<String, String>() {
        {
            put("canvas", 
                    "    @JavaScriptBody(\n" +
                    "            args = {\"el\"},\n" +
                    "            body = \"var e = window.document.getElementById(el._id());\\n\"\n" +
                    "            + \"return e.getContext('2d');\\n\")\n" +
                    "    private native static Object getContextImpl(Canvas el);\n" +
                    "    \n" +
                    "    public GraphicsContext getContext() {\n" +
                    "        return new GraphicsContext(getContextImpl(this));\n" +
                    "    }");
        }
    };
    
    static void appendImports(Writer w, String tag) throws IOException {
        String text = IMPORTS.get(tag.toLowerCase());
        if (text != null) {
            w.append(text).append("\n");
        }
    }
    
    static void appendFields(Writer w, String tag) throws IOException {
        String text = FIELDS.get(tag.toLowerCase());
        if (text != null) {
            w.append(text).append("\n");
        }
    }
}
