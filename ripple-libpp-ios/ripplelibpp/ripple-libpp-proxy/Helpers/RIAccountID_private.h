//
//  RIAccountID_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/13/17.
//
//

#ifndef RIAccountID_private_h
#define RIAccountID_private_h

#import "RIAccountID.h"
#import <ripple/protocol/AccountID.h>

using namespace ripple;

@interface RIAccountID()

@property (nonatomic) AccountID *accountID;

- (instancetype)initWith:(AccountID *)accountID;

@end

#endif /* RIAccountID_private_h */
