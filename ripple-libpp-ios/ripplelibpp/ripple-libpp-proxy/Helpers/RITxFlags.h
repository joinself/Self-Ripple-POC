//
//  RITxFlags.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#import <Foundation/Foundation.h>

@interface RITxFlags : NSObject

// Universal Transaction flags:

@property (class, nonatomic, readonly) UInt32 tfFullyCanonicalSig;
@property (class, nonatomic, readonly) UInt32 tfUniversal;
@property (class, nonatomic, readonly) UInt32 tfUniversalMask;

@end
