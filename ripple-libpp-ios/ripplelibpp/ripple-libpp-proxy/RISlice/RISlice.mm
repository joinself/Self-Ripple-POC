//
//  RISlice.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#import "RISlice_private.h"

@implementation RISlice

- (void)dealloc {
    delete self.slice;
}

@end
