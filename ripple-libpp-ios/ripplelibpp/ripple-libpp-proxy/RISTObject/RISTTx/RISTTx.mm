
#import "RISTObject_private.h"
#import "RISTTx.h"
#import "RISerializer_private.h"
#import "RISTTxBuilder_private.h"
#import "RIBlob.h"
#import "RIJson_private.h"
#import "RIPublicKey_private.h"
#import "RISecretKey_private.h"
#import "RIBaseUInt_private.h"
#import "RISTTxSign.h"

#import <ripple/basics/StringUtilities.h>
#import <ripple/protocol/TxFlags.h>
#import <ripple/protocol/HashPrefix.h>
#import <ripple/protocol/TxFormats.h>


#import "RISeed_private.h"
#import "RIKeyPair.h"
#import "RIAccountID_private.h"
#include <ripple/protocol/AccountID.h>
#include <ripple/protocol/digest.h>
#include <ripple/protocol/HashPrefix.h>
#include <ripple/protocol/JsonFields.h>
#include <ripple/protocol/Sign.h>
#include <ripple/protocol/st.h>
#include <ripple/protocol/TxFlags.h>
#include <ripple/basics/StringUtilities.h>
#include <ripple/json/to_string.h>
#include <ripple-libpp/version.hpp>
#include <boost/version.hpp>
#include <algorithm>
//#import "RIKeyType.h"

@implementation RISTTx

- (instancetype)initWithBuilder:(RISTTxBuilder *)builder txType:(RITxType)txType {
    self = [super initPrivate];
    
    if (self) {

        TxType riTxType;
        
        switch (txType) {
            case riINVALID:
                riTxType = TxType::ttINVALID;
                break;
                
            case riPAYMENT:
                riTxType = TxType::ttPAYMENT;
                break;
                
            case riESCROW_CREATE:
                riTxType = TxType::ttESCROW_CREATE;
                break;
                
            case riESCROW_FINISH:
                riTxType = TxType::ttESCROW_FINISH;
                break;
                
            case riACCOUNT_SET:
                riTxType = TxType::ttACCOUNT_SET;
                break;
                
            case riESCROW_CANCEL:
                riTxType = TxType::ttESCROW_CANCEL;
                break;
                
            case riREGULAR_KEY_SET:
                riTxType = TxType::ttREGULAR_KEY_SET;
                break;
                
            case riNICKNAME_SET:
                riTxType = TxType::ttNICKNAME_SET;
                break;
                
            case riOFFER_CREATE:
                riTxType = TxType::ttOFFER_CREATE;
                break;
                
            case riOFFER_CANCEL:
                riTxType = TxType::ttOFFER_CANCEL;
                break;
                
            case ri_no_longer_used:
                riTxType = TxType::no_longer_used;
                break;
                
            case riTICKET_CREATE:
                riTxType = TxType::ttTICKET_CREATE;
                break;
                
            case riTICKET_CANCEL:
                riTxType = TxType::ttTICKET_CANCEL;
                break;
                
            case riSIGNER_LIST_SET:
                riTxType = TxType::ttSIGNER_LIST_SET;
                break;
                
            case riPAYCHAN_CREATE:
                riTxType = TxType::ttPAYCHAN_CREATE;
                break;
                
            case riPAYCHAN_FUND:
                riTxType = TxType::ttPAYCHAN_FUND;
                break;
                
            case riPAYCHAN_CLAIM:
                riTxType = TxType::ttPAYCHAN_CLAIM;
                break;
                
            case riTRUST_SET:
                riTxType = TxType::ttTRUST_SET;
                break;
                
            case riAMENDMENT:
                riTxType = TxType::ttAMENDMENT;
                break;
                
            case riFEE:
                riTxType = TxType::ttFEE;
                break;

                break;
        }
        
        self.stObject = [builder build: riTxType];
    }
    
    return self;
}

- (RISerializer *)getSerializer {
    Serializer serializer = self.sttx->getSerializer();
    return [[RISerializer alloc] initWithSerializer: &serializer];
}

#pragma mark: RIStrHexConvertible

- (instancetype _Nullable)initWithStrHex:(NSString *_Nonnull)strHex {
    self = [super initPrivate];
    
    if (self) {
        
        //Was taken from test.cpp
        //TODO: Move these dependencies to separete wrappers to provide the ability to use them outside of this class
        std::string blob = strHex.UTF8String;
        auto ret{ strUnHex(blob) };
        
        if (!ret.second || !ret.first.size()) {
            NSException* sttxExs = [NSException exceptionWithName:@"STTx_initWithStrHex_fail"
                                                           reason: @"Transaction not valid hex"
                                                         userInfo: nil];
            [sttxExs raise];
            return nil;
        }
        
        SerialIter sitTrans{ makeSlice(ret.first) };
        
        // Can Throw
        try {
            self.stObject = new STTx(*std::make_shared<STTx>(std::ref(sitTrans)).get());
        } catch (std::runtime_error e) {
            NSException* sttxExs = [NSException exceptionWithName:@"STTx_initWithStrHex_fail"
                                                           reason: [NSString stringWithUTF8String: e.what()]
                                                         userInfo: nil];
            [sttxExs raise];
            return nil;
        }
    }
    
    return self;
}

- (NSString *)strHex {
    return [[[self getSerializer] peekData] strHex];
}

- (RIJson *)getJson:(int)index {
    auto value = self.sttx->getJson(index);
    return [[RIJson alloc]initWithJsonValue: &value];
}

- (RIJson *)getJson:(int)index binary: (BOOL)binary {
    auto value = self.sttx->getJson(index, binary);
    return [[RIJson alloc]initWithJsonValue: &value];
}

- (RIBaseUInt *)getTransactionID {
    auto transId = self.sttx->getTransactionID();
    return [[RIBaseUInt alloc]initWithUInt256:transId];
}

- (void)sign:(RIPublicKey *)publicKey secretKey:(RISecretKey *)secretKey {
    auto pub = publicKey.publicKey;
    auto secret = secretKey.secretKey;
    self.sttx->sign(*pub, *secret);
}

- (RISTTxSign *)checkSign:(BOOL)allowMultiSign {
    RISTTxSign *signResult = [[RISTTxSign alloc]init];
    
    auto rSignResult = self.sttx->checkSign(allowMultiSign);
    
    signResult.result = rSignResult.first;
    signResult.errorString = [NSString stringWithUTF8String: rSignResult.second.data()];
    
    return signResult;
}

- (BOOL)verify:(RIPublicKey *)pubicKey mustBeFullyCanonical:(BOOL)mustBeFullyCanonical {
    STTx rSttx = *self.sttx;
    auto const& signatureSlice = rSttx[sfTxnSignature];
    Blob const data = [&]
    {
        // This is a copy of the static `getSigningData` function body
        // which is needed by `verify`.
        Serializer s;
        s.add32(HashPrefix::txSign);
        self.sttx->addWithoutSigningFields(s);
        return s.getData();
    }();
    
    // STTx::checkSign calls `verify` indirectly via `checkSingleSign`
    return verify(*(pubicKey.publicKey),
                  makeSlice(data),
                  signatureSlice,
                  mustBeFullyCanonical);
}

- (STTx *)sttx {
    return static_cast<STTx*>(self.stObject);
}


- (BOOL)multisign:(NSString *)name seed:(RISeed *)seed pair:(RIKeyPair *)pair accID:(RIAccountID *)accID keyType:(RIKeyType)keyType {

    // Get the TxnSignature.
    Serializer s = buildMultiSigningData (*self.stObject, *(accID.accountID));
    
    auto const multisig = sign (*pair.publicKey.publicKey, *pair.secretKey.secretKey, s.slice());
    
    // Make the signer object that we'll inject into the array.
    STObject element (sfSigner);
    element[sfAccount] = *(accID.accountID);
    element[sfSigningPubKey] = *pair.publicKey.publicKey;
    
    element[sfTxnSignature] = multisig;
    
    std::cout << element;
    
    // If a Signers array does not yet exist make one.
    if (! self.stObject->isFieldPresent (sfSigners))
        self.stObject->setFieldArray (sfSigners, {});
    
    // Insert the signer into the array.
    STArray& signers {self.stObject->peekFieldArray (sfSigners)};
    signers.emplace_back (std::move (element));
    
    // Sort the Signers array by Account.  If it is not sorted when submitted
    // to the network then it will be rejected.
    std::sort (signers.begin(), signers.end(),
               [](STObject const& a, STObject const& b)
               {
                   auto acc1 = a[sfAccount];
                   auto acc2 = a.getAccountID(sfAccount);
                   
                   assert(acc1 == acc2);
                   
                   return a.getAccountID(sfAccount) < b.getAccountID(sfAccount);// (a[sfAccount] < b[sfAccount]);
               });
    
    // Verify that the signature is valid.
    bool const pass = self.sttx->checkSign(true).first;
    assert (pass);
    
    // To submit multisigned JSON to the network use this RPC command:
    // $ rippled submit_multisigned '<all JSON>'
    std::cout << "\nMultisigned JSON: \n"
    << self.sttx->getJson(0, false).toStyledString()  << std::endl;
    
    // Alternatively, to submit the multisigned blob to the network:
    //  1. Extract the hex string (including the quotes) following "tx"
    //  2. Then use this RPC command:
    //     $ rippled submit <quoted hex string>
    std::cout << "Multisigned blob:"
    << self.sttx->getJson(0, true) << std::endl;
    
    return pass;
}
@end
