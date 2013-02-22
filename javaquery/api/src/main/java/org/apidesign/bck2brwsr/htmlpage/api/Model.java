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

/** Defines a model class named {@link #className()} which contains
 * defined {@link #properties()}. This class can have methods 
 * annotated by {@link ComputedProperty} which define derived
 * properties in the model class.
 * <p>
 * The {@link #className() generated class} will have methods
 * to convert the object <code>toJSON</code> and <code>fromJSON</code>.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Model {
    /** Name of the model class */
    String className();
    /** List of properties in the model.
     */
    Property[] properties();
}
