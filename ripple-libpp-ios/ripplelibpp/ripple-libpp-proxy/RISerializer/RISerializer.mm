
#import "RISerializer_private.h"
#import "RIBlob_private.h"
#import "RISTObject_private.h"
#import "RIAccountID_private.h"
#import "RISlice_private.h"

#import <ripple/protocol/Sign.h>

//#import 
@implementation RISerializer

-(void)dealloc {
    delete self.serializer;
}

- (instancetype)initWithSerializer:(Serializer *) sr {
    self = [super init];
    if (self) {
        self.serializer = new Serializer(*sr);
    }
    return self;
}

- (instancetype)initPrivate{
    self = [super init];
    return self;
}

- (RIBlob *)peekData {
    Blob blb = self.serializer->peekData();
    return [[RIBlob alloc]initWithBlob: &blb];
}

+ (instancetype)buildMultiSigningData:(RISTObject *)stObject accountID:(RIAccountID *)accountID {
    RISerializer *serializer = [[RISerializer alloc]initPrivate];
    serializer.serializer = new Serializer(buildMultiSigningData(*(stObject.stObject), *(accountID.accountID)));
    return serializer;
}

- (RISlice *)getSlice {
    RISlice *slice = [[RISlice alloc]init];
    slice.slice = new Slice (self.serializer->slice());
    return slice;
}
    
@end
