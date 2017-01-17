/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
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

    @Compare public boolean valueCanBeCleared() {
        String one = LOWER.get(Runnable.class);
        LOWER.remove(Runnable.class);
        String two = LOWER.get(Runnable.class);
        return one != two;
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

    @Factory
    public static Object[] create() {
        return VMTest.create(ReflectionClassValueTest.class);
    }
}
