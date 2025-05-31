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
package org.apidesign.bck2brwsr.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Influence the inheritance of your class when converted to JavaScript.
 * Sometimes one does not want
 * to mimic the Java hierarchy, but modify it a bit. For example it makes
 * sense to treat every (JavaScript) string literal as {@link String}.
 * One can do it by making {@link String} subclass JavaScript <code>String</code>
 * and use <code>String.prototype</code> as a container for all {@link String}
 * methods.
 * 
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE })
public @interface JavaScriptPrototype {
    /** Expression that identifies the function where all methods
     * should be added into. If this attribute is unspecified
     * all methods are added to the same object specified by
     * {@link #prototype()}.
     * 
     * @return name of function to contain methods found in given class
     */
    String container() default "";
    /** Expression that defines the way to construct prototype for this
     * class.
     * @return expression to construct prototype
     */
    String prototype();
}
