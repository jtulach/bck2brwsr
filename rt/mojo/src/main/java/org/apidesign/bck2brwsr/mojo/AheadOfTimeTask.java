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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apidesign.vm4brwsr.ObfuscationLevel;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.OutputFiles;

public class AheadOfTimeTask extends DefaultTask {
    static final String CONF_NAME = "bck2brwsr";
    private Task jarTask;
    @OutputFile
    private File bck2brwsrJs;
    @OutputFile
    private File mainJs;
    @OutputFiles
    private Map<String,File> libraries;

    public AheadOfTimeTask() {
    }

    void registerJarTask(Project p, Task task) {
        assert this.jarTask == null;
        this.jarTask = task;
        this.bck2brwsrJs = new File(webDir(p), "bck2brwsr.js");
        this.mainJs = new File(webDir(p), "main.js");
        this.libraries = new TreeMap<>();
    }

    private File webDir(Project p) {
        return new File(p.getBuildDir(), "web");
    }

    private static Collection<ResolvedArtifact> mainClassPath(Project p, String confName, boolean fail) {
        Configuration conf = p.getConfigurations().getByName(confName);
        if (conf == null) {
            if (fail) {
                throw new GradleException("Cannot find " + confName + " configuration for project " + p);
            }
            return Collections.emptyList();
        }
        return conf.getResolvedConfiguration().getResolvedArtifacts();
    }

    void generate(final Project p) {
        class Work extends AheadOfTimeBase<ResolvedArtifact> {
            private Collection<ResolvedArtifact> bck2brwsr;
            private Collection<ResolvedArtifact> compileClasspath;

            @Override
            protected File vm() {
                return getBck2brwsrJs();
            }

            @Override
            protected File mainJavaScript() {
                return getMainJs();
            }

            @Override
            protected File libraryPath(String path) {
                File dir = new File(mainJavaScript().getParent(), "lib");
                File jsFile = new File(dir, path);
                libraries.put(path, jsFile);
                return jsFile;
            }

            @Override
            protected ObfuscationLevel obfuscation() {
                Map<String, ?> props = p.getProperties();
                Object gen = props.get("bck2brwsrObfuscation");
                if (gen instanceof ObfuscationLevel) {
                    return (ObfuscationLevel) gen;
                }
                if (gen instanceof String) {
                    try {
                        return ObfuscationLevel.valueOf((String) gen);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Expeceting one of " + Arrays.toString(ObfuscationLevel.values()) + " but was " + gen);
                    }
                }
                return ObfuscationLevel.MINIMAL;
            }

            @Override
            protected String[] exports() {
                String mainClass = AheadOfTimeGradle.findMainClass(p);
                if (mainClass != null) {
                    return new String[] { mainClass };
                }
                return new String[0];
            }

            @Override
            protected boolean ignoreBootClassPath() {
                return true;
            }

            @Override
            protected boolean generateAotLibraries() {
                Map<String, ?> props = p.getProperties();
                Object gen = props.get("bck2brwsrGenerateAotLibraries");
                if (gen instanceof String) {
                    return Boolean.valueOf((String)gen);
                }
                if (gen instanceof Boolean) {
                    return (Boolean)gen;
                }
                return true;
            }

            @Override
            protected File mainJar() {
                return invoke(File.class, jarTask, "getArchivePath");
            }

            @Override
            protected Iterable<ResolvedArtifact> artifacts() {
                Set<ResolvedArtifact> all = new LinkedHashSet<>();
                if (bck2brwsr == null) {
                    bck2brwsr = mainClassPath(p, CONF_NAME, true);
                }
                all.addAll(bck2brwsr);
                if (compileClasspath == null) {
                    compileClasspath = mainClassPath(p, "compileClasspath", false);
                }
                all.addAll(compileClasspath);
                return all;
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
            protected Scope scope(ResolvedArtifact a) {
                if (bck2brwsr != null && bck2brwsr.contains(a)) {
                    return Scope.RUNTIME;
                }
                return Scope.PROVIDED;
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

    public File getBck2brwsrJs() {
        return bck2brwsrJs;
    }

    public File getMainJs() {
        return mainJs;
    }

    public Map<String,File> getLibraries() {
        return libraries;
    }

}
