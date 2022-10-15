//
//  RISTObjectBuilder.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#import <Foundation/Foundation.h>
#import "RISField.h"

@class RIAccountID;
@class RIPublicKey;
@class RIBuffer;


@interface RISTObjectBuilder : NSObject

- (instancetype)buildAccountID:(RIAccountID *)accountID;

- (instancetype)buildPublicKey:(RIPublicKey *)publicKey;

- (instancetype)buildPublicKeyWithEmptySlice;

- (instancetype)buildSField:(RISField)sField;

- (instancetype)buildMultisig:(RIBuffer *)buffer;
@end
