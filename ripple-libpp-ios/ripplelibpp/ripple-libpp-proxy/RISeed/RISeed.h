
#import <Foundation/Foundation.h>
#import "RIBase58Convertible.h"

@interface RISeed : NSObject<RIBase58Convertible>

- (instancetype _Nonnull)init __attribute__((unavailable("use init:(NSString *_Nonnull)str or parseGenericSeed:(NSString *_Nonnull)str")));

- (instancetype _Nullable)init:(NSString *_Nonnull)str;

+ (instancetype _Nullable)parseGenericSeed:(NSString *_Nonnull)str;
@end
