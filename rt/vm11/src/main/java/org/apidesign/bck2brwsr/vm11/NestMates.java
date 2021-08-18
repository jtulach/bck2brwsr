/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.vm11;

public class NestMates {
    NestMates() {
    }

    public static void main(String... args) {
    }

    public static String privateMessage() {
        Subclass bar = new Subclass();
        return Nested.findPrivate(bar);
    }

    public static String protectedMessage() {
        Subclass bar = new Subclass();
        return Nested.findProtected(bar);
    }

    static class Nested {
        public static String findPrivate(NestMates f) {
            return f.messagePrivate();
        }
        public static String findProtected(NestMates f) {
            return f.messageProtected();
        }
    }
    
    protected String messageProtected() {
        return messagePrivate();
    }

    private String messagePrivate() {
        return "NestMates";
    }
}

class Subclass extends NestMates {
    private String messagePrivate() {
        return "Subclass";
    }

    @Override
    protected String messageProtected() {
        return messagePrivate();
    }
}
