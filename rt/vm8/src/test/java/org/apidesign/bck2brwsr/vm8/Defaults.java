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
package org.apidesign.bck2brwsr.vm8;

public interface Defaults {
    public static int staticValue() {
        return 42;
    }
    
    public default int value() {
        return 42;
    }
    
    public static Defaults create(int type) {
        class X implements Defaults {
        }
        class Y implements Defaults {
            @Override
            public int value() {
                return 7;
            }
        }
        class Z implements DoubleDefaults {
        }
        switch (type) {
            case 0: return new X();
            case 1: return new Y();
            default: return new Z();
        }
    }
    
    public static int defaultValue() {
        return create(0).value();
    }
    
    public static int myValue() {
        return create(1).value();
    }

    public static int sndValue() {
        return create(2).value();
    }
    
    public interface DoubleDefaults extends Defaults {
        @Override
        public default int value() {
            return 84;
        }
    }
}
