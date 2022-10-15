//
//  RIUInt256_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/17/17.
//
//

#ifndef RIUInt256_private_h
#define RIUInt256_private_h

#import "RIBaseUInt.h"
#import <ripple/basics/base_uint.h>

using namespace ripple;

@interface RIBaseUInt ()

- (instancetype)initWithUInt256:(uint256)value;

- (uint256)getUInt256Value;

@end

#endif /* RIUInt256_private_h */
