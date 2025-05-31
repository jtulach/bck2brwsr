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

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach
 */
public class StaticMethod {
    public static final int MISSING_CONSTANT = 1;
    private static int cnt;
    private static Object NULL;

    public static int minusOne() {
        return -1;
    }
    
    public static Object none(int x, int y) {
        Object toRet = null;
        for (int i = x; i < y; i++) {
            if (i == 2) {
                toRet = null;
            } else {
                toRet = new Object();
            }
        }
        return toRet;
    }
    
    public static boolean isNull() {
        return NULL == null;
    }
    
    public static int sum(int x, int y) {
        return x + y;
    }
    public static float power(float x) {
        return x * x;
    }
    public static double minus(double x, long y) {
        return x - y;
    }
    public static int div(byte c, double d) {
        return (int)(d / c);
    }
    public static int mix(int a, long b, byte c, double d) {
        return (int)((b / a + c) * d);
    }
    public static long xor(int a, long b) {
        return a ^ b;
    }
    public static long orOrAnd(boolean doOr, int a, int b) {
        return doOr ? a | b : a & b;
    }
    public static int shiftLeft(int what, int much) {
        return what << much;
    }
    public static int shiftArithmRight(int what, int much, boolean signed) {
        if (signed) {
            return what >> much;
        } else {
            return what >>> much;
        }
    }
    public static long factRec(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return n * factRec(n - MISSING_CONSTANT);
        }
    }
    public static long factIter(int n) {
        long res = 1;
        for (int i = 2; i <= n; i++) {
            res *= i;
        }
        return res;
    }
    public static int inc4() {
        cnt++;
        cnt+=2;
        cnt++;
        return cnt;
    }
    
    public static int helloWorldLength(String x) {
        return (StringSample.HELLO + x).length();
    }
    
    @JavaScriptBody(
        args={"i","j"}, body="\n\r\treturn (i + j).toString();"
    )
    public static String i2s(int i, int j) {
        throw new IllegalStateException();
    }
    
    public static String castNull(boolean n) {
        Object value = n ? null : "Ahoj";
        return (String)value;
    }
    
    public static String swtch(int what) {
        switch (what) {
            case 0: return "Jarda";
            case 1: return "Darda";
            case 2: return "Parda";
            default: return "Marda";
        }
    }
    public static String swtch2(int what) {
        switch (what) {
            case 0: return "Jarda";
            case 11: return "Darda";
            case 22: return "Parda";
            default: return "Marda";
        }
    }
    
    public static int castString(Object o) {
        return ((String)o).length();
    }
    
    public static int initInflater(int w, boolean nowrap) {
        Instance i = new Instance(w, 0.0);
        return i.sum(nowrap?-w:w, 1);
    }
    
    public static int intHolder() {
        return new IntHolder(new Integer(10)).i.intValue();
    }

    private static class IntHolder {
        Integer i;

        public IntHolder(Integer i) {
            this.i = i;
        }
    }
    
    public static String toStringArr() {
        class N implements Next {
            int idx = 0;
            
            @Override
            public boolean hasNext() {
                return idx < 5;
            }

            @Override
            public String next() {
                switch (idx++) {
                    case 0: return "Zero";
                    case 1: return "One";
                    case 2: return "Two";
                    case 3: return "Three";
                    case 4: return "Four";
                }
                throw new IllegalStateException();
            }
        }
        return toString(null, new N()).toString();
    }
    
    static String toString(Object thiz, Next it) {
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            String e = it.next();
            sb.append(e == thiz ? "(this Collection)" : e);
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }
    
    static interface Next {
        boolean hasNext();
        String next();
    }
    
    
    static {
        // check order of initializers
        StaticUse.NON_NULL.equals(new Object());
    }
}
