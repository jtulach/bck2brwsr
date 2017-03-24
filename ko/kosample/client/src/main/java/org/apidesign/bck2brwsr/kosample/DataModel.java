/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.kosample;

import java.util.Timer;
import java.util.TimerTask;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;
import org.apidesign.bck2brwsr.kosample.js.Dialogs;

/** Model annotation generates class Data with
 * one message property, boolean property and read only words property
 */
@Model(className = "Data", targetId="", instance = true, properties = {
    @Property(name = "message", type = String.class),
    @Property(name = "rotating", type = boolean.class)
})
final class DataModel {
    @ComputedProperty static java.util.List<String> words(String message) {
        String[] arr = new String[6];
        String[] words = message == null ? new String[0] : message.split(" ", 6);
        for (int i = 0; i < 6; i++) {
            arr[i] = words.length > i ? words[i] : "!";
        }
        return java.util.Arrays.asList(arr);
    }

    @Function static void turnAnimationOn(Data model) {
        model.setRotating(true);
    }

    @Function static void turnAnimationOff(final Data model) {
        Dialogs.confirmByUser("Really turn off?", new Runnable() {
            @Override
            public void run() {
                model.setRotating(false);
            }
        });
    }

    private static final Timer TIMER = new Timer("Pending tasks");
    private static void schedule(Runnable run, long delay) {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        }, delay);
    }

    @Function static void rotate5s(final Data model) {
        model.setRotating(true);
        schedule(() -> model.setRotating(false), 5000);
    }

    @Function static void showScreenSize(Data model) {
        model.setMessage(Dialogs.screenSize());
    }
    private static Data ui;
    /**
     * Called when the page is ready.
     */
    static void onPageLoad() throws Exception {
        ui = new Data();
        ui.setMessage("Hello World from HTML and Java!");
        ui.applyBindings();

        schedule(() -> ui.startTest(), 1000);
    }

    //
    // testing
    //

    @ModelOperation
    static void startTest(Data model) {
        Dialogs.triggerClick("beginTest");
    }

    private boolean inTesting;

    @Function
    void beginTest(Data model) {
        if (inTesting) {
            model.setRotating(false);
            model.setMessage("Hello World from HTML and Java!");
            inTesting = false;
            return;
        }

        inTesting = true;
        model.setMessage("In testing mode stop Automatic testing?");
        model.setRotating(true);
        schedule(() -> {
            if (inTesting) {
                model.setMessage("In testing mode count down 3s");
            }
        }, 3000);
        schedule(() -> {
            if (inTesting) {
                model.setMessage("In testing mode count down 2s");
            }
        }, 4000);
        schedule(() -> {
            if (inTesting) {
                model.setMessage("In testing mode count down 1s");
            }
        }, 5000);
        schedule(() -> {
            if (inTesting) {
                model.setMessage("Finished testing mode close the browser");
                model.setRotating(false);
                System.exit(0);
            }
        }, 6000);
    }
}
