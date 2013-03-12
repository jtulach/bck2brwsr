package ${package};

import org.apidesign.bck2brwsr.htmlpage.api.*;
import static org.apidesign.bck2brwsr.htmlpage.api.OnEvent.*;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;
import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;

/** Edit the index.xhtml file. Use 'id' to name certain HTML elements.
 * Use this class to define behavior of the elements.
 */
@Page(xhtml="index.html", className="Index", properties={
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
        GraphicsContext g = m.CANVAS.getContext();
        g.clearRect(0, 0, 1000, 1000);
        g.setFont("italic 40px Calibri");
        g.fillText(m.getHelloMessage(), 10, 40);
    }
    
    @ComputedProperty
    static String helloMessage(String name) {
        return "Hello " + name + "!";
    }
}
