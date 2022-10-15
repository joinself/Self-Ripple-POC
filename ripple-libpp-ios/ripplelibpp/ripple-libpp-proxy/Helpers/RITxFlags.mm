//
//  RITxFlags.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import "RITxFlags.h"
#import <ripple/protocol/TxFlags.h>

@implementation RITxFlags

+ (UInt32)tfFullyCanonicalSig   {   return ripple::tfFullyCanonicalSig;     }
+ (UInt32)tfUniversal           {   return ripple::tfUniversal;             }
+ (UInt32)tfUniversalMask       {   return ripple::tfUniversalMask;         }

//@property (class, nonatomic, readonly) UInt32 tfUniversal;
//@property (class, nonatomic, readonly) UInt32 tfUniversal;

//TODO: implement

//
//// AccountSet flags:
//// VFALCO TODO Javadoc comment every one of these constants
////const std::uint32_t TxFlag::requireDestTag       = 0x00010000;
//const std::uint32_t tfOptionalDestTag      = 0x00020000;
//const std::uint32_t tfRequireAuth          = 0x00040000;
//const std::uint32_t tfOptionalAuth         = 0x00080000;
//const std::uint32_t tfDisallowXRP          = 0x00100000;
//const std::uint32_t tfAllowXRP             = 0x00200000;
//const std::uint32_t tfAccountSetMask       = ~ (tfUniversal | TxFlag::requireDestTag | tfOptionalDestTag
//                                                | tfRequireAuth | tfOptionalAuth
//                                                | tfDisallowXRP | tfAllowXRP);
//
//// AccountSet SetFlag/ClearFlag values
//const std::uint32_t asfRequireDest         = 1;
//const std::uint32_t asfRequireAuth         = 2;
//const std::uint32_t asfDisallowXRP         = 3;
//const std::uint32_t asfDisableMaster       = 4;
//const std::uint32_t asfAccountTxnID        = 5;
//const std::uint32_t asfNoFreeze            = 6;
//const std::uint32_t asfGlobalFreeze        = 7;
//const std::uint32_t asfDefaultRipple       = 8;
//
//// OfferCreate flags:
//const std::uint32_t tfPassive              = 0x00010000;
//const std::uint32_t tfImmediateOrCancel    = 0x00020000;
//const std::uint32_t tfFillOrKill           = 0x00040000;
//const std::uint32_t tfSell                 = 0x00080000;
//const std::uint32_t tfOfferCreateMask      = ~ (tfUniversal | tfPassive | tfImmediateOrCancel | tfFillOrKill | tfSell);
//
//// Payment flags:
//const std::uint32_t tfNoRippleDirect       = 0x00010000;
//const std::uint32_t tfPartialPayment       = 0x00020000;
//const std::uint32_t tfLimitQuality         = 0x00040000;
//const std::uint32_t tfPaymentMask          = ~ (tfUniversal | tfPartialPayment | tfLimitQuality | tfNoRippleDirect);
//
//// TrustSet flags:
//const std::uint32_t tfSetfAuth             = 0x00010000;
//const std::uint32_t tfSetNoRipple          = 0x00020000;
//const std::uint32_t tfClearNoRipple        = 0x00040000;
//const std::uint32_t tfSetFreeze            = 0x00100000;
//const std::uint32_t tfClearFreeze          = 0x00200000;
//const std::uint32_t tfTrustSetMask         = ~ (tfUniversal | tfSetfAuth | tfSetNoRipple | tfClearNoRipple
//                                                | tfSetFreeze | tfClearFreeze);
//
//// EnableAmendment flags:
//const std::uint32_t tfGotMajority          = 0x00010000;
//const std::uint32_t tfLostMajority         = 0x00020000;
//
//// PaymentChannel flags:
//const std::uint32_t tfRenew                = 0x00010000;
//const std::uint32_t tfClose                = 0x000

@end
