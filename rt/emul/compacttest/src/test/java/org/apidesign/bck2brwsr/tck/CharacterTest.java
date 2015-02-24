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
public class CharacterTest {
    @Compare public boolean dolarJavaStart() {
        return Character.isJavaIdentifierStart('$');
    }

    @Compare public boolean dolarJavaPart() {
        return Character.isJavaIdentifierPart('$');
    }

    @Compare public boolean numberJavaStart() {
        return Character.isJavaIdentifierStart('3');
    }

    @Compare public boolean numberJavaPart() {
        return Character.isJavaIdentifierPart('3');
    }

    @Compare public String testWhiteSpaces() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 128; i++) {
            char ch = (char)i;
            if (Character.isWhitespace(ch)) {
                sb.append(i).append(",");
            }
        }
        return sb.toString();
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(CharacterTest.class);
    }

}
