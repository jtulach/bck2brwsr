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

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import org.apidesign.bck2brwsr.launcher.Launcher;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class ShowTask extends DefaultTask {
    void show(final Project p) throws IOException {
        File web = new File(p.getBuildDir(), "web");
        web.mkdirs();
        File index = new File(web, "index.html");
        if (!index.exists()) {
            String mainClass = (String) p.getProperties().get("mainClassName");
            if (mainClass == null) {
                throw new GradleScriptException("Define property ext.mainClassName in the project", null);
            }
            try (FileWriter w = new FileWriter(index)) {
                w.write(""
                    + "<html>\n"
                    + "<body>\n"
                    + "<script src='bck2brwsr.js'></script>\n"
                    + "<script>\n"
                    + "var vm = bck2brwsr('main.js');\n"
                    + "vm.loadClass('" + mainClass + "', function(mainClass) {\n"
                    + "  mainClass.invoke('main');\n"
                    + "});\n"
                    + "</script>\n"
                    + "</body>\n"
                    + "</html>\n"
                );
            }
        }

        final Logger gradleLogger = this.getLogger();
        final java.util.logging.Logger javaLogger = java.util.logging.Logger.getLogger("org.apidesign.bck2brwsr.launcher.BaseHTTPLauncher");
        final java.util.logging.Logger serverLogger = java.util.logging.Logger.getLogger("org.glassfish.grizzly.http.server");
        final DelegatingHandler handler = new DelegatingHandler(gradleLogger);
        javaLogger.addHandler(handler);
        serverLogger.addHandler(handler);

        try (Closeable httpServer = Launcher.showDir("bck2brwsr", web, null, "index.html")) {
            keepOneHandler(javaLogger, handler);
            keepOneHandler(serverLogger, handler);

            final CountDownLatch finished = new CountDownLatch(1);
            if (httpServer instanceof Flushable) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ((Flushable) httpServer).flush();
                        } catch (IOException ex) {
                            gradleLogger.warn(ex.getMessage(), ex);
                        } finally {
                            finished.countDown();
                        }
                    }
                });
            }
            while (finished.getCount() > 0) {
                handler.logPending();
                try {
                    finished.await(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    gradleLogger.warn(ex.getMessage(), ex);
                }
            }
        }
        gradleLogger.warn("Finished");
    }

    private void keepOneHandler(final java.util.logging.Logger logger, final Handler handler) {
        for (Handler h : logger.getHandlers()) {
            if (h != handler) {
                logger.removeHandler(h);
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

        public void logPending() {
            LogRecord[] arr;
            synchronized (this) {
                arr = pending.toArray(new LogRecord[0]);
                pending.clear();
            }
            SimpleFormatter sf = new SimpleFormatter();
            for (LogRecord record : arr) {
                String msg = sf.formatMessage(record);
                for (String line : msg.split("\n")) {
                    if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
                        gradleLogger.warn(line);
                    } else {
                        gradleLogger.lifecycle(line);
                    }
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
