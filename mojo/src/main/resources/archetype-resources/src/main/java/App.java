package ${package};

import org.apidesign.bck2brwsr.htmlpage.api.*;
import static org.apidesign.bck2brwsr.htmlpage.api.OnEvent.*;
import org.apidesign.bck2brwsr.htmlpage.api.Page;

/** Edit the index.xhtml file. Use 'id' to name certain HTML elements.
 * Use this class to define behavior of the elements.
 */
@Page(xhtml="index.xhtml", className="Index")
public class App {
    @On(event = CLICK, id="hello")
    static void hello() {
        Index.HELLO.setDisabled(true);
        Element.alert("Hello World!");
    }
}
