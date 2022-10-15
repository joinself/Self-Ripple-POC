
#import <Foundation/Foundation.h>

@protocol RIBase58Convertible <NSObject>

- (instancetype _Nullable)initWithBase58:(NSString *_Nonnull)base58;

+ (instancetype _Nullable)parseBase58:(NSString *_Nonnull)base58;

- (NSString *_Nonnull)toBase58;

@end
