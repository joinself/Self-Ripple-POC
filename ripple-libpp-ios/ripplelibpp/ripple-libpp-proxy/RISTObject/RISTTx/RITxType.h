//
//  RITxType.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#ifndef RITxType_h
#define RITxType_h

typedef NS_ENUM(NSInteger, RITxType) {
    riINVALID           = -1,
    
    riPAYMENT           = 0,
    riESCROW_CREATE     = 1,
    riESCROW_FINISH     = 2,
    riACCOUNT_SET       = 3,
    riESCROW_CANCEL     = 4,
    riREGULAR_KEY_SET   = 5,
    riNICKNAME_SET      = 6, // open
    riOFFER_CREATE      = 7,
    riOFFER_CANCEL      = 8,
    ri_no_longer_used   = 9,
    riTICKET_CREATE     = 10,
    riTICKET_CANCEL     = 11,
    riSIGNER_LIST_SET   = 12,
    riPAYCHAN_CREATE    = 13,
    riPAYCHAN_FUND      = 14,
    riPAYCHAN_CLAIM     = 15,
    
    riTRUST_SET         = 20,
    
    riAMENDMENT         = 100,
    riFEE               = 101,
};

#endif /* RITxType_h */
