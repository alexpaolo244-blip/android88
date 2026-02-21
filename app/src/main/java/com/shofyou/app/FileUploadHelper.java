package com.shofyou.app;

import android.app.Activity;
import android.content.Intent;
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
    private final ActivityResultLauncher<Intent> videoPicker;

    public FileUploadHelper(ComponentActivity activity) {

        this.activity = activity;

        // ✅ الصور (لا نلمسها)
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

        // 🔥 الفيديو باستخدام intent الأصلي مثل Chrome
        videoPicker = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if (fileCallback == null) return;

                    Uri[] results = null;

                    if (result.getResultCode() == Activity.RESULT_OK &&
                            result.getData() != null) {

                        if (result.getData().getClipData() != null) {

                            int count = result.getData().getClipData().getItemCount();
                            results = new Uri[count];

                            for (int i = 0; i < count; i++) {
                                results[i] =
                                        result.getData().getClipData().getItemAt(i).getUri();
                            }

                        } else if (result.getData().getData() != null) {

                            results = new Uri[]{result.getData().getData()};
                        }
                    }

                    fileCallback.onReceiveValue(results);
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

            // 🔥 نستخدم intent الافتراضي مثل Chrome
            Intent intent = params.createIntent();
            videoPicker.launch(intent);

        } else {

            // ✅ الصور كما هي
            imagePicker.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        }

        return true;
    }
}
