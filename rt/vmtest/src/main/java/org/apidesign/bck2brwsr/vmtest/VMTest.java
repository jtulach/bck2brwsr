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
package org.apidesign.bck2brwsr.vmtest;

import org.apidesign.bck2brwsr.launcher.Launcher;
import org.apidesign.bck2brwsr.vmtest.impl.CompareCase;
import org.testng.annotations.Factory;

/** A TestNG {@link Factory} that seeks for {@link Compare} and {@link BrwsrTest} annotations
 * in provided class and builds set of tests that verify the functionality of <b>Bck2Brwsr</b> 
 * based system. Use as:
 * <pre>
 * {@code @}{@link Factory} public static create() {
 *   return @{link VMTest}.{@link #create(java.lang.Class) create}(YourClass.class);
 * }</pre>
 * where <code>YourClass</code> contains methods annotated with
 * {@link Compare} and {@link BrwsrTest} annotations.
 * 
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class VMTest {
    private VMTest() {
    }
    
    /** Inspects <code>clazz</code> and for each method annotated by
     * {@link Compare} or {@link BrwsrTest} creates
     * instances of tests. 
     * <p>
     * Each {@link Compare} instance runs the test in different virtual
     * machine and at the end they compare the results.
     * <p>
     * Each {@link BrwsrTest} annotated method is executed once in {@link Launcher started
     * browser}.
     * 
     * @param clazz the class to inspect
     * @return the set of created tests
     */
    public static Object[] create(Class<?> clazz) {
        return CompareCase.create(clazz);
    }
}
