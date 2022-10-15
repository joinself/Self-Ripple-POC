//
//  RISerializer_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#ifndef RISerializer_private_h
#define RISerializer_private_h

#import <ripple/protocol/Serializer.h>
#import "RISerializer.h"
using namespace ripple;

@interface RISerializer()

@property (nonatomic) Serializer *serializer;

- (instancetype)initWithSerializer:(Serializer *) sr;

@end

#endif /* RISerializer_private_h */
