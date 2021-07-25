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

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;


public class JFXIssuesTest {
    private abstract class Application {
        public abstract int getID();
    }

    private class MyApplication extends Application {

        @Override
        public int getID() {
            return 1;
        }

    }

    @Compare public boolean isClassAssignable() {
        return Application.class.isAssignableFrom(MyApplication.class);
    }

    @Compare public boolean isNaN() {
        return Double.isNaN(Double.NaN);
    }

    @Compare public boolean isInfinite() {
        return Float.isInfinite(Float.NEGATIVE_INFINITY);
    }

    @Compare public boolean areTimesEqual() {
        long l1 = System.currentTimeMillis();
        long l2 = l1 + 0;

        return l1 == l2;
    }

    @Compare public boolean roundOnDouble() {
        long l1 = Math.round(System.currentTimeMillis() / 1.1);
        long l2 = l1 + 0;

        return l1 == l2;
    }

    private static final long val = 1238078409318L;

    @Compare public int valueConvertedToString() {
        long[] arr = { val };
        return dumpValue(arr);
    }

    int dumpValue(long[] val) {
        return (int) val[0]++;
    }


    @Compare public boolean roundOnFloat() {
        final float f = System.currentTimeMillis() / 1.1f;
        int l1 = Math.round(f);
        int l2 = l1 + 0;

        assert l1 == l2 : "Round " + l1 + " == " + l2;

        return l1 == l2;
    }


    @Compare public boolean sysprops() {
        System.setProperty("foo", "bar");
        String p = System.getProperty("foo", "foobar");
        return "bar".equals(p);
    }

    public String concatBackslash(String a, String b) {
        return a + "\\" + b;
    }

    @Compare public boolean doubleBackslash() {
        String a = "a";
        String b = "b";
        return "a\\b".equals(concatBackslash(a,b));
    }

    public static final class KeyEvent {
        public static final int LEFT = 0;

    }
    
    @FunctionalInterface
    interface EventHandler<T> {
        void handle(KeyEvent ev);
    }
    
    class Circle {
 
        public Circle() {
            initialize();
        }

        public final void setOnKeyPressed(
            EventHandler<? super KeyEvent> value) {
        }

        private void processNoKey(Object a) {
        }

        private void initialize() {
            setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent ke) {
                    processNoKey(null);
                }
            });
        }
    }

    @Compare
    public String missingComma() {
        Circle c = new Circle();
        return "CompiledOK";
    }

    class Foo_Bar {
        public Foo_Bar() {}
    }

    @Compare
    public boolean underscoreClass() {
        int err = 0;
        try {
            Class answer = Class.forName("org.apidesign.bck2brwsr.vm11.JFXIssuesTest$Foo_Bar");
        } catch (ClassNotFoundException ex) {
            err = 1;
        }
        return err == 0;
    }

    @Factory public static Object[] create() {
        return VMTest.create(JFXIssuesTest.class);
    }
}
