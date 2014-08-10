/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.lang.invoke;

import static java.lang.invoke.LambdaForm.basicTypes;
import static java.lang.invoke.MethodHandleStatics.*;

import java.lang.invoke.LambdaForm.Name;
import java.lang.invoke.LambdaForm.NamedFunction;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Arrays;
import java.util.HashMap;

import sun.invoke.util.ValueConversions;

/**
 * The flavor of method handle which emulates an invoke instruction
 * on a predetermined argument.  The JVM dispatches to the correct method
 * when the handle is created, not when it is invoked.
 *
 * All bound arguments are encapsulated in dedicated species.
 */
/* non-public */ abstract class BoundMethodHandle extends MethodHandle {

    /* non-public */ BoundMethodHandle(MethodType type, LambdaForm form) {
        super(type, form);
    }

    //
    // BMH API and internals
    //

    static MethodHandle bindSingle(MethodType type, LambdaForm form, char xtype, Object x) {
        // for some type signatures, there exist pre-defined concrete BMH classes
        try {
            switch (xtype) {
            case 'L':
                if (true)  return bindSingle(type, form, x);  // Use known fast path.
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWithType('L').constructor[0].invokeBasic(type, form, x);
            case 'I':
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWithType('I').constructor[0].invokeBasic(type, form, ValueConversions.widenSubword(x));
            case 'J':
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWithType('J').constructor[0].invokeBasic(type, form, (long) x);
            case 'F':
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWithType('F').constructor[0].invokeBasic(type, form, (float) x);
            case 'D':
                return (BoundMethodHandle) SpeciesData.EMPTY.extendWithType('D').constructor[0].invokeBasic(type, form, (double) x);
            default : throw new InternalError("unexpected xtype: " + xtype);
            }
        } catch (Throwable t) {
            throw newInternalError(t);
        }
    }

    static MethodHandle bindSingle(MethodType type, LambdaForm form, Object x) {
            return new Species_L(type, form, x);
    }

    MethodHandle cloneExtend(MethodType type, LambdaForm form, char xtype, Object x) {
        try {
            switch (xtype) {
            case 'L': return cloneExtendL(type, form, x);
            case 'I': return cloneExtendI(type, form, ValueConversions.widenSubword(x));
            case 'J': return cloneExtendJ(type, form, (long) x);
            case 'F': return cloneExtendF(type, form, (float) x);
            case 'D': return cloneExtendD(type, form, (double) x);
            }
        } catch (Throwable t) {
            throw newInternalError(t);
        }
        throw new InternalError("unexpected type: " + xtype);
    }

    @Override
    MethodHandle bindArgument(int pos, char basicType, Object value) {
        MethodType type = type().dropParameterTypes(pos, pos+1);
        LambdaForm form = internalForm().bind(1+pos, speciesData());
        return cloneExtend(type, form, basicType, value);
    }

    @Override
    MethodHandle dropArguments(MethodType srcType, int pos, int drops) {
        LambdaForm form = internalForm().addArguments(pos, srcType.parameterList().subList(pos, pos+drops));
        try {
             return clone(srcType, form);
         } catch (Throwable t) {
             throw newInternalError(t);
         }
    }

    @Override
    MethodHandle permuteArguments(MethodType newType, int[] reorder) {
        try {
             return clone(newType, form.permuteArguments(1, reorder, basicTypes(newType.parameterList())));
         } catch (Throwable t) {
             throw newInternalError(t);
         }
    }

    static final String EXTENSION_TYPES = "LIJFD";
    static final byte INDEX_L = 0, INDEX_I = 1, INDEX_J = 2, INDEX_F = 3, INDEX_D = 4;
    static byte extensionIndex(char type) {
        int i = EXTENSION_TYPES.indexOf(type);
        if (i < 0)  throw new InternalError();
        return (byte) i;
    }

    /**
     * Return the {@link SpeciesData} instance representing this BMH species. All subclasses must provide a
     * static field containing this value, and they must accordingly implement this method.
     */
    protected abstract SpeciesData speciesData();

    @Override
    final Object internalProperties() {
        return "/BMH="+internalValues();
    }

    @Override
    final Object internalValues() {
        Object[] boundValues = new Object[speciesData().fieldCount()];
        for (int i = 0; i < boundValues.length; ++i) {
            boundValues[i] = arg(i);
        }
        return Arrays.asList(boundValues);
    }

    public final Object arg(int i) {
        try {
            switch (speciesData().fieldType(i)) {
            case 'L': return argL(i);
            case 'I': return argI(i);
            case 'F': return argF(i);
            case 'D': return argD(i);
            case 'J': return argJ(i);
            }
        } catch (Throwable ex) {
            throw newInternalError(ex);
        }
        throw new InternalError("unexpected type: " + speciesData().types+"."+i);
    }
    public final Object argL(int i) throws Throwable { return          speciesData().getters[i].invokeBasic(this); }
    public final int    argI(int i) throws Throwable { return (int)    speciesData().getters[i].invokeBasic(this); }
    public final float  argF(int i) throws Throwable { return (float)  speciesData().getters[i].invokeBasic(this); }
    public final double argD(int i) throws Throwable { return (double) speciesData().getters[i].invokeBasic(this); }
    public final long   argJ(int i) throws Throwable { return (long)   speciesData().getters[i].invokeBasic(this); }

    //
    // cloning API
    //

    public abstract BoundMethodHandle clone(MethodType mt, LambdaForm lf) throws Throwable;
    public abstract BoundMethodHandle cloneExtendL(MethodType mt, LambdaForm lf, Object narg) throws Throwable;
    public abstract BoundMethodHandle cloneExtendI(MethodType mt, LambdaForm lf, int    narg) throws Throwable;
    public abstract BoundMethodHandle cloneExtendJ(MethodType mt, LambdaForm lf, long   narg) throws Throwable;
    public abstract BoundMethodHandle cloneExtendF(MethodType mt, LambdaForm lf, float  narg) throws Throwable;
    public abstract BoundMethodHandle cloneExtendD(MethodType mt, LambdaForm lf, double narg) throws Throwable;

    // The following is a grossly irregular hack:
    @Override MethodHandle reinvokerTarget() {
        try {
            return (MethodHandle) argL(0);
        } catch (Throwable ex) {
            throw newInternalError(ex);
        }
    }

    //
    // concrete BMH classes required to close bootstrap loops
    //

    private  // make it private to force users to access the enclosing class first
    static final class Species_L extends BoundMethodHandle {
        final Object argL0;
        public Species_L(MethodType mt, LambdaForm lf, Object argL0) {
            super(mt, lf);
            this.argL0 = argL0;
        }
        // The following is a grossly irregular hack:
        @Override MethodHandle reinvokerTarget() { return (MethodHandle) argL0; }
        @Override
        public SpeciesData speciesData() {
            return SPECIES_DATA;
        }
        public static final SpeciesData SPECIES_DATA = SpeciesData.getForClass("L", Species_L.class);
        @Override
        public final BoundMethodHandle clone(MethodType mt, LambdaForm lf) throws Throwable {
            return new Species_L(mt, lf, argL0);
        }
        @Override
        public final BoundMethodHandle cloneExtendL(MethodType mt, LambdaForm lf, Object narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_L).constructor[0].invokeBasic(mt, lf, argL0, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendI(MethodType mt, LambdaForm lf, int narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_I).constructor[0].invokeBasic(mt, lf, argL0, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendJ(MethodType mt, LambdaForm lf, long narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_J).constructor[0].invokeBasic(mt, lf, argL0, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendF(MethodType mt, LambdaForm lf, float narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_F).constructor[0].invokeBasic(mt, lf, argL0, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendD(MethodType mt, LambdaForm lf, double narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_D).constructor[0].invokeBasic(mt, lf, argL0, narg);
        }
    }

/*
    static final class Species_LL extends BoundMethodHandle {
        final Object argL0;
        final Object argL1;
        public Species_LL(MethodType mt, LambdaForm lf, Object argL0, Object argL1) {
            super(mt, lf);
            this.argL0 = argL0;
            this.argL1 = argL1;
        }
        @Override
        public SpeciesData speciesData() {
            return SPECIES_DATA;
        }
        public static final SpeciesData SPECIES_DATA = SpeciesData.getForClass("LL", Species_LL.class);
        @Override
        public final BoundMethodHandle clone(MethodType mt, LambdaForm lf) throws Throwable {
            return new Species_LL(mt, lf, argL0, argL1);
        }
        @Override
        public final BoundMethodHandle cloneExtendL(MethodType mt, LambdaForm lf, Object narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_L).constructor[0].invokeBasic(mt, lf, argL0, argL1, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendI(MethodType mt, LambdaForm lf, int narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_I).constructor[0].invokeBasic(mt, lf, argL0, argL1, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendJ(MethodType mt, LambdaForm lf, long narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_J).constructor[0].invokeBasic(mt, lf, argL0, argL1, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendF(MethodType mt, LambdaForm lf, float narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_F).constructor[0].invokeBasic(mt, lf, argL0, argL1, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendD(MethodType mt, LambdaForm lf, double narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_D).constructor[0].invokeBasic(mt, lf, argL0, argL1, narg);
        }
    }

    static final class Species_JL extends BoundMethodHandle {
        final long argJ0;
        final Object argL1;
        public Species_JL(MethodType mt, LambdaForm lf, long argJ0, Object argL1) {
            super(mt, lf);
            this.argJ0 = argJ0;
            this.argL1 = argL1;
        }
        @Override
        public SpeciesData speciesData() {
            return SPECIES_DATA;
        }
        public static final SpeciesData SPECIES_DATA = SpeciesData.getForClass("JL", Species_JL.class);
        @Override public final long   argJ0() { return argJ0; }
        @Override public final Object argL1() { return argL1; }
        @Override
        public final BoundMethodHandle clone(MethodType mt, LambdaForm lf) throws Throwable {
            return new Species_JL(mt, lf, argJ0, argL1);
        }
        @Override
        public final BoundMethodHandle cloneExtendL(MethodType mt, LambdaForm lf, Object narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_L).constructor[0].invokeBasic(mt, lf, argJ0, argL1, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendI(MethodType mt, LambdaForm lf, int narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_I).constructor[0].invokeBasic(mt, lf, argJ0, argL1, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendJ(MethodType mt, LambdaForm lf, long narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_J).constructor[0].invokeBasic(mt, lf, argJ0, argL1, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendF(MethodType mt, LambdaForm lf, float narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_F).constructor[0].invokeBasic(mt, lf, argJ0, argL1, narg);
        }
        @Override
        public final BoundMethodHandle cloneExtendD(MethodType mt, LambdaForm lf, double narg) throws Throwable {
            return (BoundMethodHandle) SPECIES_DATA.extendWithIndex(INDEX_D).constructor[0].invokeBasic(mt, lf, argJ0, argL1, narg);
        }
    }
*/

    //
    // BMH species meta-data
    //

    /**
     * Meta-data wrapper for concrete BMH classes.
     */
    static class SpeciesData {
        final String                             types;
        final Class<? extends BoundMethodHandle> clazz;
        // Bootstrapping requires circular relations MH -> BMH -> SpeciesData -> MH
        // Therefore, we need a non-final link in the chain.  Use array elements.
        final MethodHandle[]                     constructor;
        final MethodHandle[]                     getters;
        final SpeciesData[]                      extensions;

        public int fieldCount() {
            return types.length();
        }
        public char fieldType(int i) {
            return types.charAt(i);
        }

        public String toString() {
            return "SpeciesData["+(isPlaceholder() ? "<placeholder>" : clazz.getSimpleName())+":"+types+"]";
        }

        /**
         * Return a {@link LambdaForm.Name} containing a {@link LambdaForm.NamedFunction} that
         * represents a MH bound to a generic invoker, which in turn forwards to the corresponding
         * getter.
         */
        Name getterName(Name mhName, int i) {
            MethodHandle mh = getters[i];
            assert(mh != null) : this+"."+i;
            return new Name(mh, mhName);
        }

        NamedFunction getterFunction(int i) {
            return new NamedFunction(getters[i]);
        }

        static final SpeciesData EMPTY = new SpeciesData("", BoundMethodHandle.class);

        private SpeciesData(String types, Class<? extends BoundMethodHandle> clazz) {
            this.types = types;
            this.clazz = clazz;
            if (!INIT_DONE) {
                this.constructor = new MethodHandle[1];
                this.getters = new MethodHandle[types.length()];
            } else {
                throw new IllegalStateException("bound method handle");
//                this.constructor = Factory.makeCtors(clazz, types, null);
//                this.getters = Factory.makeGetters(clazz, types, null);
            }
            this.extensions = new SpeciesData[EXTENSION_TYPES.length()];
        }

        private void initForBootstrap() {
            assert(!INIT_DONE);
            if (constructor[0] == null) {
//                Factory.makeCtors(clazz, types, this.constructor);
//                Factory.makeGetters(clazz, types, this.getters);
            }
        }

        private SpeciesData(String types) {
            // Placeholder only.
            this.types = types;
            this.clazz = null;
            this.constructor = null;
            this.getters = null;
            this.extensions = null;
        }
        private boolean isPlaceholder() { return clazz == null; }

        private static final HashMap<String, SpeciesData> CACHE = new HashMap<>();
        static { CACHE.put("", EMPTY); }  // make bootstrap predictable
        private static final boolean INIT_DONE;  // set after <clinit> finishes...

        SpeciesData extendWithType(char type) {
            int i = extensionIndex(type);
            SpeciesData d = extensions[i];
            if (d != null)  return d;
            extensions[i] = d = get(types+type);
            return d;
        }

        SpeciesData extendWithIndex(byte index) {
            SpeciesData d = extensions[index];
            if (d != null)  return d;
            extensions[index] = d = get(types+EXTENSION_TYPES.charAt(index));
            return d;
        }

        private static SpeciesData get(String types) {
            // Acquire cache lock for query.
            SpeciesData d = lookupCache(types);
            if (!d.isPlaceholder())
                return d;
            synchronized (d) {
                // Use synch. on the placeholder to prevent multiple instantiation of one species.
                // Creating this class forces a recursive call to getForClass.
                if (lookupCache(types).isPlaceholder())
                    throw new IllegalStateException("Cannot generate anything");
            }
            // Reacquire cache lock.
            d = lookupCache(types);
            // Class loading must have upgraded the cache.
            assert(d != null && !d.isPlaceholder());
            return d;
        }
        static SpeciesData getForClass(String types, Class<? extends BoundMethodHandle> clazz) {
            // clazz is a new class which is initializing its SPECIES_DATA field
            return updateCache(types, new SpeciesData(types, clazz));
        }
        private static synchronized SpeciesData lookupCache(String types) {
            SpeciesData d = CACHE.get(types);
            if (d != null)  return d;
            d = new SpeciesData(types);
            assert(d.isPlaceholder());
            CACHE.put(types, d);
            return d;
        }
        private static synchronized SpeciesData updateCache(String types, SpeciesData d) {
            SpeciesData d2;
            assert((d2 = CACHE.get(types)) == null || d2.isPlaceholder());
            assert(!d.isPlaceholder());
            CACHE.put(types, d);
            return d;
        }

        static {
            // pre-fill the BMH speciesdata cache with BMH's inner classes
            final Class<BoundMethodHandle> rootCls = BoundMethodHandle.class;
            SpeciesData d0 = BoundMethodHandle.SPECIES_DATA;  // trigger class init
            assert(d0 == null || d0 == lookupCache("")) : d0;
            try {
                /*
                for (Class<?> c : rootCls.getDeclaredClasses()) {
                    if (rootCls.isAssignableFrom(c)) {
                        final Class<? extends BoundMethodHandle> cbmh = c.asSubclass(BoundMethodHandle.class);
                        SpeciesData d = Factory.speciesDataFromConcreteBMHClass(cbmh);
                        assert(d != null) : cbmh.getName();
                        assert(d.clazz == cbmh);
                        assert(d == lookupCache(d.types));
                    }
                }
                */
            } catch (Throwable e) {
                throw newInternalError(e);
            }

            for (SpeciesData d : CACHE.values()) {
                d.initForBootstrap();
            }
            // Note:  Do not simplify this, because INIT_DONE must not be
            // a compile-time constant during bootstrapping.
            INIT_DONE = Boolean.TRUE;
        }
    }

    static SpeciesData getSpeciesData(String types) {
        return SpeciesData.get(types);
    }



    private static final Lookup LOOKUP = Lookup.IMPL_LOOKUP;

    /**
     * All subclasses must provide such a value describing their type signature.
     */
    static final SpeciesData SPECIES_DATA = SpeciesData.EMPTY;
}
