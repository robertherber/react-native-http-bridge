#import "RCTHttpServer.h"
#import "React/RCTBridge.h"
#import "React/RCTLog.h"
#import "React/RCTEventDispatcher.h"

#import "WGCDWebServer.h"
#import "WGCDWebServerDataResponse.h"
#import "WGCDWebServerDataRequest.h"

@interface RCTHttpServer : NSObject <RCTBridgeModule> {
    WGCDWebServer* _webServer;
    NSMutableDictionary* _requestResponses;
}
@end

static RCTBridge *bridge;

@implementation RCTHttpServer

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

int lengthOfRandomString = 16;
NSString *letters = @"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

RCT_EXPORT_METHOD(start:(NSInteger) port
                  serviceName:(NSString *) serviceName)
{
    RCTLogInfo(@"Running HTTP bridge server: %d", port);
    
    _requestResponses = [[NSMutableDictionary alloc] init];

    dispatch_sync(dispatch_get_main_queue(), ^{
        _webServer = [[WGCDWebServer alloc] init];

        [_webServer addDefaultHandlerForMethod:@"GET"
                    requestClass:[WGCDWebServerDataRequest class]
                    processBlock:^WGCDWebServerResponse *(WGCDWebServerRequest* request) {

            WGCDWebServerDataRequest* dataRequest = (WGCDWebServerDataRequest*)request;
            WGCDWebServerDataResponse* _requestResponse;
                        
            NSMutableString *requestId = [NSMutableString stringWithCapacity: lengthOfRandomString];
            
            for (int i=0; i<lengthOfRandomString; i++) {
                [requestId appendFormat: @"%C", [letters characterAtIndex: arc4random_uniform([letters length])]];
            }
                    
                    
                        
            [self.bridge.eventDispatcher sendAppEventWithName:@"httpServerResponseReceived"
                                                         body:@{
                                                             @"requestId": requestId,
                                                                @"method": @"GET",
                                                                @"headers": dataRequest.headers,
                                                                @"query":
                                                                    dataRequest.query,
                                                                     @"url": dataRequest.URL.relativeString}];
            
            while (_requestResponse == NULL) {
                _requestResponse = [_requestResponses objectForKey:requestId];
                [NSThread sleepForTimeInterval:0.02f];
            }
                        
            [_requestResponses removeObjectForKey:requestId ];

            return _requestResponse;
        }];

        [_webServer addDefaultHandlerForMethod:@"POST"
                    requestClass:[WGCDWebServerDataRequest class]
                    processBlock:^WGCDWebServerResponse *(WGCDWebServerRequest* request) {

            WGCDWebServerDataRequest* dataRequest = (WGCDWebServerDataRequest*)request;
            WGCDWebServerDataResponse* _requestResponse;
                        
            NSMutableString *requestId = [NSMutableString stringWithCapacity: lengthOfRandomString];
            
            for (int i=0; i<lengthOfRandomString; i++) {
                [requestId appendFormat: @"%C", [letters characterAtIndex: arc4random_uniform([letters length])]];
            }
                    
                    
                        
            [self.bridge.eventDispatcher sendAppEventWithName:@"httpServerResponseReceived"
                                                         body:@{@"postData": dataRequest.jsonObject,
                                                                @"requestId": requestId,
                                                                @"method": @"POST",
                                                                @"headers": dataRequest.headers,
                                                                @"query":
                                                                    dataRequest.query,
                                                                     @"url": dataRequest.URL.relativeString}];
            
            while (_requestResponse == NULL) {
                _requestResponse = [_requestResponses objectForKey:requestId];
                [NSThread sleepForTimeInterval:0.02f];
            }
                        
            [_requestResponses removeObjectForKey:requestId ];

            return _requestResponse;
        }];

        [_webServer startWithPort:port bonjourName:serviceName];
    });
}

RCT_EXPORT_METHOD(stop)
{
    RCTLogInfo(@"Stopping HTTP bridge server");

    //dispatch_sync(dispatch_get_main_queue(), ^{
        if (_webServer != nil) {
            [_webServer stop];
            [_webServer removeAllHandlers];
            _webServer = nil;
        }
    //});
}

RCT_EXPORT_METHOD(respond:(NSInteger) code
                  type: (NSString *) type
                  body: (NSString *) body
                  requestId: (NSString *) requestId)
{
    NSData* data = [body dataUsingEncoding:NSUTF8StringEncoding];
    [_requestResponses setObject:[[WGCDWebServerDataResponse alloc] initWithData:data contentType:type] forKey: requestId];
}

@end
