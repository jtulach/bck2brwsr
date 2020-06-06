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
package org.apidesign.bck2brwsr.aot.nb.test;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.openide.util.NbBundle;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class NbBundleMessagesTest {

    @NbBundle.Messages({"Hello=Hello from Bck2Brwsr!"})
    @Compare
    public String nbBundleMessages() {
        return Bundle.Hello();
    }

    @NbBundle.Messages({
        "HelloCount=Hello {0} times!"
    })
    @Compare
    public String nbBundleMessagesWithIntArg() {
        return Bundle.HelloCount(3);
    }

    @Compare
    public String nbBundleMessagesWithStringArg() {
        return Bundle.HelloCount("3");
    }

    @Compare
    public String nbBundleMessagesWithFloatArg() {
        return Bundle.HelloCount(3.0f);
    }

    @Compare
    public String nbBundleMessagesWithDoubleArg() {
        return Bundle.HelloCount(3.0);
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(NbBundleMessagesTest.class);
    }

}
