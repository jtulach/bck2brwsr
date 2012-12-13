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

//import java.io.PrintStream;
//import java.util.Random;

public class Matrix {

    private final int rank;
    private float data[][];
    
    public Matrix(int r) {
        rank = r;
        data = new float[r][r];
    }
    
    public void setElement(int i, int j, float value) {
        data[i][j] = value;
    }
    public float getElement(int i, int j) {
        return data[i][j];
    }
    
    public void generateData() {
        //final Random rand = new Random();
        //final int x = 10;
        for (int i = 0; i < rank; i++) {
            for (int j = 0; j < rank; j++) {
                data[i][j] = i + j;
            }
        }
    }

    public Matrix multiply(Matrix m) {
        if (rank != m.rank) {
            throw new IllegalArgumentException("Rank doesn't match");
        }
        
        final float res[][] = new float[rank][rank];
        for (int i = 0; i < rank; i++) {
            for (int j = 0; j < rank; j++) {
                float ij = 0;
                for (int q = 0; q < rank; q++) {
                    ij += data[i][q] * m.data[q][j];
                }
                res[i][j] = ij;
            }
        }
        data = res;
        
        return this;
    }
    
    /*
    public void printOn(PrintStream s) {
        for (int i = 0; i < rank; i++) {
            for (int j = 0; j < rank; j++) {
                s.printf("%f ", data[i][j]);
            }
            s.println();
        }
    }
    */
    
}
