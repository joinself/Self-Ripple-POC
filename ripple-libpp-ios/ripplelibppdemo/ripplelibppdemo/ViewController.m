//
//  ViewController.m
//  ripplelibppdemo
//
//  Created by Alexey Pelykh on 7/11/17.
//
//

#import "ViewController.h"
#import <ripplelibpp/ripplelibpp.h>
#import "RippleLibDemo.h"

@interface ViewController ()

@end

@implementation ViewController

- (IBAction)runWrapped:(id)sender {
    [[[RippleLibDemo alloc]init] runDemo];
}

- (IBAction)runRipple:(id)sender {
    [[[RIDemoTest alloc]init] runDemo];
}

@end
