/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

    MethodData findMethod(ClassData currentClass,
                          final String name,
                          final String signature) throws IOException {
        while (currentClass != null) {
            final MethodData methodData =
                    currentClass.findMethod(name, signature);
            if (methodData != null) {
                return methodData;
            }

            final String superClassName = currentClass.getSuperClassName();
            if (superClassName == null) {
                break;
            }
            currentClass = getClassData(superClassName);
        }

        return null;
    }

    FieldData findField(ClassData currentClass,
                        final String name,
                        final String signature) throws IOException {
        while (currentClass != null) {
            final FieldData fieldData =
                    currentClass.findField(name, signature);
            if (fieldData != null) {
                return fieldData;
            }

            final String superClassName = currentClass.getSuperClassName();
            if (superClassName == null) {
                break;
            }
            currentClass = getClassData(superClassName);
        }

        return null;
    }

    private static InputStream loadClass(Bck2Brwsr.Resources l, String name)
            throws IOException {
        return l.get(name + ".class"); // NOI18N
    }
}
