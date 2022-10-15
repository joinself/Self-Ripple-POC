#import "RISeed_private.h"

@implementation RISeed

@synthesize seed;

- (void)dealloc {
    delete seed;
}

- (instancetype _Nullable)init:(NSString *_Nonnull)str {
    self = [super init];
    if (self) {
        seed = new Seed(parseGenericSeed(std::string(str.UTF8String)).value());
    }
    return self;
}

+ (instancetype _Nullable)parseGenericSeed:(NSString *_Nonnull)str {
    return [[RISeed alloc]init:str];
}

#pragma mark: RIBase58Convertible

- (instancetype _Nullable)initWithBase58:(NSString *_Nonnull)base58 {
    self = [super init];
    if (self) {
        seed = new Seed(parseBase58<Seed>(std::string(base58.UTF8String)).value());
    }
    return self;
}

+ (instancetype _Nullable)parseBase58:(NSString *_Nonnull)base58 {
    return [[RISeed alloc]initWithBase58:base58];
}

- (NSString *_Nonnull)toBase58 {
    std::string base58 = toBase58(*(self.seed));
    return [NSString stringWithUTF8String: base58.data()];
}


@end
