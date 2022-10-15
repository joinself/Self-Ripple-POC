#import "RIBase58Convertible.h"

@class RIPublicKey;

NS_ASSUME_NONNULL_BEGIN

@interface RIAccountID : NSObject<RIBase58Convertible>

- (instancetype)init __attribute__((unavailable("use init:(RIPublicKey *)publicKey or calcAccountID:(RIPublicKey *)publicKey")));

- (instancetype)init:(RIPublicKey *)publicKey;

+ (instancetype)calcAccountID:(RIPublicKey *)publicKey;

- (NSComparisonResult)compare:(RIAccountID *)acc;

@end

NS_ASSUME_NONNULL_END
