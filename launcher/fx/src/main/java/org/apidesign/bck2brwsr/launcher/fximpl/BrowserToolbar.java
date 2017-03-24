/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

final class BrowserToolbar extends ToolBar {
    private final ArrayList<ResizeBtn> resizeButtons;
    private final WebView webView;
    private final Pane container;
    private final ToggleGroup resizeGroup = new ToggleGroup();
    private final ComboBox<String> comboZoom = new ComboBox<String>();
    
    BrowserToolbar(WebView webView, Pane container, boolean useFirebug) {
        this.webView = webView;
        this.container = container;
        
        List<ResizeOption> options = ResizeOption.loadAll();
        options.add( 0, ResizeOption.SIZE_TO_FIT );
        resizeButtons = new ArrayList<ResizeBtn>( options.size() );

        for( ResizeOption ro : options ) {
            ResizeBtn button = new ResizeBtn(ro);
            resizeButtons.add( button );
            resizeGroup.getToggles().add( button );
            getItems().add( button );
        }
        resizeButtons.get( 0 ).setSelected( true );
        resizeGroup.selectedToggleProperty().addListener( new InvalidationListener() {

            @Override
            public void invalidated( Observable o ) {
                resize();
            }
        });
        
        getItems().add( new Separator() );

        getItems().add( comboZoom );
        ArrayList<String> zoomModel = new ArrayList<String>( 6 );
        zoomModel.add( "200%" ); //NOI18N
        zoomModel.add( "150%" ); //NOI18N
        zoomModel.add( "100%" ); //NOI18N
        zoomModel.add( "75%" ); //NOI18N
        zoomModel.add( "50%" ); //NOI18N
        comboZoom.setItems( FXCollections.observableList( zoomModel ) );
        comboZoom.setEditable( true );
        comboZoom.setValue( "100%" ); //NOI18N
        comboZoom.valueProperty().addListener( new ChangeListener<String>() {

            @Override
            public void changed( ObservableValue<? extends String> ov, String t, String t1 ) {
                String newZoom = zoom( t1 );
                comboZoom.setValue( newZoom );
            }
        });
        
        if (useFirebug) {
            getItems().add(new Separator());

            final ToggleButton firebug = new ToggleButton(null, new ImageView(
                new Image(BrowserToolbar.class.getResourceAsStream("firebug.png"))
            ));
            firebug.setTooltip(new Tooltip("Show/Hide firebug"));
            firebug.selectedProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable o) {
                    toggleFireBug(firebug.isSelected());
                }
            });
            getItems().add(firebug);
        }
    }

    private String zoom( String zoomFactor ) {
        if( zoomFactor.trim().isEmpty() )
            return null;

        try {
            zoomFactor = zoomFactor.replaceAll( "\\%", ""); //NOI18N
            zoomFactor = zoomFactor.trim();
            double zoom = Double.parseDouble( zoomFactor );
            zoom = Math.abs( zoom )/100;
            if( zoom <= 0.0 )
                return null;
            webView.setScaleX(zoom);
            webView.setScaleY(zoom);
            webView.setScaleZ(zoom);
            return (int)(100*zoom) + "%"; //NOI18N
        } catch( NumberFormatException nfe ) {
            //ignore
        }
        return null;
    }

    private void resize() {
        Toggle selection = resizeGroup.getSelectedToggle();
        if( selection instanceof ResizeBtn ) {
            ResizeOption ro = ((ResizeBtn)selection).getResizeOption();
            if( ro == ResizeOption.SIZE_TO_FIT ) {
                _autofit();
            } else {
                _resize( ro.getWidth(), ro.getHeight() );
            }
        }

    }

    private void _resize( final double width, final double height ) {
        ScrollPane scroll;
        if( !(container.getChildren().get( 0) instanceof ScrollPane) ) {
            scroll = new ScrollPane();
            scroll.setContent( webView );
            container.getChildren().clear();
            container.getChildren().add( scroll );
        } else {
            scroll = ( ScrollPane ) container.getChildren().get( 0 );
        }
        scroll.setPrefViewportWidth( width );
        scroll.setPrefViewportHeight(height );
        webView.setMaxWidth( width );
        webView.setMaxHeight( height );
        webView.setMinWidth( width );
        webView.setMinHeight( height );
    }

    private void _autofit() {
        if( container.getChildren().get( 0) instanceof ScrollPane ) {
            container.getChildren().clear();
            container.getChildren().add( webView );
        }
        webView.setMaxWidth( Integer.MAX_VALUE );
        webView.setMaxHeight( Integer.MAX_VALUE );
        webView.setMinWidth( -1 );
        webView.setMinHeight( -1 );
        webView.autosize();
    }

    final void toggleFireBug(boolean enable) {
        WebEngine eng = webView.getEngine();
        Object installed = eng.executeScript("window.Firebug");
        if ("undefined".equals(installed)) {
            StringBuilder sb = new StringBuilder();
            sb.append("var scr = window.document.createElement('script');\n");
            sb.append("scr.type = 'text/javascript';\n");
            sb.append("scr.src = 'https://getfirebug.com/firebug-lite.js';\n");
            sb.append("scr.text = '{ startOpened: true }';\n");
            sb.append("var head = window.document.getElementsByTagName('head')[0];");
            sb.append("head.appendChild(scr);\n");
            sb.append("var html = window.document.getElementsByTagName('html')[0];");
            sb.append("html.debug = true;\n");
            eng.executeScript(sb.toString());
        } else {
            if (enable) {
                eng.executeScript("Firebug.chrome.open()");
            } else {
                eng.executeScript("Firebug.chrome.close()");
            }
        }
    }

    /**
     * Button to resize the browser window.
     * Taken from NetBeans. Kept GPLwithCPEx license.
     * Portions Copyrighted 2012 Sun Microsystems, Inc.
     *
     * @author S. Aubrecht
     */
    static final class ResizeBtn extends ToggleButton {

        private final ResizeOption resizeOption;

        ResizeBtn(ResizeOption resizeOption) {
            super(null, new ImageView(toImage(resizeOption)));
            this.resizeOption = resizeOption;
            setTooltip(new Tooltip(resizeOption.getToolTip()));
        }

        ResizeOption getResizeOption() {
            return resizeOption;
        }

        static Image toImage(ResizeOption ro) {
            if (ro == ResizeOption.SIZE_TO_FIT) {
                return ResizeOption.Type.CUSTOM.getImage();
            }
            return ro.getType().getImage();
        }
    }

    /**
     * Immutable value class describing a single button to resize web browser window.
     * Taken from NetBeans. Kept GPLwithCPEx license.
     * Portions Copyrighted 2012 Sun Microsystems, Inc.
     *
     * @author S. Aubrecht
     */
    static final class ResizeOption {

        private final Type type;
        private final String displayName;
        private final int width;
        private final int height;
        private final boolean isDefault;

        enum Type {
            DESKTOP("desktop.png"), 
            TABLET_PORTRAIT("tabletPortrait.png"), 
            TABLET_LANDSCAPE("tabletLandscape.png"), 
            SMARTPHONE_PORTRAIT("handheldPortrait.png"), 
            SMARTPHONE_LANDSCAPE("handheldLandscape.png"), 
            WIDESCREEN("widescreen.png"), 
            NETBOOK("netbook.png"), 
            CUSTOM("sizeToFit.png");
            
            
            private final String resource;

            private Type(String r) {
                resource = r;
            }

            public Image getImage() {
                return new Image(Type.class.getResourceAsStream(resource));
            }
        }

        private ResizeOption(Type type, String displayName, int width, int height, boolean showInToolbar, boolean isDefault) {
            super();
            this.type = type;
            this.displayName = displayName;
            this.width = width;
            this.height = height;
            this.isDefault = isDefault;
        }

        static List<ResizeOption> loadAll() {
            List<ResizeOption> res = new ArrayList<ResizeOption>(10);
            res.add(ResizeOption.create(ResizeOption.Type.DESKTOP, "Desktop", 1280, 1024, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.TABLET_LANDSCAPE, "Tablet Landscape", 1024, 768, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.TABLET_PORTRAIT, "Tablet Portrait", 768, 1024, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.SMARTPHONE_LANDSCAPE, "Smartphone Landscape", 480, 320, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.SMARTPHONE_PORTRAIT, "Smartphone Portrait", 320, 480, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.WIDESCREEN, "Widescreen", 1680, 1050, false, true));
            res.add(ResizeOption.create(ResizeOption.Type.NETBOOK, "Netbook", 1024, 600, false, true));
            return res;
        }
        
        /**
         * Creates a new instance.
         * @param type
         * @param displayName Display name to show in tooltip, cannot be empty.
         * @param width Screen width
         * @param height Screen height
         * @param showInToolbar True to show in web developer toolbar.
         * @param isDefault True if this is a predefined option that cannot be removed.
         * @return New instance.
         */
        public static ResizeOption create(Type type, String displayName, int width, int height, boolean showInToolbar, boolean isDefault) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Invalid screen dimensions: " + width + " x " + height); //NOI18N
            }
            return new ResizeOption(type, displayName, width, height, showInToolbar, isDefault);
        }
        /**
         * An extra option to size the browser content to fit its window.
         */
        public static final ResizeOption SIZE_TO_FIT = new ResizeOption(Type.CUSTOM, "Size To Fit", -1, -1, true, true);

        public String getDisplayName() {
            return displayName;
        }

        public Type getType() {
            return type;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isDefault() {
            return isDefault;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public String getToolTip() {
            if (width < 0 || height < 0) {
                return displayName;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(width);
            sb.append(" x "); //NOI18N
            sb.append(height);
            sb.append(" ("); //NOI18N
            sb.append(displayName);
            sb.append(')'); //NOI18N
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ResizeOption other = (ResizeOption) obj;
            if (this.type != other.type) {
                return false;
            }
            if ((this.displayName == null) ? (other.displayName != null) : !this.displayName.equals(other.displayName)) {
                return false;
            }
            if (this.width != other.width) {
                return false;
            }
            if (this.height != other.height) {
                return false;
            }
            if (this.isDefault != other.isDefault) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (this.type != null ? this.type.hashCode() : 0);
            hash = 11 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
            hash = 11 * hash + this.width;
            hash = 11 * hash + this.height;
            hash = 11 * hash + (this.isDefault ? 1 : 0);
            return hash;
        }
    }
}
