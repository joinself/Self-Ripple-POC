package com.ripple.ripplelibpp;

/**
 * Created by ipuzzle on 7/18/17.
 */

public class Blob {
    private long nativePtr;

    private Blob(long ptr) {
        nativePtr = ptr;
    }

    public Blob() {
        initialise();
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise();

    private native void delete();

    // TODO: implement jni method
    public native byte[] getRawData();
}
