//
//  RIIssue.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import "RIIssue_private.h"
#import "RICurrency_private.h"
#import "RIAccountID_private.h"

@interface RIIssue()

@property (nonatomic, strong, readwrite) RICurrency *currency;
@property (nonatomic, strong, readwrite) RIAccountID *accountID;

@end

@implementation RIIssue

-(void)dealloc {
    delete self.issue;
}

- (instancetype _Nonnull)initWithCurrency:(RICurrency *_Nonnull)currency andAccountID:(RIAccountID *_Nonnull)accountID {
    self = [super init];
    if (self) {
        self.currency = currency;
        self.accountID = accountID;
        
        Currency curr = *currency.currency;
        AccountID accID = *(accountID.accountID);
        self.issue = new Issue(curr, accID);
    }
    return self;
}
@end
