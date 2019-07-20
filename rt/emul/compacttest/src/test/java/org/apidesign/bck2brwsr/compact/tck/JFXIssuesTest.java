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
package org.apidesign.bck2brwsr.compact.tck;

import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;


public class JFXIssuesTest {
    private abstract class Application {
        public abstract int getID();
    }
    
    private class MyApplication extends Application {

        @Override
        public int getID() {
            return 1;
        }
        
    } 
    
    @Compare public boolean isClassAssignable() {
        return Application.class.isAssignableFrom(MyApplication.class);
    }

    @Compare public boolean isNaN() {
        return Double.isNaN(Double.NaN);
    }

    @Compare public boolean isInfinite() {
        return Float.isInfinite(Float.NEGATIVE_INFINITY);
    }

    @Compare public boolean areTimesEqual() {
        long l1 = System.currentTimeMillis();
        long l2 = l1 + 0;

        return l1 == l2;
    }
    
    @Compare public boolean roundOnDouble() {
        long l1 = Math.round(System.currentTimeMillis() / 1.1);
        long l2 = l1 + 0;
        
        return l1 == l2;
    }

    private static final long val = 1238078409318L;
    
    @Compare public int valueConvertedToString() {
        long[] arr = { val };
        return dumpValue(arr);
    }
    
    int dumpValue(long[] val) {
        return (int) val[0]++;
    }
    
    
    @Compare public boolean roundOnFloat() {
        final float f = System.currentTimeMillis() / 1.1f;
        int l1 = Math.round(f);
        int l2 = l1 + 0;
        
        assert l1 == l2 : "Round " + l1 + " == " + l2;
        
        return l1 == l2;
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(JFXIssuesTest.class);
    }
}
