#import "MunbynWrapper.h"
#import <Cordova/CDVPlugin.h>
#import "PrinterSDK.h"
#import <CoreBluetooth/CoreBluetooth.h>
#import <objc/runtime.h>
#import <objc/message.h>

@implementation MunbynWrapper

static NSString *printString = nil;
BOOL connected;

- (void)write:(CDVInvokedUrlCommand*)command
{
    NSArray * arguments = [command arguments];
    NSString *printerName = [arguments objectAtIndex:0];
    printString = [arguments objectAtIndex:1];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handlePrinterConnectedNotification:) name:PrinterConnectedNotification object:nil];
    
    
    CDVPluginResult* pluginResult = nil;
    NSString* message = [command.arguments objectAtIndex:0];
    
    if (printString != nil && printerName != nil) {
        if(!connected)
        {
            double delayInSeconds = 1.0f;
            dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
            dispatch_after(popTime, dispatch_get_main_queue(), ^(void)
                       {        [[PrinterSDK defaultPrinterSDK] scanPrintersWithCompletion:^(Printer *printer)
            {
               if([printer.name isEqualToString:@"BlueTooth Printer"])
               {
                  [[PrinterSDK defaultPrinterSDK] connectBT:printer];
               }
            }];
                       });
        }
        else
        {
            [[PrinterSDK defaultPrinterSDK] printText:printString];
			printString = nil;
        }
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:message];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

-(void)pluginInitialize
{
    [PrinterSDK defaultPrinterSDK];
    connected = false;
}

- (void)handlePrinterConnectedNotification:(NSNotification*)notification
{
    connected = true;
    double delayInSeconds = 1.0f;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void)
                   {
                       [[PrinterSDK defaultPrinterSDK] printText:printString];
					   printString = nil;
                   });
}
@end
