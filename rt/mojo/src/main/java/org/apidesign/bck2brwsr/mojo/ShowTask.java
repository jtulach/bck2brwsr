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
import java.util.LinkedList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import static org.apidesign.bck2brwsr.mojo.AheadOfTimeGradle.PROP_MAIN_CLASS_NAME;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class ShowTask extends DefaultTask {
    void show(final Project p, boolean continuous) throws IOException {
        String mainClass = AheadOfTimeGradle.findMainClass(p);
        if (mainClass == null) {
            throw new GradleScriptException("Define property " + PROP_MAIN_CLASS_NAME + " in the project", null);
        }

        File web = new File(p.getBuildDir(), "web");
        web.mkdirs();
        UtilBase.verifyIndexHtml(web, "main.js", mainClass);

        /*
        URL u = getClass().getClassLoader().getResource("META-INF/gradle-plugins/bck2brwsr.properties");
        JarURLConnection juc = (JarURLConnection) u.openConnection();
        URL myJar = juc.getJarFileURL();
        getLogger().info("cp: " + myJar);
        getLogger().info("web: " + web);
        */

        final DelegatingHandler handler = new DelegatingHandler(getLogger());
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread t = Thread.currentThread();
                try {
                    ShowMain.execute(handler, web);
                } catch (IOException ex) {
                    getLogger().error(ex.getMessage(), ex);
                } finally {
                    getLogger().warn("HTTP server is shutting down.");
                }
            }
        }, "Bck2Brwsr Server");
        t.setDaemon(true);
        t.start();
        if (!continuous) {
            try {
                getLogger().warn("Avaiting HTTP server...");
                t.join();
            } catch (InterruptedException ex) {
                getLogger().warn("Interrupted", ex);
            } finally {
                getLogger().warn("HTTP server finished. Continuing...");
            }
        }
    }

    private static final class DelegatingHandler extends Handler {
        private final Logger gradleLogger;
        private final LinkedList<LogRecord> pending;

        public DelegatingHandler(Logger gradleLogger) {
            this.gradleLogger = gradleLogger;
            this.pending = new LinkedList<>();
            setLevel(Level.ALL);
        }

        @Override
        public synchronized void publish(LogRecord record) {
            this.pending.add(record);
        }

        private void logPending() {
            LogRecord[] arr;
            synchronized (this) {
                arr = pending.toArray(new LogRecord[0]);
                pending.clear();
            }
            SimpleFormatter sf = new SimpleFormatter();
            for (LogRecord record : arr) {
                String msg = sf.formatMessage(record);
                if (msg == null) {
                    msg = "";
                }
                if (gradleLogger == null) {
                    System.out.append(msg);
                } else {
                    for (String line : msg.split("\n")) {
                        if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                            gradleLogger.warn(line);
                        } else {
                            gradleLogger.lifecycle(line);
                        }
                    }
                }
            }
        }

        @Override
        public void flush() {
            logPending();
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
