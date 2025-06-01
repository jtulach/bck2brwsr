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
package org.apidesign.bck2brwsr.mini.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
public class CloneTest {
    private int value;
    
    @Compare
    public Object notSupported() throws CloneNotSupportedException {
        return this.clone();
    }

    @Compare public String sameClass() throws CloneNotSupportedException {
        return new Clnbl().clone().getClass().getName();
    }

    @Compare public boolean differentInstance() throws CloneNotSupportedException {
        Clnbl orig = new Clnbl();
        return orig == orig.clone();
    }

    @Compare public int sameReference() throws CloneNotSupportedException {
        CloneTest self = this;
        Clnbl orig = new Clnbl();
        self.value = 33;
        orig.ref = self;
        return ((Clnbl)orig.clone()).ref.value;
    }

    @Compare public int sameValue() throws CloneNotSupportedException {
        Clnbl orig = new Clnbl();
        orig.value = 10;
        return ((Clnbl)orig.clone()).value;
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(CloneTest.class);
    }
    
    public static final class Clnbl implements Cloneable {
        public CloneTest ref;
        private int value;

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}
