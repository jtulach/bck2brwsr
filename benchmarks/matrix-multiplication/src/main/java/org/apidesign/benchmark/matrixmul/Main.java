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
package org.apidesign.benchmark.matrixmul;

public class Main {

    public static final int ITERATION_COUNT = 100000;
    
    public static void main(String[] args) {
        Matrix m1 = new Matrix(5);
        Matrix m2 = new Matrix(5);
        
        m1.generateData();
        m2.generateData();
        
        //m1.printOn(System.out);
        //System.out.println("x");
        //m2.printOn(System.out);
        
        for (int i = 0; i < ITERATION_COUNT; i++) {
            m1.multiply(m2);
        }
        
        //System.out.println("=");
        //m1.printOn(System.out);
    }
}
