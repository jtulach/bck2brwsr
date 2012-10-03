package org.apidesign.bck2brwsr.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ExtraJavaScript {
    /** location of a script to load */
    String resource();
    /** should the class file still be processed or not? */
    boolean processByteCode() default true;
}
