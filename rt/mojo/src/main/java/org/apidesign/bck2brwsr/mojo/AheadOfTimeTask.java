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
package org.apidesign.bck2brwsr.mojo;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.apidesign.vm4brwsr.ObfuscationLevel;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class AheadOfTimeTask extends DefaultTask {
    private Task jarTask;

    void registerJarTask(Task task) {
        assert this.jarTask == null;
        this.jarTask = task;
    }


    void generate(final Project p) {
        class Work extends AheadOfTimeBase<Object> {
            @Override
            protected File vm() {
                return new File(p.getBuildDir(), "bck2brwsr.js");
            }

            @Override
            protected File mainJavaScript() {
                return new File(p.getBuildDir(), "test.js");
            }

            @Override
            protected String classPathPrefix() {
                return "";
            }

            @Override
            protected ObfuscationLevel obfuscation() {
                return ObfuscationLevel.FULL;
            }

            @Override
            protected String[] exports() {
                return new String[0];
            }

            @Override
            protected boolean ignoreBootClassPath() {
                return true;
            }

            @Override
            protected boolean generateAotLibraries() {
                return true;
            }

            @Override
            protected File mainJar() {
                return invoke(File.class, jarTask, "getArchivePath");
            }

            @Override
            protected Collection<Object> artifacts() {
                return Collections.emptyList();
            }

            @Override
            protected void logInfo(String msg) {
                p.getLogger().info(msg);
            }

            @Override
            protected Exception failure(String msg, Throwable cause) {
                if (cause == null) {
                    return new GradleException(msg);
                } else {
                    return new GradleException(msg, cause);
                }
            }

            @Override
            protected File file(Object a) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected String scope(Object a) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected String classifier(Object a) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected String artifactId(Object a) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected String groupId(Object a) {
                throw new UnsupportedOperationException();
            }

            @Override
            protected String version(Object a) {
                throw new UnsupportedOperationException();
            }

        }

        final Set<?> allSourceSets = (Set<?>) p.property("sourceSets");
        if (allSourceSets == null) {
            throw new GradleException("Cannot find sourceSets for project " + p);
        }
        for (Object sourceSet : allSourceSets) {
            Iterable cp = invoke(Iterable.class, sourceSet, "getRuntimeClasspath");
            for (Object elem : cp) {
                final File pathElement = (File) elem;
                //process.addClasspathEntry(pathElement);
                System.err.println("addClasspathEntry: " + pathElement);
            }
            Iterable<?> outs = invoke(Iterable.class, sourceSet, "getOutput");
            for (Object classes : outs) {
                //process.addRoot((File) classes);
                System.err.println("addRoot: " + classes);
            }
        }

        try {
            new Work().work();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    private static <T> T invoke(Class<T> returnType, Object obj, String methodName) {
        try {
            Method methodOutput = obj.getClass().getMethod(methodName);
            Object res = methodOutput.invoke(obj);
            return returnType.cast(res);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

}
