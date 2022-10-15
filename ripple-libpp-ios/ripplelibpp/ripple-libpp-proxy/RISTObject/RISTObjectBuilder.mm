//
//  RISTObjectBuilder.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#import "RISTObjectBuilder_private.h"
#import "RIAccountID_private.h"
#import "RIPublicKey_private.h"
#import "RIBuffer_private.h"
#include <ripple/protocol/st.h>

@implementation RISTObjectBuilder

-(void)dealloc {
    delete self.slice;
}

- (instancetype)buildAccountID:(RIAccountID *)accountID {
    self.accountID = accountID;
    return self;
}

- (instancetype)buildPublicKey:(RIPublicKey *)publicKey {
    self.publicKey = publicKey;
    return self;
}

- (instancetype)buildPublicKeyWithEmptySlice {
    self.slice = new Slice(nullptr, 0);
    return self;
}

- (instancetype)buildSField:(RISField)sField {
    self.usedField = sField;
    return self;
}

- (instancetype)buildMultisig:(RIBuffer *)buffer {
    self.multisig = buffer;
    return self;
}

- (STObject *)build {
    STObject *object;
    if (self.usedField != riNone) {
        switch (self.usedField) {
            case risfSigner:
                object = new STObject(sfSigner);
                break;
                
            case risfSigners:
                object = new STObject(sfSigners);
                break;
                
            case risfAccount:
                object = new STObject(sfAccount);
                break;
            default:
                break;
        }
    }
    
    STObject element = *object;
    element[sfAccount] = *self.accountID.accountID;
    element[sfSigningPubKey] = *self.publicKey.publicKey;
    

    element[sfTxnSignature] = *self.multisig.buffer;
    
    std::cout << element;

    return object;
}


@end
