

#import "RIBlob_private.h"
#import <ripple/basics/StringUtilities.h>

@implementation RIBlob

@synthesize blob;

-(void)dealloc {
    delete self.blob;
}

- (instancetype)initWithBlob:(Blob *) blb {
    self = [super init];
    if (self) {
        blob = new Blob(*blb);
    }
    return self;
}

- (NSString *)strHex {
    auto const hex = strHex(*blob);
    return [NSString stringWithUTF8String: hex.data()];
}

    - (instancetype)initWithStrHex:(NSString *)strHex {
        self = [super init];
        if (self) {
            std::string blbString = strHex.UTF8String;
            auto ret{ strUnHex(blbString) };
            if (!ret.second || !ret.first.size()) {
                return nil;
            }
            self.blob = &ret.first;
        }
        return self;
    }
    @end
