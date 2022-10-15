//
//  RIAmount.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import "RIAmount_private.h"
#import "RIIssue_private.h"


@implementation RIAmount

- (void)dealloc {
    delete self.amount;
}

- (instancetype)initWithAmountValue:(UInt64)amount {
    self = [super init];
    if (self) {
        self.amount = new STAmount { amount };;
    }
    return self;
}

    - (instancetype _Nonnull)initWithIssue:(RIIssue *)issue mantissa:(int)mantissa exponent:(int)exponent {
        self = [super init];
        if (self) {
            Issue iss = *issue.issue;
            self.amount = new STAmount(iss, mantissa, exponent);
        }
        return self;
    }

@end
