/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sun.tools.javap;

/** A JavaScript ready replacement for java.util.Vector
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Vector {
    private Object[] arr;
    
    Vector() {
    }

    Vector(int i) {
        this();
    }

    void add(Object objectType) {
        addElement(objectType);
    }
    void addElement(Object obj) {
        final int s = size();
        setSize(s + 1);
        setElementAt(obj, s);
    }

    int size() {
        return arr == null ? 0 : arr.length;
    }

    void copyInto(Object[] newArr) {
        if (arr == null) {
            return;
        }
        int min = Math.min(newArr.length, arr.length);
        for (int i = 0; i < min; i++) {
            newArr[i] = arr[i];
        }
    }

    Object elementAt(int index) {
        return arr[index];
    }

    void setSize(int len) {
        Object[] newArr = new Object[len];
        copyInto(newArr);
        arr = newArr;
    }

    void setElementAt(Object val, int index) {
        arr[index] = val;
    }
}
