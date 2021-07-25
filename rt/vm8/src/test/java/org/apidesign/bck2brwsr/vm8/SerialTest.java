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

import java.io.Serializable;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

public class SerialTest {

    @Compare
    public int staticLambdas() {
        int[] sum = {0};
        SerialCompute<Integer> inc = () -> sum[0]++;
        SerialCompute<Integer> ret = () -> sum[0];
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
        SerialCompute<Integer> inc = sum::increment;
        SerialCompute<Integer> ret = sum::value;
        return inc.andThen(inc).andThen(inc).andThen(ret).get();
    }

    @Compare
    public int primitiveToObjectInstanceLambda() {
        class Cnt {
            int sum = 0;

            int increment() {
                return sum++;
            }

            int value() {
                return sum;
            }
        }
        Cnt sum = new Cnt();
        SerialCompute<Integer> inc = sum::increment;
        SerialCompute<Integer> ret = sum::value;
        if (ret instanceof Serializable) {
            return inc.andThen(inc).andThen(inc).andThen(ret).get();
        } else {
            return -1;
        }
    }

    private interface SerialCompute<T> extends Serializable {
        public T get();

        default <X> SerialCompute<X> andThen(SerialCompute<X> next) {
            return () -> {
                this.get();
                return next.get();
            };
        }
    }

    public interface LongCompute extends Serializable {

        public long get();

        default LongCompute andThen(LongCompute next) {
            return () -> {
                this.get();
                return next.get();
            };
        }
    }

    @Compare
    public int primitiveInstanceLambda() {
        class Cnt {
            int sum = 0;

            int increment() {
                return sum++;
            }

            int value() {
                return sum;
            }
        }
        Cnt sum = new Cnt();
        LongCompute inc = sum::increment;
        LongCompute ret = sum::value;
        long r = inc.andThen(inc).andThen(inc).andThen(ret).get();
        return (int)r;
    }

    @Compare
    public int objectToPrimitiveInstanceLambda() {
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
        LongCompute inc = sum::increment;
        LongCompute ret = sum::value;
        long r = inc.andThen(inc).andThen(inc).andThen(ret).get();
        return (int)r;
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
        SerialCompute<Integer> inc = sum::increment;
        SerialCompute<Integer> ret = sum::value;
        return inc.andThen(inc).andThen(ret).get();
    }

    @Factory public static Object[] create() {
        return VMTest.create(SerialTest.class);
    }
}
