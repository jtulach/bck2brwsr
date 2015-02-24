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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.htmlpage.api;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Anton Epple <toni.epple@eppleton.de>
 */
public class ImageData {

    private Object imageData;
    private Data data;

    public ImageData(Object imageData) {
        this.imageData = imageData;
    }
    
    public Data getData(){
        if (data == null){
            data = new Data(getDataImpl(imageData));
        }
        return data;
    }
    
    @JavaScriptBody(args = {"imageData"}, body = "return imageData.data")
    public native Object getDataImpl(Object imageData);

    public double getWidth() {
        return getWidthImpl(imageData);
    }

    @JavaScriptBody(args = {"imageData"}, body = "return imagedata.width;")
    private static native double getWidthImpl(Object imageData);

    public double getHeight() {
        return getHeightImpl(imageData);
    }

    @JavaScriptBody(args = {"imageData"}, body = "return imagedata.height;")
    private static native double getHeightImpl(Object imageData);

    Object object() {
        return imageData;
    }

    public static class Data {

        Object data;

        public Data(Object data) {
            this.data = data;
        }

        public int get(int index) {
            return getImpl(data, index);
        }

        public void set(int index, int value) {
            setImpl(data, index, value);
        }

        @JavaScriptBody(args = {"data", "index", "value"}, body = "data[index]=value;")
        private static native void setImpl(Object data, int index, int value);

        @JavaScriptBody(args = {"imagedata", "index"}, body = "return data[index];")
        private static native int getImpl(Object data, int index);
    }
}
