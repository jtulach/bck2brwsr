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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private final List<Class> classes = new ArrayList<>();
    private final List<String> launcher = new ArrayList<>();
    
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
     * @param clazz the class (or classes) to inspect
     * @return the set of created tests
     */
    public static Object[] create(Class clazz) {
        return newTests().addClass(clazz).build();
    }
    
    /** Creates new builder for test execution. Continue with methods
     * like {@link #addClass(java.lang.Class[])} or {@link #addLauncher(java.lang.String[])}.
     * Finish the process by calling {@link #build()}.
     * 
     * @return new instance of a builder
     * @since 0.7
     */
    public static VMTest newTests() {
        return new VMTest();
    }
    
    /** Adds class (or classes) to the test execution. The classes are inspected
     * to contain methods annotated by
     * {@link Compare} or {@link BrwsrTest}. Appropriate set of TestNG test
     * cases is then created.
     * <p>
     * Each {@link Compare} instance runs the test in different virtual
     * machine and at the end they compare the results.
     * <p>
     * Each {@link BrwsrTest} annotated method is executed once in {@link Launcher started
     * browser}.
     * 
     * @param classes one or more classes to inspect
     * @since 0.7
     */
    public final VMTest addClass(Class... classes) {
        this.classes.addAll(Arrays.asList(classes));
        return this;
    }

    /** Adds list of launchers that should be used to execute tests defined
     * by {@link Compare} and {@link BrwsrTest} annotations. This value 
     * can be overrided by using <code>vmtest.brwsrs</code> property.
     * List of supported launchers is available in the documentation of
     * {@link Launcher}.
     * 
     * @param launcher names of one or more launchers to use for the execution
     *   of tests
     * @since 0.7
     */
    public final VMTest addLauncher(String... launcher) {
        this.launcher.addAll(Arrays.asList(launcher));
        return this;
    }
    
    /** Assembles the provided information into the final array of tests.
     * @return array of TestNG tests
     * @since 0.7
     */
    public final Object[] build() {
        return CompareCase.create(
            launcher.toArray(new String[0]), 
            classes.toArray(new Class[0])
        );
    }
}
