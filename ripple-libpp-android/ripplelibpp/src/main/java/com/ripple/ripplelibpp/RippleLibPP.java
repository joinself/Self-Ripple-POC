package com.ripple.ripplelibpp;

/*
Methods
Methods used in ripple-libpp_demo.cpp
*parseGenericSeed
*generateKeyPair
*calcAccountID
*toBase58
*parseBase58
STTx - all methods used in ripple-libpp_demo.cpp
This is on extras/rippled/src/ripple/protocol/impl/STTx.cpp
serialize(noopTx);
deserialize(serialized);

 */

public class RippleLibPP {

    static {
        System.loadLibrary("ripplelibpp");
    }

    // Native methods

    public static native String getVersion();

    public static native void startTest();
}
