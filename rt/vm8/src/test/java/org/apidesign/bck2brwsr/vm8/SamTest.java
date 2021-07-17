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

public class SamTest {

    @Compare
    public int staticLambdas() {
        int[] sum = {0};
        Functions.Compute<Integer> inc = () -> sum[0]++;
        Functions.Compute<Integer> ret = () -> sum[0];
        return inc.andThen(inc).andThen(inc).andThen(ret).get();
    }

    @Compare
    public int instanceLambda() {
        class Cnt {
            int sum = 0;

            Integer increment() {
                return sum++;
            }

            Integer value() {
                return sum;
            }
        }
        Cnt sum = new Cnt();
        Functions.Compute<Integer> inc = sum::increment;
        Functions.Compute<Integer> ret = sum::value;
        return inc.andThen(inc).andThen(inc).andThen(ret).get();
    }

    static abstract class VirtualLambdaSum {
        abstract Integer increment();
        abstract Integer value();

        static VirtualLambdaSum create() {
            class Cnt extends VirtualLambdaSum {
                int sum = 0;

                Integer increment() {
                    return sum++;
                }

                Integer value() {
                    return sum;
                }
            }
            return new Cnt();
        }
    }

    @Compare
    public int virtualLambda() {
        VirtualLambdaSum sum = VirtualLambdaSum.create();
        Functions.Compute<Integer> inc = sum::increment;
        Functions.Compute<Integer> ret = sum::value;
        return inc.andThen(inc).andThen(ret).get();
    }

    @Factory public static Object[] create() {
        return VMTest.create(SamTest.class);
    }
}
