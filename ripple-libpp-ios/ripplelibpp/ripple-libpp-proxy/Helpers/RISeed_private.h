//
//  RISeed_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/13/17.
//
//

#ifndef RISeed_private_h
#define RISeed_private_h

#import "RISeed.h"
#import <ripple/protocol/Seed.h>
using namespace ripple;

@interface RISeed()

@property (nonatomic) Seed *seed;

@end

#endif /* RISeed_private_h */
