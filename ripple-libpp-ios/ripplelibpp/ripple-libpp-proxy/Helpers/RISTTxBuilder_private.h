//
//  RISTTxBuilder_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import "RISTTxBuilder.h"
#import <ripple/protocol/STTx.h>

@class RIAmount;
@class RIPublicKey;
@class RIAmount;
@class RIAccountID;

using namespace ripple;

@interface RISTTxBuilder()

- (STTx *)build:(TxType)txType;

@property (nonatomic, strong) RIAmount *fee;
@property (nonatomic, strong) RIAmount *amount;
@property (nonatomic, strong) RIAccountID *destination;
@property (nonatomic, strong) RIAmount *sendMax;
@property (nonatomic) UInt32 flags;
@property (nonatomic) UInt32 sequence;

@end
