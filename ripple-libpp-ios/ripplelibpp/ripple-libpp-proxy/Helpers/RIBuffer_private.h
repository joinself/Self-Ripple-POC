//
//  RIBuffer_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#ifndef RIBuffer_private_h
#define RIBuffer_private_h

#import "RIBuffer.h"
#import <ripple/basics/Buffer.h>

using namespace ripple;

@interface RIBuffer()

@property (nonatomic) Buffer *buffer;

@end

#endif /* RIBuffer_private_h */
