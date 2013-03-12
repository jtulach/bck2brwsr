/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.htmlpage.api;

/** Handler to be called when an event in an HTML {@link Page} appears.
 * @see OnEvent
 * @see OnController
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public interface OnHandler {
    /** Called when a DOM event appears
     * 
     * @param event the event as produced by the browser
     * @throws Exception execution can throw exception
     */
    public void onEvent(Object event) throws Exception;
}
