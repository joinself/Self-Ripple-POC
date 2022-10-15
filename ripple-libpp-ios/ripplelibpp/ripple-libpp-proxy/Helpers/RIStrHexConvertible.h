//
//  RIStrHexConvertible.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#ifndef RIStrHexConvertible_h
#define RIStrHexConvertible_h

#import <Foundation/Foundation.h>

@protocol RIStrHexConvertible <NSObject>

//    @optional
- (NSString *_Nonnull)strHex;
    - (instancetype _Nullable)initWithStrHex:(NSString *_Nonnull)strHex;

@end

#endif /* RIStrHexConvertible_h */
