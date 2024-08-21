#import <Cordova/CDVPlugin.h>
#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>

@interface MunbynWrapper : CDVPlugin

- (void)write:(CDVInvokedUrlCommand*)command;

@end
