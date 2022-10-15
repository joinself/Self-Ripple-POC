package com.ripple.ripplelibpp;

/**
 * Created by depry on 17.07.2017.
 */

public class Serializer {

    private long nativePtr;

    private Serializer(long ptr) {
        nativePtr = ptr;
    }

    public Serializer() {
        this(256);
    }

    public Serializer(int n) {
        initialise(n);
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise(int n);

    public native byte[] data();

    public native int size();

    private native void delete();

    public native long add32(int i);

    public native Blob getData();

    public native Slice slice();

    public native static Serializer buildMultiSigningData(STTx tx, AccountID signingID);
}
