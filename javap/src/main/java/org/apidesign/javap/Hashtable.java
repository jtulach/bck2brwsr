/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.javap;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** A JavaScript optimized replacement for Hashtable.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Hashtable {
    private Object[] keys;
    private Object[] values;

    Hashtable(int i) {
        this();
    }

    Hashtable(int i, double d) {
        this();
    }

    Hashtable() {
    }

    @JavaScriptBody(args = { "self", "key", "val" }, body = 
        "self[key] = val;"
    )
    synchronized void put(Object key, Object val) {
        int[] where = { -1, -1 };
        Object found = get(key, where);
        if (where[0] != -1) {
            // key exists
            values[where[0]] = val;
        } else {
            if (where[1] != -1) {
                // null found
                keys[where[1]] = key;
                values[where[1]] = val;
            } else {
                if (keys == null) {
                    keys = new Object[11];
                    values = new Object[11];
                    keys[0] = key;
                    values[0] = val;
                } else {
                    Object[] newKeys = new Object[keys.length * 2];
                    Object[] newValues = new Object[values.length * 2];
                    for (int i = 0; i < keys.length; i++) {
                        newKeys[i] = keys[i];
                        newValues[i] = values[i];
                    }
                    newKeys[keys.length] = key;
                    newValues[keys.length] = val;
                    keys = newKeys;
                    values = newValues;
                }
            }
        }
    }

    @JavaScriptBody(args = {"self", "key" }, body = 
        "var r = self[key]; return r ? r : null;"
    )
    Object get(Object key) {
        return get(key, null);
    }
    private synchronized Object get(Object key, int[] foundAndNull) {
        if (keys == null) {
            return null;
        }
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] == null) {
                if (foundAndNull != null) {
                    foundAndNull[1] = i;
                }
            } else if (keys[i].equals(key)) {
                if (foundAndNull != null) {
                    foundAndNull[0] = i;
                }
                return values[i];
            }
        }
        return null;
    }
    
}
