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
package org.apidesign.bck2brwsr.htmlpage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apidesign.bck2brwsr.core.JavaScriptOnly;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class KOList<T> extends ArrayList<T> {
    private final String name;
    private final String[] deps;
    private Knockout model;

    public KOList(String name, String... deps) {
        this.name = name;
        this.deps = deps;
    }
    
    public void assign(Knockout model) {
        this.model = model;
    }

    @Override
    public boolean add(T e) {
        boolean ret = super.add(e);
        notifyChange();
        return ret;
    }

    @Override
    public boolean remove(Object o) {
        boolean ret = super.remove(o);
        notifyChange();
        return ret;
    }

    @Override
    public void clear() {
        super.clear();
        notifyChange();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean ret = super.removeAll(c);
        notifyChange();
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean ret = super.retainAll(c);
        notifyChange();
        return ret;
    }

    @Override
    public T set(int index, T element) {
        T ret = super.set(index, element);
        notifyChange();
        return ret;
    }

    @Override
    public void add(int index, T element) {
        super.add(index, element);
        notifyChange();
    }

    @Override
    public T remove(int index) {
        T ret = super.remove(index);
        notifyChange();
        return ret;
    }

    @Override
    public String toString() {
        Iterator<T> it = iterator();
        if (!it.hasNext()) {
            return "[]";
        }
        String sep = "";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (it.hasNext()) {
            T t = it.next();
            sb.append(sep);
            sb.append(ConvertTypes.toJSON(t));
            sep = ",";
        }
        sb.append(']');
        return sb.toString();
    }
    
    
    @JavaScriptOnly(name = "koArray", value = "function() { return this.toArray___3Ljava_lang_Object_2(); }")
    private static native int koArray();

    private void notifyChange() {
        Knockout m = model;
        if (m == null) {
            return;
        }
        m.valueHasMutated(name);
        for (String dependant : deps) {
            m.valueHasMutated(dependant);
        }
    }
    
    
}
