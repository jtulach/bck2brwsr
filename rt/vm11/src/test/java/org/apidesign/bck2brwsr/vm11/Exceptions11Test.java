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

/**
 *
 * @author Jaroslav Tulach
 */
public class Exceptions11Test {
    @Compare
    public String escapeMessageText() throws Exception {
        try {
            return MyException.withLen(-3);
        } catch (MyException ex) {
            return ex.getMessage();
        }
    }

    @Compare
    public String concatenateInteger() throws Exception {
        try {
            return MyException.withInteger(-3);
        } catch (MyException ex) {
            return ex.getMessage();
        }
    }


    static class MyException extends Exception {
        public MyException(String message) {
            super(message);
        }

        static String withLen(int len) throws MyException {
            throw new MyException("\u0005Can't\nresize\tto\r" + len);
        }

        static String withInteger(Integer len) throws MyException {
            throw new MyException("\u0005Can't\nresize\tto\r" + len);
        }
    }

    @Factory public static Object[] create() {
        return VMTest.create(Exceptions11Test.class);
    }
}
