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

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary storing the type of attributes here. This should be implemented in HTML 5 model
 *
 * @author Jan Horvath <jhorvath@netbeans.org>
 */
public class Attributes {
    
    static final Map<String, String> TYPES = new HashMap<String, String>() {
        {
            // HTML Global Attributes
            // id attribute is already defined in Element, don't add it again
            put("accesskey", "String");
            put("class", "String");
            put("contenteditable", "Boolean");
            put("contextmenu", "String");
            put("dir", "String");
            put("draggable", "Boolean");
            put("dropzone", "String");
            put("hidden", "Boolean");
            put("lang", "String");
            put("spellcheck", "Boolean");
            put("style", "String");
            put("tabindex", "String");
            put("title", "String");
            put("translate", "Boolean");
            put("width", "Integer");
            put("height", "Integer");
            
            put("value", "String");
            put("disabled", "Boolean");
            
//          put("text", "String"); 'text' field is used to set innerHTML of element
        }
    };
}
