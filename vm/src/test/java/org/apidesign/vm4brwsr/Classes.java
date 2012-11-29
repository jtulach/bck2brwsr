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
import java.net.MalformedURLException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Classes {
    public static boolean equalsClassesOfExceptions() {
        return MalformedURLException.class.getSuperclass() == IOException.class;
    }
    public static boolean differenceInClasses() {
        Class<?> c1 = MalformedURLException.class;
        Class<?> c2 = IOException.class;
        return c1 != c2;
    }
    
    public static String name() {
        return IOException.class.getName();
    }
    public static String simpleName() {
        return IOException.class.getSimpleName();
    }
    public static String canonicalName() {
        return IOException.class.getCanonicalName();
    }
    public static IOException newInstance() throws InstantiationException, IllegalAccessException {
        return IOException.class.newInstance();
    }
}
