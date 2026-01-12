package com.example.traildiary.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;

public class FileProviderUtil {

    /**
     * 获取文件的Uri
     */
    public static Uri getFileUri(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );
        } else {
            return Uri.fromFile(file);
        }
    }

    /**
     * 创建应用专属的图片文件
     */
    public static File createAppImageFile(Context context) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        return new File(storageDir, imageFileName);
    }

    /**
     * 获取应用图片目录
     */
    public static File getAppImageDirectory(Context context) {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        return storageDir;
    }

    /**
     * 清理临时文件
     */
    public static void clearTempFiles(Context context) {
        File storageDir = getAppImageDirectory(context);
        if (storageDir != null && storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                long currentTime = System.currentTimeMillis();
                long oneDayMillis = 24 * 60 * 60 * 1000;

                for (File file : files) {
                    // 删除一天前的临时文件
                    if (currentTime - file.lastModified() > oneDayMillis) {
                        file.delete();
                    }
                }
            }
        }
    }
}