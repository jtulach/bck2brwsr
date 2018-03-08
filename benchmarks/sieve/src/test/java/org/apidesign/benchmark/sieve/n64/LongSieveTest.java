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
package org.apidesign.benchmark.sieve.n64;

import java.io.IOException;
import net.java.html.js.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LongSieveTest extends Primes {
    public LongSieveTest() {
    }

    @JavaScriptBody(args = {  }, body = "return new Date().getTime();")
    protected int time() {
        return (int) System.currentTimeMillis();
    }

    @Compare
    public long oneThousand() throws IOException {
        LongSieveTest sieve = new LongSieveTest();
        int now = time();
        long res = sieve.compute(1000);
        int took = time() - now;
        log("oneThousand in " + took + " ms");
        return res;
    }

    @Compare(slowdown = 20.0)
    public long twoThousand() throws IOException {
        LongSieveTest sieve = new LongSieveTest();
        int now = time();
        long res = sieve.compute(2000);
        int took = time() - now;
        log("twoThousand in " + took + " ms");
        return res;

    }

    @Compare(slowdown = 20.0)
    public long threeThousand() throws IOException {
        LongSieveTest sieve = new LongSieveTest();
        int now = time();
        long res = sieve.compute(3000);
        int took = time() - now;
        log("threeThousand in " + took + " ms");
        return res;

    }

/*
    @Compare(slowdown = 3.0)
    public long tenThousand() throws IOException {
        LongSieveTest sieve = new LongSieveTest();
        int now = time();
        long res = sieve.compute(10000);
        int took = time() - now;
        log("tenThousand in " + took + " ms");
        return res;
    }
*/
    @Factory
    public static Object[] create() {
        return VMTest.create(LongSieveTest.class);
    }

    @JavaScriptBody(args = { "msg" }, body = "if (typeof console !== 'undefined') console.log(msg);")
    @Override
    protected void log(String msg) {
        System.err.println(msg);
    }
}
