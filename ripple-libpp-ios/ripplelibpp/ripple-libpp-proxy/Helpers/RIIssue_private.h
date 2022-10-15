//
//  RIIssue_private.h
//  ripplelibpp
//
//  Created by Dmitriy Shulzhenko on 7/14/17.
//
//

#ifndef RIIssue_private_h
#define RIIssue_private_h

#import "RIIssue.h"
#import <ripple/protocol/Issue.h>
using namespace ripple;

@interface RIIssue()
    
    @property (nonatomic) Issue *issue;
    
    @end
#endif /* RIIssue_private_h */
