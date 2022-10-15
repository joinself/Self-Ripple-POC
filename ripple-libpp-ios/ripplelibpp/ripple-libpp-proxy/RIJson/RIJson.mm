//
//  RIJson.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/16/17.
//
//

#import "RIJson_private.h"

@implementation RIJson

-(void)dealloc {
    delete self.jsonValue;
}

- (instancetype)initWithJsonValue:(Value *)value {
    self = [super init];
    if (self) {
        self.jsonValue = new Value(* value);
    }
    return self;
}

- (NSString *)toStyledString {
    auto styledString = self.jsonValue->toStyledString();
    return [NSString stringWithUTF8String: styledString.data()];

}

@end
