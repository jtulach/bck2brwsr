/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.vmtest.impl;

import org.apidesign.bck2brwsr.vmtest.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** A TestNG {@link Factory} that seeks for {@link Compare} annotations
 * in provided class and builds set of tests that compare the computations
 * in real as well as JavaScript virtual machines. Use as:<pre>
 * {@code @}{@link Factory} public static create() {
 *   return @{link VMTest}.{@link #create(YourClass.class);
 * }</pre>
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class CompareCase implements ITest {
    private final Bck2BrwsrCase first, second;
    private final Method m;
    
    private CompareCase(Method m, Bck2BrwsrCase first, Bck2BrwsrCase second) {
        this.first = first;
        this.second = second;
        this.m = m;
    }

    /** Inspects <code>clazz</code> and for each {@lik Compare} method creates
     * instances of tests. Each instance runs the test in different virtual
     * machine and at the end they compare the results.
     * 
     * @param clazz the class to inspect
     * @return the set of created tests
     */
    public static Object[] create(Class<?> clazz) {
        Method[] arr = clazz.getMethods();
        List<Object> ret = new ArrayList<>();
        
        final Launchers l = Launchers.INSTANCE;
    
        ret.add(l);
        
        for (Method m : arr) {
            Compare c = m.getAnnotation(Compare.class);
            if (c == null) {
                continue;
            }
            final Bck2BrwsrCase real = new Bck2BrwsrCase(m, 0, null);
            final Bck2BrwsrCase js = new Bck2BrwsrCase(m, 1, l);
            final Bck2BrwsrCase brwsr = new Bck2BrwsrCase(m, 2, l);
            
            ret.add(real);
            ret.add(js);
            ret.add(brwsr);
            
            ret.add(new CompareCase(m, real, js));
            ret.add(new CompareCase(m, real, brwsr));
        }
        return ret.toArray();
    }

    /** Test that compares the previous results.
     * @throws Throwable 
     */
    @Test(dependsOnGroups = "run") public void compareResults() throws Throwable {
        Object v1 = first.value;
        Object v2 = second.value;
        if (v1 != null) {
            v1 = v1.toString();
        } else {
            v1 = "null";
        }
        Assert.assertEquals(v2, v1, "Comparing results");
    }
    
    /** Test name.
     * @return name of the tested method followed by a suffix
     */
    @Override
    public String getTestName() {
        return m.getName() + "[Compare " + second.typeName() + "]";
    }
    
    static StringBuilder dumpJS(CharSequence sb) throws IOException {
        File f = File.createTempFile("execution", ".js");
        try (FileWriter w = new FileWriter(f)) {
            w.append(sb);
        }
        return new StringBuilder(f.getPath());
    }
}
