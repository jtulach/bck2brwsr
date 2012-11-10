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
        setSize(size() + 1);
        setElementAt(obj, size());
    }

    int size() {
        return arr == null ? 0 : arr.length;
    }

    void copyInto(Object[] accflags) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Object elementAt(int index) {
        return arr[index];
    }

    void setSize(int len) {
        if (arr == null) {
            arr = new Object[len];
        } else {
            Object[] newArr = new Object[len];
            int min = Math.min(len, arr.length);
            for (int i = 0; i < min; i++) {
                newArr[i] = arr[i];
            }
            arr = newArr;
        }
    }

    void setElementAt(Object val, int index) {
        arr[index] = val;
    }
}
