package com.ripple.ripplelibpp;

/**
 * Created by denys on 13.07.17.
 */

public class STTx {

    private long  nativePtr;

    private STTx(long ptr) {
        nativePtr = ptr;
    }

    public STTx(int txType, AccountID sfAccount, STAmount sfFee, long sfFlags, int sfSequence,
                PublicKey sfSigningPubKey, STAmount sfAmount, AccountID sfDestination,
                STAmount sfSendMax) {

        initialise(txType, sfAccount, sfFee, sfFlags, sfSequence,
                sfSigningPubKey, sfAmount, sfDestination, sfSendMax);
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativePtr != 0) delete();

        super.finalize();
    }

    public boolean multisign(Credentials signer) {
        return multisign(signer.id(), signer.getKeys().getFirst(), signer.getKeys().getSecond());
    }

    private native void initialise(int txType, AccountID sfAccount, STAmount sfFee, long sfFlags, int sfSequence,
                                   PublicKey sfSigningPubKey, STAmount sfAmount, AccountID sfDestination,
                                   STAmount sfSendMax);

    private native void delete();

    public String getStyledJsonString(int options) {
        return getStyledJsonString(options, false);
    }

    public native String getStyledJsonString(int options, boolean binary);

    public native void sign(PublicKey publicKey, SecretKey secretKey);

    public static native String serialize(STTx tx);

    public static native STTx deserialize(String blob);

    public static native STTx buildMultisignTx(AccountID id, int seq, int fee);

    public native byte[] getTransactionID();

    public native Pair<Boolean, String> checkSign(boolean allowMultiSign);

    public native void addWithoutSigningFields(Serializer s);

    public native boolean verify(PublicKey publicKey, boolean mustBeFullyCanonical);

    private native boolean multisign(AccountID id, PublicKey pk, SecretKey sk);

    public static class Builder {

        private int txType;
        private AccountID sfAccount;
        private STAmount sfFee;
        private long sfFlags;
        private int sfSequence;
        private PublicKey sfSigningPubKey;
        private STAmount sfAmount;
        private AccountID sfDestination;
        private STAmount sfSendMax;

        public Builder setFee(STAmount fee) {
            this.sfFee = fee;
            return this;
        }

        public Builder setAccount(AccountID id) {
            this.sfAccount = id;
            return this;
        }

        public Builder setFlags(long flags) {
            this.sfFlags = flags;
            return this;
        }

        public Builder setSequence(int sequence) {
            this.sfSequence = sequence;
            return this;
        }

        public Builder setSigningPublicKey(PublicKey publicKey) {
            this.sfSigningPubKey = publicKey;
            return this;
        }

        public Builder setAmount(STAmount amount) {
            this.sfAmount = amount;
            return this;
        }

        public Builder setDestination(AccountID destination) {
            this.sfDestination = destination;
            return this;
        }

        public Builder setSendMax(STAmount sendMax) {
            this.sfSendMax = sendMax;
            return this;
        }

        public Builder setTxType(int txType) {
            this.txType = txType;
            return this;
        }

        public STTx build() {
            return new STTx(txType, sfAccount, sfFee, sfFlags, sfSequence,
                    sfSigningPubKey, sfAmount, sfDestination, sfSendMax);
        }
    }
}

