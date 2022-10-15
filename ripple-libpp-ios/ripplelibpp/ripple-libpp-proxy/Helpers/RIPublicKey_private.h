//
//  RIPublicKey_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/13/17.
//
//

#ifndef RIPublicKey_private_h
#define RIPublicKey_private_h


#import "RIPublicKey.h"
#import <ripple/protocol/PublicKey.h>
using namespace ripple;

@interface RIPublicKey()

@property (nonatomic) PublicKey *publicKey;

- (instancetype)initWithPublicKey:(PublicKey *) pk;

@end

#endif /* RIPublicKey_private_h */
