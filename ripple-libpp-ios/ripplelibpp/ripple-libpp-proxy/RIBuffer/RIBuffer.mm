//
//  RIBuffer.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#import "RIBuffer_private.h"

@implementation RIBuffer

- (void)dealloc {
    delete self.buffer;
}

@end
