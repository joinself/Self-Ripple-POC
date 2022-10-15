//
//  RISTObject.m
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/18/17.
//
//

#import "RISTObject_private.h"
#import "RISTObjectBuilder_private.h"
#import "RIAccountID_private.h"

#import <ripple/protocol/SField.h>
#import <ripple/protocol/STArray.h>

@implementation RISTObject

-(void)dealloc {
    delete self.stObject;
}

- (instancetype)initPrivate {
    self = [super init];
    return self;
}

- (instancetype)initWithBuilder:(RISTObjectBuilder *)builder {
    self = [super init];
    if (self) {
        self.stObject = [builder build];
    }
    return self;
}

- (void)setField:(RISField)field toArray:(NSArray <RISTObject *>*)array {
    STArray sArray = {};
    for (RISTObject *object in array) {
        sArray.emplace_back (std::move (*object.stObject));
    }
    
    switch (field) {
        case risfSigner:
            self.stObject->setFieldArray (sfSigner, sArray);
            break;
            
        case risfSigners:
            self.stObject->setFieldArray (sfSigners, sArray);
            break;
            
        case risfAccount:
            self.stObject->setFieldArray (sfAccount, sArray);
            break;
            
        default:
            break;
    }
}

- (NSArray <RISTObject *> *)peekFieldArray:(RISField)field {
    NSMutableArray <RISTObject *> *objects = [@[] mutableCopy];
    STArray rObjects;
    
    switch (field) {
        case risfSigner:
            rObjects = self.stObject->peekFieldArray (sfSigner);
            break;
            
        case risfSigners:
            rObjects = self.stObject->peekFieldArray (sfSigners);
            break;
            
        case risfAccount:
            rObjects = self.stObject->peekFieldArray (sfAccount);
            break;
            
        default:
            break;
    }
    
    for (auto obj : rObjects) {
        RISTObject *stObj = [[RISTObject alloc]initPrivate];
        stObj.stObject = &obj;
        [objects addObject: stObj];
    }
    
    return objects;
}

- (BOOL)isFieldPresent:(RISField)field {
//    self.stObject->ge÷
    BOOL result = false;
    switch (field) {
        case risfSigner:
            result = self.stObject->isFieldPresent(sfSigner);
            break;
            
        case risfSigners:
            result = self.stObject->isFieldPresent(sfSigners);
            break;
            
        case risfAccount:
            result = self.stObject->isFieldPresent(sfAccount);
            break;
            
        default:
            break;
    }
    return result;
}

- (RIAccountID *)getAccountID:(RISField)field {
    RIAccountID *acc;
    
    switch (field) {
        case risfSigner:
            acc = [[RIAccountID alloc]initWith: new AccountID(self.stObject->getAccountID(sfSigner))];
            break;
            
        case risfSigners:
            acc = [[RIAccountID alloc]initWith: new AccountID(self.stObject->getAccountID(sfSigners))];
            break;
            
        case risfAccount:
            acc = [[RIAccountID alloc]initWith: new AccountID(self.stObject->getAccountID(sfAccount))];
            break;
            
        default:
            break;
    }
    
    return acc;
}

- (void)emplaceBack:(RISTObject *)element {
    
    
    STArray& signers {self.stObject->peekFieldArray (sfSigners)};
    signers.emplace_back (std::move (*(element.stObject)));
    
    // Sort the Signers array by Account.  If it is not sorted when submitted
    // to the network then it will be rejected.
    std::sort (signers.begin(), signers.end(),
               [](STObject const& a, STObject const& b)
               {
//                   auto acc1 = a[sfAccount];
//                   auto acc2 = a.getAccountID(sfAccount);
//                   
//                   assert(acc1 == acc2);
                   
                   return a.getAccountID(sfAccount) < b.getAccountID(sfAccount);// (a[sfAccount] < b[sfAccount]);
               });
}
@end
