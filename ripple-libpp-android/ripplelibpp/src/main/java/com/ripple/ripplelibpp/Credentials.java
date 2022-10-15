package com.ripple.ripplelibpp;

/**
 * Created by denys on 13.07.17.
 */

public class Credentials {

    final String name;
    final int keyType;
    final Seed seed;
    final Pair<PublicKey, SecretKey> keys;
    final AccountID id;

    public Credentials(String name) {
        this(name, KeyType.SECP256K1);
    }

    public Credentials(String name, int keyType) {
        this.name = name;
        this.keyType = keyType;

        this.seed = Seed.parseGenericSeed(this.name);
        this.keys = SecretKey.generateKeyPair(this.keyType, seed);
        this.id = AccountID.calcAccountID(keys.getFirst());
    }

    public String getName() {
        return name;
    }

    public int getKeyType() {
        return keyType;
    }

    public Seed getSeed() {
        return seed;
    }

    public Pair<PublicKey, SecretKey> getKeys() {
        return keys;
    }

    public AccountID id() {
        return id;
    }
}
