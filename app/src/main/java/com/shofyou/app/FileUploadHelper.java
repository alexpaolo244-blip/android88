package com.shofyou.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class FileUploadHelper {

    private ValueCallback<Uri[]> fileCallback;
    private final Activity activity;
    private ActivityResultLauncher<Intent> launcher;

    public FileUploadHelper(Activity activity) {
        this.activity = activity;

        launcher = ((androidx.activity.ComponentActivity) activity)
                .registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (fileCallback == null) return;

                            Uri[] results = null;

                            if (result.getResultCode() == Activity.RESULT_OK &&
                                    result.getData() != null &&
                                    result.getData().getData() != null) {

                                results = new Uri[]{result.getData().getData()};
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

        Intent intent;

        if (isVideo) {
            // فيديو فقط
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
        } else {
            // 🔥 صور فقط (بدون مدير ملفات)
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
        }

        launcher.launch(intent);
        return true;
    }
}
