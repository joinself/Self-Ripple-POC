//
//  RICurrency.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#ifndef RICurrency_h
#define RICurrency_h

#import "RICurrency.h"
#import <ripple/protocol/UintTypes.h>
using namespace ripple;

@interface RICurrency()
    
    @property (nonatomic) Currency *currency;
    
    @end

#endif /* RICurrency_h */
