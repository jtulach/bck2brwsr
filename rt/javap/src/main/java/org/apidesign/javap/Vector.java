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
package org.apidesign.javap;

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.core.JavaScriptPrototype;

/** A JavaScript ready replacement for java.util.Vector
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@JavaScriptPrototype(prototype = "new Array" )
final class Vector {
    private Object[] arr;
    
    Vector() {
    }

    Vector(int i) {
    }

    void add(Object objectType) {
        addElement(objectType);
    }
    @JavaScriptBody(args = { "obj" }, body = 
        "this.push(obj);"
    )
    void addElement(Object obj) {
        final int s = size();
        setSize(s + 1);
        setElementAt(obj, s);
    }

    @JavaScriptBody(args = { }, body = 
        "return this.length;"
    )
    int size() {
        return arr == null ? 0 : arr.length;
    }

    @JavaScriptBody(args = { "newArr" }, body =
        "for (var i = 0; i < this.length; i++) {\n"
      + "  newArr[i] = this[i];\n"
      + "}\n")
    void copyInto(Object[] newArr) {
        if (arr == null) {
            return;
        }
        int min = Math.min(newArr.length, arr.length);
        for (int i = 0; i < min; i++) {
            newArr[i] = arr[i];
        }
    }

    @JavaScriptBody(args = { "index" }, body =
        "return this[index];"
    )
    Object elementAt(int index) {
        return arr[index];
    }

    private void setSize(int len) {
        Object[] newArr = new Object[len];
        copyInto(newArr);
        arr = newArr;
    }

    @JavaScriptBody(args = { "val", "index" }, body = 
        "this[index] = val;"
    )
    void setElementAt(Object val, int index) {
        arr[index] = val;
    }
}
