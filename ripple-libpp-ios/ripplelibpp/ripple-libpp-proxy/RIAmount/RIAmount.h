//
//  RIAmount.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import <Foundation/Foundation.h>
@class RIIssue;

@interface RIAmount : NSObject

    - (instancetype _Nonnull)init __attribute__((unavailable));

- (instancetype _Nonnull)initWithAmountValue:(UInt64)amount;
    - (instancetype _Nonnull)initWithIssue:(RIIssue *_Nonnull)issue mantissa:(int)mantissa exponent:(int)exponent;

@end
