#import "RISecretKey_private.h"

@implementation RISecretKey

@synthesize secretKey;

- (instancetype)initWithSecretKey:(SecretKey *) sk {
    self = [super init];
    if (self) {
        secretKey = new SecretKey(*sk);
    }
    return self;
}

-(void)dealloc {
    delete secretKey;
}

@end
