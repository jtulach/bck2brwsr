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
