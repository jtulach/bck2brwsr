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
import org.apidesign.vm4brwsr.ByteCodeParser.AnnotationParser;
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;
import org.apidesign.vm4brwsr.ByteCodeParser.FieldData;
import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;

@ExtraJavaScript(processByteCode = false, resource="")
final class ExportedSymbols {
    private final Bck2Brwsr.Resources resources;
    private final StringArray exported;
    private final Map<Object, Boolean> isMarkedAsExportedCache;

    ExportedSymbols(final Bck2Brwsr.Resources resources, StringArray explicitlyExported) {
        this.resources = resources;
        this.exported = explicitlyExported;

        isMarkedAsExportedCache = new HashMap<Object, Boolean>();
    }

    boolean isExported(ClassData classData) throws IOException {
        if (exported.contains(classData.getClassName())) {
            return true;
        }
        return classData.isPublic() && isMarkedAsExportedPackage(
                                           classData.getPkgName())
                   || isMarkedAsExported(classData);
    }

    boolean isExported(MethodData methodData) throws IOException {
        return isAccessible(methodData.access) && isExported(methodData.cls)
                   || isMarkedAsExported(methodData);
    }

    boolean isExported(FieldData fieldData) throws IOException {
        if (
            isAccessible(fieldData.access) && 
            isExported(fieldData.cls) || isMarkedAsExported(fieldData)
        ) {
            return true;
        }
        if (
            fieldData.isStatic() && fieldData.getName().equals("$VALUES") &&
            "java/lang/Enum".equals(fieldData.cls.getSuperClassName())
        ) {
            // enum values need to be exported
            return true;
        }
        return false;
    }

    private boolean isMarkedAsExportedPackage(String pkgName) {
        if (pkgName == null) {
            return false;
        }
        if (pkgName.startsWith("java/")) {
            return true;
        }

        final Boolean cachedValue = isMarkedAsExportedCache.get(pkgName);
        if (cachedValue != null) {
            return cachedValue;
        }

        final boolean newValue = resolveIsMarkedAsExportedPackage(pkgName);
        isMarkedAsExportedCache.put(pkgName, newValue);

        return newValue;
    }

    private boolean isMarkedAsExported(ClassData classData)
            throws IOException {
        final Boolean cachedValue = isMarkedAsExportedCache.get(classData);
        if (cachedValue != null) {
            return cachedValue;
        }

        final boolean newValue =
                isMarkedAsExported(classData.findAnnotationData(true),
                                   classData);
        isMarkedAsExportedCache.put(classData, newValue);

        return newValue;
    }

    private boolean isMarkedAsExported(MethodData methodData)
            throws IOException {
        return isMarkedAsExported(methodData.findAnnotationData(true),
                                  methodData.cls);
    }

    private boolean isMarkedAsExported(FieldData fieldData)
            throws IOException {
        return isMarkedAsExported(fieldData.findAnnotationData(true),
                                  fieldData.cls);
    }

    private boolean resolveIsMarkedAsExportedPackage(String pkgName) {
        if (exported.contains(pkgName + '/')) {
            return true;
        }
        try {
            final InputStream is =
                    resources.get(pkgName + "/package-info.class");
            if (is == null) {
                return false;
            }

            try {
                final ClassData pkgInfoClass = new ClassData(is);
                return isMarkedAsExported(
                               pkgInfoClass.findAnnotationData(true),
                               pkgInfoClass);
            } finally {
                is.close();
            }
        } catch (final IOException e) {
            return false;
        }
    }

    static boolean isMarkedAsExported(byte[] arrData, ClassData cd)
            throws IOException {
        if (arrData == null) {
            return false;
        }

        final boolean[] found = { false };
        final AnnotationParser annotationParser =
                new AnnotationParser(false, false) {
                    @Override
                    protected void visitAnnotationStart(
                            String type,
                            boolean top) {
                        if (top && type.equals("Lorg/apidesign/bck2brwsr"
                                                   + "/core/Exported;")) {
                            found[0] = true;
                        }
                    }
                };
        annotationParser.parse(arrData, cd);
        return found[0];
    }

    private static boolean isAccessible(int access) {
        return (access & (ByteCodeParser.ACC_PUBLIC
                              | ByteCodeParser.ACC_PROTECTED)) != 0;
    }
}
