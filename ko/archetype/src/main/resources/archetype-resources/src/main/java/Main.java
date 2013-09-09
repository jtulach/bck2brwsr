package ${package};

public final class Main {
    private Main() {
    }
    
    /**
     * Called when the page is ready.
     */
    static {
        Data d = new Data();
        d.setMessage("Hello World from HTML and Java!");
        d.applyBindings();
    }
}
