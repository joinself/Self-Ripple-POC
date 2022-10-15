//
//  RICurrency.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import "RICurrency_private.h"

@implementation RICurrency

-(void)dealloc {
    delete self.currency;
}

- (instancetype _Nonnull)initWithCurrencyString:(NSString *)currencyString {
    self = [super init];
    if (self) {
        std::string currencyStr = currencyString.UTF8String;
        self.currency = new Currency(to_currency(currencyStr));
    }
    return self;
}

+(instancetype _Nonnull)toCurrency:(NSString *)currencyString {
    return [[RICurrency alloc]initWithCurrencyString:currencyString];
}

@end
