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
import org.apidesign.vm4brwsr.ByteCodeParser.ClassData;
import org.apidesign.vm4brwsr.ByteCodeParser.FieldData;
import org.apidesign.vm4brwsr.ByteCodeParser.MethodData;

abstract class ObfuscationDelegate {
    static ObfuscationDelegate NULL =
            new ObfuscationDelegate() {
                @Override
                public void exportJSProperty(Appendable out,
                                             String destObject,
                                             String propertyName)
                            throws IOException {
                }

                @Override
                public void exportClass(Appendable out,
                                        String destObject,
                                        String mangledName,
                                        ClassData classData)
                                            throws IOException {
                }

                @Override
                public void exportMethod(Appendable out,
                                         String destObject,
                                         String mangledName,
                                         MethodData methodData)
                                             throws IOException {
                }

                @Override
                public void exportField(Appendable out,
                                        String destObject,
                                        String mangledName,
                                        FieldData fieldData)
                                            throws IOException {
                }
            };

    public abstract void exportJSProperty(
            Appendable out, String destObject, String propertyName)
                throws IOException;

    public abstract void exportClass(
            Appendable out, String destObject, String mangledName,
            ClassData classData) throws IOException;

    public abstract void exportMethod(
            Appendable out, String destObject, String mangledName,
            MethodData methodData) throws IOException;

    public abstract void exportField(
            Appendable out, String destObject, String mangledName,
            FieldData fieldData) throws IOException;
}
