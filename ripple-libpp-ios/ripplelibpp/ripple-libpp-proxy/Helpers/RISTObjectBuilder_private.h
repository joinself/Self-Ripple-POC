//
//  RISTObjectBuilder.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#ifndef RISTObjectBuilder_h
#define RISTObjectBuilder_h

#import "RISTObjectBuilder.h"
#import <ripple/basics/Slice.h>
#import <ripple/protocol/STObject.h>
#import "RISField.h"

@class RIBuffer;

//using namespace ripple;

@interface RISTObjectBuilder()

@property (nonatomic, strong) RIAccountID *accountID;

@property (nonatomic, strong) RIPublicKey *publicKey;

@property (nonatomic) ripple::Slice *slice;

@property (nonatomic) RISField usedField;

@property (nonatomic) RIBuffer *multisig;

- (ripple::STObject *)build;

@end
#endif /* RISTObjectBuilder_h */
