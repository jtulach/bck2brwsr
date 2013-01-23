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
package org.apidesign.bck2brwsr.htmlpage.api;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Anton Epple <toni.epple@eppleton.de>
 */
public class GraphicsContext {

    Object context;

    GraphicsContext(Object contextImpl) {
        this.context = contextImpl;
    }

    @JavaScriptBody(args = {"centerx", "centery", "radius", "startangle", "endangle", "ccw"},
            body = "this.fld_context.arc(centerx,centery, radius, startangle, endangle,ccw);")
    public native void arc(double centerX,
            double centerY,
            double startAngle,
            double radius,
            double endAngle,
            boolean ccw);

    @JavaScriptBody(args = {"x1", "y1", "x2", "y2", "r"},
            body = "this.fld_context.arcTo(x1,y1,x2,y2,r);")
    public native void arcTo(double x1,
            double y1,
            double x2,
            double y2,
            double r);

    @JavaScriptBody(args = {"x", "y"},
            body = "return this.fld_context.isPointInPath(x,y);")
    public native boolean isPointInPath(double x, double y);

    @JavaScriptBody(args = {}, body = "this.fld_context.fill();")
    public native void fill();

    @JavaScriptBody(args = {}, body = "this.fld_context.stroke();")
    public native void stroke();

    @JavaScriptBody(args = {}, body = "this.fld_context.beginPath();")
    public native void beginPath();

    @JavaScriptBody(args = {}, body = "this.fld_context.closePath();")
    public native void closePath();

    @JavaScriptBody(args = {}, body = "this.fld_context.clip();")
    public native void clip();

    @JavaScriptBody(args = {"x", "y"}, body = "this.fld_context.moveTo(x,y);")
    public native void moveTo(double x, double y);

    @JavaScriptBody(args = {"x", "y"}, body = "this.fld_context.lineTo(x,y);")
    public native void lineTo(double x, double y);

    @JavaScriptBody(args = {"cpx", "cpy", "x", "y"}, body = "this.fld_context.quadraticCurveTo(cpx,cpy,x,y);")
    public native void quadraticCurveTo(double cpx, double cpy, double x, double y);

    @JavaScriptBody(args = {"cp1x", "cp1y", "cp2x", "cp2y", "x", "y"},
            body = "this.fld_context.bezierCurveTo(cp1x,cp1y,cp2x,cp2y,x,y);")
    public native void bezierCurveTo(double cp1x, double cp1y, double cp2x, double cp2y, double x, double y);

    @JavaScriptBody(args = {"x", "y", "width", "height"}, body = "this.fld_context.fillRect(x,y,width,height);")
    public native void fillRect(double x, double y, double width, double height);

    @JavaScriptBody(args = {"x", "y", "width", "height"}, body = "this.fld_context.strokeRect(x,y,width,height);")
    public native void strokeRect(double x, double y, double width, double height);

    @JavaScriptBody(args = {"x", "y", "width", "height"}, body = "this.fld_context.clearRect(x,y,width,height);")
    public native void clearRect(double x, double y, double width, double height);

    @JavaScriptBody(args = {"x", "y", "width", "height"}, body = "this.fld_context.rectect(x,y,width,height);")
    public native void rect(double x, double y, double width, double height);

    @JavaScriptBody(args = {}, body = "this.fld_context.save();")
    public native void save();

    @JavaScriptBody(args = {}, body = "this.fld_context.restore();")
    public native void restore();

    @JavaScriptBody(args = {"angle"}, body = "this.fld_context.rotate(angle);")
    public native void rotate(double angle);

    @JavaScriptBody(args = {"a", "b", "c", "d", "e", "f"}, body = "this.fld_context.transform(a,b,c,d,e,f);")
    public native void transform(double a, double b, double c, double d, double e, double f);

    @JavaScriptBody(args = {"a", "b", "c", "d", "e", "f"}, body = "this.fld_context.setTransform(a,b,c,d,e,f);")
    public native void setTransform(double a, double b, double c, double d, double e, double f);

    @JavaScriptBody(args = {"x", "y"}, body = "this.fld_context.translate(x,y);")
    public native void translate(double x, double y);

    @JavaScriptBody(args = {"x", "y"}, body = "this.fld_context.scale(x,y);")
    public native void scale(double x, double y);

    public void drawImage(Image image, double x, double y) {
        drawImageImpl(context, Element.getElementById(image), x, y);
    }

    public void drawImage(Image image, double x, double y, double width, double height) {
        drawImageImpl(context, Element.getElementById(image), x, y, width, height);
    }

    public void drawImage(Image image, double sx, double sy, double sWidth, double sHeight, double x, double y, double width, double height) {
        drawImageImpl(context, Element.getElementById(image), sx, sy, sWidth, sHeight, x, y, width, height);
    }

    @JavaScriptBody(args = {"ctx", "img", "x", "y", "width", "height"}, body = "ctx.drawImage(img,x,y,width,height);")
    private native static void drawImageImpl(Object ctx, Object img, double x, double y, double width, double height);

    @JavaScriptBody(args = {"ctx", "img", "sx", "sy", "swidth", "sheight", "x", "y", "width", "height"}, body = "ctx.drawImage(img,sx,sy,swidth,sheight,x,y,width,height);")
    private native static void drawImageImpl(Object ctx, Object img, double sx, double sy, double sWidth, double sHeight, double x, double y, double width, double height);

    @JavaScriptBody(args = {"ctx", "img", "x", "y"}, body = "ctx.drawImage(img,x,y);")
    private native static void drawImageImpl(Object ctx, Object img, double x, double y);

    @JavaScriptBody(args = {"style"}, body = "this.fld_context.fillStyle=style;")
    public native void setFillStyle(String style);

    @JavaScriptBody(args = {}, body = "return this.fld_context.fillStyle;")
    public native String getFillStyle();

    public void setFillStyle(LinearGradient style) {
        setFillStyleImpl(context, style.object());
    }

    public void setFillStyle(RadialGradient style) {
        setFillStyleImpl(context, style.object());
    }

    public void setFillStyle(Pattern style) {
        setFillStyleImpl(context, style.object());
    }

    @JavaScriptBody(args = {"context","obj"}, body = "context.fillStyle=obj;")
    private native void setFillStyleImpl(Object context, Object obj);

    @JavaScriptBody(args = {"style"}, body = "this.fld_context.strokeStyle=style;")
    public native void setStrokeStyle(String style);

    public void setStrokeStyle(LinearGradient style) {
        setStrokeStyleImpl(context, style.object());
    }

    public void setStrokeStyle(RadialGradient style) {
        setStrokeStyleImpl(context, style.object());
    }

    @JavaScriptBody(args = {"style"}, body = "this.fld_context.fillStyle=style;")
    public void setStrokeStyle(Pattern style) {
        setStrokeStyleImpl(context, style.object());
    }

    @JavaScriptBody(args = {"context","obj"}, body = "context.strokeStyle=obj;")
    private native void setStrokeStyleImpl(Object context, Object obj);

    @JavaScriptBody(args = {"color"}, body = "this.fld_context.shadowColor=color;")
    public native void setShadowColor(String color);

    @JavaScriptBody(args = {"blur"}, body = "this.fld_context.shadowBlur=blur;")
    public native void setShadowBlur(double blur);
    
    @JavaScriptBody(args = {"x"}, body = "this.fld_context.shadowOffsetX=x;")
    public native void setShadowOffsetX(double x);

    @JavaScriptBody(args = {"y"}, body = "this.fld_context.shadowOffsetY=y;")
    public native void setShadowOffsetY(double y);

    @JavaScriptBody(args = {}, body = "return this.fld_context.strokeStyle;")
    public native String getStrokeStyle();

    @JavaScriptBody(args = {}, body = "return this.fld_context.shadowColor;")
    public native String getShadowColor();

    @JavaScriptBody(args = {}, body = "return this.fld_context.shadowBlur;")
    public native double getShadowBlur();

    @JavaScriptBody(args = {}, body = "return this.fld_context.shadowOffsetX;")
    public native double getShadowOffsetX();

    @JavaScriptBody(args = {}, body = "return this.fld_context.shadowOffsetY;")
    public native double getShadowOffsetY();

    @JavaScriptBody(args = {}, body = "return this.fld_context.lineCap;")
    public native String getLineCap();

    @JavaScriptBody(args = {"style"}, body = "this.fld_context.lineCap=style;")
    public native void setLineCap(String style);

    @JavaScriptBody(args = {}, body = "return this.fld_context.lineJoin;")
    public native String getLineJoin();

    @JavaScriptBody(args = {"style"}, body = "this.fld_context.lineJoin=style;")
    public native void setLineJoin(String style) ;

    @JavaScriptBody(args = {}, body = "return this.fld_context.lineWidth;")
    public native double getLineWidth();

    @JavaScriptBody(args = {"width"}, body = "this.fld_context.lineJoin=width;")
    public native void setLineWidth(double width);

    @JavaScriptBody(args = {}, body = "return this.fld_context.miterLimit;")
    public native double getMiterLimit();

    @JavaScriptBody(args = {"limit"}, body = "this.fld_context.miterLimit=limit;")
    public native void setMiterLimit(double limit);

    @JavaScriptBody(args = {}, body = "return this.fld_context.font;")
    public native String getFont();

    @JavaScriptBody(args = {"font"}, body = "this.fld_context.font=font;")
    public native void setFont(String font);

    @JavaScriptBody(args = {}, body = "return this.fld_context.textAlign;")
    public native String getTextAlign();

    @JavaScriptBody(args = {"textalign"}, body = "this.fld_context.textAlign=textalign;")
    public native void setTextAlign(String textAlign);

    @JavaScriptBody(args = {}, body = "return this.fld_context.textBaseline;")
    public native String getTextBaseline();

    @JavaScriptBody(args = {"textbaseline"}, body = "this.fld_context.textBaseline=textbaseline;")
    public native void setTextBaseline(String textbaseline);

    @JavaScriptBody(args = {"text", "x", "y"}, body = "this.fld_context.fillText(text,x,y);")
    public native void fillText(String text, double x, double y);

    @JavaScriptBody(args = {"text", "x", "y", "maxwidth"}, body = "this.fld_context.fillText(text,x,y,maxwidth);")
    public void fillText(String text, double x, double y, double maxWidth) {
    }

    public TextMetrics measureText(String text) {
        return new TextMetrics(measureTextImpl(text));
    }

    @JavaScriptBody(args = {"text"},
            body = "return this.fld_context.measureText(text);")
    private native Object measureTextImpl(String text);

    @JavaScriptBody(args = {"text", "x", "y"}, body = "this.fld_context.strokeText(text,x,y);")
    public native void strokeText(String text, double x, double y);

    @JavaScriptBody(args = {"text", "x", "y", "maxWidth"}, body = "this.fld_context.strokeText(text,x,y,maxWidth);")
    public native void strokeText(String text, double x, double y, double maxWidth) ;

    public ImageData createImageData(double x, double y) {
        return new ImageData(createImageDataImpl(x, y));
    }

    @JavaScriptBody(args = {"x", "y"},
            body = "return this.fld_context.createImageData(x,y);")
    private native Object createImageDataImpl(double x, double y);

    public ImageData createImageData(ImageData imageData) {
        return new ImageData(createImageDataImpl(imageData.getWidth(), imageData.getHeight()));
    }

    public ImageData getImageData(double x, double y, double width, double height) {
        return new ImageData(getImageDataImpl(x, y, width, height));
    }

    @JavaScriptBody(args = {"x", "y", "width", "height"},
            body = "return this.fld_context.getImageData(x,y,width,height);")
    private native Object getImageDataImpl(double x, double y, double width, double height);

    public void putImageData(ImageData imageData, double x, double y) {
        putImageDataImpl(imageData.object(), x, y);
    }

    @JavaScriptBody(args = {"imageData", "x", "y"},
            body = "this.fld_context.putImageData(imageData,x,y);")
    private native void putImageDataImpl(Object imageData, double x, double y);

    public void putImageData(ImageData imageData, double x, double y, double dirtyx, double dirtyy, double dirtywidth, double dirtyheight) {
        putImageDataImpl(imageData.object(), x, y, dirtyx, dirtyy, dirtywidth, dirtyheight);
    }

    @JavaScriptBody(args = {"imageData", "x", "y", "dirtyx", "dirtyy", "dirtywidth", "dirtyheight"},
            body = "this.fld_context.putImageData(imageData,x,y, dirtyx, dirtyy, dirtywidth,dirtyheight);")
    private native void putImageDataImpl(Object imageData, double x, double y, double dirtyx, double dirtyy, double dirtywidth, double dirtyheight);

    @JavaScriptBody(args = {"alpha"}, body = "this.fld_context.globalAlpha=alpha;")
    public native void setGlobalAlpha(double alpha) ;

    @JavaScriptBody(args = {}, body = "return this.fld_context.globalAlpha;")
    public native double getGlobalAlpha();

    @JavaScriptBody(args = {"operation"}, body = "this.fld_context.globalCompositeOperation=operation;")
    public native void setGlobalCompositeOperation(double alpha);

    @JavaScriptBody(args = {}, body = "return this.fld_context.globalCompositeOperation;")
    public native double getGlobalCompositeOperation();

    public LinearGradient createLinearGradient(double x0, double y0, double x1, double y1) {
        return new LinearGradient(createLinearGradientImpl(context, x0, y0, x1, y1));
    }

    @JavaScriptBody(args = {"context", "x0", "y0", "x1", "y1"}, body = "return context.createLinearGradient(x0,y0,x1,y1);")
    private  native Object createLinearGradientImpl(Object context, double x0, double y0, double x1, double y1);

    public Pattern createPattern(Image image, String repeat) {
        return new Pattern(createPatternImpl(context, image, repeat));
    }

    @JavaScriptBody(args = {"context", "image", "repeat"}, body = "return context.createPattern(image, repeat);")
    private static native Object createPatternImpl(Object context, Image image, String repeat);

    public RadialGradient createRadialGradient(double x0, double y0, double r0, double x1, double y1, double r1) {
        return new RadialGradient(createRadialGradientImpl(context, x0, y0, r0, x1, y1, r1));
    }

    @JavaScriptBody(args = {"context", "x0", "y0", "r0", "x1", "y1", "r1"}, body = "return context.createRadialGradient(x0,y0,r0,x1,y1,r1);")
    private static native Object createRadialGradientImpl(Object context, double x0, double y0, double r0, double x1, double y1, double r1);
}
