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
package org.apidesign.bck2brwsr.aot.nb.test;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class LookupGetTest {
    
    @Compare public boolean getDefault() {
        return Lookup.getDefault() != null;
    }

    @Compare public String findImpl() {
        Iface a = Lookup.getDefault().lookup(Iface.class);
        return a.getClass().getName();
    }

    @Factory public static Object[] create() {
        return VMTest.create(LookupGetTest.class);
    }
    
    public static interface Iface {
    }

    @ServiceProvider(service = Iface.class)
    public static final class Impl implements Iface {
    }
}
