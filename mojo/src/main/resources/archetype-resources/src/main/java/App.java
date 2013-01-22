package ${package};

import org.apidesign.bck2brwsr.htmlpage.api.*;
import static org.apidesign.bck2brwsr.htmlpage.api.OnEvent.*;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;

/** Edit the index.xhtml file. Use 'id' to name certain HTML elements.
 * Use this class to define behavior of the elements.
 */
@Page(xhtml="index.xhtml", className="Index", properties={
    @Property(name="name", type=String.class)
})
public class App {
    static {
        Index model = new Index();
        model.setName("World");
        model.applyBindings();
    }
    
    @On(event = CLICK, id="hello")
    static void hello(Index m) {
        Element.alert(m.getHelloMessage());
    }
    
    @ComputedProperty
    static String helloMessage(String name) {
        return "Hello " + name + "!";
    }
}
