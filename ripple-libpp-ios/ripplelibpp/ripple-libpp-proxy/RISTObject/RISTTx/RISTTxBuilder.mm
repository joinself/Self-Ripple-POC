
#import "RISTTxBuilder_private.h"
#import "RISTObjectBuilder_private.h"
#import "RIAccountID_private.h"
#import "RIAmount_private.h"
#import "RIPublicKey_private.h"
#import "RIBuffer_private.h"

#import <ripple/protocol/TxFormats.h>
#import <ripple/protocol/TxFlags.h>
#import <ripple/protocol/st.h>

@implementation RISTTxBuilder

- (instancetype)buildFee:(RIAmount *)fee {
    self.fee = fee;
    return self;
}

- (instancetype)buildFlags:(UInt32)flags {
    self.flags = flags;
    return self;
}
- (instancetype)buildSequence:(UInt32)sequence {
    self.sequence = sequence;
    return self;
}

- (instancetype)buildAmount:(RIAmount *)amount {
    self.amount = amount;
    return self;
}
- (instancetype)buildDestination:(RIAccountID *)destination {
    self.destination = destination;
    return self;
}
- (instancetype)buildSendMax:(RIAmount *)sendMax {
    self.sendMax = sendMax;
    return self;
}

- (STTx *)build:(TxType)txType {

    return new STTx(txType,
                    [&](auto& obj)
                    {
                        // General transaction fields
                        if (self.accountID.accountID) {
                            obj[sfAccount] = *self.accountID.accountID;
                        }
                    
                        if (self.fee.amount) {
                            obj[sfFee] = *self.fee.amount;
                        }
                        
                        if (self.flags) {
                            obj[sfFlags] = self.flags;
                        }
                        if (self.sequence) {
                            obj[sfSequence] = self.sequence;
                        }
                        if (self.publicKey) {
                            obj[sfSigningPubKey] = self.publicKey.publicKey->slice();
                        }
                        if (self.slice) {
                            obj[sfSigningPubKey] = *self.slice;
                        }
                        // Payment-specific fields
                        if (self.amount.amount) {
                            obj[sfAmount] = *self.amount.amount;
                        }
                        
                        if (self.destination.accountID) {
                            obj[sfDestination] = *self.destination.accountID;
                        }
                        if (self.sendMax.amount) {
                            obj[sfSendMax] = *self.sendMax.amount;
                        }
                        if (self.multisig) {
                            obj[sfTxnSignature] = *self.multisig.buffer;
                        }
//                        if (self.usedField)
                    });
}
@end
