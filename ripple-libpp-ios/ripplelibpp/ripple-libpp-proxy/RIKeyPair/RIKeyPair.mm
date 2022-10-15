#import "RIKeyPair.h"
#import "RIPublicKey_private.h"
#import "RISecretKey_private.h"
#import "RISeed_private.h"
#import "RIBuffer_private.h"
#import "RISlice_private.h"

#import <ripple/protocol/SecretKey.h>

@interface RIKeyPair()

@property (nonatomic, strong, readwrite) RIPublicKey *publicKey;

@property (nonatomic, strong, readwrite) RISecretKey *secretKey;

@end


@implementation RIKeyPair

- (instancetype)init:(RIKeyType)key seed:(RISeed *)seed {
    self = [super init];
    if (self) {
        
        KeyType type;
        
        switch (key) {
            case invalid:
                type = KeyType::invalid;
                break;
                
            case unknown:
                type = KeyType::unknown;
                break;
                
            case secp256k1:
                type = KeyType::secp256k1;
                break;
                
            case ed25519:
                type = KeyType::ed25519;
                break;
                
            default:
                break;
        }
        
        std::pair<PublicKey, SecretKey> keyPair = generateKeyPair(type, *(seed.seed));
        
        PublicKey *pk = &keyPair.first;
        SecretKey *sk = &keyPair.second;
        
        self.publicKey = [[RIPublicKey alloc]initWithPublicKey: pk];
        self.secretKey = [[RISecretKey alloc]initWithSecretKey: sk];
    }
    
    return self;
}

+ (instancetype)generateKeyPair:(RIKeyType)key seed:(RISeed *)seed {
    return [[RIKeyPair alloc]init:key seed:seed];
}

- (RIBuffer *)sign:(RISlice *)slice {
    auto multisig = sign(*self.publicKey.publicKey, *self.secretKey.secretKey, *slice.slice);
    RIBuffer *buffer = [[RIBuffer alloc]init];
    buffer.buffer = new Buffer(multisig);
    return buffer;
}

@end
