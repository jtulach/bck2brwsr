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

/** Static methods in classes annotated by {@link Page}
 * can be marked by this annotation to establish a 
 * <a href="http://en.wikipedia.org/wiki/JSON">JSON</a>
 * communication point.
 * The associated model page then gets new method to invoke a network
 * connection. Example follows:
 * 
 * <pre>
 * {@link Page @Page}(className="MyModel", xhtml="page.html", properties={
 *   {@link Property @Property}(name = "people", type=Person.class, array=true)
 * })
 * class MyModelImpl {
 *   {@link Model @Model}(className="Person", properties={
 *     {@link Property @Property}(name = "firstName", type=String.class),
 *     {@link Property @Property}(name = "lastName", type=String.class)
 *   })
 *   static class PersonImpl {
 *     {@link ComputedProperty @ComputedProperty}
 *     static String fullName(String firstName, String lastName) {
 *       return firstName + " " + lastName;
 *     }
 *   }
 * 
 *   {@link OnReceive @OnReceive}(url = "{protocol}://your.server.com/person/{name}")
 *   static void getANewPerson(MyModel m, Person p) {
 *     {@link Element#alert Element.alert}("Adding " + p.getFullName() + '!');
 *     m.getPeople().add(p);
 *   }
 * 
 *   // the above will generate method <code>getANewPerson</code> in class <code>MyModel</code>.
 *   // with <code>protocol</code> and <code>name</code> arguments
 *   // which asynchronously contacts the server and in case of success calls
 *   // your {@link OnReceive @OnReceive} with parsed in data
 * 
 *   {@link On @On}(event={@link OnEvent#CLICK OnEvent.CLICK}, id="rqst")
 *   static void requestSmith(MyModel m) {
 *     m.getANewPerson("http", "Smith");
 *   }
 * }
 * </pre>
 * When the server returns <code>{ "firstName" : "John", "lastName" : "Smith" }</code>
 * the browser will show alert message <em>Adding John Smith!</em>.
 * 
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 0.6
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
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
