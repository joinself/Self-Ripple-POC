package com.ripple.ripplelibpp;

/**
 * Created by depry on 15.07.2017.
 */

public class AccountID {

    private long nativePtr;

    private AccountID(long ptr) {
        nativePtr = ptr;
    }

    public AccountID() {
        initialise();
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    private native void initialise();

    private native void delete();

    public static native String toBase58(AccountID v);

    public static native AccountID parseBase58(String s);

    public static native AccountID parseHex(String s);

    public static native AccountID parseHexOrBase58(String s);

    public static native AccountID calcAccountID(PublicKey pk);
}
