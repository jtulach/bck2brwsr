/*
 * Copyright (c) 1996, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Serialization's descriptor for classes.  It contains the name and
 * serialVersionUID of the class.  The ObjectStreamClass for a specific class
 * loaded in this Java VM can be found/created using the lookup method.
 *
 * <p>The algorithm to compute the SerialVersionUID is described in
 * <a href="../../../platform/serialization/spec/class.html#4100">Object
 * Serialization Specification, Section 4.6, Stream Unique Identifiers</a>.
 *
 * @author      Mike Warres
 * @author      Roger Riggs
 * @see ObjectStreamField
 * @see <a href="../../../platform/serialization/spec/class.html">Object Serialization Specification, Section 4, Class Descriptors</a>
 * @since   JDK1.1
 */
public class ObjectStreamClass implements Serializable {

    /** serialPersistentFields value indicating no serializable fields */
    public static final ObjectStreamField[] NO_FIELDS =
        new ObjectStreamField[0];

    private static final long serialVersionUID = -6120832682080437368L;
    private static final ObjectStreamField[] serialPersistentFields =
        NO_FIELDS;


    /** class associated with this descriptor (if any) */
    private Class<?> cl;
    /** name of class represented by this descriptor */
    private String name;
    /** serialVersionUID of represented class (null if not computed yet) */
    private volatile Long suid;

    /** true if represents dynamic proxy class */
    private boolean isProxy;
    /** true if represents enum type */
    private boolean isEnum;
    /** true if represented class implements Serializable */
    private boolean serializable;
    /** true if represented class implements Externalizable */
    private boolean externalizable;
    /** true if desc has data written by class-defined writeObject method */
    private boolean hasWriteObjectData;
    /**
     * true if desc has externalizable data written in block data format; this
     * must be true by default to accommodate ObjectInputStream subclasses which
     * override readClassDescriptor() to return class descriptors obtained from
     * ObjectStreamClass.lookup() (see 4461737)
     */
    private boolean hasBlockExternalData = true;

    /** exception (if any) thrown while attempting to resolve class */
    private ClassNotFoundException resolveEx;
    /** exception (if any) to throw if non-enum deserialization attempted */
    private InvalidClassException deserializeEx;
    /** exception (if any) to throw if non-enum serialization attempted */
    private InvalidClassException serializeEx;
    /** exception (if any) to throw if default serialization attempted */
    private InvalidClassException defaultSerializeEx;

    /** serializable fields */
    private ObjectStreamField[] fields;
    /** aggregate marshalled size of primitive fields */
    private int primDataSize;
    /** number of non-primitive fields */
    private int numObjFields;
    /** reflector for setting/getting serializable field values */
//    private FieldReflector fieldRefl;
    /** data layout of serialized objects described by this class desc */
    private volatile ClassDataSlot[] dataLayout;

    /** serialization-appropriate constructor, or null if none */
    private Constructor cons;
    /** class-defined writeObject method, or null if none */
    private Method writeObjectMethod;
    /** class-defined readObject method, or null if none */
    private Method readObjectMethod;
    /** class-defined readObjectNoData method, or null if none */
    private Method readObjectNoDataMethod;
    /** class-defined writeReplace method, or null if none */
    private Method writeReplaceMethod;
    /** class-defined readResolve method, or null if none */
    private Method readResolveMethod;

    /** local class descriptor for represented class (may point to self) */
    private ObjectStreamClass localDesc;
    /** superclass descriptor appearing in stream */
    private ObjectStreamClass superDesc;

    /**
     * Initializes native code.
     */
    private static native void initNative();
    static {
        initNative();
    }

    /**
     * Find the descriptor for a class that can be serialized.  Creates an
     * ObjectStreamClass instance if one does not exist yet for class. Null is
     * returned if the specified class does not implement java.io.Serializable
     * or java.io.Externalizable.
     *
     * @param   cl class for which to get the descriptor
     * @return  the class descriptor for the specified class
     */
    public static ObjectStreamClass lookup(Class<?> cl) {
        return lookup(cl, false);
    }

    /**
     * Returns the descriptor for any class, regardless of whether it
     * implements {@link Serializable}.
     *
     * @param        cl class for which to get the descriptor
     * @return       the class descriptor for the specified class
     * @since 1.6
     */
    public static ObjectStreamClass lookupAny(Class<?> cl) {
        return lookup(cl, true);
    }

    /**
     * Returns the name of the class described by this descriptor.
     * This method returns the name of the class in the format that
     * is used by the {@link Class#getName} method.
     *
     * @return a string representing the name of the class
     */
    public String getName() {
        return name;
    }

    /**
     * Return the serialVersionUID for this class.  The serialVersionUID
     * defines a set of classes all with the same name that have evolved from a
     * common root class and agree to be serialized and deserialized using a
     * common format.  NonSerializable classes have a serialVersionUID of 0L.
     *
     * @return  the SUID of the class described by this descriptor
     */
    public long getSerialVersionUID() {
        // REMIND: synchronize instead of relying on volatile?
        if (suid == null) {
            return computeDefaultSUID(cl);
        }
        return suid.longValue();
    }

    /**
     * Return the class in the local VM that this version is mapped to.  Null
     * is returned if there is no corresponding local class.
     *
     * @return  the <code>Class</code> instance that this descriptor represents
     */
    public Class<?> forClass() {
        return cl;
    }

    /**
     * Return an array of the fields of this serializable class.
     *
     * @return  an array containing an element for each persistent field of
     *          this class. Returns an array of length zero if there are no
     *          fields.
     * @since 1.2
     */
    public ObjectStreamField[] getFields() {
        return getFields(true);
    }

    /**
     * Get the field of this class by name.
     *
     * @param   name the name of the data field to look for
     * @return  The ObjectStreamField object of the named field or null if
     *          there is no such named field.
     */
    public ObjectStreamField getField(String name) {
        return getField(name, null);
    }

    /**
     * Return a string describing this ObjectStreamClass.
     */
    public String toString() {
        return name + ": static final long serialVersionUID = " +
            getSerialVersionUID() + "L;";
    }

    /**
     * Looks up and returns class descriptor for given class, or null if class
     * is non-serializable and "all" is set to false.
     *
     * @param   cl class to look up
     * @param   all if true, return descriptors for all classes; if false, only
     *          return descriptors for serializable classes
     */
    static ObjectStreamClass lookup(Class<?> cl, boolean all) {
        if (!(all || Serializable.class.isAssignableFrom(cl))) {
            return null;
        }
        Object entry = null;
        EntryFuture future = null;
        if (entry == null) {
            EntryFuture newEntry = new EntryFuture();
            Reference<?> newRef = new SoftReference<>(newEntry);
            if (entry == null) {
                future = newEntry;
            }
        }

        if (entry instanceof ObjectStreamClass) {  // check common case first
            return (ObjectStreamClass) entry;
        }
        if (entry instanceof EntryFuture) {
            future = (EntryFuture) entry;
            if (true) {
                /*
                 * Handle nested call situation described by 4803747: waiting
                 * for future value to be set by a lookup() call further up the
                 * stack will result in deadlock, so calculate and set the
                 * future value here instead.
                 */
                entry = null;
            } else {
                entry = future.get();
            }
        }
        if (entry == null) {
            try {
                entry = new ObjectStreamClass(cl);
            } catch (Throwable th) {
                entry = th;
            }
            // nested lookup call already set future
            entry = future.get();
        }

        if (entry instanceof ObjectStreamClass) {
            return (ObjectStreamClass) entry;
        } else if (entry instanceof RuntimeException) {
            throw (RuntimeException) entry;
        } else if (entry instanceof Error) {
            throw (Error) entry;
        } else {
            throw new InternalError("unexpected entry: " + entry);
        }
    }

    /**
     * Placeholder used in class descriptor and field reflector lookup tables
     * for an entry in the process of being initialized.  (Internal) callers
     * which receive an EntryFuture belonging to another thread as the result
     * of a lookup should call the get() method of the EntryFuture; this will
     * return the actual entry once it is ready for use and has been set().  To
     * conserve objects, EntryFutures synchronize on themselves.
     */
    private static class EntryFuture {

        private static final Object unset = new Object();
        private Object entry = unset;

        /**
         * Attempts to set the value contained by this EntryFuture.  If the
         * EntryFuture's value has not been set already, then the value is
         * saved, any callers blocked in the get() method are notified, and
         * true is returned.  If the value has already been set, then no saving
         * or notification occurs, and false is returned.
         */
        synchronized boolean set(Object entry) {
            if (this.entry != unset) {
                return false;
            }
            this.entry = entry;
            notifyAll();
            return true;
        }

        /**
         * Returns the value contained by this EntryFuture, blocking if
         * necessary until a value is set.
         */
        synchronized Object get() {
            boolean interrupted = false;
            while (entry == unset) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
            return entry;
        }
    }

    /**
     * Creates local class descriptor representing given class.
     */
    private ObjectStreamClass(final Class<?> cl) {
        this.cl = cl;
        name = cl.getName();
        isProxy = Proxy.isProxyClass(cl);
        isEnum = Enum.class.isAssignableFrom(cl);
        serializable = Serializable.class.isAssignableFrom(cl);
        externalizable = Externalizable.class.isAssignableFrom(cl);

        Class<?> superCl = cl.getSuperclass();
        superDesc = (superCl != null) ? lookup(superCl, false) : null;
        localDesc = this;

        suid = Long.valueOf(0);
        fields = NO_FIELDS;


        if (deserializeEx == null) {
            if (isEnum) {
                deserializeEx = new InvalidClassException(name, "enum type");
            } else if (cons == null) {
                deserializeEx = new InvalidClassException(
                    name, "no valid constructor");
            }
        }
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getField() == null) {
                defaultSerializeEx = new InvalidClassException(
                    name, "unmatched serializable field(s) declared");
            }
        }
    }

    /**
     * Creates blank class descriptor which should be initialized via a
     * subsequent call to initProxy(), initNonProxy() or readNonProxy().
     */
    ObjectStreamClass() {
    }

    /**
     * Initializes class descriptor representing a proxy class.
     */
    void initProxy(Class<?> cl,
                   ClassNotFoundException resolveEx,
                   ObjectStreamClass superDesc)
        throws InvalidClassException
    {
        this.cl = cl;
        this.resolveEx = resolveEx;
        this.superDesc = superDesc;
        isProxy = true;
        serializable = true;
        suid = Long.valueOf(0);
        fields = NO_FIELDS;

        if (cl != null) {
            localDesc = lookup(cl, true);
            if (!localDesc.isProxy) {
                throw new InvalidClassException(
                    "cannot bind proxy descriptor to a non-proxy class");
            }
            name = localDesc.name;
            externalizable = localDesc.externalizable;
            cons = localDesc.cons;
            writeReplaceMethod = localDesc.writeReplaceMethod;
            readResolveMethod = localDesc.readResolveMethod;
            deserializeEx = localDesc.deserializeEx;
        }
    }

    /**
     * Initializes class descriptor representing a non-proxy class.
     */
    void initNonProxy(ObjectStreamClass model,
                      Class<?> cl,
                      ClassNotFoundException resolveEx,
                      ObjectStreamClass superDesc)
        throws InvalidClassException
    {
        this.cl = cl;
        this.resolveEx = resolveEx;
        this.superDesc = superDesc;
        name = model.name;
        suid = Long.valueOf(model.getSerialVersionUID());
        isProxy = false;
        isEnum = model.isEnum;
        serializable = model.serializable;
        externalizable = model.externalizable;
        hasBlockExternalData = model.hasBlockExternalData;
        hasWriteObjectData = model.hasWriteObjectData;
        fields = model.fields;
        primDataSize = model.primDataSize;
        numObjFields = model.numObjFields;

        if (cl != null) {
            localDesc = lookup(cl, true);
            if (localDesc.isProxy) {
                throw new InvalidClassException(
                    "cannot bind non-proxy descriptor to a proxy class");
            }
            if (isEnum != localDesc.isEnum) {
                throw new InvalidClassException(isEnum ?
                    "cannot bind enum descriptor to a non-enum class" :
                    "cannot bind non-enum descriptor to an enum class");
            }

            if (serializable == localDesc.serializable &&
                !cl.isArray() &&
                suid.longValue() != localDesc.getSerialVersionUID())
            {
                throw new InvalidClassException(localDesc.name,
                    "local class incompatible: " +
                    "stream classdesc serialVersionUID = " + suid +
                    ", local class serialVersionUID = " +
                    localDesc.getSerialVersionUID());
            }

            if (!classNamesEqual(name, localDesc.name)) {
                throw new InvalidClassException(localDesc.name,
                    "local class name incompatible with stream class " +
                    "name \"" + name + "\"");
            }

            if (!isEnum) {
                if ((serializable == localDesc.serializable) &&
                    (externalizable != localDesc.externalizable))
                {
                    throw new InvalidClassException(localDesc.name,
                        "Serializable incompatible with Externalizable");
                }

                if ((serializable != localDesc.serializable) ||
                    (externalizable != localDesc.externalizable) ||
                    !(serializable || externalizable))
                {
                    deserializeEx = new InvalidClassException(localDesc.name,
                        "class invalid for deserialization");
                }
            }

            cons = localDesc.cons;
            writeObjectMethod = localDesc.writeObjectMethod;
            readObjectMethod = localDesc.readObjectMethod;
            readObjectNoDataMethod = localDesc.readObjectNoDataMethod;
            writeReplaceMethod = localDesc.writeReplaceMethod;
            readResolveMethod = localDesc.readResolveMethod;
            if (deserializeEx == null) {
                deserializeEx = localDesc.deserializeEx;
            }
        }
        // reassign to matched fields so as to reflect local unshared settings
        fields = null;
    }

    /**
     * Reads non-proxy class descriptor information from given input stream.
     * The resulting class descriptor is not fully functional; it can only be
     * used as input to the ObjectInputStream.resolveClass() and
     * ObjectStreamClass.initNonProxy() methods.
     */
    void readNonProxy(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        name = in.readUTF();
        suid = Long.valueOf(in.readLong());
        isProxy = false;

        byte flags = in.readByte();
        hasWriteObjectData =
            ((flags & ObjectStreamConstants.SC_WRITE_METHOD) != 0);
        hasBlockExternalData =
            ((flags & ObjectStreamConstants.SC_BLOCK_DATA) != 0);
        externalizable =
            ((flags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0);
        boolean sflag =
            ((flags & ObjectStreamConstants.SC_SERIALIZABLE) != 0);
        if (externalizable && sflag) {
            throw new InvalidClassException(
                name, "serializable and externalizable flags conflict");
        }
        serializable = externalizable || sflag;
        isEnum = ((flags & ObjectStreamConstants.SC_ENUM) != 0);
        if (isEnum && suid.longValue() != 0L) {
            throw new InvalidClassException(name,
                "enum descriptor has non-zero serialVersionUID: " + suid);
        }

        int numFields = in.readShort();
        if (isEnum && numFields != 0) {
            throw new InvalidClassException(name,
                "enum descriptor has non-zero field count: " + numFields);
        }
        fields = (numFields > 0) ?
            new ObjectStreamField[numFields] : NO_FIELDS;
        for (int i = 0; i < numFields; i++) {
            char tcode = (char) in.readByte();
            String fname = in.readUTF();
            String signature = ((tcode == 'L') || (tcode == '[')) ?
                in.readTypeString() : new String(new char[] { tcode });
            try {
                fields[i] = new ObjectStreamField(fname, signature, false);
            } catch (RuntimeException e) {
                throw (IOException) new InvalidClassException(name,
                    "invalid descriptor for field " + fname).initCause(e);
            }
        }
        computeFieldOffsets();
    }

    /**
     * Writes non-proxy class descriptor information to given output stream.
     */
    void writeNonProxy(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
        out.writeLong(getSerialVersionUID());

        byte flags = 0;
        if (externalizable) {
            flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
            int protocol = out.getProtocolVersion();
            if (protocol != ObjectStreamConstants.PROTOCOL_VERSION_1) {
                flags |= ObjectStreamConstants.SC_BLOCK_DATA;
            }
        } else if (serializable) {
            flags |= ObjectStreamConstants.SC_SERIALIZABLE;
        }
        if (hasWriteObjectData) {
            flags |= ObjectStreamConstants.SC_WRITE_METHOD;
        }
        if (isEnum) {
            flags |= ObjectStreamConstants.SC_ENUM;
        }
        out.writeByte(flags);

        out.writeShort(fields.length);
        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i];
            out.writeByte(f.getTypeCode());
            out.writeUTF(f.getName());
            if (!f.isPrimitive()) {
                out.writeTypeString(f.getTypeString());
            }
        }
    }

    /**
     * Returns ClassNotFoundException (if any) thrown while attempting to
     * resolve local class corresponding to this class descriptor.
     */
    ClassNotFoundException getResolveException() {
        return resolveEx;
    }

    /**
     * Throws an InvalidClassException if object instances referencing this
     * class descriptor should not be allowed to deserialize.  This method does
     * not apply to deserialization of enum constants.
     */
    void checkDeserialize() throws InvalidClassException {
        if (deserializeEx != null) {
            InvalidClassException ice =
                new InvalidClassException(deserializeEx.classname,
                                          deserializeEx.getMessage());
            ice.initCause(deserializeEx);
            throw ice;
        }
    }

    /**
     * Throws an InvalidClassException if objects whose class is represented by
     * this descriptor should not be allowed to serialize.  This method does
     * not apply to serialization of enum constants.
     */
    void checkSerialize() throws InvalidClassException {
        if (serializeEx != null) {
            InvalidClassException ice =
                new InvalidClassException(serializeEx.classname,
                                          serializeEx.getMessage());
            ice.initCause(serializeEx);
            throw ice;
        }
    }

    /**
     * Throws an InvalidClassException if objects whose class is represented by
     * this descriptor should not be permitted to use default serialization
     * (e.g., if the class declares serializable fields that do not correspond
     * to actual fields, and hence must use the GetField API).  This method
     * does not apply to deserialization of enum constants.
     */
    void checkDefaultSerialize() throws InvalidClassException {
        if (defaultSerializeEx != null) {
            InvalidClassException ice =
                new InvalidClassException(defaultSerializeEx.classname,
                                          defaultSerializeEx.getMessage());
            ice.initCause(defaultSerializeEx);
            throw ice;
        }
    }

    /**
     * Returns superclass descriptor.  Note that on the receiving side, the
     * superclass descriptor may be bound to a class that is not a superclass
     * of the subclass descriptor's bound class.
     */
    ObjectStreamClass getSuperDesc() {
        return superDesc;
    }

    /**
     * Returns the "local" class descriptor for the class associated with this
     * class descriptor (i.e., the result of
     * ObjectStreamClass.lookup(this.forClass())) or null if there is no class
     * associated with this descriptor.
     */
    ObjectStreamClass getLocalDesc() {
        return localDesc;
    }

    /**
     * Returns arrays of ObjectStreamFields representing the serializable
     * fields of the represented class.  If copy is true, a clone of this class
     * descriptor's field array is returned, otherwise the array itself is
     * returned.
     */
    ObjectStreamField[] getFields(boolean copy) {
        return copy ? fields.clone() : fields;
    }

    /**
     * Looks up a serializable field of the represented class by name and type.
     * A specified type of null matches all types, Object.class matches all
     * non-primitive types, and any other non-null type matches assignable
     * types only.  Returns matching field, or null if no match found.
     */
    ObjectStreamField getField(String name, Class<?> type) {
        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i];
            if (f.getName().equals(name)) {
                if (type == null ||
                    (type == Object.class && !f.isPrimitive()))
                {
                    return f;
                }
                Class<?> ftype = f.getType();
                if (ftype != null && type.isAssignableFrom(ftype)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if class descriptor represents a dynamic proxy class, false
     * otherwise.
     */
    boolean isProxy() {
        return isProxy;
    }

    /**
     * Returns true if class descriptor represents an enum type, false
     * otherwise.
     */
    boolean isEnum() {
        return isEnum;
    }

    /**
     * Returns true if represented class implements Externalizable, false
     * otherwise.
     */
    boolean isExternalizable() {
        return externalizable;
    }

    /**
     * Returns true if represented class implements Serializable, false
     * otherwise.
     */
    boolean isSerializable() {
        return serializable;
    }

    /**
     * Returns true if class descriptor represents externalizable class that
     * has written its data in 1.2 (block data) format, false otherwise.
     */
    boolean hasBlockExternalData() {
        return hasBlockExternalData;
    }

    /**
     * Returns true if class descriptor represents serializable (but not
     * externalizable) class which has written its data via a custom
     * writeObject() method, false otherwise.
     */
    boolean hasWriteObjectData() {
        return hasWriteObjectData;
    }

    /**
     * Returns true if represented class is serializable/externalizable and can
     * be instantiated by the serialization runtime--i.e., if it is
     * externalizable and defines a public no-arg constructor, or if it is
     * non-externalizable and its first non-serializable superclass defines an
     * accessible no-arg constructor.  Otherwise, returns false.
     */
    boolean isInstantiable() {
        return (cons != null);
    }

    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant writeObject method.  Otherwise,
     * returns false.
     */
    boolean hasWriteObjectMethod() {
        return (writeObjectMethod != null);
    }

    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant readObject method.  Otherwise,
     * returns false.
     */
    boolean hasReadObjectMethod() {
        return (readObjectMethod != null);
    }

    /**
     * Returns true if represented class is serializable (but not
     * externalizable) and defines a conformant readObjectNoData method.
     * Otherwise, returns false.
     */
    boolean hasReadObjectNoDataMethod() {
        return (readObjectNoDataMethod != null);
    }

    /**
     * Returns true if represented class is serializable or externalizable and
     * defines a conformant writeReplace method.  Otherwise, returns false.
     */
    boolean hasWriteReplaceMethod() {
        return (writeReplaceMethod != null);
    }

    /**
     * Returns true if represented class is serializable or externalizable and
     * defines a conformant readResolve method.  Otherwise, returns false.
     */
    boolean hasReadResolveMethod() {
        return (readResolveMethod != null);
    }

    /**
     * Creates a new instance of the represented class.  If the class is
     * externalizable, invokes its public no-arg constructor; otherwise, if the
     * class is serializable, invokes the no-arg constructor of the first
     * non-serializable superclass.  Throws UnsupportedOperationException if
     * this class descriptor is not associated with a class, if the associated
     * class is non-serializable or if the appropriate no-arg constructor is
     * inaccessible/unavailable.
     */
    Object newInstance()
        throws InstantiationException, InvocationTargetException,
               UnsupportedOperationException
    {
        if (cons != null) {
            try {
                return cons.newInstance();
            } catch (IllegalAccessException ex) {
                // should not occur, as access checks have been suppressed
                throw new InternalError();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Invokes the writeObject method of the represented serializable class.
     * Throws UnsupportedOperationException if this class descriptor is not
     * associated with a class, or if the class is externalizable,
     * non-serializable or does not define writeObject.
     */
    void invokeWriteObject(Object obj, ObjectOutputStream out)
        throws IOException, UnsupportedOperationException
    {
        if (writeObjectMethod != null) {
            try {
                writeObjectMethod.invoke(obj, new Object[]{ out });
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof IOException) {
                    throw (IOException) th;
                } else {
                    throwMiscException(th);
                }
            } catch (IllegalAccessException ex) {
                // should not occur, as access checks have been suppressed
                throw new InternalError();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Invokes the readObject method of the represented serializable class.
     * Throws UnsupportedOperationException if this class descriptor is not
     * associated with a class, or if the class is externalizable,
     * non-serializable or does not define readObject.
     */
    void invokeReadObject(Object obj, ObjectInputStream in)
        throws ClassNotFoundException, IOException,
               UnsupportedOperationException
    {
        if (readObjectMethod != null) {
            try {
                readObjectMethod.invoke(obj, new Object[]{ in });
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) th;
                } else if (th instanceof IOException) {
                    throw (IOException) th;
                } else {
                    throwMiscException(th);
                }
            } catch (IllegalAccessException ex) {
                // should not occur, as access checks have been suppressed
                throw new InternalError();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Invokes the readObjectNoData method of the represented serializable
     * class.  Throws UnsupportedOperationException if this class descriptor is
     * not associated with a class, or if the class is externalizable,
     * non-serializable or does not define readObjectNoData.
     */
    void invokeReadObjectNoData(Object obj)
        throws IOException, UnsupportedOperationException
    {
        if (readObjectNoDataMethod != null) {
            try {
                readObjectNoDataMethod.invoke(obj, (Object[]) null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw (ObjectStreamException) th;
                } else {
                    throwMiscException(th);
                }
            } catch (IllegalAccessException ex) {
                // should not occur, as access checks have been suppressed
                throw new InternalError();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Invokes the writeReplace method of the represented serializable class and
     * returns the result.  Throws UnsupportedOperationException if this class
     * descriptor is not associated with a class, or if the class is
     * non-serializable or does not define writeReplace.
     */
    Object invokeWriteReplace(Object obj)
        throws IOException, UnsupportedOperationException
    {
        if (writeReplaceMethod != null) {
            try {
                return writeReplaceMethod.invoke(obj, (Object[]) null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw (ObjectStreamException) th;
                } else {
                    throwMiscException(th);
                    throw new InternalError();  // never reached
                }
            } catch (IllegalAccessException ex) {
                // should not occur, as access checks have been suppressed
                throw new InternalError();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Invokes the readResolve method of the represented serializable class and
     * returns the result.  Throws UnsupportedOperationException if this class
     * descriptor is not associated with a class, or if the class is
     * non-serializable or does not define readResolve.
     */
    Object invokeReadResolve(Object obj)
        throws IOException, UnsupportedOperationException
    {
        if (readResolveMethod != null) {
            try {
                return readResolveMethod.invoke(obj, (Object[]) null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw (ObjectStreamException) th;
                } else {
                    throwMiscException(th);
                    throw new InternalError();  // never reached
                }
            } catch (IllegalAccessException ex) {
                // should not occur, as access checks have been suppressed
                throw new InternalError();
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Class representing the portion of an object's serialized form allotted
     * to data described by a given class descriptor.  If "hasData" is false,
     * the object's serialized form does not contain data associated with the
     * class descriptor.
     */
    static class ClassDataSlot {

        /** class descriptor "occupying" this slot */
        final ObjectStreamClass desc;
        /** true if serialized form includes data for this slot's descriptor */
        final boolean hasData;

        ClassDataSlot(ObjectStreamClass desc, boolean hasData) {
            this.desc = desc;
            this.hasData = hasData;
        }
    }

    /**
     * Returns array of ClassDataSlot instances representing the data layout
     * (including superclass data) for serialized objects described by this
     * class descriptor.  ClassDataSlots are ordered by inheritance with those
     * containing "higher" superclasses appearing first.  The final
     * ClassDataSlot contains a reference to this descriptor.
     */
    ClassDataSlot[] getClassDataLayout() throws InvalidClassException {
        // REMIND: synchronize instead of relying on volatile?
        if (dataLayout == null) {
            dataLayout = getClassDataLayout0();
        }
        return dataLayout;
    }

    private ClassDataSlot[] getClassDataLayout0()
        throws InvalidClassException
    {
        ArrayList<ClassDataSlot> slots = new ArrayList<>();
        Class<?> start = cl, end = cl;

        // locate closest non-serializable superclass
        while (end != null && Serializable.class.isAssignableFrom(end)) {
            end = end.getSuperclass();
        }

        for (ObjectStreamClass d = this; d != null; d = d.superDesc) {

            // search up inheritance hierarchy for class with matching name
            String searchName = (d.cl != null) ? d.cl.getName() : d.name;
            Class<?> match = null;
            for (Class<?> c = start; c != end; c = c.getSuperclass()) {
                if (searchName.equals(c.getName())) {
                    match = c;
                    break;
                }
            }

            // add "no data" slot for each unmatched class below match
            if (match != null) {
                for (Class<?> c = start; c != match; c = c.getSuperclass()) {
                    slots.add(new ClassDataSlot(
                        ObjectStreamClass.lookup(c, true), false));
                }
                start = match.getSuperclass();
            }

            // record descriptor/class pairing
            slots.add(new ClassDataSlot(d.getVariantFor(match), true));
        }

        // add "no data" slot for any leftover unmatched classes
        for (Class<?> c = start; c != end; c = c.getSuperclass()) {
            slots.add(new ClassDataSlot(
                ObjectStreamClass.lookup(c, true), false));
        }

        // order slots from superclass -> subclass
        Collections.reverse(slots);
        return slots.toArray(new ClassDataSlot[slots.size()]);
    }

    /**
     * Returns aggregate size (in bytes) of marshalled primitive field values
     * for represented class.
     */
    int getPrimDataSize() {
        return primDataSize;
    }

    /**
     * Returns number of non-primitive serializable fields of represented
     * class.
     */
    int getNumObjFields() {
        return numObjFields;
    }

    /**
     * Fetches the serializable primitive field values of object obj and
     * marshals them into byte array buf starting at offset 0.  It is the
     * responsibility of the caller to ensure that obj is of the proper type if
     * non-null.
     */
    void getPrimFieldValues(Object obj, byte[] buf) {
    }

    /**
     * Sets the serializable primitive fields of object obj using values
     * unmarshalled from byte array buf starting at offset 0.  It is the
     * responsibility of the caller to ensure that obj is of the proper type if
     * non-null.
     */
    void setPrimFieldValues(Object obj, byte[] buf) {
    }

    /**
     * Fetches the serializable object field values of object obj and stores
     * them in array vals starting at offset 0.  It is the responsibility of
     * the caller to ensure that obj is of the proper type if non-null.
     */
    void getObjFieldValues(Object obj, Object[] vals) {
    }

    /**
     * Sets the serializable object fields of object obj using values from
     * array vals starting at offset 0.  It is the responsibility of the caller
     * to ensure that obj is of the proper type if non-null.
     */
    void setObjFieldValues(Object obj, Object[] vals) {
    }

    /**
     * Calculates and sets serializable field offsets, as well as primitive
     * data size and object field count totals.  Throws InvalidClassException
     * if fields are illegally ordered.
     */
    private void computeFieldOffsets() throws InvalidClassException {
        primDataSize = 0;
        numObjFields = 0;
        int firstObjIndex = -1;

        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i];
            switch (f.getTypeCode()) {
                case 'Z':
                case 'B':
                    f.setOffset(primDataSize++);
                    break;

                case 'C':
                case 'S':
                    f.setOffset(primDataSize);
                    primDataSize += 2;
                    break;

                case 'I':
                case 'F':
                    f.setOffset(primDataSize);
                    primDataSize += 4;
                    break;

                case 'J':
                case 'D':
                    f.setOffset(primDataSize);
                    primDataSize += 8;
                    break;

                case '[':
                case 'L':
                    f.setOffset(numObjFields++);
                    if (firstObjIndex == -1) {
                        firstObjIndex = i;
                    }
                    break;

                default:
                    throw new InternalError();
            }
        }
        if (firstObjIndex != -1 &&
            firstObjIndex + numObjFields != fields.length)
        {
            throw new InvalidClassException(name, "illegal field order");
        }
    }

    /**
     * If given class is the same as the class associated with this class
     * descriptor, returns reference to this class descriptor.  Otherwise,
     * returns variant of this class descriptor bound to given class.
     */
    private ObjectStreamClass getVariantFor(Class<?> cl)
        throws InvalidClassException
    {
        if (this.cl == cl) {
            return this;
        }
        ObjectStreamClass desc = new ObjectStreamClass();
        if (isProxy) {
            desc.initProxy(cl, null, superDesc);
        } else {
            desc.initNonProxy(this, cl, null, superDesc);
        }
        return desc;
    }

    /**
     * Returns public no-arg constructor of given class, or null if none found.
     * Access checks are disabled on the returned constructor (if any), since
     * the defining class may still be non-public.
     */
    private static Constructor getExternalizableConstructor(Class<?> cl) {
        throw new SecurityException();
    }

    /**
     * Returns subclass-accessible no-arg constructor of first non-serializable
     * superclass, or null if none found.  Access checks are disabled on the
     * returned constructor (if any).
     */
    private static Constructor getSerializableConstructor(Class<?> cl) {
        Class<?> initCl = cl;
        while (Serializable.class.isAssignableFrom(initCl)) {
            if ((initCl = initCl.getSuperclass()) == null) {
                return null;
            }
        }
        throw new SecurityException();
    }

    /**
     * Returns non-static, non-abstract method with given signature provided it
     * is defined by or accessible (via inheritance) by the given class, or
     * null if no match found.  Access checks are disabled on the returned
     * method (if any).
     */
    private static Method getInheritableMethod(Class<?> cl, String name,
                                               Class<?>[] argTypes,
                                               Class<?> returnType)
    {
        throw new SecurityException();
    }

    /**
     * Returns non-static private method with given signature defined by given
     * class, or null if none found.  Access checks are disabled on the
     * returned method (if any).
     */
    private static Method getPrivateMethod(Class<?> cl, String name,
                                           Class<?>[] argTypes,
                                           Class<?> returnType)
    {
        throw new SecurityException();
    }

    /**
     * Returns true if classes are defined in the same runtime package, false
     * otherwise.
     */
    private static boolean packageEquals(Class<?> cl1, Class<?> cl2) {
        return (cl1.getClassLoader() == cl2.getClassLoader() &&
                getPackageName(cl1).equals(getPackageName(cl2)));
    }

    /**
     * Returns package name of given class.
     */
    private static String getPackageName(Class<?> cl) {
        String s = cl.getName();
        int i = s.lastIndexOf('[');
        if (i >= 0) {
            s = s.substring(i + 2);
        }
        i = s.lastIndexOf('.');
        return (i >= 0) ? s.substring(0, i) : "";
    }

    /**
     * Compares class names for equality, ignoring package names.  Returns true
     * if class names equal, false otherwise.
     */
    private static boolean classNamesEqual(String name1, String name2) {
        name1 = name1.substring(name1.lastIndexOf('.') + 1);
        name2 = name2.substring(name2.lastIndexOf('.') + 1);
        return name1.equals(name2);
    }

    /**
     * Returns JVM type signature for given class.
     */
    private static String getClassSignature(Class<?> cl) {
        StringBuilder sbuf = new StringBuilder();
        while (cl.isArray()) {
            sbuf.append('[');
            cl = cl.getComponentType();
        }
        if (cl.isPrimitive()) {
            if (cl == Integer.TYPE) {
                sbuf.append('I');
            } else if (cl == Byte.TYPE) {
                sbuf.append('B');
            } else if (cl == Long.TYPE) {
                sbuf.append('J');
            } else if (cl == Float.TYPE) {
                sbuf.append('F');
            } else if (cl == Double.TYPE) {
                sbuf.append('D');
            } else if (cl == Short.TYPE) {
                sbuf.append('S');
            } else if (cl == Character.TYPE) {
                sbuf.append('C');
            } else if (cl == Boolean.TYPE) {
                sbuf.append('Z');
            } else if (cl == Void.TYPE) {
                sbuf.append('V');
            } else {
                throw new InternalError();
            }
        } else {
            sbuf.append('L' + cl.getName().replace('.', '/') + ';');
        }
        return sbuf.toString();
    }

    /**
     * Returns JVM type signature for given list of parameters and return type.
     */
    private static String getMethodSignature(Class<?>[] paramTypes,
                                             Class<?> retType)
    {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append('(');
        for (int i = 0; i < paramTypes.length; i++) {
            sbuf.append(getClassSignature(paramTypes[i]));
        }
        sbuf.append(')');
        sbuf.append(getClassSignature(retType));
        return sbuf.toString();
    }

    /**
     * Convenience method for throwing an exception that is either a
     * RuntimeException, Error, or of some unexpected type (in which case it is
     * wrapped inside an IOException).
     */
    private static void throwMiscException(Throwable th) throws IOException {
        if (th instanceof RuntimeException) {
            throw (RuntimeException) th;
        } else if (th instanceof Error) {
            throw (Error) th;
        } else {
            IOException ex = new IOException("unexpected exception type");
            ex.initCause(th);
            throw ex;
        }
    }

    /**
     * Returns ObjectStreamField array describing the serializable fields of
     * the given class.  Serializable fields backed by an actual field of the
     * class are represented by ObjectStreamFields with corresponding non-null
     * Field objects.  Throws InvalidClassException if the (explicitly
     * declared) serializable fields are invalid.
     */
    private static ObjectStreamField[] getSerialFields(Class<?> cl)
        throws InvalidClassException
    {
        ObjectStreamField[] fields;
        if (Serializable.class.isAssignableFrom(cl) &&
            !Externalizable.class.isAssignableFrom(cl) &&
            !Proxy.isProxyClass(cl) &&
            !cl.isInterface())
        {
            if ((fields = getDeclaredSerialFields(cl)) == null) {
                fields = getDefaultSerialFields(cl);
            }
            Arrays.sort(fields);
        } else {
            fields = NO_FIELDS;
        }
        return fields;
    }

    /**
     * Returns serializable fields of given class as defined explicitly by a
     * "serialPersistentFields" field, or null if no appropriate
     * "serialPersistentFields" field is defined.  Serializable fields backed
     * by an actual field of the class are represented by ObjectStreamFields
     * with corresponding non-null Field objects.  For compatibility with past
     * releases, a "serialPersistentFields" field with a null value is
     * considered equivalent to not declaring "serialPersistentFields".  Throws
     * InvalidClassException if the declared serializable fields are
     * invalid--e.g., if multiple fields share the same name.
     */
    private static ObjectStreamField[] getDeclaredSerialFields(Class<?> cl)
        throws InvalidClassException
    {
        throw new SecurityException();
    }

    /**
     * Returns array of ObjectStreamFields corresponding to all non-static
     * non-transient fields declared by given class.  Each ObjectStreamField
     * contains a Field object for the field it represents.  If no default
     * serializable fields exist, NO_FIELDS is returned.
     */
    private static ObjectStreamField[] getDefaultSerialFields(Class<?> cl) {
        throw new SecurityException();
    }

    /**
     * Returns explicit serial version UID value declared by given class, or
     * null if none.
     */
    private static Long getDeclaredSUID(Class<?> cl) {
        return null;
    }

    /**
     * Computes the default serial version UID value for the given class.
     */
    private static long computeDefaultSUID(Class<?> cl) {
        throw new SecurityException();
    }

}
