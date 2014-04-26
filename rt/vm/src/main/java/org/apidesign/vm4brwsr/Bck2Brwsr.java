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
import org.apidesign.bck2brwsr.core.Exported;

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
    private final ObfuscationLevel level;
    private final StringArray rootcls;
    private final StringArray classes;
    private final Resources res;
    private final boolean extension;

    private Bck2Brwsr(ObfuscationLevel level, StringArray rootcls, StringArray classes, Resources resources, boolean extension) {
        this.level = level;
        this.rootcls = rootcls;
        this.classes = classes;
        this.res = resources;
        this.extension = extension;
    }
    
    /** Helper method to generate virtual machine from bytes served by a <code>resources</code>
     * provider.
     *
     * @param out the output to write the generated JavaScript to
     * @param resources provider of class files to use
     * @param classes additional classes to include in the generated script
     * @throws IOException I/O exception can be thrown when something goes wrong
     */
    public static void generate(Appendable out, Resources resources, String... classes) throws IOException {
        newCompiler().resources(resources).addRootClasses(classes).generate(out);
    }

    /** Helper method to generate virtual machine from bytes served by a class loader.
     *
     * @param out the output to write the generated JavaScript to
     * @param loader class loader to load needed classes from
     * @param classes additional classes to include in the generated script
     * @throws IOException I/O exception can be thrown when something goes wrong
     */
    public static void generate(Appendable out, ClassLoader loader, String... classes) throws IOException {
        newCompiler().resources(loader).addRootClasses(classes).generate(out);
    }
    
    /** Creates new instance of Bck2Brwsr compiler which is ready to generate
     * empty Bck2Brwsr virtual machine. The instance can be further
     * configured by calling chain of methods. For example: 
     * <pre>
     * {@link #createCompiler()}.{@link #resources(org.apidesign.vm4brwsr.Bck2Brwsr.Resources) resources(loader)}.{@link #addRootClasses(java.lang.String[]) addRootClasses("your/Clazz")}.{@link #generate(java.lang.Appendable) generate(out)};
     * </pre>
     * 
     * @return new instance of the Bck2Brwsr compiler
     * @since 0.5
     */
    public static Bck2Brwsr newCompiler() {
        return new Bck2Brwsr(ObfuscationLevel.NONE, new StringArray(), new StringArray(), null, false);
    }

    /** Adds additional classes 
     * to the list of those that should be included in the generated
     * JavaScript file.
     * These classes are guaranteed to be available in the
     * generated virtual machine code accessible using their fully 
     * qualified name. This brings the same behavior as if the
     * classes were added by {@link #addClasses(java.lang.String...) } and
     * were annotated with {@link Exported} annotation.
     * 
     * @param classes the classes to add to the compilation
     * @return new instance of the Bck2Brwsr compiler which inherits
     * all values from <code>this</code>
     */
    public Bck2Brwsr addRootClasses(String... classes) {
        if (classes.length == 0) {
            return this;
        } else {
            return new Bck2Brwsr(level, rootcls.addAndNew(classes), this.classes, res,
                                 extension);
        }
    }
    
    /** Adds additional classes 
     * to the list of those that should be included in the generated
     * JavaScript file. These classes are guaranteed to be present,
     * but they may not be accessible through their fully qualified
     * name.
     * 
     * @param classes the classes to add to the compilation
     * @return new instance of the Bck2Brwsr compiler which inherits
     * all values from <code>this</code>
     * @since 0.9
     */
    public Bck2Brwsr addClasses(String... classes) {
        if (classes.length == 0) {
            return this;
        } else {
            return new Bck2Brwsr(level, rootcls, this.classes.addAndNew(classes), res,
                extension);
        }
    }
    
    /** Changes the obfuscation level for the compiler by creating new instance
     * which inherits all values from <code>this</code> and adjust the level
     * of obfuscation.
     * 
     * @param level the new level of obfuscation
     * @return new instance of the compiler with changed level of obfuscation
     * @since 0.5
     */
    public Bck2Brwsr obfuscation(ObfuscationLevel level) {
        return new Bck2Brwsr(level, rootcls, classes, res, extension);
    }
    
    /** A way to change the provider of additional resources (classes) for the 
     * compiler. 
     * 
     * @param res the implementation of resources provider
     * @return new instance of the compiler with all values remaining the same, just 
     *   with different resources provider
     * @since 0.5
     */
    public Bck2Brwsr resources(Resources res) {
        return new Bck2Brwsr(level, rootcls, classes, res, extension);
    }

    /** Should one generate a library? By default the system generates
     * all transitive classes needed by the the transitive closure of
     * {@link #addRootClasses(java.lang.String...)} and {@link #addClasses(java.lang.String...)}.
     * By turning on the library mode, only classes explicitly listed
     * will be included in the archive. The others will be referenced
     * as external ones.
     * 
     * @param library turn on the library mode?
     * @return new instance of the compiler with library flag changed
     * @since 0.9
     */
    public Bck2Brwsr library(boolean library) {
        return new Bck2Brwsr(level, rootcls, classes, res, library);
    }

    /** A way to change the provider of additional resources (classes) for the 
     * compiler by specifying classloader to use for loading them.
     * 
     * @param loader class loader to load the resources from
     * @return new instance of the compiler with all values being the same, just 
     *   different resources provider
     * @since 0.5
     */
    public Bck2Brwsr resources(final ClassLoader loader) {
        return resources(new LdrRsrcs(loader));
    }
    
    /** Generates virtual machine based on previous configuration of the 
     * compiler.
     * 
     * @param out the output to write the generated JavaScript to
     * @since 0.5
     */
    public void generate(Appendable out) throws IOException {
        Resources r = res != null ? res : new LdrRsrcs(Bck2Brwsr.class.getClassLoader());
        if (level != ObfuscationLevel.NONE) {
            try {
                ClosureWrapper.produceTo(out, level, r, rootcls, classes, extension);
                return;
            } catch (IOException ex) {
                throw ex;
            } catch (Throwable ex) {
                out.append("/* Failed to obfuscate: " + ex.getMessage()
                               + " */\n");
            }
        }

        VM.compile(out, r, rootcls, classes, extension);
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
