#import "RIAccountID_private.h"
#import "RIPublicKey_private.h"

@implementation RIAccountID

@synthesize accountID;

- (void)dealloc {
    delete accountID;
}

#pragma mark: Custom

- (instancetype)init:(RIPublicKey *)publicKey {
    self = [super init];
    if (self) {
        accountID = new AccountID(calcAccountID(*publicKey.publicKey));
    }
    return self;
}

+ (instancetype)calcAccountID:(RIPublicKey *)publicKey {
    return [[RIAccountID alloc]init:publicKey];
}

- (instancetype)initWith:(AccountID *)acc {
    self = [super init];
    if (self) {
        self.accountID = acc;
    }
    return self;
}

#pragma mark: RIBase58Convertible

- (instancetype _Nullable)initWithBase58:(NSString *_Nonnull)base58 {
    self = [super init];
    if (self) {
        accountID = new AccountID(parseBase58<AccountID>(std::string(base58.UTF8String)).value());
    }
    return self;
}

+ (instancetype)parseBase58:(NSString *)base58 {
    return [[RIAccountID alloc]initWithBase58:base58];
}

- (NSString *)toBase58 {
    std::string base58 = toBase58(*accountID);
    return [NSString stringWithUTF8String: base58.data()];
}

- (NSComparisonResult)compare:(RIAccountID *)acc {
    if (self.accountID < acc.accountID) {
        return NSOrderedAscending;
    } else if (self.accountID > acc.accountID) {
        return NSOrderedDescending;
    } else {
        return NSOrderedSame;
    }
}

@end
