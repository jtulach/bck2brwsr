package org.frontend.app;

import com.dukescript.api.canvas.GraphicsContext2D;
import com.dukescript.api.canvas.Style;
import com.dukescript.api.events.EventSource;
import com.dukescript.api.events.KeyEvent;
import com.dukescript.api.events.MouseEvent;

public final class Demo  {
    private final GraphicsContext2D ctx;
    private final EventSource es;
    private int y = 100;

    private Demo(String id) {
        this.ctx = GraphicsContext2D.getOrCreate(id);
        this.es = EventSource.create(ctx, id);


        es.addEventHandler(KeyEvent.KEY_PRESSED, (ev) -> {
            System.err.println("ev: " + ev);
            KeyEvent kev = (KeyEvent) ev;
            switch (kev.getKeyCode()) {
                case UP:
                    y -= 10;
                    break;
                case DOWN:
                    y += 10;
                    break;
            }
            paint();
        });
        es.addEventHandler(MouseEvent.MOUSE_MOVED, (ev) -> {
            System.err.println("mouse: " + ev);
            MouseEvent mev = (MouseEvent) ev;
            y = (int) mev.getY();
            paint();
        });
    }

    void paint() {
        ctx.clearRect(0, 0, 1024, 1024);
        ctx.setStrokeStyle(Style.Color.BLACK);
        ctx.setFillStyle(Style.Color.BLACK);
        ctx.strokeRect(100, y - 150, 300, 400);
        ctx.beginPath();
        ctx.moveTo(100, y-150);
        ctx.lineTo(100 + 300, y - 150 + 400);
        ctx.stroke();
    }

    public static void onPageLoad() {
        Demo model = new Demo("canvas");
        model.paint();
    }
}
