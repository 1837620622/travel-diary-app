package com.example.traildiary.utils;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MediaAccessUtil {

    /**
     * 获取安全的图片路径（自动处理权限问题）
     */
    public static String getSafeImagePath(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+：复制到应用专属目录
            File copiedFile = copyToAppDir(context, uri, "images");
            if (copiedFile != null) {
                return copiedFile.getAbsolutePath();
            }
        } else {
            // Android 10以下：直接获取路径（需要权限）
            String path = getRealPathFromUri(context, uri);
            if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                // 检查是否有权限
                if (hasStoragePermission(context)) {
                    return path;
                } else {
                    // 如果没有权限，也复制到应用专属目录
                    File copiedFile = copyToAppDir(context, uri, "images");
                    if (copiedFile != null) {
                        return copiedFile.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 将文件复制到应用专属目录
     */
    private static File copyToAppDir(Context context, Uri uri, String subDir) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            // 创建应用专属目录
            File storageDir;
            if (subDir != null) {
                storageDir = new File(context.getExternalFilesDir(null), subDir);
            } else {
                storageDir = context.getExternalFilesDir(null);
            }

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            // 生成文件名
            String fileName = "image_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(storageDir, fileName);

            outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return outputFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是否有存储权限
     */
    private static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true; // Android 10+ 不需要WRITE_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 获取Uri的真实路径（兼容各版本）
     */
    private static String getRealPathFromUri(Context context, Uri uri) {
        return ImageUtil.getRealPathFromUri(context, uri);
    }
}