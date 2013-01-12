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

/** Controller created via {@link OnEvent#of(org.apidesign.bck2brwsr.htmlpage.api.Element[])}.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class OnController {
    private final Element[] arr;
    private final OnEvent event;
    
    OnController(OnEvent event, Element[] arr) {
        this.event = event;
        this.arr = arr;
    }
    
    /** Registers a runnable to be performed on associated {@link OnEvent} 
     * and {@link Element}.
     * 
     * @see OnEvent#of
     */
    public void perform(Runnable r) {
        for (Element e : arr) {
            e.on(event, r);
        }
    }
}
