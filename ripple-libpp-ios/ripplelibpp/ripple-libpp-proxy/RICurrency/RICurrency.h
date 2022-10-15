//
//  RICurrency.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import <Foundation/Foundation.h>

@interface RICurrency : NSObject

    
    - (instancetype _Nonnull)init __attribute__((unavailable));
    
- (instancetype _Nonnull)initWithCurrencyString:(NSString *_Nonnull)currencyString;
    
    +(instancetype _Nonnull)toCurrency:(NSString *_Nonnull)currencyString;
    
@end
