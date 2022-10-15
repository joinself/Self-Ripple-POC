package com.ripple.ripplelibpp;

/**
 * Created by depry on 15.07.2017.
 */

public class Buffer {

    private long nativePtr;

    private Buffer(long ptr) {
        nativePtr = ptr;
    }

    public Buffer() {
        initialise();
    }

    public Buffer(Buffer other) {
        initialiseo(other);
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise();

    private native void initialiseo(Buffer other);

    public native byte[] data();

    public native int size();

    private native void delete();
}
