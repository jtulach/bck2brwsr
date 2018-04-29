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

import java.util.Set;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public final class AheadOfTimeGradle implements Plugin<Project> {

    @Override
    public void apply(final Project p) {
        final AheadOfTimeTask aot = p.getTasks().create("bck2brwsrAot", AheadOfTimeTask.class, new Action<AheadOfTimeTask>() {
            @Override
            public void execute(AheadOfTimeTask process) {
            }
        });
        p.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(final Project p) {
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
            }
        });
    }
}
