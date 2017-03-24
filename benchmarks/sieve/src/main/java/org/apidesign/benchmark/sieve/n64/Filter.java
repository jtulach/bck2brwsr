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

final class Filter {
    private final long number;
    private final Filter next;

    public Filter(long number, Filter next) {
        this.number = number;
        this.next = next;
    }

    public boolean accept(long n) {
        Filter filter = this;
        for (;;) {
            if (n % filter.number == 0) {
                return false;
            }
            filter = filter.next;
            if (filter == null) {
                return true;
            }
        }
    }
}
