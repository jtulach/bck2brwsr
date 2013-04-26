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
package org.apidesign.bck2brwsr.htmlpage.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * @deprecated Replaced by new {@link net.java.html.json.OnReceive net.java.html.json} API.
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 0.6
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@Deprecated
public @interface OnReceive {
    /** The URL to connect to. Can contain variable names surrounded by '{' and '}'.
     * Those parameters will then become variables of the associated method.
     * 
     * @return the (possibly parametrized) url to connect to
     */
    String url();
    
    /** Support for <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> requires
     * a callback from the server generated page to a function defined in the
     * system. The name of such function is usually specified as a property
     * (of possibly different names). By defining the <code>jsonp</code> attribute
     * one turns on the <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> 
     * transmission and specifies the name of the property. The property should
     * also be used in the {@link #url()} attribute on appropriate place.
     * 
     * @return name of a property to carry the name of <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a>
     *    callback function.
     */
    String jsonp() default "";
}
