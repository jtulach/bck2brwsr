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
