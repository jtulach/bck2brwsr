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
package org.apidesign.vm4brwsr;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class StringArray {
    private String[] arr;

    public StringArray() {
    }

    private StringArray(String[] arr) {
        this.arr = arr;
    }
    
    public void add(String s) {
        add(s, false);
    }
    private boolean add(String s, boolean check) {
        if (arr == null) {
            arr = new String[1];
        } else {
            String[] tmp = new String[arr.length + 1];
            for (int i = 0; i < arr.length; i++) {
                if (check && s.equals(arr[i])) {
                    return false;
                }
                tmp[i] = arr[i];
            }
            arr = tmp;
        }
        arr[arr.length - 1] = s;
        return true;
    }

    StringArray addAndNew(String... values) {
        int j;
        String[] tmp;
        if (arr == null) {
            tmp = new String[values.length];
            j = 0;
        } else {
            tmp = new String[arr.length + values.length];
            for (int i = 0; i < arr.length; i++) {
                tmp[i] = arr[i];
            }
            j = arr.length;
        }
        for (int i = 0; i < values.length;) {
            tmp[j++] = values[i++];
        }
        return new StringArray(tmp);
    }
    
    public String[] toArray() {
        return arr == null ? new String[0] : arr;
    }
    
    static StringArray asList(String... names) {
        return new StringArray(names);
    }

    void reverse() {
        for (int i = 0, j = arr.length; i < j; i++) {
            String s = arr[i];
            arr[i] = arr[--j];
            arr[j] = s;
        }
    }

    boolean contains(String n) {
        if (arr == null) {
            return false;
        }
        for (int i = 0; i < arr.length; i++) {
            if (n.equals(arr[i])) {
                return true;
            }
        }
        return false;
    }

    void delete(int indx) {
        if (arr == null || indx < 0 || indx >= arr.length) {
            return;
        }
        String[] tmp = new String[arr.length - 1];
        for (int i = 0, j = 0; i < arr.length; i++) {
            if (i != indx) {
                tmp[j++] = arr[i];
            }
        }
        arr = tmp;
    }
    void remove(String item) {
        int f = indexOf(item);
        if (f != -1) {
            delete(f);
        }
    }

    int indexOf(String ic) {
        if (arr != null) for (int i = 0; i < arr.length; i++) {
            if (ic.equals(arr[i])) {
                return i;
            }
        }
        return -1;
    }

    String getAndClear(int count, boolean clear) {
        String s = arr[count];
        if (clear) {
            arr[count] = null;
        }
        return s;
    }

    void clear() {
        arr = null;
    }

    boolean addIfMissing(String s) {
        return add(s, true);
    }
}
