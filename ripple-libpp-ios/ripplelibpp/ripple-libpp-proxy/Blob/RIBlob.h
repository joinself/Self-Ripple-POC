//
//  RIBlob.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#ifndef RIBlob_h
#define RIBlob_h

#import <Foundation/Foundation.h>
#import "RIStrHexConvertible.h"

@interface RIBlob : NSObject<RIStrHexConvertible>
- (instancetype _Nonnull)init __attribute__((unavailable));

@end

#endif /* RIBlob_h */
