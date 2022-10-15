//
//  RISecretKey_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/13/17.
//
//

#ifndef RISecretKey_private_h
#define RISecretKey_private_h


#import "RISecretKey.h"
#import <ripple/protocol/SecretKey.h>
using namespace ripple;

@interface RISecretKey()

@property (nonatomic) SecretKey *secretKey;

- (instancetype)initWithSecretKey:(SecretKey *) sk;

@end


#endif /* RISecretKey_private_h */
