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
    private Runnable onchange;

    public KOList(String name, String... deps) {
        this.name = name;
        this.deps = deps;
    }
    
    public void assign(Knockout model) {
        if (this.model != model) {
            this.model = model;
            notifyChange();
        }
    }
    
    public KOList<T> onChange(Runnable r) {
        if (this.onchange != null) {
            throw new IllegalStateException();
        }
        this.onchange = r;
        return this;
    }

    @Override
    public boolean add(T e) {
        boolean ret = super.add(e);
        notifyChange();
        return ret;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean ret = super.addAll(c);
        notifyChange();
        return ret;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean ret = super.addAll(index, c);
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
        if (m != null) {
            m.valueHasMutated(name);
            for (String dependant : deps) {
                m.valueHasMutated(dependant);
            }
        }
        Runnable r = onchange;
        if (r != null) {
            r.run();
        }
    }

    @Override
    public KOList clone() {
        KOList ko = (KOList)super.clone();
        ko.model = null;
        return ko;
    }
    
}
