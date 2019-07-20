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
package org.apidesign.benchmark.sieve.kotlin

import net.java.html.js.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

class SieveTest : Primes() {
    @Compare
    fun oneThousand(): Int {
        return compute(1000);
    }

    @Compare
    fun fiveThousand(): Int {
        return compute(5000);
    }

    @JavaScriptBody(args = arrayOf("msg"), body = "if (typeof console !== 'undefined') console.log(msg);")
    override fun log(msg: String): Unit {
        System.err.println(msg);
    }

    companion object {
        @Factory @JvmStatic
        fun create(): Array<out Any> {
            return VMTest.create(SieveTest::class.java);
        }
    }


}