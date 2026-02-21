package com.shofyou.app;

import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.List;

public class FileUploadHelper {

    private ValueCallback<Uri[]> fileCallback;
    private final ComponentActivity activity;

    private final ActivityResultLauncher<PickVisualMediaRequest> imagePicker;
    private final ActivityResultLauncher<PickVisualMediaRequest> videoPicker;

    public FileUploadHelper(ComponentActivity activity) {

        this.activity = activity;

        imagePicker = activity.registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                uris -> {
                    if (fileCallback == null) return;

                    if (uris != null && !uris.isEmpty()) {
                        Uri[] results = new Uri[uris.size()];
                        for (int i = 0; i < uris.size(); i++) {
                            results[i] = uris.get(i);
                        }
                        fileCallback.onReceiveValue(results);
                    } else {
                        fileCallback.onReceiveValue(null);
                    }

                    fileCallback = null;
                });

        videoPicker = activity.registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                uris -> {
                    if (fileCallback == null) return;

                    if (uris != null && !uris.isEmpty()) {
                        Uri[] results = new Uri[uris.size()];
                        for (int i = 0; i < uris.size(); i++) {
                            results[i] = uris.get(i);
                        }
                        fileCallback.onReceiveValue(results);
                    } else {
                        fileCallback.onReceiveValue(null);
                    }

                    fileCallback = null;
                });
    }

    public boolean handleFileChooser(ValueCallback<Uri[]> callback,
                                     WebChromeClient.FileChooserParams params) {

        fileCallback = callback;

        boolean isVideo = false;

        String[] types = params.getAcceptTypes();
        if (types != null) {
            for (String t : types) {
                if (t != null && t.toLowerCase().contains("video")) {
                    isVideo = true;
                    break;
                }
            }
        }

        if (isVideo) {
            videoPicker.launch(
                    new PickVisualMediaRequest
                            .Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                            .build()
            );
        } else {
            imagePicker.launch(
                    new PickVisualMediaRequest
                            .Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        }

        return true;
    }
}
