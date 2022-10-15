package com.ripple.ripplelibpp;

/**
 * Created by denys on 17.07.17.
 */

public class Issue {

    private long nativePtr;

    private Issue(long ptr) {
        nativePtr = ptr;
    }

    public Issue(Currency c, AccountID a) {
        initialise(c, a);
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise(Currency c, AccountID a);

    private native void delete();
}
