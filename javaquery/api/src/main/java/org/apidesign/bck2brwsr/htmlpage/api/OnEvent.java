/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

/** Type of events to use in connection with {@link On} annotation.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public enum OnEvent {
    ABORT("onabort"),
    BLUR("onblur"),
    CAN_PLAY("oncanplay"),
    CAN_PLAY_THROUGH("oncanplaythrough"),
    CHANGE("onchange"),
    CLICK("onclick"),
    CONTEXT_MENU("oncontextmenu"),
    DBL_CLICK("ondblclick"),
    DRAG("ondrag"),
    DRAG_END("ondragend"),
    DRAG_ENTER("ondragenter"),
    DRAG_LEAVE("ondragleave"),
    DRAG_OVER("ondragover"),
    DRAG_START("ondragstart"),
    DROP("ondrop"),
    DURATION_CHANGE("ondurationchange"),
    EMPTIED("onemptied"),
    ENDED("onended"),
    ERROR("onerror"),
    FOCUS("onfocus"),
    FORM_CHANGE("onformchange"),
    FORM_INPUT("onforminput"),
    INPUT("oninput"),
    INVALID("oninvalid"),
    KEY_DOWN("onkeydown"),
    KEY_PRESS("onkeypress"),
    KEY_UP("onkeyup"),
    LOAD("onload"),
    LOADED_DATA("onloadeddata"),
    LOADED_META_DATA("onloadedmetadata"),
    LOAD_START("onloadstart"),
    MOUSE_DOWN("onmousedown"),
    MOUSE_MOVE("onmousemove"),
    MOUSE_OUT("onmouseout"),
    MOUSE_OVER("onmouseover"),
    MOUSE_UP("onmouseup"),
    MOUSE_WHEEL("onmousewheel"),
    PAUSE("onpause"),
    PLAY("onplay"),
    PLAYING("onplaying"),
    PROGRESS("onprogress"),
    RATE_CHANGE("onratechange"),
    READY_STATE_CHANGE("onreadystatechange"),
    SCROLL("onscroll"),
    SEEKED("onseeked"),
    SEEKING("onseeking"),
    SELECT("onselect"),
    SHOW("onshow"),
    STALLED("onstalled"),
    SUBMIT("onsubmit"),
    SUSPEND("onsuspend"),
    TIME_UPDATE("ontimeupdate"),
    VOLUME_CHANGE("onvolumechange"),
    WAITING("onwaiting");
    
    final String id;
    
    private OnEvent(String id) {
        this.id = id;
    }
    
    /** The name of property this event is referenced by from an {@link Element}.
     * For {@link OnEvent#CHANGE}, it is <code>onchange</code>.
     */
    public String getElementPropertyName() {
        return id;
    }
    
    /** What should happen when this even happen on one
     * of associated elements. Continue by calling {@link OnController#perform(java.lang.Runnable)}
     * method.
     * 
     * @param elmnts one or more elements
     * @return controller with <code>perform</code> method.
     */
    public OnController of(Element... elmnts) {
        return new OnController(this, elmnts);
    }
}
