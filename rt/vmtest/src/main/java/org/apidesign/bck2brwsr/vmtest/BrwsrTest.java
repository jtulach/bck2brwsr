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
package org.apidesign.bck2brwsr.vmtest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to indicate that given method should be executed
 * in a browser environment. Has to be used in conjunction with {@link VMTest#create(java.lang.Class)}
 * factory method. 
 * <p>
 * The browser to is by default executed via {@link java.awt.Desktop#browse(java.net.URI)},
 * but one can change that by specifying <code>-Dvmtest.brwsrs=firefox,google-chrome</code>
 * property.
 * <p>
 * If the annotated method throws {@link InterruptedException}, it will return
 * the processing to the browser and after 100ms, called again. This is useful
 * for testing asynchronous communication, etc.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BrwsrTest {
}
