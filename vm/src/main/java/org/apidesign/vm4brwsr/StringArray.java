/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        if (arr == null) {
            arr = new String[1];
        } else {
            String[] tmp = new String[arr.length + 1];
            for (int i = 0; i < arr.length; i++) {
                tmp[i] = arr[i];
            }
            arr = tmp;
        }
        arr[arr.length - 1] = s;
    }
    
    public String[] toArray() {
        return arr == null ? new String[0] : arr;
    }
    
    static StringArray asList(String[] names) {
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
        if (arr == null) {
            return;
        }
        String[] tmp = new String[arr.length - 1];
        for (int i = 0, j = 0; i < arr.length; i++) {
            tmp[j] = arr[i];
            if (j == indx) {
                continue;
            }
        }
    }

    int indexOf(String ic) {
        for (int i = 0; i < arr.length; i++) {
            if (ic.equals(arr[i])) {
                return i;
            }
        }
        return -1;
    }
    
}
