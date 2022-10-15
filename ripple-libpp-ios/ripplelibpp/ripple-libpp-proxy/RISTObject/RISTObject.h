//
//  RISTObject.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#import <Foundation/Foundation.h>
#import "RISField.h"

@class RISTObjectBuilder;
@class RIAccountID;

@interface RISTObject : NSObject

- (instancetype)init __attribute__((unavailable));

- (instancetype)initWithBuilder:(RISTObjectBuilder *)builder;

- (void)setField:(RISField)field toArray:(NSArray <RISTObject *> *)array;

- (NSArray <RISTObject *> *)peekFieldArray:(RISField)field;

- (BOOL)isFieldPresent:(RISField)field;

- (RIAccountID *)getAccountID:(RISField)field;

- (void)emplaceBack:(RISTObject *)element;

@end
