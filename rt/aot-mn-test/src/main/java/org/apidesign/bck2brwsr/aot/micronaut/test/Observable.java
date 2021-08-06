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
package org.apidesign.bck2brwsr.aot.micronaut.test;

import io.micronaut.aop.Around;
import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.Introspected;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.html.json.spi.Proto;

public interface Observable {
    /** Returns {@link Proto} associated with this object.
     * 
     * @return non-null proto instance
     */
    Proto proto();

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.TYPE})
    @Around(proxyTarget = true)
    @Type(value = ObservableInterceptor.class)
    @Introspected
    @Introduction(interfaces = Observable.class)
    public static @interface UI {
    }
}
