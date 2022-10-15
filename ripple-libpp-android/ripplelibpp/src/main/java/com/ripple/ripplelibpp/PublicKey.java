package com.ripple.ripplelibpp;

/**
 * Created by depry on 15.07.2017.
 */

public class PublicKey {

    private long nativePtr;

    private PublicKey(long ptr) {
        nativePtr = ptr;
    }

    public PublicKey() {
        initialise();
    }

    public PublicKey(PublicKey other) {
        initialiseo(other);
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise();

    private native void initialiseo(PublicKey other);

    public native byte[] data();

    public native int size();

    private native void delete();

    private static native String toBase58(int tokenType, PublicKey pk);

    private static native PublicKey parseBase58(int tokenType, String s);
}
