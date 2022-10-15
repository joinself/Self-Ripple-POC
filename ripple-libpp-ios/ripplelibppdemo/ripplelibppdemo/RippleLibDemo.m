//
//  RippleLibDemo.m
//  ripplelibppdemo
//
//  Created by Dmitriy Shulzhenko on 7/13/17.
//
//

#import "RippleLibDemo.h"
#import <ripplelibpp/ripplelibpp.h>

@interface DemoCredentials : NSObject

@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) RISeed *seed;
@property (nonatomic, strong) RIKeyPair *pair;
@property (nonatomic, strong) RIAccountID *accID;
@property (nonatomic) RIKeyType keyType;

- (instancetype)initWithName:(NSString *)name;
- (instancetype)initWithName:(NSString *)name keyType:(RIKeyType)keyType;

@end
@implementation DemoCredentials

- (instancetype)initWithName:(NSString *)name {
    self = [super init];
    if (self) {
        self = [self initWithName:name keyType:secp256k1];
    }
    return self;
}

- (instancetype)initWithName:(NSString *)name keyType:(RIKeyType)keyType {
    self = [super init];
    if (self) {
        self.name = name;
        self.keyType = keyType;
        self.seed = [RISeed parseGenericSeed:name];
        self.pair = [RIKeyPair generateKeyPair:keyType seed:self.seed];
        self.accID = [RIAccountID calcAccountID:self.pair.publicKey];
    }
    return self;
}

@end


@implementation RippleLibDemo

- (NSString *)serialize: (RISTTx *)sttx {
    return [sttx strHex];
}

- (RISTTx *)deserialize: (NSString *) strBlob {
    return [[RISTTx alloc]initWithStrHex: strBlob];
}

//------------------------------------------------------------------------------

- (BOOL)demonstrateSigning:(RIKeyType)keyType seedStr:(NSString *)seedStr expectedAccount:(NSString *)expectedAccount {
    RISeed *seed = [RISeed parseGenericSeed:seedStr];
    
    assert(seed != nil);
    
    RIKeyPair *pair = [RIKeyPair generateKeyPair:keyType seed:seed];
    RIAccountID *accountID = [RIAccountID calcAccountID:pair.publicKey];
    
    assert([accountID.toBase58 isEqualToString: expectedAccount]);
    
    
    NSLog(@"\n %ld secret: %@ generates secret key: %@ and public key: %@", (long)keyType, seedStr, seed.toBase58, accountID.toBase58 );
    
    RIAccountID *destination = [RIAccountID parseBase58:@"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"];
    RIAccountID *gateway1 = [RIAccountID parseBase58:@"rhub8VRN55s94qWKDv6jmDy1pUykJzF3wq"];
    RIAccountID *gateway2 = [RIAccountID parseBase58:@"razqQKzJRdB4UxFPWf5NEpEG3WMkmwgcXA"];
    
    assert(destination != nil && gateway1 != nil && gateway2 != nil);
    
    RISTTxBuilder *builder = [[RISTTxBuilder alloc]init];
    // General transaction fields
    [builder buildAccountID:accountID];
    [builder buildFee:[[RIAmount alloc] initWithAmountValue: 100]];
    [builder buildFlags: RITxFlags.tfFullyCanonicalSig];
    [builder buildSequence: RITxFlags.tfFullyCanonicalSig];
    [builder buildPublicKey: pair.publicKey];
    // Payment-specific fields
    
    RICurrency *currency = [RICurrency toCurrency: @"USD"];
    RIIssue *issue = [[RIIssue alloc]initWithCurrency: currency andAccountID: gateway1];
    RIAmount *amount = [[RIAmount alloc]initWithIssue:issue mantissa:1234 exponent:5];
    
    [builder buildAmount: amount];
    [builder buildDestination: destination];
    
    currency = [RICurrency toCurrency: @"CNY"];
    issue = [[RIIssue alloc]initWithCurrency: currency andAccountID: gateway2];
    amount = [[RIAmount alloc]initWithIssue: issue mantissa: 56789 exponent: 7];
    
    [builder buildSendMax: amount];
    
    RISTTx *sttx = [[RISTTx alloc]initWithBuilder: builder txType: riPAYMENT];
    
    NSLog(@"\nBefore signing: \n %@ \n Serialized: %@", [sttx getJson: 0].toStyledString, [sttx getJson: 0 binary:true].toStyledString);
    
    [sttx sign: pair.publicKey secretKey: pair.secretKey];
    
    NSString *serialized = [self serialize:sttx];
    
    NSLog(@"\nAfter signing: \n%@ \nSerialized: %@", [sttx getJson: 0].toStyledString, serialized);
    
    RISTTx *deserialized = [self deserialize:serialized];
    assert(deserialized);
    assert([deserialized.getTransactionID isEqual: sttx.getTransactionID]);
    
    NSLog(@"Deserialized: %@ \n", [deserialized getJson: 0].toStyledString);
    
    RISTTxSign *check1 = [sttx checkSign:false];
    
    NSLog(@"Check 1: %@ \n", check1.result ? @"Good" : @"Bad");
    
    assert(check1.result);
    
    BOOL check2 = [sttx verify: pair.publicKey mustBeFullyCanonical: true];
    
    return check1.result && check2;
}

- (BOOL)exerciseSingleSign {
    BOOL passes = YES;
    
    passes &=[self demonstrateSigning: secp256k1
                              seedStr:@"alice"
                      expectedAccount:@"rG1QQv2nh2gr7RCZ1P8YYcBUKCCN633jCn"];
    
    passes &= [self demonstrateSigning: ed25519
                               seedStr:@"alice"
                       expectedAccount:@"r9mC1zjD9u5SJXw56pdPhxoDSHaiNcisET"];
    
    passes &= [self demonstrateSigning: secp256k1
                               seedStr:@"snoPBrXtMeMyMHUVTgbuqAfg1SUTb"
                       expectedAccount:@"rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh"];
    
    BOOL failPass = false;
    
    @try {
        [self deserialize:@""];
    }
    @catch (NSException *exception) {
        failPass = true;
        assert([exception.reason isEqualToString:@"Transaction not valid hex"]);
    }
    
    passes &= failPass;
    
    return passes;
}

//------------------------------------------------------------------------------

// Demonstrate multisigning.


- (RISTTx *)buildMultisignTx: (RIAccountID *) accountID seq:(UInt32)seq fee:(UInt64)fee {
    RISTTxBuilder *builder = [[RISTTxBuilder alloc]init];
    [builder buildAccountID:accountID];
    [builder buildFlags:RITxFlags.tfFullyCanonicalSig];
    [builder buildFee: [[RIAmount alloc] initWithAmountValue: fee]];
    [builder buildSequence:seq];
    [builder buildPublicKeyWithEmptySlice];
    
    RISTTx *noopTx = [[RISTTx alloc]initWithBuilder:builder txType:riACCOUNT_SET];

    NSLog(@"\nBefore signing: \n %@", [noopTx getJson:0 binary:false].toStyledString);
    
    return noopTx;
}


// Apply one multi-signature to the supplied transaction.  The signer
// provides their AccountID, PublicKey, and SecretKey.

- (BOOL)multisign:(RISTTx *)tx signer:(DemoCredentials *)signer {
    return [tx multisign:signer.name seed:signer.seed pair:signer.pair accID:signer.accID keyType:signer.keyType];
//    RISerializer *s = [RISerializer buildMultiSigningData:tx accountID:signer.accID];
//    [tx sign:signer.pair.publicKey secretKey:signer.pair.secretKey];
//    RIBuffer *multisig = [signer.pair sign: s.getSlice];
//    
//    RISTObjectBuilder *builder = [[RISTObjectBuilder alloc]init];
//    [builder buildAccountID:signer.accID];
//    [builder buildPublicKey:signer.pair.publicKey];
//    [builder buildMultisig:multisig];
//    [builder buildSField:risfSigner];
//    
//    RISTObject *element = [[RISTObject alloc]initWithBuilder:builder];
//    
//    [tx emplaceBack: element];
//
////    if (![tx isFieldPresent: risfSigners]) {
////        [tx setField:risfSigners toArray:@[]];
////    }
////    
////    NSMutableArray <RISTObject *> *signers = [[tx peekFieldArray: risfSigners] mutableCopy];
////    
////    [signers addObject:element];
////    
////    [signers sortUsingComparator:^NSComparisonResult(RISTObject *  _Nonnull obj1, RISTObject *  _Nonnull obj2) {
////        return [obj1 getAccountID: risfAccount] < [obj2 getAccountID: risfAccount];
////    }];
//    
//    BOOL pass = [tx checkSign:YES].result;
//    
//    assert(pass);
//    
//    NSLog(@"\nMultisigned JSON: \n%@", [tx getJson:0 binary:false].toStyledString);
//
//    NSLog(@"\nMultisigned blob: \n%@", [tx getJson:0 binary:true].toStyledString);
//
//    return pass;
}


- (BOOL)exerciseMultiSign {
    DemoCredentials *alice = [[DemoCredentials alloc]initWithName:@"alice"];
    DemoCredentials *billy = [[DemoCredentials alloc]initWithName:@"billy"];
    DemoCredentials *carol = [[DemoCredentials alloc]initWithName:@"carol"];
    
    RISTTx *tx = [self buildMultisignTx:alice.accID seq:2 fee:100];
    
    // billy and carol sign alice's transaction for her.
    BOOL allPass = [self multisign:tx signer:billy];
    allPass &= [self multisign:tx signer:carol];
    
    return allPass;
}

- (void)runDemo {
    //
    BOOL allPass = [self exerciseSingleSign];
    
    allPass &= [self exerciseMultiSign];

    assert(allPass);
    
    NSLog(@"%@", allPass ?
          @"All checks pass.\n" : @"Some checks fail.\n");

}

@end
