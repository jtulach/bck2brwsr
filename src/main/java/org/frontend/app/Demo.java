package org.frontend.app;

import com.dukescript.api.javafx.beans.FXBeanInfo;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import static net.java.html.json.Models.applyBindings;

@FXBeanInfo.Generate
public final class Demo extends DemoBeanInfo {
    final StringProperty desc = new SimpleStringProperty(this, "desc", "");
    final ObjectProperty<Item> selected = new SimpleObjectProperty<>(null);
    final ListProperty<Item> todos = new SimpleListProperty<>(this, "todos", FXCollections.observableArrayList(item -> new Observable[] { item.done }));
    final IntegerBinding numTodos = Bindings.createIntegerBinding(todos::size, todos);
    final IntegerBinding numPending = Bindings.createIntegerBinding(() -> {
        return (int) todos.stream().filter((item) -> !item.done.get()).count();
    }, todos);
    final NumberBinding numFinished = Bindings.subtract(numTodos, numPending);
    final StringBinding selectedHash = Bindings.createStringBinding(() -> {
        final Item item = selected.getValue();
        String hash = Route.sanitize(item == null ? "" : item.desc);
        Route.setLocation("hash", hash);
        return hash;
    }, selected);

    void addTodo() {
        todos.getValue().add(new Item(desc.getValue(), null, null, null, null));
        desc.setValue("");
    }

    void clearTodos() {
        todos.removeIf((item) -> item.done.get());
        if (!todos.contains(selected.getValue())) {
            selected.setValue(todos.isEmpty() ? null : todos.get(0));
        }
    }

    void addItem(Item item) {
        todos.add(item);
        if (selected.getValue() == null) {
            selected.setValue(item);
        }
    }

    @FXBeanInfo.Generate
    final class Item extends ItemBeanInfo {
        final String desc;
        final BooleanProperty done = new SimpleBooleanProperty();
        final BooleanBinding selected = Bindings.createBooleanBinding(() -> this == Demo.this.selected.getValue(), Demo.this.selected);
        final String html;
        final String url;
        final String img;
        final String urlInfo;

        public Item(String desc, String img, String html, String url, String urlInfo) {
            this.desc = desc;
            this.html = html;
            this.url = url;
            this.img = img == null ? "images/java.svg" : img;
            this.urlInfo = urlInfo;
        }

        void check() {
            this.done.set(!this.done.get());
        }

        void select() {
            Demo.this.selected.setValue(this);
        }
    }

    public static void onPageLoad() {
        Demo model = new Demo();
        model.desc.setValue("Enjoy Java in browser!");
        Lyrics.initialize(model);
        applyBindings(model);
    }

}
