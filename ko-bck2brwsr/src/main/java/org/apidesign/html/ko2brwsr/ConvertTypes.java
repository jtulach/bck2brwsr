/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.ko2brwsr;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class ConvertTypes {
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

    public static <T> T toModel(Class<T> modelClass, Object object, String property) {
        Object ret = getProperty(object, property);
        if (ret == null || modelClass.isInstance(ret)) {
            return modelClass.cast(ret);
        }
        throw new IllegalStateException("Value " + ret + " is not of type " + modelClass);
    }
    
    public static String toJSON(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Enum) {
            value = value.toString();
        }
        if (value instanceof String) {
            return '"' + 
                ((String)value).
                    replace("\"", "\\\"").
                    replace("\n", "\\n").
                    replace("\r", "\\r").
                    replace("\t", "\\t")
                + '"';
        }
        return value.toString();
    }
    
    @JavaScriptBody(args = { "object", "property" },
        body = "if (property === null) return object;\n"
        + "var p = object[property]; return p ? p : null;"
    )
    private static Object getProperty(Object object, String property) {
        return null;
    }
    
    public static String createJSONP(Object[] jsonResult, Runnable whenDone) {
        int h = whenDone.hashCode();
        String name;
        for (;;) {
            name = "jsonp" + Integer.toHexString(h);
            if (defineIfUnused(name, jsonResult, whenDone)) {
                return name;
            }
            h++;
        }
    }

    @JavaScriptBody(args = { "name", "arr", "run" }, body = 
        "if (window[name]) return false;\n "
      + "window[name] = function(data) {\n "
      + "  delete window[name];\n"
      + "  var el = window.document.getElementById(name);\n"
      + "  el.parentNode.removeChild(el);\n"
      + "  arr[0] = data;\n"
      + "  run.run__V();\n"
      + "};\n"
      + "return true;\n"
    )
    private static boolean defineIfUnused(String name, Object[] arr, Runnable run) {
        return true;
    }
    
    @JavaScriptBody(args = { "s" }, body = "return eval('(' + s + ')');")
    static Object parse(String s) {
        return s;
    }
    
    @JavaScriptBody(args = { "url", "arr", "callback" }, body = ""
        + "var request = new XMLHttpRequest();\n"
        + "request.open('GET', url, true);\n"
        + "request.setRequestHeader('Content-Type', 'application/json; charset=utf-8');\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (this.readyState!==4) return;\n"
        + "  try {\n"
        + "    arr[0] = eval('(' + this.response + ')');\n"
        + "  } catch (error) {;\n"
        + "    throw 'Cannot parse' + error + ':' + this.response;\n"
        + "  };\n"
        + "  callback.run__V();\n"
        + "};"
        + "request.send();"
    )
    private static void loadJSON(
        String url, Object[] jsonResult, Runnable whenDone
    ) {
    }
    
    public static void loadJSON(
        String url, Object[] jsonResult, Runnable whenDone, String jsonp
    ) {
        if (jsonp == null) {
            loadJSON(url, jsonResult, whenDone);
        } else {
            loadJSONP(url, jsonp);
        }
    }
    
    @JavaScriptBody(args = { "url", "jsonp" }, body = 
        "var scrpt = window.document.createElement('script');\n "
        + "scrpt.setAttribute('src', url);\n "
        + "scrpt.setAttribute('id', jsonp);\n "
        + "scrpt.setAttribute('type', 'text/javascript');\n "
        + "var body = document.getElementsByTagName('body')[0];\n "
        + "body.appendChild(scrpt);\n"
    )
    private static void loadJSONP(String url, String jsonp) {
        
    }
    
    public static void extractJSON(Object jsonObject, String[] props, Object[] values) {
        for (int i = 0; i < props.length; i++) {
            values[i] = getProperty(jsonObject, props[i]);
        }
    }
    
}
