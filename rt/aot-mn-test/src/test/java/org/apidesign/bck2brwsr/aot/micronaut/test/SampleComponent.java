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
package org.apidesign.bck2brwsr.aot.micronaut.test;

import io.micronaut.context.annotation.Executable;

@Observable.UI
public class SampleComponent {
    public SampleComponent() {
    }

    boolean ok = true;
    private String fine = "ok";
    private final String immutable = "Hi";

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getFine() {
        return fine;
    }

    public void setFine(String fine) {
        this.fine = fine;
    }

    public String getImmutable() {
        return immutable;
    }
    int counter;
    Object ev;

    private void callback(Object ev) {
        counter++;
        this.ev = ev;
    }

    @Executable
    void noArgCallback() {
        counter++;
        this.ev = null;
    }

    @Executable
    void actionCallback(Object ev) {
        counter++;
        this.ev = ev;
    }

    /*
    void actionDataCallback(ActionDataEvent ev) {
    counter += ev.getProperty(Number.class, null).intValue();
    this.ev = ev;
    }
     */
    @Executable
    int notAnAction() {
        return 0;
    }

    static void ignore() {
    }
}
