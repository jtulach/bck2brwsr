/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apidesign.bck2brwsr.dew;

import javax.tools.JavaFileObject;

/**
 *
 * @author Tomas Zezula
 */
interface InferableJavaFileObject extends JavaFileObject {
    String infer();
}
