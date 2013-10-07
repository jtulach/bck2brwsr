package ${package};

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

/** Model annotation generates class Data with 
 * one message property, boolean property and read only words property
 */
@Model(className = "Data", properties = {
    @Property(name = "message", type = String.class),
    @Property(name = "on", type = boolean.class)
})
final class DataModel {
    @ComputedProperty static java.util.List<String> words(String message) {
        String[] arr = new String[6];
        String[] words = message == null ? new String[0] : message.split(" ", 6);
        for (int i = 0; i < 6; i++) {
            arr[i] = words.length > i ? words[i] : "!";
        }
        return java.util.Arrays.asList(arr);
    }
    
    @Function static void turnOn(Data model) {
        model.setOn(true);
    }
    @Function static void turnOff(Data model) {
        model.setOn(false);
    }
}
