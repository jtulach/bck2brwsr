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
package org.apidesign.bck2brwsr.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class PackagesTest {
    class Inner {
    }

    @Compare
    public String topMostPackage() {
        return getClass().getPackage().getName();
    }

    @Compare
    public String innerPackage() {
        return new Inner().getClass().getPackage().getName();
    }

    @Compare
    public boolean areTheSame() {
        Package p1 = getClass().getPackage();
        Package p2 = new Inner().getClass().getPackage();
        return p1 == p2;
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(PackagesTest.class);
    }
}
