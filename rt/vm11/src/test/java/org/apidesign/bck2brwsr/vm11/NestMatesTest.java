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

public class NestMatesTest {
    @Compare
    public String callPrivateMethod() throws Exception {
        return NestMates.privateMessage();
    }

    @Compare
    public String callProtectedMethod() throws Exception {
        return NestMates.protectedMessage();
    }

    @Compare
    public String concatenateBothMethods() throws Exception {
        return NestMates.privateMessage() + NestMates.protectedMessage();
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(NestMatesTest.class);
    }

}
