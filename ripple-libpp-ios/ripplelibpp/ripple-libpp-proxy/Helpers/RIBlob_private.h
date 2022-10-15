//
//  RIBlob_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#ifndef RIBlob_private_h
#define RIBlob_private_h

#import "RIBlob.h"
#import <ripple/protocol/STBlob.h>
#import <ripple/basics/StringUtilities.h>
using namespace ripple;

@interface RIBlob()

@property (nonatomic) std::vector <unsigned char> *blob;

- (instancetype)initWithBlob:(Blob *) blb;

@end


#endif /* RIBlob_private_h */
