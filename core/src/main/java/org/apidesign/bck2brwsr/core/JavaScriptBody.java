/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Put this method on a method in case it should have a special
 * body in the <em>JavaScript</em>.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface JavaScriptBody {
    /** Names of parameters for the method. 
     * 
     * <!--
     * If not specified
     * it will be <code>arg0, arg1, arg2</code>. In case of
     * instance methods, the <code>arg0</code> is reference
     * to <code>this</code>.
     * -->
     * 
     * @return array of the names of parameters for the method
     *    in JavaScript
     */
    public String[] args();
    
    /** The actual body of the method in JavaScript. This string will be
     * put into generated header (ends with '{') and footer (ends with '}').
     */
    public String body();
}
