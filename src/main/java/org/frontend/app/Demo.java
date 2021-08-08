package org.frontend.app;

import com.dukescript.api.javafx.beans.FXBeanInfo;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import static net.java.html.json.Models.applyBindings;

@FXBeanInfo.Generate
public final class Demo extends DemoBeanInfo {

    final StringProperty desc = new SimpleStringProperty(this, "desc", "");
    final ListProperty<String> todos = new SimpleListProperty<>(this, "todos", FXCollections.observableArrayList());
    final IntegerBinding numTodos = Bindings.createIntegerBinding(todos::size, todos);
    
    void addTodo() {
        todos.getValue().add(desc.getValue());
        desc.setValue("");
    }
    
    public static void onPageLoad() {
        Demo model = new Demo();
        model.desc.setValue("Try Java in browser @ " + System.currentTimeMillis());
        applyBindings(model);
    }
}
