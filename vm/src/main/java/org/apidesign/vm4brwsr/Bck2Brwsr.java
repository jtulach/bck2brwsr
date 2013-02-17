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
package org.apidesign.vm4brwsr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/** Build your own virtual machine! Use methods in this class to generate
 * a skeleton JVM in JavaScript that contains pre-compiled classes of your
 * choice. The generated script defines one JavaScript method that can
 * be used to bootstrap and load the virtual machine: <pre>
 * var vm = bck2brwsr();
 * var main = vm.loadClass('org.your.pkg.Main');
 * main.main__V_3Ljava_lang_String_2(null);
 * </pre>
 * In case one wants to initialize the virtual machine with ability to
 * load classes lazily when needed, one can provide a loader function to
 * when creating the virtual machine: <pre>
 * var vm = bck2brwsr(function(resource) { 
 *   return null; // byte[] for the resource
 * });
 * </pre>
 * In this scenario, when a request for an unknown class is made, the loader
 * function is asked for its byte code and the system dynamically transforms
 * it to JavaScript.
 * <p>
 * Instead of a loader function, one can also provide a URL to a JAR file.
 * The <code>bck2brwsr</code> system will do its best to download the file
 * and provide loader function for it automatically.
 * <p>
 * One can provide as many loader functions and JAR URL references as necessary.
 * Then the initialization code would look like:<pre>
 * var vm = bck2brwsr(url1, url2, fnctn1, url3, functn2);
 * </pre>
 * The provided URLs and loader functions will be consulted one by one.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Bck2Brwsr {
    private Bck2Brwsr() {
    }
    
    /** Generates virtual machine from bytes served by a <code>resources</code>
     * provider.
     * 
     * @param out the output to write the generated JavaScript to
     * @param resources provider of class files to use
     * @param classes additional classes to include in the generated script
     * @throws IOException I/O exception can be thrown when something goes wrong
     */
    public static void generate(Appendable out, Resources resources, String... classes) throws IOException {
        StringArray arr = StringArray.asList(classes);
        arr.add(VM.class.getName().replace('.', '/'));
        VM.compile(resources, out, arr);
    }
    
    /** Generates virtual machine from bytes served by a class loader.
     * 
     * @param out the output to write the generated JavaScript to
     * @param loader class loader to load needed classes from
     * @param classes additional classes to include in the generated script
     * @throws IOException I/O exception can be thrown when something goes wrong
     */
    public static void generate(Appendable out, final ClassLoader loader, String... classes) throws IOException {
        class R implements Resources {
            @Override
            public InputStream get(String name) throws IOException {
                Enumeration<URL> en = loader.getResources(name);
                URL u = null;
                while (en.hasMoreElements()) {
                    u = en.nextElement();
                }
                if (u == null) {
                    throw new IOException("Can't find " + name);
                }
                return u.openStream();
            }
        }
        generate(out, new R(), classes);
    }
    
    /** Provider of resources (classes and other files). The 
     * {@link #generate(java.lang.Appendable, org.apidesign.vm4brwsr.Bck2Brwsr.Resources, java.lang.String[]) 
     * generator method} will call back here for all classes needed during
     * translation to JavaScript.
     */
    public interface Resources {
        /** Loads given resource (class or other file like image). The 
         * resource name to load bytes for the {@link String} class
         * would be <code>"java/lang/String.class"</code>.
         * 
         * @param resource path to resource to load
         * @return the input stream for the resource 
         * @throws IOException can be thrown if the loading fails on some error
         *   or the file cannot be found
         */
        public InputStream get(String resource) throws IOException;
    }
}
