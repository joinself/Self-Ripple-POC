//
//  RIUInt256.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/17/17.
//
//

#import "RIBaseUInt_private.h"

@interface RIBaseUInt()

@property (nonatomic) uint256 uint256value;

@end


@implementation RIBaseUInt

- (instancetype)initWithUInt256:(uint256)value {
    self = [super init];
    if (self) {
        self.uint256value = value;
    }
    return self;
}

- (uint256)getUInt256Value {
    return self.uint256value;
}

- (BOOL)isEqual:(id)object {
    RIBaseUInt *other = (RIBaseUInt *)object;
    
    return self.uint256value == other.uint256value;
}

@end
