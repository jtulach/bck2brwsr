/*
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
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
package org.apidesign.javap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/** An abstract parser for annotation definitions. Analyses the bytes and
 * performs some callbacks to the overriden parser methods.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AnnotationParser {
    private final boolean textual;
    private final boolean iterateArray;
    
    protected AnnotationParser(boolean textual, boolean iterateArray) {
        this.textual = textual;
        this.iterateArray = iterateArray;
    }

    protected void visitAnnotationStart(String type) throws IOException {
    }

    protected void visitAnnotationEnd(String type) throws IOException {
    }

    protected void visitValueStart(String attrName, char type) throws IOException {
    }

    protected void visitValueEnd(String attrName, char type) throws IOException {
    }

    
    protected void visitAttr(
        String annoType, String attr, String attrType, String value
    ) throws IOException {
    }
    
    /** Initialize the parsing with constant pool from <code>cd</code>.
     * 
     * @param attr the attribute defining annotations
     * @param cd constant pool
     * @throws IOException in case I/O fails
     */
    public final void parse(byte[] attr, ClassData cd) throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(attr);
        DataInputStream dis = new DataInputStream(is);
        try {
            read(dis, cd);
        } finally {
            is.close();
        }
    }
    
    private void read(DataInputStream dis, ClassData cd) throws IOException {
    	int cnt = dis.readUnsignedShort();
        for (int i = 0; i < cnt; i++) {
            readAnno(dis, cd);
        }
    }

    private void readAnno(DataInputStream dis, ClassData cd) throws IOException {
        int type = dis.readUnsignedShort();
        String typeName = cd.StringValue(type);
        visitAnnotationStart(typeName);
    	int cnt = dis.readUnsignedShort();
    	for (int i = 0; i < cnt; i++) {
            String attrName = cd.StringValue(dis.readUnsignedShort());
            readValue(dis, cd, typeName, attrName);
        }
        visitAnnotationEnd(typeName);
        if (cnt == 0) {
            visitAttr(typeName, null, null, null);
        }
    }

    private void readValue(
        DataInputStream dis, ClassData cd, String typeName, String attrName
    ) throws IOException {
        char type = (char)dis.readByte();
        visitValueStart(attrName, type);
        if (type == '@') {
            readAnno(dis, cd);
        } else if ("CFJZsSIDB".indexOf(type) >= 0) { // NOI18N
            int primitive = dis.readUnsignedShort();
            String val = cd.stringValue(primitive, textual);
            String attrType;
            if (type == 's') {
                attrType = "Ljava_lang_String_2";
                if (textual) {
                    val = '"' + val + '"';
                }
            } else {
                attrType = "" + type;
            }
            visitAttr(typeName, attrName, attrType, val);
        } else if (type == 'c') {
            int cls = dis.readUnsignedShort();
        } else if (type == '[') {
            int cnt = dis.readUnsignedShort();
            for (int i = 0; i < cnt; i++) {
                readValue(dis, cd, typeName, iterateArray ? attrName : null);
            }
        } else if (type == 'e') {
            int enumT = dis.readUnsignedShort();
            String attrType = cd.stringValue(enumT, textual);
            int enumN = dis.readUnsignedShort();
            String val = cd.stringValue(enumN, textual);
            if (textual) {
                val = "vm." + attrType.substring(1, attrType.length() - 1).replace('/', '_') + "(false).constructor." + val;
            }
            visitAttr(typeName, attrName, attrType, val);
        } else {
            throw new IOException("Unknown type " + type);
        }
        visitValueEnd(attrName, type);
    }
}
