//
//  RISerializer.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import <Foundation/Foundation.h>
@class RIBlob;
@class RISTObject;
@class RIAccountID;
@class RISlice;

NS_ASSUME_NONNULL_BEGIN

@interface RISerializer : NSObject

- (instancetype)init __attribute__((unavailable));
- (RIBlob *)peekData;

+ (instancetype)buildMultiSigningData:(RISTObject *)stObject accountID:(RIAccountID *)accountID;

- (RISlice *)getSlice;

@end

NS_ASSUME_NONNULL_END
