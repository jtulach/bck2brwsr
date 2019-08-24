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
package org.apidesign.bck2brwsr.vm8;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

public class FunctionsTest {
    @Compare
    public int inner5() throws Exception {
        return Functions.inner5().invoke(null, null, null, null, null);
    }
    @Compare
    public int function5() throws Exception {
        return Functions.function5().invoke(null, null, null, null, null);
    }
    @Compare
    public int inner4() throws Exception {
        return Functions.inner4().invoke(null, null, null, null, null);
    }
    @Compare
    public int function4() throws Exception {
        final Functions.BaseOne<Void, Void, Void, Void, Object, Integer> fn = Functions.function4();
        if (fn == null) {
            return -1;
        }
        return fn.invoke(null, null, null, null, null);
    }

    @Compare
    public int computeComposition() {
        int[] sum = {0};
        Functions.Compute<Integer> inc = () -> sum[0]++;
        Functions.Compute<Integer> ret = () -> sum[0];
        return inc.andThen(inc).andThen(inc).andThen(inc).andThen(ret).get();
    }

    @Compare
    public int absorbComposition() {
        int[] sum = {0};
        Functions.Absorb<Integer> inc = (value) -> sum[0] += value;
        Functions.Absorb<Integer> incFiveTimes = inc.andThen(inc).andThen(inc).andThen(inc).andThen(inc);
        incFiveTimes.use(30);
        return sum[0];
    }

    @Factory public static Object[] create() {
        return VMTest.create(FunctionsTest.class);
    }
}
