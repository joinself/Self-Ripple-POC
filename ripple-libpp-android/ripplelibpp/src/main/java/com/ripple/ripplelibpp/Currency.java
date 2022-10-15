package com.ripple.ripplelibpp;

/**
 * Created by depry on 15.07.2017.
 */

public class Currency {

    private long nativePtr;

    private Currency(long ptr) {
        nativePtr = ptr;
    }

    public Currency() {
        initialise();
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise();

    private native void delete();

    public static native String toString(Currency c);

    public static native Currency toCurrency(String s);

}
