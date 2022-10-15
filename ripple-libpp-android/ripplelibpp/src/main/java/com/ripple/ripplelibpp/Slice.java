package com.ripple.ripplelibpp;

/**
 * Created by depry on 15.07.2017.
 */

public class Slice {

    private long nativePtr;

    private Slice(long ptr) {
        nativePtr = ptr;
    }

    public Slice() {
        initialise();
    }

    public Slice(Slice other) {
        initialiseo(other);
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise();

    private native void initialiseo(Slice other);

    public native byte[] data();

    public native int size();

    private native void delete();
}
