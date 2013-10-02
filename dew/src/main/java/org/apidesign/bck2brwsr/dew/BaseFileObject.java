/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apidesign.bck2brwsr.dew;

import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

/**
 *
 * @author tom
 */
public abstract class BaseFileObject implements InferableJavaFileObject {

    protected final String path;
    protected final Kind kind;

    BaseFileObject(
        String path,
        Kind kind) {
        if (!path.startsWith("/")) {    //NOI18N
            throw new IllegalArgumentException();
        }
        this.path = path;
        this.kind = kind;
    }


    @Override
    public String infer() {
        return ClassLoaderFileManager.convertResourceToFQN(path);
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        return this.kind == kind &&
        getSimpleName(path).equals(simpleName);
    }

    @Override
    public NestingKind getNestingKind() {
        return null;
    }

    @Override
    public Modifier getAccessLevel() {
        return null;
    }

    @Override
    public URI toUri() {
        return URI.create(escape(path));
    }

    @Override
    public String getName() {
        return path;
    }



    protected static String getSimpleName(String path) {
        int slashIndex = path.lastIndexOf('/');
        assert slashIndex >= 0;
        return (slashIndex + 1 < path.length()) ?
            path.substring(slashIndex + 1) :
            ""; //NOI18N
    }

    protected static Kind getKind(final String path) {
        final String simpleName = getSimpleName(path);
        final int dotIndex = simpleName.lastIndexOf('.'); //NOI18N
        final String ext = dotIndex > 0 ?
            simpleName.substring(dotIndex) :
            "";
        for (Kind k : Kind.values()) {
            if (k.extension.equals(ext)) {
                return k;
            }
        }
        return Kind.OTHER;
    }

    private String escape(String path) {
        return path;
    }


}
