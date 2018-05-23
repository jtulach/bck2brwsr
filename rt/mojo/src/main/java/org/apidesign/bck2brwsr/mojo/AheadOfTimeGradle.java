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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import static org.apidesign.bck2brwsr.mojo.AheadOfTimeTask.CONF_NAME;
import org.gradle.api.Action;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.Copy;

public final class AheadOfTimeGradle implements Plugin<Project> {

    @Override
    public void apply(final Project p) {
        final ConfigurationContainer confs = p.getConfigurations();
        if (confs.findByName(CONF_NAME) == null) {
            Configuration bck2brwsr = confs.create(CONF_NAME);
            Configuration runtime = confs.findByName("runtime");
            if (runtime != null) {
                bck2brwsr.extendsFrom(runtime);
            }
        }
        final Configuration bck2brwsr = confs.findByName(CONF_NAME);
        final AheadOfTimeTask aot = p.getTasks().create("bck2brwsrAot", AheadOfTimeTask.class, new Action<AheadOfTimeTask>() {
            @Override
            public void execute(AheadOfTimeTask process) {
            }
        });
        final Copy copyPages = p.getTasks().create("bck2brwsrPages", Copy.class, new Action<Copy>() {
            @Override
            public void execute(Copy t) {
            }
        });
        copyPages.from(p.fileTree("src/main/webapp/pages"));
        copyPages.into(new File(p.getBuildDir(), "web"));
        final ShowTask show = p.getTasks().create("bck2brwsrShow", ShowTask.class, new Action<ShowTask>() {
            @Override
            public void execute(ShowTask process) {
            }
        });
        show.dependsOn(aot, copyPages);
        p.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project p) {
                if (bck2brwsr.getDependencies().isEmpty()) {
                    p.getDependencies().add("bck2brwsr", "org.apidesign.bck2brwsr:emul:" + findOwnVersion() + ":rt");
                    p.getDependencies().add("bck2brwsr", "org.apidesign.bck2brwsr:emul:" + findOwnVersion() + ":bck2brwsr");
                }
                Set<? extends Task> tasks = (Set<? extends Task>) p.property("tasks");
                for (Task task : tasks) {
                    if (task.getName().startsWith("jar")) {
                        aot.dependsOn(task);
                        aot.registerJarTask(task);
                    }
                }
                aot.doLast(new Action<Task>() {
                    @Override
                    public void execute(Task t) {
                        aot.generate(p);
                    }
                });
                show.doLast(new Action<Task>() {
                    @Override
                    public void execute(Task t) {
                        try {
                            show.show(p);
                        } catch (IOException ex) {
                            throw new GradleScriptException(ex.getMessage(), ex);
                        }
                    }
                });
            }
        });
    }

    String findOwnVersion() {
        try (InputStream is = AheadOfTimeGradle.class.getResourceAsStream(
            "/META-INF/maven/org.apidesign.bck2brwsr/bck2brwsr-maven-plugin/pom.properties")
        ) {
            if (is == null) {
                return "1.0-SNAPSHOT";
            }
            Properties p = new Properties();
            p.load(is);
            String version = p.getProperty("version");
            if (version == null) {
                throw new IllegalStateException("Cannot find version");
            }
            return version;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read version", ex);
        }
    }
}
