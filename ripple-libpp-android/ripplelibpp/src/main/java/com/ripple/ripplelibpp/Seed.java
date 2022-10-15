package com.ripple.ripplelibpp;

import android.util.Log;

/**
 * Created by depry on 15.07.2017.
 */

public class Seed {

    private long nativePtr;

    /* hide */
    private Seed() {
    }

    private Seed(long ptr) {
        nativePtr = ptr;
    }

    public Seed(Seed other) {
        initialiseo(other);
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialiseo(Seed other);

    public native byte[] data();

    public native int size();

    private native void delete();

    public static native Seed generateSeed(String passPhrase);

    public static native Seed parseBase58(String s);

    public static native Seed parseGenericSeed(String str);

    public static native String seedAs1751(Seed seed);

    public static native String toBase58(Seed seed);
}
