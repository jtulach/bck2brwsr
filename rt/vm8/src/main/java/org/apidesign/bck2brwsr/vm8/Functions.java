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
package org.apidesign.bck2brwsr.vm8;

public class Functions {
    public interface SimpleOne<P1, P2, P3, P4, R> extends BaseOne<P1, P2, P3, P4, java.lang.Object, R> {
        public R invoke(P1 p1, P2 p2, P3 p3, P4 p4);

        @Override
        public default R invoke(P1 p1, P2 p2, P3 p3, P4 p4, java.lang.Object ignore) {
            return invoke(p1, p2, p3, p4);
        }
    }

    public interface BaseOne<P1, P2, P3, P4, P5, R> {
        public R invoke(P1 p1, P2 p2, P3 p3, P4 p4, P5 p5);
    }

    public static BaseOne<Void, Void, Void, Void, Object, Integer> inner5() {
        return new BaseOne<Void, Void, Void, Void, Object, Integer>() {
            @Override
            public Integer invoke(Void p1, Void p2, Void p3, Void p4, Object p5) {
                return 42;
            }
        };
    }

    public static BaseOne<Void, Void, Void, Void, Object, Integer> inner4() {
        return new SimpleOne<Void, Void, Void, Void, Integer>() {
            @Override
            public Integer invoke(Void p1, Void p2, Void p3, Void p4) {
                return 42;
            }
        };
    }
    public static BaseOne<Void, Void, Void, Void, Object, Integer> function5() {
        return (Void p1, Void p2, Void p3, Void p4, Object p5) -> 42;
    }

    public static BaseOne<Void, Void, Void, Void, Object, Integer> function4() {
        return function4impl();
    }

    private static SimpleOne<Void, Void, Void, Void, Integer> function4impl() {
        return (Void p1, Void p2, Void p3, Void p4) -> 42;
    }
}
