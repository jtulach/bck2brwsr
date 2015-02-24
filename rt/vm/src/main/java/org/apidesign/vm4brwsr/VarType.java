/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

final class VarType {
    public static final int INTEGER = 0;
    public static final int LONG = 1;
    public static final int FLOAT = 2;
    public static final int DOUBLE = 3;
    public static final int REFERENCE = 4;

    public static final int LAST = REFERENCE;

    private VarType() {
    }

    public static boolean isCategory2(final int varType) {
        return (varType == LONG) || (varType == DOUBLE);
    }

    public static int fromStackMapType(final int smType) {
        switch (smType & 0xff) {
            case ByteCodeParser.ITEM_Integer:
                return VarType.INTEGER;
            case ByteCodeParser.ITEM_Float:
                return VarType.FLOAT;
            case ByteCodeParser.ITEM_Double:
                return VarType.DOUBLE;
            case ByteCodeParser.ITEM_Long:
                return VarType.LONG;
            case ByteCodeParser.ITEM_Null:
            case ByteCodeParser.ITEM_InitObject:
            case ByteCodeParser.ITEM_Object:
            case ByteCodeParser.ITEM_NewObject:
                return VarType.REFERENCE;

            case ByteCodeParser.ITEM_Bogus:
                /* unclear how to handle for now */
            default:
                throw new IllegalStateException("Unhandled stack map type");
        }
    }

    public static int fromConstantType(final byte constantTag) {
        switch (constantTag) {
            case ByteCodeParser.CONSTANT_INTEGER:
                return VarType.INTEGER;
            case ByteCodeParser.CONSTANT_FLOAT:
                return VarType.FLOAT;
            case ByteCodeParser.CONSTANT_LONG:
                return VarType.LONG;
            case ByteCodeParser.CONSTANT_DOUBLE:
                return VarType.DOUBLE;

            case ByteCodeParser.CONSTANT_CLASS:
            case ByteCodeParser.CONSTANT_UTF8:
            case ByteCodeParser.CONSTANT_UNICODE:
            case ByteCodeParser.CONSTANT_STRING:
                return VarType.REFERENCE;

            case ByteCodeParser.CONSTANT_FIELD:
            case ByteCodeParser.CONSTANT_METHOD:
            case ByteCodeParser.CONSTANT_INTERFACEMETHOD:
            case ByteCodeParser.CONSTANT_NAMEANDTYPE:
                /* unclear how to handle for now */
            default:
                throw new IllegalStateException("Unhandled constant tag");
        }
    }

    public static int fromFieldType(final char fieldType) {
        switch (fieldType) {
            case 'B':
            case 'C':
            case 'S':
            case 'Z':
            case 'I':
                return VarType.INTEGER;
            case 'J':
                return VarType.LONG;
            case 'F':
                return VarType.FLOAT;
            case 'D':
                return VarType.DOUBLE;
            case 'L':
            case '[':
                return VarType.REFERENCE;

            default:
                throw new IllegalStateException("Unhandled field type");
        }
    }
}
