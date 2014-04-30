/*
 * Copyright (c) 1999, 2010, Oracle and/or its affiliates. All rights reserved.
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

package org.apidesign.bck2brwsr.emul.reflect;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ListIterator;
import java.util.WeakHashMap;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.emul.reflect.MethodImpl;

/**
 * {@code Proxy} provides static methods for creating dynamic proxy
 * classes and instances, and it is also the superclass of all
 * dynamic proxy classes created by those methods.
 *
 * <p>To create a proxy for some interface {@code Foo}:
 * <pre>
 *     InvocationHandler handler = new MyInvocationHandler(...);
 *     Class proxyClass = Proxy.getProxyClass(
 *         Foo.class.getClassLoader(), new Class[] { Foo.class });
 *     Foo f = (Foo) proxyClass.
 *         getConstructor(new Class[] { InvocationHandler.class }).
 *         newInstance(new Object[] { handler });
 * </pre>
 * or more simply:
 * <pre>
 *     Foo f = (Foo) Proxy.newProxyInstance(Foo.class.getClassLoader(),
 *                                          new Class[] { Foo.class },
 *                                          handler);
 * </pre>
 *
 * <p>A <i>dynamic proxy class</i> (simply referred to as a <i>proxy
 * class</i> below) is a class that implements a list of interfaces
 * specified at runtime when the class is created, with behavior as
 * described below.
 *
 * A <i>proxy interface</i> is such an interface that is implemented
 * by a proxy class.
 *
 * A <i>proxy instance</i> is an instance of a proxy class.
 *
 * Each proxy instance has an associated <i>invocation handler</i>
 * object, which implements the interface {@link InvocationHandler}.
 * A method invocation on a proxy instance through one of its proxy
 * interfaces will be dispatched to the {@link InvocationHandler#invoke
 * invoke} method of the instance's invocation handler, passing the proxy
 * instance, a {@code java.lang.reflect.Method} object identifying
 * the method that was invoked, and an array of type {@code Object}
 * containing the arguments.  The invocation handler processes the
 * encoded method invocation as appropriate and the result that it
 * returns will be returned as the result of the method invocation on
 * the proxy instance.
 *
 * <p>A proxy class has the following properties:
 *
 * <ul>
 * <li>Proxy classes are public, final, and not abstract.
 *
 * <li>The unqualified name of a proxy class is unspecified.  The space
 * of class names that begin with the string {@code "$Proxy"}
 * should be, however, reserved for proxy classes.
 *
 * <li>A proxy class extends {@code java.lang.reflect.Proxy}.
 *
 * <li>A proxy class implements exactly the interfaces specified at its
 * creation, in the same order.
 *
 * <li>If a proxy class implements a non-public interface, then it will
 * be defined in the same package as that interface.  Otherwise, the
 * package of a proxy class is also unspecified.  Note that package
 * sealing will not prevent a proxy class from being successfully defined
 * in a particular package at runtime, and neither will classes already
 * defined by the same class loader and the same package with particular
 * signers.
 *
 * <li>Since a proxy class implements all of the interfaces specified at
 * its creation, invoking {@code getInterfaces} on its
 * {@code Class} object will return an array containing the same
 * list of interfaces (in the order specified at its creation), invoking
 * {@code getMethods} on its {@code Class} object will return
 * an array of {@code Method} objects that include all of the
 * methods in those interfaces, and invoking {@code getMethod} will
 * find methods in the proxy interfaces as would be expected.
 *
 * <li>The {@link Proxy#isProxyClass Proxy.isProxyClass} method will
 * return true if it is passed a proxy class-- a class returned by
 * {@code Proxy.getProxyClass} or the class of an object returned by
 * {@code Proxy.newProxyInstance}-- and false otherwise.
 *
 * <li>The {@code java.security.ProtectionDomain} of a proxy class
 * is the same as that of system classes loaded by the bootstrap class
 * loader, such as {@code java.lang.Object}, because the code for a
 * proxy class is generated by trusted system code.  This protection
 * domain will typically be granted
 * {@code java.security.AllPermission}.
 *
 * <li>Each proxy class has one public constructor that takes one argument,
 * an implementation of the interface {@link InvocationHandler}, to set
 * the invocation handler for a proxy instance.  Rather than having to use
 * the reflection API to access the public constructor, a proxy instance
 * can be also be created by calling the {@link Proxy#newProxyInstance
 * Proxy.newProxyInstance} method, which combines the actions of calling
 * {@link Proxy#getProxyClass Proxy.getProxyClass} with invoking the
 * constructor with an invocation handler.
 * </ul>
 *
 * <p>A proxy instance has the following properties:
 *
 * <ul>
 * <li>Given a proxy instance {@code proxy} and one of the
 * interfaces implemented by its proxy class {@code Foo}, the
 * following expression will return true:
 * <pre>
 *     {@code proxy instanceof Foo}
 * </pre>
 * and the following cast operation will succeed (rather than throwing
 * a {@code ClassCastException}):
 * <pre>
 *     {@code (Foo) proxy}
 * </pre>
 *
 * <li>Each proxy instance has an associated invocation handler, the one
 * that was passed to its constructor.  The static
 * {@link Proxy#getInvocationHandler Proxy.getInvocationHandler} method
 * will return the invocation handler associated with the proxy instance
 * passed as its argument.
 *
 * <li>An interface method invocation on a proxy instance will be
 * encoded and dispatched to the invocation handler's {@link
 * InvocationHandler#invoke invoke} method as described in the
 * documentation for that method.
 *
 * <li>An invocation of the {@code hashCode},
 * {@code equals}, or {@code toString} methods declared in
 * {@code java.lang.Object} on a proxy instance will be encoded and
 * dispatched to the invocation handler's {@code invoke} method in
 * the same manner as interface method invocations are encoded and
 * dispatched, as described above.  The declaring class of the
 * {@code Method} object passed to {@code invoke} will be
 * {@code java.lang.Object}.  Other public methods of a proxy
 * instance inherited from {@code java.lang.Object} are not
 * overridden by a proxy class, so invocations of those methods behave
 * like they do for instances of {@code java.lang.Object}.
 * </ul>
 *
 * <h3>Methods Duplicated in Multiple Proxy Interfaces</h3>
 *
 * <p>When two or more interfaces of a proxy class contain a method with
 * the same name and parameter signature, the order of the proxy class's
 * interfaces becomes significant.  When such a <i>duplicate method</i>
 * is invoked on a proxy instance, the {@code Method} object passed
 * to the invocation handler will not necessarily be the one whose
 * declaring class is assignable from the reference type of the interface
 * that the proxy's method was invoked through.  This limitation exists
 * because the corresponding method implementation in the generated proxy
 * class cannot determine which interface it was invoked through.
 * Therefore, when a duplicate method is invoked on a proxy instance,
 * the {@code Method} object for the method in the foremost interface
 * that contains the method (either directly or inherited through a
 * superinterface) in the proxy class's list of interfaces is passed to
 * the invocation handler's {@code invoke} method, regardless of the
 * reference type through which the method invocation occurred.
 *
 * <p>If a proxy interface contains a method with the same name and
 * parameter signature as the {@code hashCode}, {@code equals},
 * or {@code toString} methods of {@code java.lang.Object},
 * when such a method is invoked on a proxy instance, the
 * {@code Method} object passed to the invocation handler will have
 * {@code java.lang.Object} as its declaring class.  In other words,
 * the public, non-final methods of {@code java.lang.Object}
 * logically precede all of the proxy interfaces for the determination of
 * which {@code Method} object to pass to the invocation handler.
 *
 * <p>Note also that when a duplicate method is dispatched to an
 * invocation handler, the {@code invoke} method may only throw
 * checked exception types that are assignable to one of the exception
 * types in the {@code throws} clause of the method in <i>all</i> of
 * the proxy interfaces that it can be invoked through.  If the
 * {@code invoke} method throws a checked exception that is not
 * assignable to any of the exception types declared by the method in one
 * of the proxy interfaces that it can be invoked through, then an
 * unchecked {@code UndeclaredThrowableException} will be thrown by
 * the invocation on the proxy instance.  This restriction means that not
 * all of the exception types returned by invoking
 * {@code getExceptionTypes} on the {@code Method} object
 * passed to the {@code invoke} method can necessarily be thrown
 * successfully by the {@code invoke} method.
 *
 * @author      Peter Jones
 * @see         InvocationHandler
 * @since       1.3
 */
public final class ProxyImpl implements java.io.Serializable {

    private static final long serialVersionUID = -2222568056686623797L;

    /** prefix for all proxy class names */
    private final static String proxyClassNamePrefix = "$Proxy";

    /** parameter types of a proxy class constructor */
    private final static Class[] constructorParams =
        { InvocationHandler.class };

    /** maps a class loader to the proxy class cache for that loader */
    private static Map<ClassLoader, Map<List<String>, Object>> loaderToCache
        = new WeakHashMap<>();

    /** marks that a particular proxy class is currently being generated */
    private static Object pendingGenerationMarker = new Object();

    /** next number to use for generation of unique proxy class names */
    private static long nextUniqueNumber = 0;
    private static Object nextUniqueNumberLock = new Object();

    /** set of all generated proxy classes, for isProxyClass implementation */
    private static Map<Class<?>, Void> proxyClasses =
        Collections.synchronizedMap(new WeakHashMap<Class<?>, Void>());

    /**
     * the invocation handler for this proxy instance.
     * @serial
     */
    protected InvocationHandler h;

    /**
     * Prohibits instantiation.
     */
    private ProxyImpl() {
    }

    /**
     * Constructs a new {@code Proxy} instance from a subclass
     * (typically, a dynamic proxy class) with the specified value
     * for its invocation handler.
     *
     * @param   h the invocation handler for this proxy instance
     */
    protected ProxyImpl(InvocationHandler h) {
        this.h = h;
    }

    /**
     * Returns the {@code java.lang.Class} object for a proxy class
     * given a class loader and an array of interfaces.  The proxy class
     * will be defined by the specified class loader and will implement
     * all of the supplied interfaces.  If a proxy class for the same
     * permutation of interfaces has already been defined by the class
     * loader, then the existing proxy class will be returned; otherwise,
     * a proxy class for those interfaces will be generated dynamically
     * and defined by the class loader.
     *
     * <p>There are several restrictions on the parameters that may be
     * passed to {@code Proxy.getProxyClass}:
     *
     * <ul>
     * <li>All of the {@code Class} objects in the
     * {@code interfaces} array must represent interfaces, not
     * classes or primitive types.
     *
     * <li>No two elements in the {@code interfaces} array may
     * refer to identical {@code Class} objects.
     *
     * <li>All of the interface types must be visible by name through the
     * specified class loader.  In other words, for class loader
     * {@code cl} and every interface {@code i}, the following
     * expression must be true:
     * <pre>
     *     Class.forName(i.getName(), false, cl) == i
     * </pre>
     *
     * <li>All non-public interfaces must be in the same package;
     * otherwise, it would not be possible for the proxy class to
     * implement all of the interfaces, regardless of what package it is
     * defined in.
     *
     * <li>For any set of member methods of the specified interfaces
     * that have the same signature:
     * <ul>
     * <li>If the return type of any of the methods is a primitive
     * type or void, then all of the methods must have that same
     * return type.
     * <li>Otherwise, one of the methods must have a return type that
     * is assignable to all of the return types of the rest of the
     * methods.
     * </ul>
     *
     * <li>The resulting proxy class must not exceed any limits imposed
     * on classes by the virtual machine.  For example, the VM may limit
     * the number of interfaces that a class may implement to 65535; in
     * that case, the size of the {@code interfaces} array must not
     * exceed 65535.
     * </ul>
     *
     * <p>If any of these restrictions are violated,
     * {@code Proxy.getProxyClass} will throw an
     * {@code IllegalArgumentException}.  If the {@code interfaces}
     * array argument or any of its elements are {@code null}, a
     * {@code NullPointerException} will be thrown.
     *
     * <p>Note that the order of the specified proxy interfaces is
     * significant: two requests for a proxy class with the same combination
     * of interfaces but in a different order will result in two distinct
     * proxy classes.
     *
     * @param   loader the class loader to define the proxy class
     * @param   interfaces the list of interfaces for the proxy class
     *          to implement
     * @return  a proxy class that is defined in the specified class loader
     *          and that implements the specified interfaces
     * @throws  IllegalArgumentException if any of the restrictions on the
     *          parameters that may be passed to {@code getProxyClass}
     *          are violated
     * @throws  NullPointerException if the {@code interfaces} array
     *          argument or any of its elements are {@code null}
     */
    public static Class<?> getProxyClass(ClassLoader loader,
                                         Class<?>... interfaces)
        throws IllegalArgumentException
    {
        if (interfaces.length > 65535) {
            throw new IllegalArgumentException("interface limit exceeded");
        }

        Class<?> proxyClass = null;

        /* collect interface names to use as key for proxy class cache */
        String[] interfaceNames = new String[interfaces.length];

        // for detecting duplicates
        Set<Class<?>> interfaceSet = new HashSet<>();

        for (int i = 0; i < interfaces.length; i++) {
            /*
             * Verify that the class loader resolves the name of this
             * interface to the same Class object.
             */
            String interfaceName = interfaces[i].getName();
            Class<?> interfaceClass = null;
            try {
                interfaceClass = Class.forName(interfaceName, false, loader);
            } catch (ClassNotFoundException e) {
            }
            if (interfaceClass != interfaces[i]) {
                throw new IllegalArgumentException(
                    interfaces[i] + " is not visible from class loader");
            }

            /*
             * Verify that the Class object actually represents an
             * interface.
             */
            if (!interfaceClass.isInterface()) {
                throw new IllegalArgumentException(
                    interfaceClass.getName() + " is not an interface");
            }

            /*
             * Verify that this interface is not a duplicate.
             */
            if (interfaceSet.contains(interfaceClass)) {
                throw new IllegalArgumentException(
                    "repeated interface: " + interfaceClass.getName());
            }
            interfaceSet.add(interfaceClass);

            interfaceNames[i] = interfaceName;
        }

        /*
         * Using string representations of the proxy interfaces as
         * keys in the proxy class cache (instead of their Class
         * objects) is sufficient because we require the proxy
         * interfaces to be resolvable by name through the supplied
         * class loader, and it has the advantage that using a string
         * representation of a class makes for an implicit weak
         * reference to the class.
         */
        List<String> key = Arrays.asList(interfaceNames);

        /*
         * Find or create the proxy class cache for the class loader.
         */
        Map<List<String>, Object> cache;
        synchronized (loaderToCache) {
            cache = loaderToCache.get(loader);
            if (cache == null) {
                cache = new HashMap<>();
                loaderToCache.put(loader, cache);
            }
            /*
             * This mapping will remain valid for the duration of this
             * method, without further synchronization, because the mapping
             * will only be removed if the class loader becomes unreachable.
             */
        }

        /*
         * Look up the list of interfaces in the proxy class cache using
         * the key.  This lookup will result in one of three possible
         * kinds of values:
         *     null, if there is currently no proxy class for the list of
         *         interfaces in the class loader,
         *     the pendingGenerationMarker object, if a proxy class for the
         *         list of interfaces is currently being generated,
         *     or a weak reference to a Class object, if a proxy class for
         *         the list of interfaces has already been generated.
         */
        synchronized (cache) {
            /*
             * Note that we need not worry about reaping the cache for
             * entries with cleared weak references because if a proxy class
             * has been garbage collected, its class loader will have been
             * garbage collected as well, so the entire cache will be reaped
             * from the loaderToCache map.
             */
            do {
                Object value = cache.get(key);
                if (value instanceof Reference) {
                    proxyClass = (Class<?>) ((Reference) value).get();
                }
                if (proxyClass != null) {
                    // proxy class already generated: return it
                    return proxyClass;
                } else if (value == pendingGenerationMarker) {
                    // proxy class being generated: wait for it
                    try {
                        cache.wait();
                    } catch (InterruptedException e) {
                        /*
                         * The class generation that we are waiting for should
                         * take a small, bounded time, so we can safely ignore
                         * thread interrupts here.
                         */
                    }
                    continue;
                } else {
                    /*
                     * No proxy class for this list of interfaces has been
                     * generated or is being generated, so we will go and
                     * generate it now.  Mark it as pending generation.
                     */
                    cache.put(key, pendingGenerationMarker);
                    break;
                }
            } while (true);
        }

        try {
            String proxyPkg = null;     // package to define proxy class in

            /*
             * Record the package of a non-public proxy interface so that the
             * proxy class will be defined in the same package.  Verify that
             * all non-public proxy interfaces are in the same package.
             */
            for (int i = 0; i < interfaces.length; i++) {
                int flags = interfaces[i].getModifiers();
                if (!Modifier.isPublic(flags)) {
                    String name = interfaces[i].getName();
                    int n = name.lastIndexOf('.');
                    String pkg = ((n == -1) ? "" : name.substring(0, n + 1));
                    if (proxyPkg == null) {
                        proxyPkg = pkg;
                    } else if (!pkg.equals(proxyPkg)) {
                        throw new IllegalArgumentException(
                            "non-public interfaces from different packages");
                    }
                }
            }

            if (proxyPkg == null) {     // if no non-public proxy interfaces,
                proxyPkg = "";          // use the unnamed package
            }

            {
                /*
                 * Choose a name for the proxy class to generate.
                 */
                long num;
                synchronized (nextUniqueNumberLock) {
                    num = nextUniqueNumber++;
                }
                String proxyName = proxyPkg + proxyClassNamePrefix + num;
                /*
                 * Verify that the class loader hasn't already
                 * defined a class with the chosen name.
                 */

                /*
                 * Generate the specified proxy class.
                 */
                Generator gen = new Generator(proxyName, interfaces);
                final byte[] proxyClassFile = gen.generateClassFile();
                try {
                    proxyClass = defineClass0(loader, proxyName,
                        proxyClassFile);
                } catch (ClassFormatError e) {
                    /*
                     * A ClassFormatError here means that (barring bugs in the
                     * proxy class generation code) there was some other
                     * invalid aspect of the arguments supplied to the proxy
                     * class creation (such as virtual machine limitations
                     * exceeded).
                     */
                    throw new IllegalArgumentException(e.toString());
                }
                gen.fillInMethods(proxyClass);
            }
            // add to set of all generated proxy classes, for isProxyClass
            proxyClasses.put(proxyClass, null);

        } finally {
            /*
             * We must clean up the "pending generation" state of the proxy
             * class cache entry somehow.  If a proxy class was successfully
             * generated, store it in the cache (with a weak reference);
             * otherwise, remove the reserved entry.  In all cases, notify
             * all waiters on reserved entries in this cache.
             */
            synchronized (cache) {
                if (proxyClass != null) {
                    cache.put(key, new WeakReference<Class<?>>(proxyClass));
                } else {
                    cache.remove(key);
                }
                cache.notifyAll();
            }
        }
        return proxyClass;
    }

    /**
     * Returns an instance of a proxy class for the specified interfaces
     * that dispatches method invocations to the specified invocation
     * handler.  This method is equivalent to:
     * <pre>
     *     Proxy.getProxyClass(loader, interfaces).
     *         getConstructor(new Class[] { InvocationHandler.class }).
     *         newInstance(new Object[] { handler });
     * </pre>
     *
     * <p>{@code Proxy.newProxyInstance} throws
     * {@code IllegalArgumentException} for the same reasons that
     * {@code Proxy.getProxyClass} does.
     *
     * @param   loader the class loader to define the proxy class
     * @param   interfaces the list of interfaces for the proxy class
     *          to implement
     * @param   h the invocation handler to dispatch method invocations to
     * @return  a proxy instance with the specified invocation handler of a
     *          proxy class that is defined by the specified class loader
     *          and that implements the specified interfaces
     * @throws  IllegalArgumentException if any of the restrictions on the
     *          parameters that may be passed to {@code getProxyClass}
     *          are violated
     * @throws  NullPointerException if the {@code interfaces} array
     *          argument or any of its elements are {@code null}, or
     *          if the invocation handler, {@code h}, is
     *          {@code null}
     */
    public static Object newProxyInstance(ClassLoader loader,
                                          Class<?>[] interfaces,
                                          InvocationHandler h)
        throws IllegalArgumentException
    {
        if (h == null) {
            throw new NullPointerException();
        }

        /*
         * Look up or generate the designated proxy class.
         */
        Class<?> cl = getProxyClass(loader, interfaces);

        /*
         * Invoke its constructor with the designated invocation handler.
         */
        try {
            Constructor cons = cl.getConstructor(constructorParams);
            return cons.newInstance(new Object[] { h });
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        } catch (IllegalAccessException e) {
            throw new InternalError(e.toString());
        } catch (InstantiationException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Returns true if and only if the specified class was dynamically
     * generated to be a proxy class using the {@code getProxyClass}
     * method or the {@code newProxyInstance} method.
     *
     * <p>The reliability of this method is important for the ability
     * to use it to make security decisions, so its implementation should
     * not just test if the class in question extends {@code Proxy}.
     *
     * @param   cl the class to test
     * @return  {@code true} if the class is a proxy class and
     *          {@code false} otherwise
     * @throws  NullPointerException if {@code cl} is {@code null}
     */
    public static boolean isProxyClass(Class<?> cl) {
        if (cl == null) {
            throw new NullPointerException();
        }

        return proxyClasses.containsKey(cl);
    }

    /**
     * Returns the invocation handler for the specified proxy instance.
     *
     * @param   proxy the proxy instance to return the invocation handler for
     * @return  the invocation handler for the proxy instance
     * @throws  IllegalArgumentException if the argument is not a
     *          proxy instance
     */
    public static InvocationHandler getInvocationHandler(Object proxy)
        throws IllegalArgumentException
    {
        /*
         * Verify that the object is actually a proxy instance.
         */
        if (!isProxyClass(proxy.getClass())) {
            throw new IllegalArgumentException("not a proxy instance");
        }

        ProxyImpl p = (ProxyImpl) proxy;
        return p.h;
    }

    @JavaScriptBody(args = { "ignore", "name", "byteCode" }, 
        body = "return vm._reload(name, byteCode).constructor.$class;"
    )
    private static native Class defineClass0(
        ClassLoader loader, String name, byte[] b
    );
    
    private static class Generator {
        /*
         * In the comments below, "JVMS" refers to The Java Virtual Machine
         * Specification Second Edition and "JLS" refers to the original
         * version of The Java Language Specification, unless otherwise
         * specified.
         */

        /* need 1.6 bytecode */
        private static final int CLASSFILE_MAJOR_VERSION = 50;
        private static final int CLASSFILE_MINOR_VERSION = 0;

        /*
         * beginning of constants copied from
         * sun.tools.java.RuntimeConstants (which no longer exists):
         */

        /* constant pool tags */
        private static final int CONSTANT_UTF8 = 1;
        private static final int CONSTANT_UNICODE = 2;
        private static final int CONSTANT_INTEGER = 3;
        private static final int CONSTANT_FLOAT = 4;
        private static final int CONSTANT_LONG = 5;
        private static final int CONSTANT_DOUBLE = 6;
        private static final int CONSTANT_CLASS = 7;
        private static final int CONSTANT_STRING = 8;
        private static final int CONSTANT_FIELD = 9;
        private static final int CONSTANT_METHOD = 10;
        private static final int CONSTANT_INTERFACEMETHOD = 11;
        private static final int CONSTANT_NAMEANDTYPE = 12;

        /* access and modifier flags */
        private static final int ACC_PUBLIC = 0x00000001;
        private static final int ACC_FINAL = 0x00000010;
        private static final int ACC_SUPER = 0x00000020;

    // end of constants copied from sun.tools.java.RuntimeConstants
        /**
         * name of the superclass of proxy classes
         */
        private final static String superclassName = "java/lang/reflect/Proxy";

        /**
         * name of field for storing a proxy instance's invocation handler
         */
        private final static String handlerFieldName = "h";

        /* preloaded Method objects for methods in java.lang.Object */
        private static Method hashCodeMethod;
        private static Method equalsMethod;
        private static Method toStringMethod;

        static {
            try {
                hashCodeMethod = Object.class.getMethod("hashCode");
                equalsMethod
                    = Object.class.getMethod("equals", new Class[]{Object.class});
                toStringMethod = Object.class.getMethod("toString");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e.getMessage());
            }
        }

        /**
         * name of proxy class
         */
        private String className;

        /**
         * proxy interfaces
         */
        private Class[] interfaces;

        /**
         * constant pool of class being generated
         */
        private ConstantPool cp = new ConstantPool();

        /**
         * maps method signature string to list of ProxyMethod objects for proxy
         * methods with that signature
         */
        private Map<String, List<ProxyMethod>> proxyMethods
            = new HashMap<String, List<ProxyMethod>>();

        /**
         * count of ProxyMethod objects added to proxyMethods
         */
        private int proxyMethodCount = 0;

        /**
         * Construct a ProxyGenerator to generate a proxy class with the
         * specified name and for the given interfaces.
         *
         * A ProxyGenerator object contains the state for the ongoing generation
         * of a particular proxy class.
         */
        private Generator(String className, Class[] interfaces) {
            this.className = className;
            this.interfaces = interfaces;
        }

        /**
         * Generate a class file for the proxy class. This method drives the
         * class file generation process.
         */
        private byte[] generateClassFile() {

            /* ============================================================
             * Step 1: Assemble ProxyMethod objects for all methods to
             * generate proxy dispatching code for.
             */

            /*
             * Record that proxy methods are needed for the hashCode, equals,
             * and toString methods of java.lang.Object.  This is done before
             * the methods from the proxy interfaces so that the methods from
             * java.lang.Object take precedence over duplicate methods in the
             * proxy interfaces.
             */
            addProxyMethod(hashCodeMethod, Object.class);
            addProxyMethod(equalsMethod, Object.class);
            addProxyMethod(toStringMethod, Object.class);

            /*
             * Now record all of the methods from the proxy interfaces, giving
             * earlier interfaces precedence over later ones with duplicate
             * methods.
             */
            for (int i = 0; i < interfaces.length; i++) {
                Method[] methods = interfaces[i].getMethods();
                for (int j = 0; j < methods.length; j++) {
                    addProxyMethod(methods[j], interfaces[i]);
                }
            }

            /*
             * For each set of proxy methods with the same signature,
             * verify that the methods' return types are compatible.
             */
            for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
                checkReturnTypes(sigmethods);
            }

            /* ============================================================
             * Step 2: Assemble FieldInfo and MethodInfo structs for all of
             * fields and methods in the class we are generating.
             */
            
            // will be done in fillInMethods

            /* ============================================================
             * Step 3: Write the final class file.
             */

            /*
             * Make sure that constant pool indexes are reserved for the
             * following items before starting to write the final class file.
             */
            cp.getClass(dotToSlash(className));
            cp.getClass(superclassName);
            for (int i = 0; i < interfaces.length; i++) {
                cp.getClass(dotToSlash(interfaces[i].getName()));
            }

            /*
             * Disallow new constant pool additions beyond this point, since
             * we are about to write the final constant pool table.
             */
            cp.setReadOnly();

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);

            try {
                /*
                 * Write all the items of the "ClassFile" structure.
                 * See JVMS section 4.1.
                 */
                // u4 magic;
                dout.writeInt(0xCAFEBABE);
                // u2 minor_version;
                dout.writeShort(CLASSFILE_MINOR_VERSION);
                // u2 major_version;
                dout.writeShort(CLASSFILE_MAJOR_VERSION);

                cp.write(dout);             // (write constant pool)

                // u2 access_flags;
                dout.writeShort(ACC_PUBLIC | ACC_FINAL | ACC_SUPER);
                // u2 this_class;
                dout.writeShort(cp.getClass(dotToSlash(className)));
                // u2 super_class;
                dout.writeShort(cp.getClass(superclassName));

                // u2 interfaces_count;
                dout.writeShort(interfaces.length);
                // u2 interfaces[interfaces_count];
                for (int i = 0; i < interfaces.length; i++) {
                    dout.writeShort(cp.getClass(
                        dotToSlash(interfaces[i].getName())));
                }

                // u2 fields_count;
                dout.writeShort(0);

                // u2 methods_count;
                dout.writeShort(0);

                // u2 attributes_count;
                dout.writeShort(0); // (no ClassFile attributes for proxy classes)

            } catch (IOException e) {
                throw new InternalError("unexpected I/O Exception");
            }

            return bout.toByteArray();
        }

        @JavaScriptBody(args = { "c", "sig", "method", "primitive" }, body = 
            "var p = c.cnstr.prototype;\n" +
            "p[sig] = function() {\n" +
            "  var h = this._h();\n" +
            "  var res = h.invoke__Ljava_lang_Object_2Ljava_lang_Object_2Ljava_lang_reflect_Method_2_3Ljava_lang_Object_2(this, method, arguments);\n" +
            "  \n" +
            "  \n" +
            "  return res;\n" +
            "};"
        )
        private static native void defineMethod(Class<?> proxyClass, String sig, Method method, boolean primitive);

        @JavaScriptBody(args = "c", body = 
              "var h = c.cnstr.cons__VLjava_lang_reflect_InvocationHandler_2 = function(h) {\n"
            + "  c.superclass.cnstr.cons__VLjava_lang_reflect_InvocationHandler_2.call(this, h);\n"
            + "}\n"
            + "h.cls = c.cnstr;\n"
        )
        private static native void defineConstructor(Class<?> proxyClass);
        
        final void fillInMethods(Class<?> proxyClass) {
            for (List<ProxyMethod> sigmethods : proxyMethods.values()) {
                for (ProxyMethod pm : sigmethods) {
                    String sig = MethodImpl.toSignature(pm.method);
                    defineMethod(proxyClass, sig, pm.method, pm.method.getReturnType().isPrimitive());
                }
            }
            defineConstructor(proxyClass);
        }

        /**
         * Add another method to be proxied, either by creating a new
         * ProxyMethod object or augmenting an old one for a duplicate method.
         *
         * "fromClass" indicates the proxy interface that the method was found
         * through, which may be different from (a subinterface of) the method's
         * "declaring class". Note that the first Method object passed for a
         * given name and descriptor identifies the Method object (and thus the
         * declaring class) that will be passed to the invocation handler's
         * "invoke" method for a given set of duplicate methods.
         */
        private void addProxyMethod(Method m, Class fromClass) {
            String name = m.getName();
            Class[] parameterTypes = m.getParameterTypes();
            Class returnType = m.getReturnType();
            Class[] exceptionTypes = m.getExceptionTypes();

            String sig = MethodImpl.toSignature(m);
            List<ProxyMethod> sigmethods = proxyMethods.get(sig);
            if (sigmethods != null) {
                for (ProxyMethod pm : sigmethods) {
                    if (returnType == pm.returnType) {
                        /*
                         * Found a match: reduce exception types to the
                         * greatest set of exceptions that can thrown
                         * compatibly with the throws clauses of both
                         * overridden methods.
                         */
                        List<Class<?>> legalExceptions = new ArrayList<Class<?>>();
                        collectCompatibleTypes(
                            exceptionTypes, pm.exceptionTypes, legalExceptions);
                        collectCompatibleTypes(
                            pm.exceptionTypes, exceptionTypes, legalExceptions);
                        pm.exceptionTypes = new Class[legalExceptions.size()];
                        pm.exceptionTypes
                            = legalExceptions.toArray(pm.exceptionTypes);
                        return;
                    }
                }
            } else {
                sigmethods = new ArrayList<ProxyMethod>(3);
                proxyMethods.put(sig, sigmethods);
            }
            sigmethods.add(new ProxyMethod(m, name, parameterTypes, returnType,
                exceptionTypes, fromClass));
        }

        /**
         * For a given set of proxy methods with the same signature, check that
         * their return types are compatible according to the Proxy
         * specification.
         *
         * Specifically, if there is more than one such method, then all of the
         * return types must be reference types, and there must be one return
         * type that is assignable to each of the rest of them.
         */
        private static void checkReturnTypes(List<ProxyMethod> methods) {
            /*
             * If there is only one method with a given signature, there
             * cannot be a conflict.  This is the only case in which a
             * primitive (or void) return type is allowed.
             */
            if (methods.size() < 2) {
                return;
            }

            /*
             * List of return types that are not yet known to be
             * assignable from ("covered" by) any of the others.
             */
            LinkedList<Class<?>> uncoveredReturnTypes = new LinkedList<Class<?>>();

            nextNewReturnType:
            for (ProxyMethod pm : methods) {
                Class<?> newReturnType = pm.returnType;
                if (newReturnType.isPrimitive()) {
                    throw new IllegalArgumentException(
                        "methods with same signature "
                        + getFriendlyMethodSignature(pm.methodName,
                            pm.parameterTypes)
                        + " but incompatible return types: "
                        + newReturnType.getName() + " and others");
                }
                boolean added = false;

                /*
                 * Compare the new return type to the existing uncovered
                 * return types.
                 */
                ListIterator<Class<?>> liter = uncoveredReturnTypes.listIterator();
                while (liter.hasNext()) {
                    Class<?> uncoveredReturnType = liter.next();

                    /*
                     * If an existing uncovered return type is assignable
                     * to this new one, then we can forget the new one.
                     */
                    if (newReturnType.isAssignableFrom(uncoveredReturnType)) {
                        assert !added;
                        continue nextNewReturnType;
                    }

                    /*
                     * If the new return type is assignable to an existing
                     * uncovered one, then should replace the existing one
                     * with the new one (or just forget the existing one,
                     * if the new one has already be put in the list).
                     */
                    if (uncoveredReturnType.isAssignableFrom(newReturnType)) {
                        // (we can assume that each return type is unique)
                        if (!added) {
                            liter.set(newReturnType);
                            added = true;
                        } else {
                            liter.remove();
                        }
                    }
                }

                /*
                 * If we got through the list of existing uncovered return
                 * types without an assignability relationship, then add
                 * the new return type to the list of uncovered ones.
                 */
                if (!added) {
                    uncoveredReturnTypes.add(newReturnType);
                }
            }

            /*
             * We shouldn't end up with more than one return type that is
             * not assignable from any of the others.
             */
            if (uncoveredReturnTypes.size() > 1) {
                ProxyMethod pm = methods.get(0);
                throw new IllegalArgumentException(
                    "methods with same signature "
                    + getFriendlyMethodSignature(pm.methodName, pm.parameterTypes)
                    + " but incompatible return types: " + uncoveredReturnTypes);
            }
        }


        /**
         * A ProxyMethod object represents a proxy method in the proxy class
         * being generated: a method whose implementation will encode and
         * dispatch invocations to the proxy instance's invocation handler.
         */
        private class ProxyMethod {

            private final Method method;
            public String methodName;
            public Class[] parameterTypes;
            public Class returnType;
            public Class[] exceptionTypes;
            public Class fromClass;
            public String methodFieldName;

            private ProxyMethod(Method m, 
                String methodName, Class[] parameterTypes, 
                Class returnType, Class[] exceptionTypes, 
                Class fromClass
            ) {
                this.method = m;
                this.methodName = methodName;
                this.parameterTypes = parameterTypes;
                this.returnType = returnType;
                this.exceptionTypes = exceptionTypes;
                this.fromClass = fromClass;
                this.methodFieldName = "m" + proxyMethodCount++;
            }

        }

        /*
         * ==================== General Utility Methods ====================
         */
        /**
         * Convert a fully qualified class name that uses '.' as the package
         * separator, the external representation used by the Java language and
         * APIs, to a fully qualified class name that uses '/' as the package
         * separator, the representation used in the class file format (see JVMS
         * section 4.2).
         */
        private static String dotToSlash(String name) {
            return name.replace('.', '/');
        }

        /**
         * Return the list of "parameter descriptor" strings enclosed in
         * parentheses corresponding to the given parameter types (in other
         * words, a method descriptor without a return descriptor). This string
         * is useful for constructing string keys for methods without regard to
         * their return type.
         */
        private static String getParameterDescriptors(Class[] parameterTypes) {
            StringBuilder desc = new StringBuilder("(");
            for (int i = 0; i < parameterTypes.length; i++) {
                desc.append(getFieldType(parameterTypes[i]));
            }
            desc.append(')');
            return desc.toString();
        }

        /**
         * Return the "field type" string for the given type, appropriate for a
         * field descriptor, a parameter descriptor, or a return descriptor
         * other than "void". See JVMS section 4.3.2.
         */
        private static String getFieldType(Class type) {
            if (type.isPrimitive()) {
                return PrimitiveTypeInfo.get(type).baseTypeString;
            } else if (type.isArray()) {
                /*
                 * According to JLS 20.3.2, the getName() method on Class does
                 * return the VM type descriptor format for array classes (only);
                 * using that should be quicker than the otherwise obvious code:
                 *
                 *     return "[" + getTypeDescriptor(type.getComponentType());
                 */
                return type.getName().replace('.', '/');
            } else {
                return "L" + dotToSlash(type.getName()) + ";";
            }
        }

        /**
         * Returns a human-readable string representing the signature of a
         * method with the given name and parameter types.
         */
        private static String getFriendlyMethodSignature(String name,
            Class[] parameterTypes) {
            StringBuilder sig = new StringBuilder(name);
            sig.append('(');
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i > 0) {
                    sig.append(',');
                }
                Class parameterType = parameterTypes[i];
                int dimensions = 0;
                while (parameterType.isArray()) {
                    parameterType = parameterType.getComponentType();
                    dimensions++;
                }
                sig.append(parameterType.getName());
                while (dimensions-- > 0) {
                    sig.append("[]");
                }
            }
            sig.append(')');
            return sig.toString();
        }

        /**
         * Add to the given list all of the types in the "from" array that are
         * not already contained in the list and are assignable to at least one
         * of the types in the "with" array.
         *
         * This method is useful for computing the greatest common set of
         * declared exceptions from duplicate methods inherited from different
         * interfaces.
         */
        private static void collectCompatibleTypes(Class<?>[] from,
            Class<?>[] with,
            List<Class<?>> list) {
            for (int i = 0; i < from.length; i++) {
                if (!list.contains(from[i])) {
                    for (int j = 0; j < with.length; j++) {
                        if (with[j].isAssignableFrom(from[i])) {
                            list.add(from[i]);
                            break;
                        }
                    }
                }
            }
        }


        /**
         * A PrimitiveTypeInfo object contains assorted information about a
         * primitive type in its public fields. The struct for a particular
         * primitive type can be obtained using the static "get" method.
         */
        private static class PrimitiveTypeInfo {

            /**
             * "base type" used in various descriptors (see JVMS section 4.3.2)
             */
            public String baseTypeString;

            /**
             * name of corresponding wrapper class
             */
            public String wrapperClassName;

            /**
             * method descriptor for wrapper class "valueOf" factory method
             */
            public String wrapperValueOfDesc;

            /**
             * name of wrapper class method for retrieving primitive value
             */
            public String unwrapMethodName;

            /**
             * descriptor of same method
             */
            public String unwrapMethodDesc;

            private static Map<Class, PrimitiveTypeInfo> table
                = new HashMap<Class, PrimitiveTypeInfo>();

            static {
                add(byte.class, Byte.class);
                add(char.class, Character.class);
                add(double.class, Double.class);
                add(float.class, Float.class);
                add(int.class, Integer.class);
                add(long.class, Long.class);
                add(short.class, Short.class);
                add(boolean.class, Boolean.class);
            }

            private static void add(Class primitiveClass, Class wrapperClass) {
                table.put(primitiveClass,
                    new PrimitiveTypeInfo(primitiveClass, wrapperClass));
            }

            private PrimitiveTypeInfo(Class primitiveClass, Class wrapperClass) {
                assert primitiveClass.isPrimitive();

                baseTypeString
                    = Array.newInstance(primitiveClass, 0)
                    .getClass().getName().substring(1);
                wrapperClassName = dotToSlash(wrapperClass.getName());
                wrapperValueOfDesc
                    = "(" + baseTypeString + ")L" + wrapperClassName + ";";
                unwrapMethodName = primitiveClass.getName() + "Value";
                unwrapMethodDesc = "()" + baseTypeString;
            }

            public static PrimitiveTypeInfo get(Class cl) {
                return table.get(cl);
            }
        }

        /**
         * A ConstantPool object represents the constant pool of a class file
         * being generated. This representation of a constant pool is designed
         * specifically for use by ProxyGenerator; in particular, it assumes
         * that constant pool entries will not need to be resorted (for example,
         * by their type, as the Java compiler does), so that the final index
         * value can be assigned and used when an entry is first created.
         *
         * Note that new entries cannot be created after the constant pool has
         * been written to a class file. To prevent such logic errors, a
         * ConstantPool instance can be marked "read only", so that further
         * attempts to add new entries will fail with a runtime exception.
         *
         * See JVMS section 4.4 for more information about the constant pool of
         * a class file.
         */
        private static class ConstantPool {

            /**
             * list of constant pool entries, in constant pool index order.
             *
             * This list is used when writing the constant pool to a stream and
             * for assigning the next index value. Note that element 0 of this
             * list corresponds to constant pool index 1.
             */
            private List<Entry> pool = new ArrayList<Entry>(32);

            /**
             * maps constant pool data of all types to constant pool indexes.
             *
             * This map is used to look up the index of an existing entry for
             * values of all types.
             */
            private Map<Object, Short> map = new HashMap<Object, Short>(16);

            /**
             * true if no new constant pool entries may be added
             */
            private boolean readOnly = false;

            /**
             * Get or assign the index for a CONSTANT_Utf8 entry.
             */
            public short getUtf8(String s) {
                if (s == null) {
                    throw new NullPointerException();
                }
                return getValue(s);
            }

            /**
             * Get or assign the index for a CONSTANT_Integer entry.
             */
            public short getInteger(int i) {
                return getValue(new Integer(i));
            }

            /**
             * Get or assign the index for a CONSTANT_Float entry.
             */
            public short getFloat(float f) {
                return getValue(new Float(f));
            }

            /**
             * Get or assign the index for a CONSTANT_Class entry.
             */
            public short getClass(String name) {
                short utf8Index = getUtf8(name);
                return getIndirect(new IndirectEntry(
                    CONSTANT_CLASS, utf8Index));
            }

            /**
             * Get or assign the index for a CONSTANT_String entry.
             */
            public short getString(String s) {
                short utf8Index = getUtf8(s);
                return getIndirect(new IndirectEntry(
                    CONSTANT_STRING, utf8Index));
            }

            /**
             * Get or assign the index for a CONSTANT_FieldRef entry.
             */
            public short getFieldRef(String className,
                String name, String descriptor) {
                short classIndex = getClass(className);
                short nameAndTypeIndex = getNameAndType(name, descriptor);
                return getIndirect(new IndirectEntry(
                    CONSTANT_FIELD, classIndex, nameAndTypeIndex));
            }

            /**
             * Get or assign the index for a CONSTANT_MethodRef entry.
             */
            public short getMethodRef(String className,
                String name, String descriptor) {
                short classIndex = getClass(className);
                short nameAndTypeIndex = getNameAndType(name, descriptor);
                return getIndirect(new IndirectEntry(
                    CONSTANT_METHOD, classIndex, nameAndTypeIndex));
            }

            /**
             * Get or assign the index for a CONSTANT_InterfaceMethodRef entry.
             */
            public short getInterfaceMethodRef(String className, String name,
                String descriptor) {
                short classIndex = getClass(className);
                short nameAndTypeIndex = getNameAndType(name, descriptor);
                return getIndirect(new IndirectEntry(
                    CONSTANT_INTERFACEMETHOD, classIndex, nameAndTypeIndex));
            }

            /**
             * Get or assign the index for a CONSTANT_NameAndType entry.
             */
            public short getNameAndType(String name, String descriptor) {
                short nameIndex = getUtf8(name);
                short descriptorIndex = getUtf8(descriptor);
                return getIndirect(new IndirectEntry(
                    CONSTANT_NAMEANDTYPE, nameIndex, descriptorIndex));
            }

            /**
             * Set this ConstantPool instance to be "read only".
             *
             * After this method has been called, further requests to get an
             * index for a non-existent entry will cause an InternalError to be
             * thrown instead of creating of the entry.
             */
            public void setReadOnly() {
                readOnly = true;
            }

            /**
             * Write this constant pool to a stream as part of the class file
             * format.
             *
             * This consists of writing the "constant_pool_count" and
             * "constant_pool[]" items of the "ClassFile" structure, as
             * described in JVMS section 4.1.
             */
            public void write(OutputStream out) throws IOException {
                DataOutputStream dataOut = new DataOutputStream(out);

                // constant_pool_count: number of entries plus one
                dataOut.writeShort(pool.size() + 1);

                for (Entry e : pool) {
                    e.write(dataOut);
                }
            }

            /**
             * Add a new constant pool entry and return its index.
             */
            private short addEntry(Entry entry) {
                pool.add(entry);
                /*
                 * Note that this way of determining the index of the
                 * added entry is wrong if this pool supports
                 * CONSTANT_Long or CONSTANT_Double entries.
                 */
                if (pool.size() >= 65535) {
                    throw new IllegalArgumentException(
                        "constant pool size limit exceeded");
                }
                return (short) pool.size();
            }

            /**
             * Get or assign the index for an entry of a type that contains a
             * direct value. The type of the given object determines the type of
             * the desired entry as follows:
             *
             * java.lang.String CONSTANT_Utf8 java.lang.Integer CONSTANT_Integer
             * java.lang.Float CONSTANT_Float java.lang.Long CONSTANT_Long
             * java.lang.Double CONSTANT_DOUBLE
             */
            private short getValue(Object key) {
                Short index = map.get(key);
                if (index != null) {
                    return index.shortValue();
                } else {
                    if (readOnly) {
                        throw new InternalError(
                            "late constant pool addition: " + key);
                    }
                    short i = addEntry(new ValueEntry(key));
                    map.put(key, new Short(i));
                    return i;
                }
            }

            /**
             * Get or assign the index for an entry of a type that contains
             * references to other constant pool entries.
             */
            private short getIndirect(IndirectEntry e) {
                Short index = map.get(e);
                if (index != null) {
                    return index.shortValue();
                } else {
                    if (readOnly) {
                        throw new InternalError("late constant pool addition");
                    }
                    short i = addEntry(e);
                    map.put(e, new Short(i));
                    return i;
                }
            }

            /**
             * Entry is the abstact superclass of all constant pool entry types
             * that can be stored in the "pool" list; its purpose is to define a
             * common method for writing constant pool entries to a class file.
             */
            private static abstract class Entry {

                public abstract void write(DataOutputStream out)
                    throws IOException;
            }

            /**
             * ValueEntry represents a constant pool entry of a type that
             * contains a direct value (see the comments for the "getValue"
             * method for a list of such types).
             *
             * ValueEntry objects are not used as keys for their entries in the
             * Map "map", so no useful hashCode or equals methods are defined.
             */
            private static class ValueEntry extends Entry {

                private Object value;

                public ValueEntry(Object value) {
                    this.value = value;
                }

                public void write(DataOutputStream out) throws IOException {
                    if (value instanceof String) {
                        out.writeByte(CONSTANT_UTF8);
                        out.writeUTF((String) value);
                    } else if (value instanceof Integer) {
                        out.writeByte(CONSTANT_INTEGER);
                        out.writeInt(((Integer) value).intValue());
                    } else if (value instanceof Float) {
                        out.writeByte(CONSTANT_FLOAT);
                        out.writeFloat(((Float) value).floatValue());
                    } else if (value instanceof Long) {
                        out.writeByte(CONSTANT_LONG);
                        out.writeLong(((Long) value).longValue());
                    } else if (value instanceof Double) {
                        out.writeDouble(CONSTANT_DOUBLE);
                        out.writeDouble(((Double) value).doubleValue());
                    } else {
                        throw new InternalError("bogus value entry: " + value);
                    }
                }
            }

            /**
             * IndirectEntry represents a constant pool entry of a type that
             * references other constant pool entries, i.e., the following
             * types:
             *
             * CONSTANT_Class, CONSTANT_String, CONSTANT_Fieldref,
             * CONSTANT_Methodref, CONSTANT_InterfaceMethodref, and
             * CONSTANT_NameAndType.
             *
             * Each of these entry types contains either one or two indexes of
             * other constant pool entries.
             *
             * IndirectEntry objects are used as the keys for their entries in
             * the Map "map", so the hashCode and equals methods are overridden
             * to allow matching.
             */
            private static class IndirectEntry extends Entry {

                private int tag;
                private short index0;
                private short index1;

                /**
                 * Construct an IndirectEntry for a constant pool entry type
                 * that contains one index of another entry.
                 */
                public IndirectEntry(int tag, short index) {
                    this.tag = tag;
                    this.index0 = index;
                    this.index1 = 0;
                }

                /**
                 * Construct an IndirectEntry for a constant pool entry type
                 * that contains two indexes for other entries.
                 */
                public IndirectEntry(int tag, short index0, short index1) {
                    this.tag = tag;
                    this.index0 = index0;
                    this.index1 = index1;
                }

                public void write(DataOutputStream out) throws IOException {
                    out.writeByte(tag);
                    out.writeShort(index0);
                    /*
                     * If this entry type contains two indexes, write
                     * out the second, too.
                     */
                    if (tag == CONSTANT_FIELD
                        || tag == CONSTANT_METHOD
                        || tag == CONSTANT_INTERFACEMETHOD
                        || tag == CONSTANT_NAMEANDTYPE) {
                        out.writeShort(index1);
                    }
                }

                public int hashCode() {
                    return tag + index0 + index1;
                }

                public boolean equals(Object obj) {
                    if (obj instanceof IndirectEntry) {
                        IndirectEntry other = (IndirectEntry) obj;
                        if (tag == other.tag
                            && index0 == other.index0 && index1 == other.index1) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
    }
    
}
