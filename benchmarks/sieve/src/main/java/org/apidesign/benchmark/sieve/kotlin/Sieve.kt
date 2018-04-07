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

class Natural {
    var n : Int = 2;

    fun next(): Int {
        return n++;
    }
}

class Filter(val number : Int, val next : Filter?) {
    fun accept(n : Int) : Boolean {
        var filter = this;
        while (true) {
            if (n % filter.number == 0) {
                return false;
            }
            filter = filter.next ?: return true
        }
    }
}

abstract class Primes {
    var filter : Filter? = null
    var natural : Natural = Natural();

    fun next() : Int {
        while (true) {
            val n = natural.next();
            if (filter?.accept(n) ?: true) {
                filter = Filter(n, filter);
                return n;
            }
        }
    }

    protected abstract fun log(msg: String) : Unit;

    public final fun compute(count: Int): Int {
        var cnt = 0;
        var res: Int;
        while (true) {
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
