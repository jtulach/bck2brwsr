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
package org.apidesign.bck2brwsr.tck;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AtomicTest {
    private volatile int intValue;
    private static AtomicIntegerFieldUpdater<AtomicTest> intUpdater;
    static AtomicIntegerFieldUpdater<AtomicTest> getIntUpdater() {
        if (intUpdater == null) {
            intUpdater = AtomicIntegerFieldUpdater.newUpdater(AtomicTest.class, "intValue");
        }
        return intUpdater;
    }
    private volatile long longValue;
    private static AtomicLongFieldUpdater<AtomicTest> longUpdater;
    static AtomicLongFieldUpdater<AtomicTest> getLongUpdater() {
        if (longUpdater == null) {
            longUpdater = AtomicLongFieldUpdater.newUpdater(AtomicTest.class, "longValue");
        }
        return longUpdater;
    }
    private volatile Number refValue;
    private static AtomicReferenceFieldUpdater<AtomicTest,Number> refUpdater;
    static AtomicReferenceFieldUpdater<AtomicTest,Number> getRefUpdater() {
        if (refUpdater == null) {
            refUpdater = AtomicReferenceFieldUpdater.newUpdater(AtomicTest.class, Number.class, "refValue");
        }
        return refUpdater;
    }
    
    @Compare public boolean atomicBoolean() {
        AtomicBoolean ab = new AtomicBoolean();
        ab.set(true);
        return ab.compareAndSet(true, false);
    }

    @Compare public int atomicInt() {
        AtomicInteger ab = new AtomicInteger();
        ab.set(30);
        assert ab.compareAndSet(30, 10);
        return ab.get();
    }
    
    @Compare public String atomicRef() {
        AtomicReference<String> ar = new AtomicReference<String>("Ahoj");
        assert ar.compareAndSet("Ahoj", "Hello");
        return ar.getAndSet("Other");
    }
    
    @Compare public int intUpdater() {
        intValue = Integer.MAX_VALUE / 2;
        final AtomicIntegerFieldUpdater<AtomicTest> u = getIntUpdater();
        u.compareAndSet(this, 0, 10);
        u.addAndGet(this, 3);
        int thirteen = u.getAndAdd(this, 7);
        return thirteen + u.get(this);
    }

    @Compare public long longUpdater() {
        longValue = Long.MAX_VALUE / 2;
        final AtomicLongFieldUpdater<AtomicTest> u = getLongUpdater();
        u.compareAndSet(this, 0, 10);
        u.addAndGet(this, 3);
        long thirteen = u.getAndAdd(this, 7);
        return thirteen + u.get(this);
    }

    @Compare public int refUpdater() {
        final AtomicReferenceFieldUpdater<AtomicTest,Number> u = getRefUpdater();
        u.set(this, 0);
        u.compareAndSet(this, 0, 10);
        Number ten = u.getAndSet(this, 3);
        return u.get(this).intValue() + ten.intValue();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(AtomicTest.class);
    }
}
