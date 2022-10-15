//
//  RIAmount_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#ifndef RIAmount_private_h
#define RIAmount_private_h

#import "RIAmount.h"
#import <ripple/protocol/STAmount.h>

using namespace ripple;

@interface RIAmount()

@property (nonatomic)  STAmount *amount;

@end

#endif /* RIAmount_private_h */
