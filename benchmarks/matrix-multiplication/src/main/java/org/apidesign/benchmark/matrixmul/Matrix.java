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
