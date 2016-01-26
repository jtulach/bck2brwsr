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
package org.apidesign.bck2brwsr.vmtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Can be applied on a method that yields a return value. 
 * Together with {@link VMTest#create} it can be used to write
 * methods which are executed in real VM as well as JavaScript VMs and
 * their results are compared.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Compare {
    /** Specifies whether the system should internal JavaScript interpreter
     * as available via {@link javax.script.ScriptEngine}. Defaults to true,
     * but in some situations (benchmarking comes to my mind), one may set this
     * to <code>false</code>. In such case only browsers provided via
     * <code>vmtest.brwsrs</code> property are used. For example
     * <code>"vmtest.brwsrs=firefox,google-chrome"</code> would run the test
     * in HotSpot VM, firefox and chrome and would compare the results.
     * @return 
     */
    boolean scripting() default true;

    /** Compare (or not) execution times. If the value is specified and
     * bigger than zero, then the fastest and slowest execution time is
     * compared and if the ratio between them is higher, the compare fails.
     * @return ratio (e.g. 2x, 3x, 3.5x) between execution times
     */
    double slowdown() default -1;
}
