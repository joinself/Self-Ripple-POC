package com.ripple.ripplelibpp;

/**
 * Created by denys on 17.07.17.
 */

public class TxFlags {

    public static long requireDestTag = 0x00010000;

    public static long tfFullyCanonicalSig    = 0x80000000;
    public static long tfUniversal            = tfFullyCanonicalSig;
    public static long tfUniversalMask        = ~ tfUniversal;

// AccountSet flags:
// VFALCO TODO Javadoc comment every one of these constants
//public staic long
//TxFlag::requireDestTag       = 0x00010000;
    public static long tfOptionalDestTag      = 0x00020000;
    public static long tfRequireAuth          = 0x00040000;
    public static long tfOptionalAuth         = 0x00080000;
    public static long tfDisallowXRP          = 0x00100000;
    public static long tfAllowXRP             = 0x00200000;
    public static long tfAccountSetMask       = ~ (tfUniversal | requireDestTag | tfOptionalDestTag
            | tfRequireAuth | tfOptionalAuth
            | tfDisallowXRP | tfAllowXRP);

// AccountSet SetFlag/ClearFlag values
    public static long asfRequireDest         = 1;
    public static long asfRequireAuth         = 2;
    public static long asfDisallowXRP         = 3;
    public static long asfDisableMaster       = 4;
    public static long asfAccountTxnID        = 5;
    public static long asfNoFreeze            = 6;
    public static long asfGlobalFreeze        = 7;
    public static long asfDefaultRipple       = 8;

// OfferCreate flags:
    public static long tfPassive              = 0x00010000;
    public static long tfImmediateOrCancel    = 0x00020000;
    public static long tfFillOrKill           = 0x00040000;
    public static long tfSell                 = 0x00080000;
    public static long tfOfferCreateMask      = ~ (tfUniversal | tfPassive | tfImmediateOrCancel | tfFillOrKill | tfSell);

// Payment flags:
    public static long tfNoRippleDirect       = 0x00010000;
    public static long tfPartialPayment       = 0x00020000;
    public static long tfLimitQuality         = 0x00040000;
    public static long tfPaymentMask          = ~ (tfUniversal | tfPartialPayment | tfLimitQuality | tfNoRippleDirect);

// TrustSet flags:
    public static long tfSetfAuth             = 0x00010000;
    public static long tfSetNoRipple          = 0x00020000;
    public static long tfClearNoRipple        = 0x00040000;
    public static long tfSetFreeze            = 0x00100000;
    public static long tfClearFreeze          = 0x00200000;
    public static long tfTrustSetMask         = ~ (tfUniversal | tfSetfAuth | tfSetNoRipple | tfClearNoRipple
            | tfSetFreeze | tfClearFreeze);

// EnableAmendment flags:
    public static long tfGotMajority          = 0x00010000;
    public static long tfLostMajority         = 0x00020000;

// PaymentChannel flags:
    public static long tfRenew                = 0x00010000;
    public static long tfClose                = 0x00020000;
}
