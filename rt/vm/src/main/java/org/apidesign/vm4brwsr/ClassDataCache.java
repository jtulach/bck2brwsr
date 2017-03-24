/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;
import org.apidesign.vm4brwsr.ByteCodeParser.FieldData;
import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;

@ExtraJavaScript(processByteCode = false, resource="")
final class ClassDataCache {
    private static final Object MISSING_CLASS = new Object();

    private final Bck2Brwsr.Resources resources;
    private final Map<String, Object> classDataMap;

    ClassDataCache(final Bck2Brwsr.Resources resources) {
        this.resources = resources;

        classDataMap = new HashMap<String, Object>();
    }

    ClassData getClassData(String className) throws IOException {
        if (className.startsWith("[")) {
            // required for accessVirtualMethod, shouldn't be problematic for
            // calls from other sources
            className = "java/lang/Object";
        }
        Object cacheEntry = classDataMap.get(className);
        if (cacheEntry == null) {
            final InputStream is = loadClass(resources, className);
            cacheEntry = (is != null) ? new ClassData(is) : MISSING_CLASS;
            classDataMap.put(className, cacheEntry);
        }

        return (cacheEntry != MISSING_CLASS) ? (ClassData) cacheEntry : null;
    }

    MethodData findMethod(final String startingClass,
                          final String name,
                          final String signature) throws IOException {
        return findMethod(getClassData(startingClass), name, signature);
    }

    FieldData findField(final String startingClass,
                        final String name,
                        final String signature) throws IOException {
        return findField(getClassData(startingClass), name, signature);
    }

    MethodData findMethod(final ClassData startingClass,
                          final String name,
                          final String signature) throws IOException {
        final FindFirstTraversalCallback<MethodData> ffTraversalCallback =
                new FindFirstTraversalCallback<MethodData>();

        findMethods(startingClass, name, signature, ffTraversalCallback);
        return ffTraversalCallback.getFirst();
    }

    FieldData findField(final ClassData startingClass,
                        final String name,
                        final String signature) throws IOException {
        final FindFirstTraversalCallback<FieldData> ffTraversalCallback =
                new FindFirstTraversalCallback<FieldData>();

        findFields(startingClass, name, signature, ffTraversalCallback);
        return ffTraversalCallback.getFirst();
    }

    void findMethods(final ClassData startingClass,
                     final String methodName,
                     final String methodSignature,
                     final TraversalCallback<MethodData> mdTraversalCallback)
                             throws IOException {
        traverseHierarchy(
                startingClass,
                new FindMethodsTraversalCallback(methodName, methodSignature,
                                                 mdTraversalCallback));
    }

    void findFields(final ClassData startingClass,
                    final String fieldName,
                    final String fieldSignature,
                    final TraversalCallback<FieldData> fdTraversalCallback)
                            throws IOException {
        traverseHierarchy(
                startingClass,
                new FindFieldsTraversalCallback(fieldName, fieldSignature,
                                                fdTraversalCallback));
    }

    private boolean traverseHierarchy(
            ClassData currentClass,
            final TraversalCallback<ClassData> cdTraversalCallback)
                throws IOException {
        while (currentClass != null) {
            if (!cdTraversalCallback.traverse(currentClass)) {
                return false;
            }

            for (final String superIfaceName:
                    currentClass.getSuperInterfaces()) {
                if (!traverseHierarchy(getClassData(superIfaceName),
                                       cdTraversalCallback)) {
                    return false;
                }
            }

            final String superClassName = currentClass.getSuperClassName();
            if (superClassName == null) {
                break;
            }

            currentClass = getClassData(superClassName);
        }

        return true;
    }

    interface TraversalCallback<T> {
        boolean traverse(T object);
    }

    private final class FindFirstTraversalCallback<T>
            implements TraversalCallback<T> {
        private T firstObject;

        @Override
        public boolean traverse(final T object) {
            firstObject = object;
            return false;
        }

        public T getFirst() {
            return firstObject;
        }
    }

    private final class FindMethodsTraversalCallback
            implements TraversalCallback<ClassData> {
        private final String methodName;
        private final String methodSignature;
        private final TraversalCallback<MethodData> mdTraversalCallback;

        public FindMethodsTraversalCallback(
                final String methodName,
                final String methodSignature,
                final TraversalCallback<MethodData> mdTraversalCallback) {
            this.methodName = methodName;
            this.methodSignature = methodSignature;
            this.mdTraversalCallback = mdTraversalCallback;
        }

        @Override
        public boolean traverse(final ClassData classData) {
            final MethodData methodData =
                    classData.findMethod(methodName, methodSignature);
            return (methodData != null)
                       ? mdTraversalCallback.traverse(methodData)
                       : true;
        }
    }

    private final class FindFieldsTraversalCallback
            implements TraversalCallback<ClassData> {
        private final String fieldName;
        private final String fieldSignature;
        private final TraversalCallback<FieldData> fdTraversalCallback;

        public FindFieldsTraversalCallback(
                final String fieldName,
                final String fieldSignature,
                final TraversalCallback<FieldData> fdTraversalCallback) {
            this.fieldName = fieldName;
            this.fieldSignature = fieldSignature;
            this.fdTraversalCallback = fdTraversalCallback;
        }

        @Override
        public boolean traverse(final ClassData classData) {
            final FieldData fieldData =
                    classData.findField(fieldName, fieldSignature);
            return (fieldData != null)
                       ? fdTraversalCallback.traverse(fieldData)
                       : true;
        }
    }

    private static InputStream loadClass(Bck2Brwsr.Resources l, String name)
            throws IOException {
        return l.get(name + ".class"); // NOI18N
    }
}
