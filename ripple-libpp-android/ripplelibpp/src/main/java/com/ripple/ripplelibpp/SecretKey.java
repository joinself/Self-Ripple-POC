package com.ripple.ripplelibpp;

/**
 * Created by depry on 15.07.2017.
 */

public class SecretKey {

    private long nativePtr;

    private SecretKey(long ptr) {
        nativePtr = ptr;
    }

    public SecretKey() {
        initialise();
    }

    public SecretKey(SecretKey other) {
        initialiseo(other);
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    @Override
    public String toString() {
        // TODO !!!
        return super.toString();
    }

    private native void initialise();

    private native void initialiseo(SecretKey other);

    public native byte[] data();

    public native int size();

    private native void delete();

    private static native String toBase58(int tokenType, SecretKey sk);

    private static native PublicKey parseBase58(int tokenType, String s);

    public static native Pair<PublicKey, SecretKey> generateKeyPair(int keyType, Seed seed);

    public static native Buffer sign(PublicKey pk, SecretKey sk, Slice m);
}
