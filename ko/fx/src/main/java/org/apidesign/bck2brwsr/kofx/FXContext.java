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
package org.apidesign.bck2brwsr.kofx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import netscape.javascript.JSObject;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.PropertyBinding;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.openide.util.lookup.ServiceProvider;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Registers {@link ContextProvider}, so {@link ServiceLoader} can find it.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = Contexts.Provider.class)
public final class FXContext
implements Technology<JSObject>, Transfer, Contexts.Provider {
    static final Logger LOG = Logger.getLogger(FXContext.class.getName());

    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        context.register(Technology.class, this, 100);
        context.register(Transfer.class, this, 100);
    }

    @Override
    public JSObject wrapModel(Object model) {
        return (JSObject) Knockout.createBinding(model).koData();
    }

    @Override
    public void bind(PropertyBinding b, Object model, JSObject data) {
        final boolean isList = false;
        final boolean isPrimitive = false;
        Knockout.bind(data, model, b, isPrimitive, isList);
    }

    @Override
    public void valueHasMutated(JSObject data, String propertyName) {
        Knockout.valueHasMutated(data, propertyName);
    }

    @Override
    public void expose(FunctionBinding fb, Object model, JSObject d) {
        Knockout.expose(d, fb);
    }

    @Override
    public void applyBindings(JSObject data) {
        Knockout.applyBindings(data);
    }

    @Override
    public Object wrapArray(Object[] arr) {
        return Knockout.toArray(arr);
    }

    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        LoadJSON.extractJSON(obj, props, values);
    }

    @Override
    public void loadJSON(final JSONCall call) {
        LoadJSON.loadJSON(call);
    }

    @Override
    public <M> M toModel(Class<M> modelClass, Object data) {
        if (data instanceof JSObject) {
            data = ((JSObject)data).getMember("ko-fx.model"); // NOI18N
        }
        return modelClass.cast(data);
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        return LoadJSON.parse(is);
    }
}
