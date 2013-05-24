/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.launcher.fximpl;

import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**
 * Demonstrates a WebView object accessing a web page.
 *
 * @see javafx.scene.web.WebView
 * @see javafx.scene.web.WebEngine
 */
public class FXBrwsr extends Application {
    private static final Logger LOG = Logger.getLogger(FXBrwsr.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = new WebViewPane(getParameters().getUnnamed());
        primaryStage.setScene(new Scene(root, 1024, 768));
        LOG.info("Showing the stage");
        primaryStage.show();
        LOG.log(Level.INFO, "State shown: {0}", primaryStage.isShowing());
    }
    
    /**
     * Create a resizable WebView pane
     */
    private class WebViewPane extends Pane {
        private final JVMBridge bridge = new JVMBridge();

        public WebViewPane(List<String> params) {
            LOG.log(Level.INFO, "Initializing WebView with {0}", params);
            VBox.setVgrow(this, Priority.ALWAYS);
            setMaxWidth(Double.MAX_VALUE);
            setMaxHeight(Double.MAX_VALUE);
            WebView view = new WebView();
            view.setMinSize(500, 400);
            view.setPrefSize(500, 400);
            final WebEngine eng = view.getEngine();
            try {
                JVMBridge.addBck2BrwsrLoad(new InitBck2Brwsr(eng));
            } catch (TooManyListenersException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            
            if (params.size() > 0) {
                LOG.log(Level.INFO, "loading page {0}", params.get(0));
                eng.load(params.get(0));
                LOG.fine("back from load");
            }
            eng.setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> t) {
                    final Stage dialogStage = new Stage();
                    dialogStage.initModality(Modality.WINDOW_MODAL);
                    dialogStage.setTitle("Warning");
                    final Button button = new Button("Close");
                    final Text text = new Text(t.getData());
                    
                    VBox box = new VBox();
                    box.setAlignment(Pos.CENTER);
                    box.setSpacing(10);
                    box.setPadding(new Insets(10));
                    box.getChildren().addAll(text, button);
                    
                    dialogStage.setScene(new Scene(box));
                    
                    button.setCancelButton(true);
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            dialogStage.close();
                        }
                    });
                    
                    dialogStage.centerOnScreen();
                    dialogStage.showAndWait();
                }
            });
            GridPane grid = new GridPane();
            grid.setVgap(5);
            grid.setHgap(5);
            GridPane.setConstraints(view, 0, 1, 2, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);
            grid.getColumnConstraints().addAll(new ColumnConstraints(100, 100, Double.MAX_VALUE, Priority.ALWAYS, HPos.CENTER, true), new ColumnConstraints(40, 40, 40, Priority.NEVER, HPos.CENTER, true));
            grid.getChildren().addAll(view);
            getChildren().add(grid);
        }

        boolean initBck2Brwsr(WebEngine webEngine) {
            JSObject jsobj = (JSObject) webEngine.executeScript("window");
            LOG.log(Level.FINE, "window: {0}", jsobj);
            Object prev = jsobj.getMember("bck2brwsr");
            if ("undefined".equals(prev)) {
                System.getProperties().put("webEngine", webEngine);
                jsobj.setMember("bck2brwsr", bridge);
                return true;
            }
            return false;
        }

        @Override
        protected void layoutChildren() {
            List<Node> managed = getManagedChildren();
            double width = getWidth();
            double height = getHeight();
            double top = getInsets().getTop();
            double right = getInsets().getRight();
            double left = getInsets().getLeft();
            double bottom = getInsets().getBottom();
            for (int i = 0; i < managed.size(); i++) {
                Node child = managed.get(i);
                layoutInArea(child, left, top, width - left - right, height - top - bottom, 0, Insets.EMPTY, true, true, HPos.CENTER, VPos.CENTER);
            }
        }

        private class InitBck2Brwsr implements ChangeListener<Void>, Runnable {
            private final WebEngine eng;

            public InitBck2Brwsr(WebEngine eng) {
                this.eng = eng;
            }

            @Override
            public synchronized void changed(ObservableValue<? extends Void> ov, Void t, Void t1) {
                Platform.runLater(this);
                try {
                    wait();
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public synchronized void run() {
                initBck2Brwsr(eng);
                notifyAll();
            }
        }
    }
    
}
