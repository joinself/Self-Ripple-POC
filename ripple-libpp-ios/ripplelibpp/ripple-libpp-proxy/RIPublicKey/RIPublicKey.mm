#import "RIPublicKey_private.h"
#import <ripple/basics/Slice.h>

@implementation RIPublicKey

@synthesize publicKey;

- (instancetype)initWithPublicKey:(PublicKey *) pk {
    self = [super init];
    if (self) {
        publicKey = new PublicKey(*pk);
    }
    return self;
}

-(void)dealloc {
    delete publicKey;
}

@end
