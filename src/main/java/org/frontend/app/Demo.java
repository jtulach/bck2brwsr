package org.frontend.app;

import com.dukescript.api.javafx.beans.FXBeanInfo;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import static net.java.html.json.Models.applyBindings;

@FXBeanInfo.Generate
public final class Demo extends DemoBeanInfo {

    final StringProperty desc = new SimpleStringProperty(this, "desc", "");
    final ListProperty<Item> todos = new SimpleListProperty<>(this, "todos", FXCollections.observableArrayList(item -> new Observable[] { item.done }));
    final IntegerBinding numTodos = Bindings.createIntegerBinding(todos::size, todos);
    final IntegerBinding numPending = Bindings.createIntegerBinding(() -> {
        return (int) todos.stream().filter((item) -> !item.done.get()).count();
    }, todos);
    final NumberBinding numFinished = Bindings.subtract(numTodos, numPending);

    void addTodo() {
        todos.getValue().add(new Item(desc.getValue()));
        desc.setValue("");
    }

    void clearTodos() {
        todos.getValue().removeIf((item) -> item.done.get());
    }

    @FXBeanInfo.Generate
    final class Item extends ItemBeanInfo {
        final StringProperty desc = new SimpleStringProperty();
        final BooleanProperty done = new SimpleBooleanProperty();

        public Item(String desc) {
            this.desc.setValue(desc);
        }

        void check() {
            this.done.set(!this.done.get());
        }
    }

    public static void onPageLoad() {
        Demo model = new Demo();
        model.desc.setValue("Try Java in browser @ " + System.currentTimeMillis());
        applyBindings(model);
    }

}
