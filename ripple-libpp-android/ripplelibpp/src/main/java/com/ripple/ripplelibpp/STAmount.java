package com.ripple.ripplelibpp;

/**
 * Created by depry on 15.07.2017.
 */

public class STAmount {

    private long nativePtr;

    public STAmount(long mantissa) {
        this(mantissa, false);
    }

    public STAmount(long mantissa, boolean negative) {
        initialise(mantissa, negative);
    }

    public STAmount(Issue issue, long mantissa, long exponent) {
        initialise_issue(issue, mantissa, exponent);
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise(long mantissa, boolean negative);

    private native void initialise_issue(Issue issue, long mantissa, long exponent);

    private native void delete();
}
