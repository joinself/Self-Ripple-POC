package com.ripple.ripplelibpp;

/**
 * Created by denys on 13.07.17.
 */

public class TxType {

    public final static int TT_INVALID           = -1;

    public final static int TT_PAYMENT           = 0;
    public final static int TT_ESCROW_CREATE     = 1;
    public final static int TT_ESCROW_FINISH     = 2;
    public final static int TT_ACCOUNT_SET       = 3;
    public final static int TT_ESCROW_CANCEL     = 4;
    public final static int TT_REGULAR_KEY_SET   = 5;
    public final static int TT_NICKNAME_SET      = 6; // open
    public final static int TT_OFFER_CREATE      = 7;
    public final static int TT_OFFER_CANCEL      = 8;
    public final static int NO_LONGER_USED       = 9;
    public final static int TT_TICKET_CREATE     = 10;
    public final static int TT_TICKET_CANCEL     = 11;
    public final static int TT_SIGNER_LIST_SET   = 12;
    public final static int TT_PAYCHAN_CREATE    = 13;
    public final static int TT_PAYCHAN_FUND      = 14;
    public final static int TT_PAYCHAN_CLAIM     = 15;

    public final static int TT_TRUST_SET         = 20;

    public final static int TT_AMENDMENT         = 100;
    public final static int TT_FEE               = 101;
}
