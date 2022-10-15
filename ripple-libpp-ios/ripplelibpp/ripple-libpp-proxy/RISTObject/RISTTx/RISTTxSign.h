//
//  RISTTxSign.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/17/17.
//
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface RISTTxSign : NSObject

@property (nonatomic) BOOL result;
@property (nonatomic, strong) NSString *errorString;

@end

NS_ASSUME_NONNULL_END
