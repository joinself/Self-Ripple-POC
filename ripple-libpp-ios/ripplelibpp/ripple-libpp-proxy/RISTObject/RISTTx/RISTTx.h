//
//  RISTTx.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/13/17.
//
//

#import <Foundation/Foundation.h>
#import "RIStrHexConvertible.h"
#import "RITxType.h"
#import "RISTObject.h"

#import "RIKeyPair.h"

@class RISerializer;
@class RISTTxBuilder;
@class RIJson;
@class RIPublicKey;
@class RISecretKey;
@class RIBaseUInt;
@class RIPublicKey;
@class RISTTxSign;


@class RISeed;
@class RIKeyPair;
@class RIAccountID;

NS_ASSUME_NONNULL_BEGIN

@interface RISTTx : RISTObject<RIStrHexConvertible>

- (instancetype)initWithBuilder:(RISTTxBuilder *)builder txType:(RITxType)txType;

- (RISerializer *)getSerializer;

- (RIJson *)getJson:(int)index;

- (RIJson *)getJson:(int)index binary: (BOOL)binary;

- (RIBaseUInt  *)getTransactionID;

- (void)sign:(RIPublicKey *)publicKey secretKey:(RISecretKey *)secretKey;

- (RISTTxSign *)checkSign:(BOOL)allowMultiSign;

- (BOOL)verify:(RIPublicKey *)pubicKey mustBeFullyCanonical:(BOOL)mustBeFullyCanonical;

- (BOOL)multisign:(NSString *)name seed:(RISeed *)seed pair:(RIKeyPair *)pair accID:(RIAccountID *)accID keyType:(RIKeyType)keyType;

@end

NS_ASSUME_NONNULL_END
