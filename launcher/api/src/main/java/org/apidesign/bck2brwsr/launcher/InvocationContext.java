/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.launcher;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Represents individual method invocation, its context and its result.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class InvocationContext {
    final CountDownLatch wait = new CountDownLatch(1);
    final Class<?> clazz;
    final String methodName;
    private final Launcher launcher;
    private String result;
    private Throwable exception;
    String html;
    String[] args;
    final List<Resource> resources = new ArrayList<>();
    private int time;

    InvocationContext(Launcher launcher, Class<?> clazz, String methodName) {
        this.launcher = launcher;
        this.clazz = clazz;
        this.methodName = methodName;
    }
    
    /** An HTML fragment to be available for the execution. Useful primarily when
     * executing in a browser via {@link Launcher#createBrowser(java.lang.String)}.
     * @param html the html fragment
     */
    public void setHtmlFragment(String html) {
        this.html = html;
    }

    /** Arguments to pass to the invoked method.
     * @param args textual arguments to pass to the method
     * @since 0.18
     */
    public void setArguments(String... args) {
        this.args = args;
    }
    
    /** HTTP resource to be available during execution. An invocation may
     * perform an HTTP query and obtain a resource relative to the page.
     */
    public void addHttpResource(String relativePath, String mimeType, String[] parameters, InputStream content) {
        if (relativePath == null || mimeType == null || content == null || parameters == null) {
            throw new NullPointerException();
        }
        resources.add(new Resource(content, mimeType, relativePath, parameters));
    }
    
    /** Invokes the associated method. 
     * @return the textual result of the invocation
     * @throws java.io.IOException if execution fails
     */
    public String invoke() throws IOException {
        launcher.runMethod(this);
        return toString();
    }

    /** Invokes the associated method.
     * @param time one element array to store the length of the invocation
     *    - can be <code>null</code>
     * @return the textual result of the invocation
     * @throws java.io.IOException if execution fails
     * @since 0.20
     */
    public String invoke(int[] time) throws IOException {
        launcher.runMethod(this);
        if (time != null) {
            time[0] = this.time;
        }
        return toString();
    }
    
    /** Obtains textual result of the invocation.
     * @return text representing the exception or result value
     */
    @Override
    public String toString() {
        if (exception != null) {
            return exception.toString();
        }
        return result;
    }
    
    /**
     * @param timeOut
     * @throws InterruptedException 
     */
    void await(long timeOut) throws InterruptedException {
        wait.await(timeOut, TimeUnit.MILLISECONDS);
    }
    
    void result(String r, int time, Throwable e) {
        this.time = time;
        this.result = r;
        this.exception = e;
        wait.countDown();
    }

    static final class Resource {
        final InputStream httpContent;
        final String httpType;
        final String httpPath;
        final String[] parameters;

        Resource(InputStream httpContent, String httpType, String httpPath,
            String[] parameters
        ) {
            httpContent.mark(Integer.MAX_VALUE);
            this.httpContent = httpContent;
            this.httpType = httpType;
            this.httpPath = httpPath;
            this.parameters = parameters;
        }
    }
}
