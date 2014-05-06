/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.lang.reflect;

import org.apidesign.bck2brwsr.core.Exported;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.core.JavaScriptPrototype;

/**
 * The {@code Array} class provides static methods to dynamically create and
 * access Java arrays.
 *
 * <p>{@code Array} permits widening conversions to occur during a get or set
 * operation, but throws an {@code IllegalArgumentException} if a narrowing
 * conversion would occur.
 *
 * @author Nakul Saraiya
 */
@JavaScriptPrototype(prototype = "new Array", container = "Array.prototype")
public final
class Array {

    /**
     * Constructor.  Class Array is not instantiable.
     */
    private Array() {}

    /**
     * Creates a new array with the specified component type and
     * length.
     * Invoking this method is equivalent to creating an array
     * as follows:
     * <blockquote>
     * <pre>
     * int[] x = {length};
     * Array.newInstance(componentType, x);
     * </pre>
     * </blockquote>
     *
     * @param componentType the {@code Class} object representing the
     * component type of the new array
     * @param length the length of the new array
     * @return the new array
     * @exception NullPointerException if the specified
     * {@code componentType} parameter is null
     * @exception IllegalArgumentException if componentType is {@link Void#TYPE}
     * @exception NegativeArraySizeException if the specified {@code length}
     * is negative
     */
    public static Object newInstance(Class<?> componentType, int length)
    throws NegativeArraySizeException {
        if (length < 0) {
            throw new NegativeArraySizeException();
        }
        String sig = findSignature(componentType);
        return newArray(componentType.isPrimitive(), sig, null, length);
    }
    
    private static String findSignature(Class<?> type) {
        if (type == Integer.TYPE) {
            return "[I";
        }
        if (type == Long.TYPE) {
            return "[J";
        }
        if (type == Double.TYPE) {
            return "[D";
        }
        if (type == Float.TYPE) {
            return "[F";
        }
        if (type == Byte.TYPE) {
            return "[B";
        }
        if (type == Boolean.TYPE) {
            return "[Z";
        }
        if (type == Short.TYPE) {
            return "[S";
        }
        if (type == Character.TYPE) {
            return "[C";
        }
        if (type.getName().equals("void")) {
            throw new IllegalStateException("Can't create array for " + type);
        }
        return "[L" + type.getName().replace('.', '/') + ";";
    }
    /**
     * Creates a new array
     * with the specified component type and dimensions.
     * If {@code componentType}
     * represents a non-array class or interface, the new array
     * has {@code dimensions.length} dimensions and
     * {@code componentType} as its component type. If
     * {@code componentType} represents an array class, the
     * number of dimensions of the new array is equal to the sum
     * of {@code dimensions.length} and the number of
     * dimensions of {@code componentType}. In this case, the
     * component type of the new array is the component type of
     * {@code componentType}.
     *
     * <p>The number of dimensions of the new array must not
     * exceed the number of array dimensions supported by the
     * implementation (typically 255).
     *
     * @param componentType the {@code Class} object representing the component
     * type of the new array
     * @param dimensions an array of {@code int} representing the dimensions of
     * the new array
     * @return the new array
     * @exception NullPointerException if the specified
     * {@code componentType} argument is null
     * @exception IllegalArgumentException if the specified {@code dimensions}
     * argument is a zero-dimensional array, or if the number of
     * requested dimensions exceeds the limit on the number of array dimensions
     * supported by the implementation (typically 255), or if componentType
     * is {@link Void#TYPE}.
     * @exception NegativeArraySizeException if any of the components in
     * the specified {@code dimensions} argument is negative.
     */
    public static Object newInstance(Class<?> componentType, int... dimensions)
        throws IllegalArgumentException, NegativeArraySizeException {
        StringBuilder sig = new StringBuilder();
        for (int i = 1; i < dimensions.length; i++) {
            sig.append('[');
        }
        sig.append(findSignature(componentType));
        return multiNewArray(sig.toString(), dimensions, 0);
    }

    /**
     * Returns the length of the specified array object, as an {@code int}.
     *
     * @param array the array
     * @return the length of the array
     * @exception IllegalArgumentException if the object argument is not
     * an array
     */
    public static int getLength(Object array)
    throws IllegalArgumentException {
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException("Argument is not an array");
        }
        return length(array);
    }
    
    @JavaScriptBody(args = { "arr" }, body = "return arr.length;")
    private static native int length(Object arr);

    /**
     * Returns the value of the indexed component in the specified
     * array object.  The value is automatically wrapped in an object
     * if it has a primitive type.
     *
     * @param array the array
     * @param index the index
     * @return the (possibly wrapped) value of the indexed component in
     * the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     */
    public static Object get(Object array, int index)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        final Class<?> t = array.getClass().getComponentType();
        if (t.isPrimitive()) {
            return fromPrimitive(t, array, index);
        } else {
            return ((Object[])array)[index];
        }
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code boolean}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native boolean getBoolean(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code byte}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static byte getByte(Object array, int index)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array.getClass().getComponentType() != Byte.TYPE) {
            throw new IllegalArgumentException();
        }
        byte[] arr = (byte[]) array;
        return arr[index];
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code char}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native char getChar(Object array, int index)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code short}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static short getShort(Object array, int index)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        final Class<?> t = array.getClass().getComponentType();
        if (t == Short.TYPE) {
            short[] arr = (short[]) array;
            return arr[index];
        }
        return getByte(array, index);
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as an {@code int}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static int getInt(Object array, int index) 
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        final Class<?> t = array.getClass().getComponentType();
        if (t == Integer.TYPE) {
            int[] arr = (int[]) array;
            return arr[index];
        }
        return getShort(array, index);
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code long}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static long getLong(Object array, int index)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        final Class<?> t = array.getClass().getComponentType();
        if (t == Long.TYPE) {
            long[] arr = (long[]) array;
            return arr[index];
        }
        return getInt(array, index);
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code float}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static float getFloat(Object array, int index)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        final Class<?> t = array.getClass().getComponentType();
        if (t == Float.TYPE) {
            float[] arr = (float[]) array;
            return arr[index];
        }
        return getLong(array, index);
    }

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a {@code double}.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static double getDouble(Object array, int index)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        final Class<?> t = array.getClass().getComponentType();
        if (t == Double.TYPE) {
            double[] arr = (double[]) array;
            return arr[index];
        }
        return getFloat(array, index);
    }

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified new value.  The new value is first
     * automatically unwrapped if the array has a primitive component
     * type.
     * @param array the array
     * @param index the index into the array
     * @param value the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the array component type is primitive and
     * an unwrapping conversion fails
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     */
    public static void set(Object array, int index, Object value)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (array.getClass().getComponentType().isPrimitive()) {
            throw new IllegalArgumentException();
        } else {
            Object[] arr = (Object[])array;
            arr[index] = value;
        }
    }

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code boolean} value.
     * @param array the array
     * @param index the index into the array
     * @param z the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setBoolean(Object array, int index, boolean z)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code byte} value.
     * @param array the array
     * @param index the index into the array
     * @param b the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static void setByte(Object array, int index, byte b)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        Class<?> t = array.getClass().getComponentType();
        if (t == Byte.TYPE) {
            byte[] arr = (byte[]) array;
            arr[index] = b;
        } else {
            setShort(array, index, b);
        }
    }

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code char} value.
     * @param array the array
     * @param index the index into the array
     * @param c the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setChar(Object array, int index, char c)
        throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code short} value.
     * @param array the array
     * @param index the index into the array
     * @param s the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static void setShort(Object array, int index, short s)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        Class<?> t = array.getClass().getComponentType();
        if (t == Short.TYPE) {
            short[] arr = (short[]) array;
            arr[index] = s;
        } else {
            setInt(array, index, s);
        }
        
    }
    
    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code int} value.
     * @param array the array
     * @param index the index into the array
     * @param i the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static void setInt(Object array, int index, int i)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        Class<?> t = array.getClass().getComponentType();
        if (t == Integer.TYPE) {
            int[] arr = (int[]) array;
            arr[index] = i;
        } else {
            setLong(array, index, i);
        }
    }

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code long} value.
     * @param array the array
     * @param index the index into the array
     * @param l the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static void setLong(Object array, int index, long l)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        Class<?> t = array.getClass().getComponentType();
        if (t == Long.TYPE) {
            long[] arr = (long[]) array;
            arr[index] = l;
        } else {
            setFloat(array, index, l);
        }
    }

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code float} value.
     * @param array the array
     * @param index the index into the array
     * @param f the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static void setFloat(Object array, int index, float f)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        Class<?> t = array.getClass().getComponentType();
        if (t == Float.TYPE) {
            float[] arr = (float[])array;
            arr[index] = f;
        } else {
            setDouble(array, index, f);
        }
    }

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified {@code double} value.
     * @param array the array
     * @param index the index into the array
     * @param d the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified {@code index}
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static void setDouble(Object array, int index, double d)
    throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        Class<?> t = array.getClass().getComponentType();
        if (t == Double.TYPE) {
            double[] arr = (double[])array;
            arr[index] = d;
        } else {
            throw new IllegalArgumentException("argument type mismatch");
        }
    }

    /*
     * Private
     */

    @JavaScriptBody(args = { "primitive", "sig", "fn", "length" }, body =
          "var arr = new Array(length);\n"
        + "var value = primitive ? 0 : null;\n"
        + "for(var i = 0; i < length; i++) arr[i] = value;\n"
        + "arr.jvmName = sig;\n"
        + "arr.fnc = fn;\n"
//        + "java.lang.System.out.println('Assigned ' + arr.jvmName + ' fn: ' + (!!arr.fnc));\n"
        + "return arr;"
    )
    @Exported
    private static native Object newArray(boolean primitive, String sig, Object fn, int length);


    @Exported
    private static Object multiNewArray(String sig, int[] dims, int index)
    throws IllegalArgumentException, NegativeArraySizeException {
        if (dims.length == index + 1) {
            return newArray(sig.length() == 2, sig, null, dims[index]);
        }
        Object arr = newArray(false, sig, null, dims[index]);
        String compsig = sig.substring(1);
        int len = getLength(arr);
        for (int i = 0; i < len; i++) {
            setArray(arr, i, multiNewArray(compsig, dims, index + 1));
        }
        return arr;
    }
    private static Object fromPrimitive(Class<?> t, Object array, int index) {
        return Method.fromPrimitive(t, atArray(array, index));
    }
    
    @JavaScriptBody(args = { "array", "index" }, body = "return array[index];")
    private static native Object atArray(Object array, int index);

    @JavaScriptBody(args = { "array", "index", "v" }, body = "array[index] = v;")
    private static native Object setArray(Object array, int index, Object v);
}
