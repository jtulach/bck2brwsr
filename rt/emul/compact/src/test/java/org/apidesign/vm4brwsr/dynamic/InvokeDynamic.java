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
package org.apidesign.vm4brwsr.dynamic;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class InvokeDynamic {

    public static String dynamicSay() {
        return TEST_dynamic_boot1(new InvokeDynamic());
    }

    private static String TEST_dynamic_boot1(InvokeDynamic instance) {
        throw new IllegalStateException("Can't touch this");
    }

    public static CallSite boot1(MethodHandles.Lookup lookup, String name, MethodType type) {
        try {
            return new ConstantCallSite(lookup.findVirtual(InvokeDynamic.class, "instance_sayHello", MethodType.methodType(String.class)));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public String instance_sayHello() {
        return "Hello from Dynamic!";
    }
}