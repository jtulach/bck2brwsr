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
import java.util.LinkedHashSet;
import java.util.Set;
import org.apidesign.vm4brwsr.ObfuscationLevel;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyArtifact;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.UnknownConfigurationException;

public class AheadOfTimeTask extends DefaultTask {
    static final String CONF_NAME = "bck2brwsr";
    private Task jarTask;

    public AheadOfTimeTask() {
    }

    void registerJarTask(Task task) {
        assert this.jarTask == null;
        this.jarTask = task;
    }

    Iterable<ResolvedArtifact> mainClassPath(Project p) {
        Configuration conf = p.getConfigurations().getByName(CONF_NAME);
        if (conf == null) {
            throw new GradleException("Cannot find " + CONF_NAME + " configuration for project " + p);
        }
        return conf.getResolvedConfiguration().getResolvedArtifacts();
    }

    void dump(Project p) {
        System.err.println("this: " + this);

        final Set<?> allSourceSets = (Set<?>) p.property("sourceSets");
        if (allSourceSets == null) {
            throw new GradleException("Cannot find sourceSets for project " + p);
        }
        for (Object sourceSet : allSourceSets) {
            final String name = invoke(String.class, sourceSet, "getName");
            System.err.println("set: " + name);
            Iterable cp = invoke(Iterable.class, sourceSet, "getCompileClasspath");
            for (Object elem : cp) {
                final File pathElement = (File) elem;
                //process.addClasspathEntry(pathElement);
                System.err.println("  addClasspathEntry: " + pathElement);
            }
            final String runtimeName = invoke(String.class, sourceSet, "getRuntimeConfigurationName");
            dumpConf(p, runtimeName);
            final String compileName = invoke(String.class, sourceSet, "getCompileConfigurationName");
            Configuration conf = dumpConf(p, compileName);
            for (Dependency d : conf.getAllDependencies()) {
                System.err.println("  dep: " + d.getGroup() + " name: " + d.getName() + " @ " + d.getVersion());
                if (d instanceof ModuleDependency) {
                    System.err.println("     trans: " + ((ModuleDependency) d).isTransitive());
                }
                Set<?> artifacts = invoke(Set.class, d, "getArtifacts");
                for (Object ao : artifacts) {
                    DependencyArtifact da = (DependencyArtifact) ao;
                    System.err.println("    a: " + da.getName() + " u: " + da.getUrl());
                }
            }
            Iterable<?> outs = invoke(Iterable.class, sourceSet, "getOutput");
            for (Object classes : outs) {
                //process.addRoot((File) classes);
                System.err.println("  addRoot: " + classes);
            }
        }
    }

    private Configuration dumpConf(Project p, final String compileName) throws UnknownConfigurationException, ResolveException {
        Configuration conf = p.getConfigurations().getByName(compileName);
        for (ResolvedArtifact a : conf.getResolvedConfiguration().getResolvedArtifacts()) {
            System.err.println("       g: " + a.getModuleVersion().getId().getGroup());
            System.err.println("       a: " + a.getModuleVersion().getId().getName());
            System.err.println("       v: " + a.getModuleVersion().getId().getVersion());
            System.err.println("       f: " + a.getFile());
        }
        return conf;
    }

    void generate(final Project p) {
        class Work extends AheadOfTimeBase<ResolvedArtifact> {
            private File webDir() {
                return new File(p.getBuildDir(), "web");
            }

            @Override
            protected File vm() {
                return new File(webDir(), "bck2brwsr.js");
            }

            @Override
            protected File mainJavaScript() {
                return new File(webDir(), "main.js");
            }

            @Override
            protected String classPathPrefix() {
                return "lib";
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
            protected Iterable<ResolvedArtifact> artifacts() {
                return mainClassPath(p);
            }

            @Override
            protected void logInfo(String msg) {
                p.getLogger().lifecycle(msg);
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
            protected File file(ResolvedArtifact a) {
                return a.getFile();
            }

            @Override
            protected String scope(ResolvedArtifact a) {
                return "runtime";
            }

            @Override
            protected String classifier(ResolvedArtifact a) {
                return a.getClassifier();
            }

            @Override
            protected String artifactId(ResolvedArtifact a) {
                return a.getName();
            }

            @Override
            protected String groupId(ResolvedArtifact a) {
                return a.getModuleVersion().getId().getGroup();
            }

            @Override
            protected String version(ResolvedArtifact a) {
                return a.getModuleVersion().getId().getVersion();
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
