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
package org.apidesign.benchmark.sieve.n64;

abstract class Primes {
    private final Natural natural;
    private Filter filter;

    protected Primes() {
        this.natural = new Natural();
    }

    long next() {
        for (;;) {
            long n = natural.next();
            if (filter == null || filter.accept(n)) {
                filter = new Filter(n, filter);
                return n;
            }
        }
    }

    protected abstract void log(String msg);

    public final long compute(long count) {
        long cnt = 0;
        long res;
        for (;;) {
            res = next();
            cnt += 1;
            if (cnt % 1000 == 0) {
                log("Computed " + cnt + " primes. Last one is " + res);
            }
            if (cnt >= count) {
                break;
            }
        }
        return res;
    }
}
