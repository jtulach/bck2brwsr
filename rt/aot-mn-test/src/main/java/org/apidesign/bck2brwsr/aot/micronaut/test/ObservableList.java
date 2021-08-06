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

import java.util.AbstractList;
import java.util.List;
import org.netbeans.html.json.spi.Proto;

final class ObservableList<T> extends AbstractList<T> {
    private final String name;
    private final Proto proto;
    private final List<T> delegate;

    private ObservableList(String name, Proto p, List<T> arr) {
        this.name = name;
        this.proto = p;
        this.delegate = arr;
    }

    static <T> List<T> wrap(String name, Proto p, List<T> res) {
        if (res instanceof ObservableList<?>) {
            ObservableList<?> ol = (ObservableList<?>) res;
            if (ol.proto == p && ol.name.equals(name)) {
                return res;
            }
        }
        return new ObservableList<>(name, p, res);
    }

    @Override
    public T get(int index) {
        notifyAccess();
        return delegate.get(index);
    }

    @Override
    public int size() {
        notifyAccess();
        return delegate.size();
    }

    @Override
    public T set(int index, T element) {
        T r = delegate.set(index, element);
        notifyChange();
        return r;
    }

    @Override
    public void add(int index, T element) {
        delegate.add(index, element);
        notifyChange();
    }

    @Override
    public T remove(int index) {
        T r = delegate.remove(index);
        notifyChange();
        return r;
    }

    private void notifyChange() {
        proto.valueHasMutated(name);
    }

    private void notifyAccess() {
        proto.accessProperty(name);
    }
}
