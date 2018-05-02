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
package org.apidesign.vm4brwsr;

import java.io.IOException;
import java.io.InputStream;

/** Build your own virtual machine! Use methods in this class to generate
 * a skeleton JVM in JavaScript that contains pre-compiled classes of your
 * choice:
 * <pre>
 * Writer w = new {@link java.io.StringWriter}();
 * {@link #newCompiler() Bck2Brwsr.newCompiler}()
 *   .{@link #resources(org.apidesign.vm4brwsr.Bck2Brwsr.Resources)}
 *   .{@link #addRootClasses(java.lang.String...)}
 *   .{@link #addClasses(java.lang.String...)}
 *   .{@link #addExported(java.lang.String...)}
 *   .{@link #generate(java.lang.Appendable) generate(w)};
 * System.out.{@link java.io.PrintStream#print(java.lang.String) print(w.toString())};
 * </pre>
 * The generated script defines one JavaScript method that can
 * be used to bootstrap and load the virtual machine: <pre>
 * var vm = bck2brwsr();
 * var main = vm.loadClass('org.your.pkg.Main');
 * main.invoke('main');
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
 * Instead of a loader function, one can also provide a URL to a JAR file
 * or a library JavaScript file generated with {@link #library(java.lang.String...)}
 * option on.
 * The <code>bck2brwsr</code> system will do its best to download the file
 * and provide loader function for it automatically. In order to use
 * the JAR file <code>emul.zip</code> module needs to be available in the system.
 * <p>
 * One can provide as many loader functions and URL references as necessary.
 * Then the initialization code would look like:<pre>
 * var vm = bck2brwsr(url1, url2, fnctn1, url3, functn2);
 * </pre>
 * The provided URLs and loader functions will be consulted one by one.
 * <p>
 * The initialization of the <b>Bck2Brwsr</b> is done asynchronously since 
 * version 0.9. E.g. call to <pre>
 * var vm = bck2brwsr('myapp.js');
 * var main = vm.loadClass('org.your.pkg.Main');
 * main.invoke('main');
 * </pre>
 * returns immediately and the call to the static main method will happen
 * once the virtual machine is initialized and the class available.
 *
 * @author Jaroslav Tulach
 */
public final class Bck2Brwsr {
    private final ObfuscationLevel level;
    private final StringArray exported;
    private final StringArray classes;
    private final StringArray resources;
    private final Resources res;
    private final Boolean extension;
    private final StringArray classpath;

    private Bck2Brwsr(
            ObfuscationLevel level, 
            StringArray exported, StringArray classes, StringArray resources, 
            Resources res, 
            Boolean extension, StringArray classpath
    ) {
        this.level = level;
        this.exported = exported;
        this.classes = classes;
        this.resources = resources;
        this.res = res;
        this.extension = extension;
        this.classpath = classpath;
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
     * {@link #newCompiler()}.{@link #resources(org.apidesign.vm4brwsr.Bck2Brwsr.Resources) resources(loader)}.{@link #addRootClasses(java.lang.String[]) addRootClasses("your/Clazz")}.{@link #generate(java.lang.Appendable) generate(out)};
     * </pre>
     * 
     * @return new instance of the Bck2Brwsr compiler
     * @since 0.5
     */
    public static Bck2Brwsr newCompiler() {
        return new Bck2Brwsr(
            ObfuscationLevel.NONE, 
            new StringArray(), new StringArray(), new StringArray(), 
            null, false, null
        );
    }
    
    /** Adds exported classes or packages. If the string ends 
     * with slash, it is considered a name of package. If it does not,
     * it is a name of a class (without <code>.class</code> suffix).
     * The exported classes are prevented from being obfuscated. 
     * All public classes in exported packages are prevented from
     * being obfuscated. By listing the packages or classes in this 
     * method, these classes are not guaranteed to be included in
     * the generated script. Use {@link #addClasses} to include
     * the classes.
     * 
     * @param exported names of classes and packages to treat as exported
     * @return new instances of the Bck2Brwsr compiler which inherits
     *   all values from <code>this</code> except list of exported classes
     */
    public Bck2Brwsr addExported(String... exported) {
        return new Bck2Brwsr(
            level, this.exported.addAndNew(exported), 
            classes, resources, res, extension, classpath
        );
    }

    /** Adds additional classes 
     * to the list of those that should be included in the generated
     * JavaScript file.
     * These classes are guaranteed to be available in the
     * generated virtual machine code accessible using their fully 
     * qualified name. This brings the same behavior as if the
     * classes were added by {@link #addClasses(java.lang.String...) } and
     * exported via {@link #addExported(java.lang.String...)}.
     * 
     * @param classes the classes to add to the compilation
     * @return new instance of the Bck2Brwsr compiler which inherits
     * all values from <code>this</code>
     */
    public Bck2Brwsr addRootClasses(String... classes) {
        if (classes.length == 0) {
            return this;
        } 
        return addExported(classes).addClasses(classes);
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
            return new Bck2Brwsr(level, exported, 
                this.classes.addAndNew(classes), resources, res,
                extension, classpath);
        }
    }
    
    /** These resources should be made available in the compiled file in
     * binary form. These resources can then be loaded
     * by {@link ClassLoader#getResource(java.lang.String)} and similar 
     * methods.
     * 
     * @param resources names of the resources to be loaded by {@link Resources#get(java.lang.String)}
     * @return new instance of the Bck2Brwsr compiler which inherits
     *   all values from <code>this</code> just adds few more resource names
     *   for processing
     * @since 0.9
     */
    public Bck2Brwsr addResources(String... resources) {
        if (resources.length == 0) {
            return this;
        } else {
            return new Bck2Brwsr(level, exported, this.classes, 
                this.resources.addAndNew(resources), res, extension, classpath
            );
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
        return new Bck2Brwsr(level, exported, classes, resources, res, extension, classpath);
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
        return new Bck2Brwsr(
            level, exported, classes, resources, 
            res, extension, classpath
        );
    }

    /** Should one generate a library? By default the system generates
     * all transitive classes needed by the the transitive closure of
     * {@link #addRootClasses(java.lang.String...)} and {@link #addClasses(java.lang.String...)}.
     * By turning on the library mode, only classes explicitly listed
     * will be included in the archive. The others will be referenced
     * as external ones.
     * <p>
     * A library archive may specify its <em>classpath</em> - e.g. link to
     * other libraries that should also be included in the application. 
     * One can specify the list of libraries as vararg to this method.
     * These are relative URL with respect to location of this library.
     * The runtime system then prefers seek for ".js" suffix of the library
     * and only then seeks for the classical ".jar" path.
     * 
     * @param classpath the array of JARs that are referenced by this library,
     *   one can specify {@code library((String[])null)} to turn the library
     *   mode on, but keep the list of libraries unchanged
     * @return new instance of the compiler with library flag changed
     * @since 0.9
     */
    public Bck2Brwsr library(String... classpath) {
        final StringArray newCP = classpath == null ? this.classpath : StringArray.asList(classpath);
        return new Bck2Brwsr(
            level, exported, classes, 
            resources, res, true, newCP
        );
    }
    
    /** Turns on the standalone mode. E.g. does the opposite of
     * calling {@link #library(java.lang.String...)},
     * but also allows to specify whether the <em>Bck2Brwsr VM</em> should
     * be included at all. If not, only the skeleton of the launcher is
     * generated without any additional VM classes referenced.
     * 
     * @param includeVM should the VM be compiled in, or left out
     * @return new instance of the compiler with standalone mode on
     * @since 0.9
     */
    public Bck2Brwsr standalone(boolean includeVM) {
        return new Bck2Brwsr(
            level, exported, classes, resources, 
            res, includeVM ? false : null, null
        );
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
        return resources(loader, false);
    }

    /** A way to change the provider of additional resources (classes) for the 
     * compiler by specifying classloader to use for loading them.
     * 
     * @param loader class loader to load the resources from
     * @param ignoreBootClassPath <code>true</code> if classes loaded
     *    from <code>rt.jar</code> 
     * @return new instance of the compiler with all values being the same, just 
     *   different resources provider
     * @since 0.9
     */
    public Bck2Brwsr resources(final ClassLoader loader, boolean ignoreBootClassPath) {
        return resources(new LdrRsrcs(loader, ignoreBootClassPath));
    }
    
    /** Generates virtual machine based on previous configuration of the 
     * compiler.
     * 
     * @param out the output to write the generated JavaScript to
     * @throws IOException I/O exception can be thrown when something goes wrong
     * @since 0.5
     */
    public void generate(Appendable out) throws IOException {
        if (level != ObfuscationLevel.NONE) {
            try {
                ClosureWrapper.produceTo(out, level, this);
                return;
            } catch (IOException ex) {
                throw ex;
            } catch (Throwable ex) {
                out.append("/* Failed to obfuscate: " + ex.getMessage()
                               + " */\n");
            }
        }

        VM.compile(out, this);
    }
    
    //
    // Internal getters
    // 
    
    Resources getResources() {
        return res != null ? res : new LdrRsrcs(Bck2Brwsr.class.getClassLoader(), false);
    }
    
    StringArray allResources() {
        return resources;
    }

    StringArray classes() {
        return classes;
    }

    StringArray exported() {
        return exported;
    }
    
    boolean isExtension() {
        return Boolean.TRUE.equals(extension);
    }
    
    boolean includeVM() {
        return extension != null;
    }
    
    StringArray classpath() {
        return classpath;
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
