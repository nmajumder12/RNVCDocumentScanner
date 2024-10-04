package com.yourcompany.RNVCDocumentScanner;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.document.DocumentScanner;
import com.google.mlkit.vision.document.DocumentScannerOptions;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class DocumentScannerPlugin extends ReactContextBaseJavaModule {

    public DocumentScannerPlugin(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNVCDocumentScanner";
    }

    private void cleanUpOldImages() {
        File cacheDir = getReactApplicationContext().getCacheDir();
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".jpg")) {
                    file.delete(); // Delete old image file
                }
            }
        }
    }

    private String saveImage(Bitmap bitmap) {
        File cacheDir = getReactApplicationContext().getCacheDir();
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File file = new File(cacheDir, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @ReactMethod
    public Object callback(@NonNull Frame frame, @NonNull Object[] params) {
        Image mediaImage = frame.getImage();
        if (mediaImage == null) {
            return null;
        }

        // Clean up old images before processing the new frame
        cleanUpOldImages();

        InputImage image = InputImage.fromMediaImage(mediaImage, frame.getRotation());
        DocumentScannerOptions options = new DocumentScannerOptions.Builder().build();
        DocumentScanner scanner = DocumentScanner.getClient(options);

        WritableNativeArray documentsArray = new WritableNativeArray();
        CountDownLatch latch = new CountDownLatch(1);

        scanner.process(image)
            .addOnSuccessListener(documents -> {
                for (InputImage docImage : documents) {
                    Bitmap bitmap = docImage.getBitmapInternal();
                    String filePath = saveImage(bitmap);
                    if (filePath != null) {
                        documentsArray.pushString(filePath);
                    }
                }
                latch.countDown();
            })
            .addOnFailureListener(e -> {
                Log.e("DocumentScannerPlugin", "Document scan failed: " + e.getMessage());
                latch.countDown();
            });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WritableNativeMap result = new WritableNativeMap();
        result.putArray("documents", documentsArray);

        return result;
    }
}
