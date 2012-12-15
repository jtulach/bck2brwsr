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
package org.apidesign.vm4brwsr.tck;

import org.apidesign.vm4brwsr.Compare;
import org.apidesign.vm4brwsr.CompareVMs;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CompareStringsTest {
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
    
    @Factory
    public static Object[] create() {
        return CompareVMs.create(CompareStringsTest.class);
    }
}
