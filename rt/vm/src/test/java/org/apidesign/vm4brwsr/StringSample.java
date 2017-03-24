/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StringSample {
    public static final String HELLO = "Hello World!";
    private static int counter;
    
    private final int cnt;
    public StringSample() {
        cnt = ++counter;
    }
    
    
    public static char sayHello(int indx) {
        return HELLO.charAt(indx);
    }
    
    public static boolean equalToHello(int from, int to) {
        return "Hello".equals(HELLO.substring(from, to));
    }
    
    public static String fromChars(char a, char b, char c) {
        char[] arr = { a, b, c };
        return new String(arr).toString();
    }
    
    public static String charsFromNumbers() {
        return chars((char)65, (char)66, (char)67);
    }

    public static String charsFromChars() {
        return chars('A', 'B', 'C');
    }

    public static String chars(char a, char b, char c) {
        return ("" + a + b +c).toString();
    }
    
    public static String replace(String s, char a, char b) {
        return s.replace(a, b);
    }
    
    public static int hashCode(String h) {
        return h.hashCode();
    }
    
    public static boolean isStringInstance() {
        return chars('a', (char)30, 'b') instanceof String;
    }
    
    public static String getBytes(String s) throws UnsupportedEncodingException {
        byte[] arr = s.getBytes("UTF-8");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]).append(" ");
        }
        return sb.toString().toString();
    }
    
    public static String unicode() {
        return "\r\n\u2028\u2029]";
    }
    
    public static String insertBuffer() {
        StringBuilder sb = new StringBuilder();
        sb.append("Jardo!");
        sb.insert(0, "Ahoj ");
        sb.delete(4, 8);
        return sb.toString().toString();
    }
    
    public static int countAB(String txt) {
        int cnt = 0;
        for (int i = 0; i < txt.length(); i++) {
            switch (txt.charAt(i)) {
                case 'A': cnt++; break;
                case 'B': cnt += 2; break;
            }
        }
        return cnt;
    }

    public static int stringSwitch(String txt) {
        switch (txt) {
            case "jedna": return 1;
            case "dve": return 2;
            case "tri": return 3;
            case "ctyri": return 4;
        }
        return -1;
    }
    
    public static String toStringTest(int howMuch) {
        counter = 0;
        StringSample ss = null;
        for (int i = 0; i < howMuch; i++) {
            ss = new StringSample();
        }
        return ss.toString().toString();
    }
    
    public static String concatStrings() {
        return (toStringTest(1) + "\\\n\r\t").toString();
    }
    
    public static int compare(String a, String b) {
        return a.compareTo(b);
    }

    @Override
    public String toString() {
        return HELLO + cnt;
    }
    
    @JavaScriptBody(args = {}, body = "return [1, 2];")
    private static native Object crtarr();
    @JavaScriptBody(args = { "o" }, body = "return o.toString();")
    private static native String toStrng(Object o);
    
    public static String toStringArray(boolean fakeArr, boolean toString) {
        final Object arr = fakeArr ? crtarr() : new Object[2];
        final String whole = toString ? arr.toString() : toStrng(arr);
        int zav = whole.indexOf('@');
        if (zav <= 0) {
            zav = whole.length();
        }
        return whole.substring(0, zav).toString().toString();
    }
    
}
