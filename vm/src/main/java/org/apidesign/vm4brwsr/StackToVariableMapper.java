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

public final class StackToVariableMapper {
    private static final String VAR_NAME_PREFIX = "stack";

    private int stackSize;
    private StringBuilder varNameBuilder;

    private int maxStackSize;

    public StackToVariableMapper() {
        varNameBuilder = new StringBuilder(VAR_NAME_PREFIX);
    }

    public void reset(final int newStackSize) {
        stackSize = newStackSize;
        if (maxStackSize < stackSize) {
            maxStackSize = stackSize;
        }
    }

    public void push(final int numOfElements) {
        stackSize += numOfElements;
        if (maxStackSize < stackSize) {
            maxStackSize = stackSize;
        }
    }

    public String push() {
        push(1);
        return get(0);
    }

    public void pop(final int numOfElements) {
        if (numOfElements > stackSize) {
            throw new IllegalStateException("Stack underflow");
        }
        stackSize -= numOfElements;
    }

    public String pop() {
        final String variableName = get(0);
        pop(1);
        return variableName;
    }

    public String get(final int indexFromTop) {
        if (indexFromTop >= stackSize) {
            throw new IllegalStateException("Stack underflow");
        }

        return constructVariableName(stackSize - indexFromTop - 1);
    }

    public String top() {
        return get(0);
    }

    public String bottom() {
        if (stackSize == 0) {
            throw new IllegalStateException("Stack underflow");
        }

        return constructVariableName(0);
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public String constructVariableName(final int index) {
        varNameBuilder.setLength(VAR_NAME_PREFIX.length());
        varNameBuilder.append(index);
        return varNameBuilder.toString();
    }
}
