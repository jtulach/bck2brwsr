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
package org.apidesign.vm4brwsr;

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

    @Override
    public String toString() {
        return HELLO + cnt;
    }
}
