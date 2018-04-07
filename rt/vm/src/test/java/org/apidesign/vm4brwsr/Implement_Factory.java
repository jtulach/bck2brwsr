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

import org.apidesign.vm4brwsr.under_score.ImplementInterface;
import org.apidesign.vm4brwsr.under_score.ImportedField;

/**
 *
 * @author Jaroslav Tulach
 */
public class Implement_Factory {
    private Implement_Factory() {
    }
    
    private static ImplementInterface create() {
        return new Impl();
    }
    
    public static String hello() {
        ImplementInterface i = create();
        return i.sayHello();
    }
    
    public static int meaning() {
        return ImportedField.Factory.create(42).x;
    }

    private static class Impl implements ImplementInterface {

        public Impl() {
        }

        @Override
        public String sayHello() {
            return "Hello!";
        }
    }
}
