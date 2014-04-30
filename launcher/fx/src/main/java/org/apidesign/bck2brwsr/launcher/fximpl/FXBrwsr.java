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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
        WebView view = new WebView();
        WebController wc = new WebController(view, getParameters().getUnnamed());
        
        FXInspect.initialize(view.getEngine());

        final VBox vbox = new VBox();
        vbox.setAlignment( Pos.CENTER );
        vbox.setStyle( "-fx-background-color: #808080;");


        HBox hbox = new HBox();
        hbox.setStyle( "-fx-background-color: #808080;");
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(vbox);
        HBox.setHgrow(vbox, Priority.ALWAYS);
        vbox.getChildren().add(view);
        VBox.setVgrow(view, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        final boolean showToolbar = "true".equals(this.getParameters().getNamed().get("toolbar")); // NOI18N
        final boolean useFirebug = "true".equals(this.getParameters().getNamed().get("firebug")); // NOI18N
        if (showToolbar) {
            final ToolBar toolbar = new BrowserToolbar(view, vbox, useFirebug);
            root.setTop( toolbar );
        }
        root.setCenter(hbox);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle( "Device Emulator" );
        primaryStage.setScene( scene );
        primaryStage.show();
    }
    
    /**
     * Create a resizable WebView pane
     */
    private static class WebController {
        private final JVMBridge bridge;

        public WebController(WebView view, List<String> params) {
            this.bridge = new JVMBridge(view.getEngine());
            LOG.log(Level.INFO, "Initializing WebView with {0}", params);
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
        }

        boolean initBck2Brwsr(WebEngine webEngine) {
            JSObject jsobj = (JSObject) webEngine.executeScript("window");
            LOG.log(Level.FINE, "window: {0}", jsobj);
            Object prev = jsobj.getMember("bck2brwsr");
            if ("undefined".equals(prev)) {
                jsobj.setMember("bck2brwsr", bridge);
                return true;
            }
            return false;
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
