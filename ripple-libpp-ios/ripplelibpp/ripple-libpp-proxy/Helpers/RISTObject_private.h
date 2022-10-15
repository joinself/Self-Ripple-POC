//
//  RISTTx_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/13/17.
//
//

#ifndef RISTTx_private_h
#define RISTTx_private_h

#import "RISTObject.h"

#import <ripple/protocol/STObject.h>

using namespace ripple;

@interface RISTObject()
@property (nonatomic) STObject *stObject;
- (instancetype)initPrivate;

@end

#endif /* RISTTx_private_h */
