//
//  RISTTxBuilder.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import <Foundation/Foundation.h>
#import "RISTObjectBuilder.h"

@class RIAccountID;
@class RIAmount;
@class RIPublicKey;

NS_ASSUME_NONNULL_BEGIN

@interface RISTTxBuilder : RISTObjectBuilder

- (instancetype)buildFee:(RIAmount *)fee;

- (instancetype)buildFlags:(UInt32)flags;

- (instancetype)buildSequence:(UInt32)sequence;

- (instancetype)buildAmount:(RIAmount *)amount;

- (instancetype)buildDestination:(RIAccountID *)destination;

- (instancetype)buildSendMax:(RIAmount *)sendMax;

@end

NS_ASSUME_NONNULL_END
