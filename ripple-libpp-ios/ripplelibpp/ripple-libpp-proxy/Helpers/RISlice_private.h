//
//  RISlice_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#ifndef RISlice_private_h
#define RISlice_private_h

#import "RISlice.h"
#import <ripple/basics/Slice.h>

using namespace ripple;

@interface RISlice()

@property (nonatomic) Slice *slice;

@end

#endif /* RISlice_private_h */
