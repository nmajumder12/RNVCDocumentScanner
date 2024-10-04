import { Frame } from 'react-native-vision-camera';
import { NativeModules, Platform } from 'react-native';
import { VisionCameraProxy } from 'react-native-vision-camera';

const LINKING_ERROR =
  `The package 'react-native-vision-camera-document-scanner' doesn't seem to be linked properly. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const RNVCDocumentScanner = NativeModules.RNVCDocumentScanner
  ? NativeModules.RNVCDocumentScanner
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

interface DocumentScannerResult {
  documents: string[]; // Array of file paths to the extracted documents
}

export function scanDocuments(frame: Frame): Promise<DocumentScannerResult> {
  'worklet';
  // @ts-ignore
  return RNVCDocumentScanner.scanDocuments(frame);
}

// Register and initialize the frame processor plugin
VisionCameraProxy.initFrameProcessorPlugin('scanDocuments', scanDocuments);
