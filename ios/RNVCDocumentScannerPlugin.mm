#import "RNVCDocumentScannerPlugin.h"
#import <Vision/Vision.h>
#import <React/RCTBridgeModule.h>
#import <UIKit/UIKit.h>

@implementation RNVCDocumentScannerPlugin

RCT_EXPORT_MODULE()

+ (id)scanDocumentsWithFrame:(Frame *)frame {
  CIImage *ciImage = [frame ciImage];
  if (!ciImage) {
    return nil;
  }

  // Clean up old images before processing the new frame
  NSString *tempDir = NSTemporaryDirectory();
  NSFileManager *fileManager = [NSFileManager defaultManager];
  NSArray *files = [fileManager contentsOfDirectoryAtPath:tempDir error:nil];
  
  for (NSString *file in files) {
    if ([file hasSuffix:@".jpg"]) {
      NSString *filePath = [tempDir stringByAppendingPathComponent:file];
      [fileManager removeItemAtPath:filePath error:nil]; // Delete old image
    }
  }

  VNDetectDocumentSegmentationRequest *request = [[VNDetectDocumentSegmentationRequest alloc] init];
  VNImageRequestHandler *handler = [[VNImageRequestHandler alloc] initWithCIImage:ciImage options:@{}];

  NSError *error = nil;
  [handler performRequests:@[request] error:&error];
  
  if (error) {
    return nil;
  }

  NSMutableArray *documents = [NSMutableArray array];

  for (VNDocumentSegmentationObservation *observation in request.results) {
    CGRect boundingBox = [observation boundingBox];
    CGRect imageRect = CGRectMake(boundingBox.origin.x * ciImage.extent.size.width,
                                  boundingBox.origin.y * ciImage.extent.size.height,
                                  boundingBox.size.width * ciImage.extent.size.width,
                                  boundingBox.size.height * ciImage.extent.size.height);

    CIImage *documentImage = [ciImage imageByCroppingToRect:imageRect];
    UIImage *uiImage = [UIImage imageWithCIImage:documentImage];

    NSData *imageData = UIImageJPEGRepresentation(uiImage, 1.0);
    NSString *fileName = [NSString stringWithFormat:@"%@.jpg", [[NSUUID UUID] UUIDString]];
    NSString *filePath = [tempDir stringByAppendingPathComponent:fileName];
    [imageData writeToFile:filePath atomically:YES];

    [documents addObject:filePath];
  }

  return @{@"documents": documents};
}

@end
