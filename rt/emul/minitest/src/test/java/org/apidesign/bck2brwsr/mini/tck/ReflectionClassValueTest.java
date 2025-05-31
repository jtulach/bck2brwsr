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
package org.apidesign.bck2brwsr.mini.tck;

import java.io.Serializable;
import java.lang.ClassValue;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class ReflectionClassValueTest {
    private static final class StringValue extends ClassValue<String> {
        private final boolean upper;

        public StringValue(boolean upper) {
            this.upper = upper;
        }

        @Override
        protected String computeValue(Class<?> type) {
            final String str = new String(type.toString());
            return upper ? str.toUpperCase() : str.toLowerCase();
        }
    }
    private static final StringValue UPPER = new StringValue(true);
    private static final StringValue LOWER = new StringValue(false);

    @Compare public String upperRunnable() {
        return UPPER.get(Runnable.class);
    }

    @Compare public String lowerRunnable() {
        return LOWER.get(Runnable.class);
    }
    
    @Compare public boolean valueIsCached() {
        String one = LOWER.get(Runnable.class);
        String two = LOWER.get(Runnable.class);
        return one == two;
    }

    @Compare public String upperObject() {
        return UPPER.get(Object.class);
    }

    @Compare public String lowerObject() {
        return LOWER.get(Object.class);
    }

    @Compare public String upperLowerString() {
        return UPPER.get(String.class) + LOWER.get(String.class);
    }

    @Compare public String lowerUpperSeq() {
        return LOWER.get(CharSequence.class) + UPPER.get(CharSequence.class);
    }

    private static final class CountingNull extends ClassValue<Object> {
        int cnt;

        @Override
        protected Object computeValue(Class<?> type) {
            cnt++;
            return null;
        }
    }

    @Compare public int getNullThreeTimes() {
        CountingNull counter = new CountingNull();
        Object o1 = counter.get(Serializable.class);
        Object o2 = counter.get(Serializable.class);
        Object o3 = counter.get(Serializable.class);
        assert o1 == null;
        assert o2 == null;
        assert o3 == null;
        return counter.cnt;
    }

    private static final class NewObj extends ClassValue<Integer> {
        int cnt;

        @Override
        protected Integer computeValue(Class<?> type) {
            cnt++;
            return new Integer(cnt);
        }
    }

    @Compare public boolean valueCanBeCleared() {
        NewObj cache = new NewObj();
        Integer one = cache.get(Runnable.class);
        Integer two = cache.get(Runnable.class);
        assert one == two;
        cache.remove(Runnable.class);
        Integer three = cache.get(Runnable.class);
        return one != three;
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(ReflectionClassValueTest.class);
    }
}
