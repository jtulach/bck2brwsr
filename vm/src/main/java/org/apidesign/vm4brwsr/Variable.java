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

public final class Variable implements CharSequence {
    public static final int TYPE_INT = 0;
    public static final int TYPE_LONG = 1;
    public static final int TYPE_FLOAT = 2;
    public static final int TYPE_DOUBLE = 3;
    public static final int TYPE_REF = 4;

    public static final int LAST_TYPE = TYPE_REF;

    private static final String STACK_VAR_PREFIX = "st";

    private final String name;
    private final int type;
    private final int index;

    private static final char[] TYPE_IDS = { 'I', 'L', 'F', 'D', 'A' };

    private Variable(final String prefix, final int type, final int index) {
        this.name = prefix + TYPE_IDS[type] + index;
        this.type = type;
        this.index = index;
    }

    public static Variable getStackVariable(
            final int type, final int index) {
        // TODO: precreate frequently used variables
        return new Variable(STACK_VAR_PREFIX, type, index);
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public boolean isCategory2() {
        return (type == TYPE_LONG) || (type == TYPE_DOUBLE);
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt(final int index) {
        return name.charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return name.subSequence(start, end);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Variable)) {
            return false;
        }

        return name.equals(((Variable) other).name);
    }

    @Override
    public String toString() {
        return name;
    }
}
