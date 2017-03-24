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
package org.apidesign.bck2brwsr.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Put this annotation on a method to provide its special implementation
 * in JavaScript. This is a way to define <em>native</em> methods that 
 * interact with the surrounding environment.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface JavaScriptBody {
    /** Names of parameters for the method generated method that can
     * be referenced from {@link #body()}.
     * 
     * @return array of the names of parameters for the method
     *    in JavaScript
     */
    public String[] args();
    
    /** The actual body of the method in JavaScript. This string will be
     * put into generated header (ends with '{') and footer (ends with '}').
     * The body can reference provided arguments. In case of non-static
     * instance method it may reference <code>this</code>. It can also 
     * call methods and access fields - if 
     * <a href="http://wiki.apidesign.org/wiki/Bck2BrwsrMangling">proper mangling</a> 
     * is used. Methods that return some value should end with <code>return</code> 
     * statement.
     */
    public String body();
}
