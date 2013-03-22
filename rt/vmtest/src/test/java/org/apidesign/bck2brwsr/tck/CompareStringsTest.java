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
package org.apidesign.bck2brwsr.tck;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CompareStringsTest {
    @Compare public String firstChar() {
        return "" + ("Hello".toCharArray()[0]);
    }
    
    @Compare public String classCast() {
        Object o = firstChar();
        return String.class.cast(o);
    }

    @Compare public String classCastThrown() {
        Object o = null;
        return String.class.cast(o);
    }
    
    @Compare public boolean equalToNull() {
        return "Ahoj".equals(null);
    }
    
    @Compare public int highByteLenght() {
        byte[] arr= { 77,97,110,105,102,101,115,116,45,86,101,114,115,105,111,110 };
        return new String(arr, 0).length();
    }
    
    @Compare public String highByte() {
        byte[] arr= { 77,97,110,105,102,101,115,116,45,86,101,114,115,105,111,110 };
        StringBuilder sb = new StringBuilder();
        sb.append("pref:");
        sb.append(new String(arr, 0));
        return sb.toString();
    }
    
    @Compare public static Object compareURLs() throws MalformedURLException {
        return new URL("http://apidesign.org:8080/wiki/").toExternalForm().toString();
    }
    
    @Compare public String deleteLastTwoCharacters() {
        StringBuilder sb = new StringBuilder();
        sb.append("453.0");
        if (sb.toString().endsWith(".0")) {
            final int l = sb.length();
            sb.delete(l - 2, l);
        }
        return sb.toString().toString();
    }
    
    @Compare public String nameOfStringClass() throws Exception {
        return Class.forName("java.lang.String").getName();
    }
    @Compare public String nameOfArrayClass() throws Exception {
        return Class.forName("org.apidesign.bck2brwsr.tck.CompareHashTest").getName();
    }
    
    @Compare public String lowerHello() {
        return "HeLlO".toLowerCase();
    }
    
    @Compare public String lowerA() {
        return String.valueOf(Character.toLowerCase('A')).toString();
    }
    @Compare public String upperHello() {
        return "hello".toUpperCase();
    }
    
    @Compare public String upperA() {
        return String.valueOf(Character.toUpperCase('a')).toString();
    }
    
    @Compare public boolean matchRegExp() throws Exception {
        return "58038503".matches("\\d*");
    }

    @Compare public boolean doesNotMatchRegExp() throws Exception {
        return "58038503GH".matches("\\d*");
    }

    @Compare public boolean doesNotMatchRegExpFully() throws Exception {
        return "Hello".matches("Hell");
    }
    
    @Compare public String emptyCharArray() {
        char[] arr = new char[10];
        return new String(arr);
    }
    
    @Compare public String variousCharacterTests() throws Exception {
        StringBuilder sb = new StringBuilder();
        
        sb.append(Character.isUpperCase('a'));
        sb.append(Character.isUpperCase('A'));
        sb.append(Character.isLowerCase('a'));
        sb.append(Character.isLowerCase('A'));
        
        sb.append(Character.isLetter('A'));
        sb.append(Character.isLetterOrDigit('9'));
        sb.append(Character.isLetterOrDigit('A'));
        sb.append(Character.isLetter('0'));
        
        return sb.toString().toString();
    }
        
    @Compare
    public String nullFieldInitialized() {
        NullField nf = new NullField();
        return ("" + nf.name).toString();
    }
    @Compare
    public String toUTFString() throws UnsupportedEncodingException {
        byte[] arr = {
            (byte) -59, (byte) -67, (byte) 108, (byte) 117, (byte) -59, (byte) -91,
            (byte) 111, (byte) 117, (byte) -60, (byte) -115, (byte) 107, (byte) -61,
            (byte) -67, (byte) 32, (byte) 107, (byte) -59, (byte) -81, (byte) -59,
            (byte) -120
        };
        return new String(arr, "utf-8");
    }

    @Compare
    public int stringToBytesLenght() throws UnsupportedEncodingException {
        return "\u017dlu\u0165ou\u010dk\u00fd k\u016f\u0148".getBytes("utf8").length;
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(CompareStringsTest.class);
    }

    private static final class NullField {

        String name;
    }
}
