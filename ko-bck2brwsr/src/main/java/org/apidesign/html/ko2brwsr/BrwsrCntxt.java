/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.ko2brwsr;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.java.html.json.Context;
import org.apidesign.html.json.spi.ContextBuilder;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class BrwsrCntxt implements Technology<Object>, Transfer {
    private BrwsrCntxt() {}
    
    public static final Context DEFAULT;
    static {
        BrwsrCntxt c = new BrwsrCntxt();
        DEFAULT = ContextBuilder.create().withTechnology(c).withTransfer(c).build();
    }
    
    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        ConvertTypes.extractJSON(obj, props, values);
    }

    @Override
    public void loadJSON(final JSONCall call) {
        class R implements Runnable {
            Object[] arr = { null };
            @Override
            public void run() {
                call.notifySuccess(arr[0]);
            }
        }
        R r = new R();
        if (call.isJSONP()) {
            String me = ConvertTypes.createJSONP(r.arr, r);
            ConvertTypes.loadJSON(call.composeURL(me), r.arr, r, me);
        } else {
            ConvertTypes.loadJSON(call.composeURL(null), r.arr, r, null);
        }
    }

    @Override
    public Object wrapModel(Object model) {
        return model;
    }

    @Override
    public void bind(PropertyBinding b, Object model, Object data) {
        Knockout.bind(data, b, b.getPropertyName(), 
            "getValue__Ljava_lang_Object_2", 
            b.isReadOnly() ? null : "setValue__VLjava_lang_Object_2", 
            false, false
        );
    }

    @Override
    public void valueHasMutated(Object data, String propertyName) {
        Knockout.valueHasMutated(data, propertyName);
    }

    @Override
    public void expose(FunctionBinding fb, Object model, Object d) {
        Knockout.expose(d, fb, fb.getFunctionName(), "call__VLjava_lang_Object_2Ljava_lang_Object_2");
    }

    @Override
    public void applyBindings(Object data) {
        Knockout.applyBindings(data);
    }

    @Override
    public Object wrapArray(Object[] arr) {
        return arr;
    }

    @Override
    public <M> M toModel(Class<M> modelClass, Object data) {
        return modelClass.cast(data);
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader r = new InputStreamReader(is);
        for (;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        return ConvertTypes.parse(sb.toString());
    }
}
