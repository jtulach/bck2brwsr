/*
 * Copyright (c) 1996, 2010, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import static java.lang.reflect.Method.fromPrimitive;
import static java.lang.reflect.Method.getAccess;
import static java.lang.reflect.Method.getParameterTypes;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.emul.reflect.TypeProvider;

/**
 * {@code Constructor} provides information about, and access to, a single
 * constructor for a class.
 *
 * <p>{@code Constructor} permits widening conversions to occur when matching the
 * actual parameters to newInstance() with the underlying
 * constructor's formal parameters, but throws an
 * {@code IllegalArgumentException} if a narrowing conversion would occur.
 *
 * @param <T> the class in which the constructor is declared
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getConstructors()
 * @see java.lang.Class#getConstructor(Class[])
 * @see java.lang.Class#getDeclaredConstructors()
 *
 * @author      Kenneth Russell
 * @author      Nakul Saraiya
 */
public final
    class Constructor<T> extends AccessibleObject implements
                                                    GenericDeclaration,
                                                    Member {

    private final Class<T> clazz;
    private final Object data;
    private final String sig;

    /**
     * Package-private constructor used by ReflectAccess to enable
     * instantiation of these objects in Java code from the java.lang
     * package via sun.reflect.LangReflectAccess.
     */
    Constructor(Class<T> declaringClass, Object data, String sig)
    {
        this.clazz = declaringClass;
        this.data = data;
        this.sig = sig;
    }

    /**
     * Package-private routine (exposed to java.lang.Class via
     * ReflectAccess) which returns a copy of this Constructor. The copy's
     * "root" field points to this Constructor.
     */
    Constructor<T> copy() {
        return this;
    }

    /**
     * Returns the {@code Class} object representing the class that declares
     * the constructor represented by this {@code Constructor} object.
     */
    public Class<T> getDeclaringClass() {
        return clazz;
    }

    /**
     * Returns the name of this constructor, as a string.  This is
     * the binary name of the constructor's declaring class.
     */
    public String getName() {
        return getDeclaringClass().getName();
    }

    /**
     * Returns the Java language modifiers for the constructor
     * represented by this {@code Constructor} object, as an integer. The
     * {@code Modifier} class should be used to decode the modifiers.
     *
     * @see Modifier
     */
    public int getModifiers() {
        return getAccess(data);
    }

    /**
     * Returns an array of {@code TypeVariable} objects that represent the
     * type variables declared by the generic declaration represented by this
     * {@code GenericDeclaration} object, in declaration order.  Returns an
     * array of length 0 if the underlying generic declaration declares no type
     * variables.
     *
     * @return an array of {@code TypeVariable} objects that represent
     *     the type variables declared by this generic declaration
     * @throws GenericSignatureFormatError if the generic
     *     signature of this generic declaration does not conform to
     *     the format specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @since 1.5
     */
    public TypeVariable<Constructor<T>>[] getTypeParameters() {
        return TypeProvider.getDefault().getTypeParameters(this);
    }


    /**
     * Returns an array of {@code Class} objects that represent the formal
     * parameter types, in declaration order, of the constructor
     * represented by this {@code Constructor} object.  Returns an array of
     * length 0 if the underlying constructor takes no parameters.
     *
     * @return the parameter types for the constructor this object
     * represents
     */
    public Class<?>[] getParameterTypes() {
        return Method.getParameterTypes(sig);
    }


    /**
     * Returns an array of {@code Type} objects that represent the formal
     * parameter types, in declaration order, of the method represented by
     * this {@code Constructor} object. Returns an array of length 0 if the
     * underlying method takes no parameters.
     *
     * <p>If a formal parameter type is a parameterized type,
     * the {@code Type} object returned for it must accurately reflect
     * the actual type parameters used in the source code.
     *
     * <p>If a formal parameter type is a type variable or a parameterized
     * type, it is created. Otherwise, it is resolved.
     *
     * @return an array of {@code Type}s that represent the formal
     *     parameter types of the underlying method, in declaration order
     * @throws GenericSignatureFormatError
     *     if the generic method signature does not conform to the format
     *     specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if any of the parameter
     *     types of the underlying method refers to a non-existent type
     *     declaration
     * @throws MalformedParameterizedTypeException if any of
     *     the underlying method's parameter types refer to a parameterized
     *     type that cannot be instantiated for any reason
     * @since 1.5
     */
    public Type[] getGenericParameterTypes() {
        return TypeProvider.getDefault().getGenericParameterTypes(this);
    }


    /**
     * Returns an array of {@code Class} objects that represent the types
     * of exceptions declared to be thrown by the underlying constructor
     * represented by this {@code Constructor} object.  Returns an array of
     * length 0 if the constructor declares no exceptions in its {@code throws} clause.
     *
     * @return the exception types declared as being thrown by the
     * constructor this object represents
     */
    public Class<?>[] getExceptionTypes() {
        throw new UnsupportedOperationException();
    }


    /**
     * Returns an array of {@code Type} objects that represent the
     * exceptions declared to be thrown by this {@code Constructor} object.
     * Returns an array of length 0 if the underlying method declares
     * no exceptions in its {@code throws} clause.
     *
     * <p>If an exception type is a type variable or a parameterized
     * type, it is created. Otherwise, it is resolved.
     *
     * @return an array of Types that represent the exception types
     *     thrown by the underlying method
     * @throws GenericSignatureFormatError
     *     if the generic method signature does not conform to the format
     *     specified in
     *     <cite>The Java&trade; Virtual Machine Specification</cite>
     * @throws TypeNotPresentException if the underlying method's
     *     {@code throws} clause refers to a non-existent type declaration
     * @throws MalformedParameterizedTypeException if
     *     the underlying method's {@code throws} clause refers to a
     *     parameterized type that cannot be instantiated for any reason
     * @since 1.5
     */
      public Type[] getGenericExceptionTypes() {
          return TypeProvider.getDefault().getGenericExceptionTypes(this);
      }

    /**
     * Compares this {@code Constructor} against the specified object.
     * Returns true if the objects are the same.  Two {@code Constructor} objects are
     * the same if they were declared by the same class and have the
     * same formal parameter types.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Constructor) {
            Constructor other = (Constructor)obj;
            return data == other.data;
        }
        return false;
    }

    /**
     * Returns a hashcode for this {@code Constructor}. The hashcode is
     * the same as the hashcode for the underlying constructor's
     * declaring class name.
     */
    public int hashCode() {
        return getDeclaringClass().getName().hashCode();
    }

    /**
     * Returns a string describing this {@code Constructor}.  The string is
     * formatted as the constructor access modifiers, if any,
     * followed by the fully-qualified name of the declaring class,
     * followed by a parenthesized, comma-separated list of the
     * constructor's formal parameter types.  For example:
     * <pre>
     *    public java.util.Hashtable(int,float)
     * </pre>
     *
     * <p>The only possible modifiers for constructors are the access
     * modifiers {@code public}, {@code protected} or
     * {@code private}.  Only one of these may appear, or none if the
     * constructor has default (package) access.
     */
    public String toString() {
        try {
            StringBuffer sb = new StringBuffer();
            int mod = getModifiers() & Modifier.constructorModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod) + " ");
            }
            sb.append(Field.getTypeName(getDeclaringClass()));
            sb.append("(");
            Class<?>[] params = getParameterTypes(); // avoid clone
            for (int j = 0; j < params.length; j++) {
                sb.append(Field.getTypeName(params[j]));
                if (j < (params.length - 1))
                    sb.append(",");
            }
            sb.append(")");
            /*
            Class<?>[] exceptions = exceptionTypes; // avoid clone
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(exceptions[k].getName());
                    if (k < (exceptions.length - 1))
                        sb.append(",");
                }
            }
            */
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    /**
     * Returns a string describing this {@code Constructor},
     * including type parameters.  The string is formatted as the
     * constructor access modifiers, if any, followed by an
     * angle-bracketed comma separated list of the constructor's type
     * parameters, if any, followed by the fully-qualified name of the
     * declaring class, followed by a parenthesized, comma-separated
     * list of the constructor's generic formal parameter types.
     *
     * If this constructor was declared to take a variable number of
     * arguments, instead of denoting the last parameter as
     * "<tt><i>Type</i>[]</tt>", it is denoted as
     * "<tt><i>Type</i>...</tt>".
     *
     * A space is used to separate access modifiers from one another
     * and from the type parameters or return type.  If there are no
     * type parameters, the type parameter list is elided; if the type
     * parameter list is present, a space separates the list from the
     * class name.  If the constructor is declared to throw
     * exceptions, the parameter list is followed by a space, followed
     * by the word "{@code throws}" followed by a
     * comma-separated list of the thrown exception types.
     *
     * <p>The only possible modifiers for constructors are the access
     * modifiers {@code public}, {@code protected} or
     * {@code private}.  Only one of these may appear, or none if the
     * constructor has default (package) access.
     *
     * @return a string describing this {@code Constructor},
     * include type parameters
     *
     * @since 1.5
     */
    public String toGenericString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.constructorModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod) + " ");
            }
            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append("<");
                for(TypeVariable<?> typeparm: typeparms) {
                    if (!first)
                        sb.append(",");
                    // Class objects can't occur here; no need to test
                    // and call Class.getName().
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }
            sb.append(Field.getTypeName(getDeclaringClass()));
            sb.append("(");
            Type[] params = getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                String param = (params[j] instanceof Class<?>)?
                    Field.getTypeName((Class<?>)params[j]):
                    (params[j].toString());
                if (isVarArgs() && (j == params.length - 1)) // replace T[] with T...
                    param = param.replaceFirst("\\[\\]$", "...");
                sb.append(param);
                if (j < (params.length - 1))
                    sb.append(",");
            }
            sb.append(")");
            Type[] exceptions = getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append((exceptions[k] instanceof Class)?
                              ((Class<?>)exceptions[k]).getName():
                              exceptions[k].toString());
                    if (k < (exceptions.length - 1))
                        sb.append(",");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    /**
     * Uses the constructor represented by this {@code Constructor} object to
     * create and initialize a new instance of the constructor's
     * declaring class, with the specified initialization parameters.
     * Individual parameters are automatically unwrapped to match
     * primitive formal parameters, and both primitive and reference
     * parameters are subject to method invocation conversions as necessary.
     *
     * <p>If the number of formal parameters required by the underlying constructor
     * is 0, the supplied {@code initargs} array may be of length 0 or null.
     *
     * <p>If the constructor's declaring class is an inner class in a
     * non-static context, the first argument to the constructor needs
     * to be the enclosing instance; see section 15.9.3 of
     * <cite>The Java&trade; Language Specification</cite>.
     *
     * <p>If the required access and argument checks succeed and the
     * instantiation will proceed, the constructor's declaring class
     * is initialized if it has not already been initialized.
     *
     * <p>If the constructor completes normally, returns the newly
     * created and initialized instance.
     *
     * @param initargs array of objects to be passed as arguments to
     * the constructor call; values of primitive types are wrapped in
     * a wrapper object of the appropriate type (e.g. a {@code float}
     * in a {@link java.lang.Float Float})
     *
     * @return a new object created by calling the constructor
     * this object represents
     *
     * @exception IllegalAccessException    if this {@code Constructor} object
     *              is enforcing Java language access control and the underlying
     *              constructor is inaccessible.
     * @exception IllegalArgumentException  if the number of actual
     *              and formal parameters differ; if an unwrapping
     *              conversion for primitive arguments fails; or if,
     *              after possible unwrapping, a parameter value
     *              cannot be converted to the corresponding formal
     *              parameter type by a method invocation conversion; if
     *              this constructor pertains to an enum type.
     * @exception InstantiationException    if the class that declares the
     *              underlying constructor represents an abstract class.
     * @exception InvocationTargetException if the underlying constructor
     *              throws an exception.
     * @exception ExceptionInInitializerError if the initialization provoked
     *              by this method fails.
     */
    public T newInstance(Object ... initargs)
        throws InstantiationException, IllegalAccessException,
               IllegalArgumentException, InvocationTargetException
    {
        Class[] types = getParameterTypes();
        if (types.length != initargs.length) {
            throw new IllegalArgumentException("Types len " + types.length + " args: " + initargs.length);
        } else {
            initargs = initargs.clone();
            for (int i = 0; i < types.length; i++) {
                Class c = types[i];
                if (c.isPrimitive() && initargs[i] != null) {
                    initargs[i] = Method.toPrimitive(initargs[i]);
                }
            }
        }
        return (T) newInstance0(this.getDeclaringClass(), "cons__" + sig, initargs);
    }

    @JavaScriptBody(args = { "self", "sig", "args" }, body =
          "\nvar c = self.cnstr;"
        + "\nvar inst = c();"
        + "\nc[sig].apply(inst, args);"
        + "\nreturn inst;"
    )
    private static native Object newInstance0(Class<?> self, String sig, Object[] args);
    
    /**
     * Returns {@code true} if this constructor was declared to take
     * a variable number of arguments; returns {@code false}
     * otherwise.
     *
     * @return {@code true} if an only if this constructor was declared to
     * take a variable number of arguments.
     * @since 1.5
     */
    public boolean isVarArgs() {
        return (getModifiers() & Modifier.VARARGS) != 0;
    }

    /**
     * Returns {@code true} if this constructor is a synthetic
     * constructor; returns {@code false} otherwise.
     *
     * @return true if and only if this constructor is a synthetic
     * constructor as defined by
     * <cite>The Java&trade; Language Specification</cite>.
     * @since 1.5
     */
    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     * @since 1.5
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == null)
            throw new NullPointerException();

        return null; // XXX (T) declaredAnnotations().get(annotationClass);
    }

    /**
     * @since 1.5
     */
    public Annotation[] getDeclaredAnnotations()  {
        return new Annotation[0]; // XXX AnnotationParser.toArray(declaredAnnotations());
    }

    /**
     * Returns an array of arrays that represent the annotations on the formal
     * parameters, in declaration order, of the method represented by
     * this {@code Constructor} object. (Returns an array of length zero if the
     * underlying method is parameterless.  If the method has one or more
     * parameters, a nested array of length zero is returned for each parameter
     * with no annotations.) The annotation objects contained in the returned
     * arrays are serializable.  The caller of this method is free to modify
     * the returned arrays; it will have no effect on the arrays returned to
     * other callers.
     *
     * @return an array of arrays that represent the annotations on the formal
     *    parameters, in declaration order, of the method represented by this
     *    Constructor object
     * @since 1.5
     */
    public Annotation[][] getParameterAnnotations() {
//        int numParameters = parameterTypes.length;
//        if (parameterAnnotations == null)
//            return new Annotation[numParameters][0];
        
        return new Annotation[0][0]; // XXX
/*
        Annotation[][] result = AnnotationParser.parseParameterAnnotations(
            parameterAnnotations,
            sun.misc.SharedSecrets.getJavaLangAccess().
                getConstantPool(getDeclaringClass()),
            getDeclaringClass());
        if (result.length != numParameters) {
            Class<?> declaringClass = getDeclaringClass();
            if (declaringClass.isEnum() ||
                declaringClass.isAnonymousClass() ||
                declaringClass.isLocalClass() )
                ; // Can't do reliable parameter counting
            else {
                if (!declaringClass.isMemberClass() || // top-level
                    // Check for the enclosing instance parameter for
                    // non-static member classes
                    (declaringClass.isMemberClass() &&
                     ((declaringClass.getModifiers() & Modifier.STATIC) == 0)  &&
                     result.length + 1 != numParameters) ) {
                    throw new AnnotationFormatError(
                              "Parameter annotations don't match number of parameters");
                }
            }
        }
        return result;
        */
    }
}
