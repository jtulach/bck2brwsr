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
package org.apidesign.vm4brwsr;

import java.io.UnsupportedEncodingException;

public abstract class Base64Convert {
    private Base64Convert() {
    }

    public abstract Object convert(String s) throws UnsupportedEncodingException;

    public static Base64Convert create() {
        return new Impl8();
    }

    private static final class Impl8 extends Base64Convert {
        @Override
        public Object convert(String s) throws UnsupportedEncodingException {
            return org.apidesign.vm4brwsr.ResourcesTest.parseBase64Binary(s);
        }
    }
}
