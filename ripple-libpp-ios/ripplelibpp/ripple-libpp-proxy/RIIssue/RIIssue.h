//
//  RIIssue.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import <Foundation/Foundation.h>
@class RICurrency;
@class RIAccountID;

@interface RIIssue : NSObject
    
    @property (nonatomic, strong, readonly) RICurrency *_Nonnull currency;
    @property (nonatomic, strong, readonly) RIAccountID *_Nonnull accountID;
    
    - (instancetype _Nonnull)init __attribute__((unavailable));
    
    - (instancetype _Nonnull)initWithCurrency:(RICurrency *_Nonnull)currency andAccountID:(RIAccountID *_Nonnull)accountID;
    
    @end
