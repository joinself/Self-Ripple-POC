

#import <Foundation/Foundation.h>
@class RIPublicKey;
@class RISecretKey;
@class RISeed;
@class RISlice;
@class RIBuffer;

typedef NS_ENUM(NSInteger, RIKeyType) {
    invalid = -1,
    unknown = -2,
    secp256k1 = 0,
    ed25519   = 1
};

NS_ASSUME_NONNULL_BEGIN

@interface RIKeyPair : NSObject

@property (nonatomic, strong, readonly) RIPublicKey *publicKey;

@property (nonatomic, strong, readonly) RISecretKey *secretKey;

- (instancetype _Nonnull)init __attribute__((unavailable("use init:(RIKeyType)key seed:(RISeed *)seed or generateKeyPair:(RIKeyType)key seed:(RISeed *)seed")));

- (instancetype)init:(RIKeyType)key seed:(RISeed *)seed;

+ (instancetype)generateKeyPair:(RIKeyType)key seed:(RISeed *)seed;

- (RIBuffer *)sign:(RISlice *)slice;

@end

NS_ASSUME_NONNULL_END
