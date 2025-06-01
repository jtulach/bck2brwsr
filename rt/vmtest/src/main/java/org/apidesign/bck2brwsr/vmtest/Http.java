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

/**
 * Exposes an {@link Resource HTTP page} or a set of {@link #value() pages} to the running {@link BrwsrTest}.
 *
 * @author Jaroslav Tulach
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Http {
    /** Set of pages to make available */
    public Resource[] value();
    
    /** Describes single HTTP page to the running {@link BrwsrTest}, so it can be 
     * accessed under the specified {@link #path() relative path}. The page
     * content can either be specified inline via {@link #content()} or as
     * an external {@link #resource() resource}.
     *
     * @author Jaroslav Tulach
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    public @interface Resource {
        /** path on the server that the test can use to access the exposed resource */
        String path();
        /** the content of the <code>Http.Resource</code> */
        String content();
        /** resource relative to the class that should be used instead of <code>content</code>.
         * Leave content equal to empty string.
         */
        String resource() default "";
        /** mime type of the resource */
        String mimeType();
        /** query parameters. Can be referenced from the {@link #content} as
         * <code>$0</code>, <code>$1</code>, etc. The values will be extracted
         * from URL parameters of the request.
         */
        String[] parameters() default {};
    }
}
