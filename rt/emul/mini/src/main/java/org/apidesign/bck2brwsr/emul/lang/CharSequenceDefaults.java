/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2021 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.emul.lang;

import java.util.ServiceLoader;
import java.util.stream.IntStream;

public abstract class CharSequenceDefaults {
    private static final CharSequenceDefaults DEFAULT;
    static {
        CharSequenceDefaults impl = null;
        try {
            impl = ServiceLoader.load(CharSequenceDefaults.class).iterator().next();
        } catch (Throwable t) {
            System.printStackTrace("Cannot initialize CharSequence defaults!");
        }
        DEFAULT = impl;
    }

    public static CharSequenceDefaults getDefault() {
        return DEFAULT;
    }

    public abstract IntStream chars(CharSequence s);
    public abstract IntStream codePoints(CharSequence s);
}
