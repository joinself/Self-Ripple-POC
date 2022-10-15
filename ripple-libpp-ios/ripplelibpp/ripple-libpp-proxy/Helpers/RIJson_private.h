//
//  RIJson_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/16/17.
//
//

#ifndef RIJson_private_h
#define RIJson_private_h

#import "RIJson.h"
#import <ripple/json/json_value.h>

using namespace Json;

@interface RIJson()

- (instancetype)initWithJsonValue:(Value *)value;

@property (nonatomic) Value *jsonValue;

@end

#endif /* RIJson_private_h */
