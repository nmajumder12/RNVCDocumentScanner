#import <Vision/Vision.h>
#import <React/RCTBridgeModule.h>
#import <UIKit/UIKit.h>

@interface RNVCDocumentScannerPlugin : NSObject <RCTBridgeModule>

+ (id)scanDocumentsWithFrame:(Frame *)frame;

@end
