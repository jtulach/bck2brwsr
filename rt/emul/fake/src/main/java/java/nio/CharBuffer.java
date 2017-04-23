package java.nio;

public abstract class CharBuffer {
    public abstract int remaining();
    public abstract CharBuffer put(char[] src, int offset, int length);
}
