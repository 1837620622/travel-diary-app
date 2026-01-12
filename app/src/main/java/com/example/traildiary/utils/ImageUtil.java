package com.example.traildiary.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtil {

    private static final int MAX_IMAGE_WIDTH = 1080;
    private static final int MAX_IMAGE_HEIGHT = 1920;
    private static final int COMPRESS_QUALITY = 80;

    /**
     * 从相册选择图片
     */
    public static void pickImageFromGallery(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 拍照
     */
    public static File takePhoto(Activity activity, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = createImageFile(activity);
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(activity,
                        activity.getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                activity.startActivityForResult(takePictureIntent, requestCode);
                return photoFile;
            }
        }
        return null;
    }

    /**
     * 创建图片文件
     */
    public static File createImageFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        File storageDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "TrailDiary");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
        }

        try {
            return File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取图片真实路径
     */
    public static String getRealPathFromUri(Context context, Uri uri) {
        String filePath = "";
        if (uri == null) {
            return filePath;
        }

        // Android 10+ 使用新的API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getFilePathForQ(context, uri);
        }

        // 如果是 file:// 开头的URI
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
            return filePath;
        }

        // 如果是 content:// 开头的URI
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 尝试从MediaStore获取
            Cursor cursor = null;
            try {
                String[] projection = {MediaStore.Images.Media.DATA};
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            // 如果以上方法获取不到，尝试通过Document获取
            if (TextUtils.isEmpty(filePath)) {
                filePath = getFilePathForDocuments(context, uri);
            }
        }

        return filePath;
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private static String getFilePathForQ(Context context, Uri uri) {
        File file = null;
        // 尝试从应用专属目录访问
        file = copyUriToAppDir(context, uri);

        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        }

        // 如果无法访问，返回原始路径（供用户手动授权）
        return getFilePathForDocuments(context, uri);
    }
    /**
     * 通过Document API获取文件路径
     */
    private static String getFilePathForDocuments(Context context, Uri uri) {
        if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            String selection = "_id=?";
            String[] selectionArgs = new String[]{split[1]};

            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
        return null;
    }

    /**
     * 复制Uri到应用专属目录
     */
    private static File copyUriToAppDir(Context context, Uri uri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            // 创建应用专属目录的文件
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                storageDir = new File(context.getFilesDir(), "Pictures");
            }

            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(storageDir, fileName);

            outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return outputFile;

        } catch (IOException e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取数据列
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Android N 以上获取文件路径
     */
    private static String getFilePathForN(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String filePath = cursor.getString(index);
            cursor.close();
            return filePath;
        }
        return null;
    }

    /**
     * 压缩图片到指定大小
     */
    public static File compressImage(Context context, String imagePath, int maxSizeKB) {
        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }

        try {
            // 获取原始图片的旋转角度
            int degree = getImageDegree(imagePath);

            // 读取原始图片
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            // 计算采样率
            int inSampleSize = calculateInSampleSize(options, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);

            // 根据采样率读取图片
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            if (bitmap == null) {
                return null;
            }

            // 旋转图片
            if (degree != 0) {
                bitmap = rotateBitmap(bitmap, degree);
            }

            // 压缩图片
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, baos);

            // 如果图片仍然太大，进一步压缩
            int optionsQuality = COMPRESS_QUALITY;
            while (baos.toByteArray().length / 1024 > maxSizeKB && optionsQuality > 10) {
                baos.reset();
                optionsQuality -= 10;
                bitmap.compress(Bitmap.CompressFormat.JPEG, optionsQuality, baos);
            }

            // 保存压缩后的图片
            File compressedFile = createImageFile(context);
            if (compressedFile != null) {
                FileOutputStream fos = new FileOutputStream(compressedFile);
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();

                // 回收Bitmap
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }

                return compressedFile;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 计算图片的采样率
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 获取图片旋转角度
     */
    private static int getImageDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        if (degree != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            if (rotatedBitmap != bitmap) {
                bitmap.recycle();
            }

            return rotatedBitmap;
        }
        return bitmap;
    }

    /**
     * 获取图片的Uri
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取文件大小
     */
    public static long getFileSize(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            long size = cursor.getLong(sizeIndex);
            cursor.close();
            return size;
        }
        return 0;
    }

    /**
     * 获取文件名
     */
    @SuppressLint("Range")
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * 复制文件到应用目录
     */
    public static File copyFileToAppDir(Context context, Uri sourceUri, String fileName) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) {
                return null;
            }

            File storageDir;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            } else {
                storageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "TrailDiary");
                if (!storageDir.exists()) {
                    storageDir.mkdirs();
                }
            }

            File outputFile = new File(storageDir, fileName);
            outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return outputFile;

        } catch (IOException e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检查是否是支持的图片格式
     */
    public static boolean isSupportedImageFormat(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }

        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg")
                || extension.equals("png") || extension.equals("gif")
                || extension.equals("bmp") || extension.equals("webp");
    }

    /**
     * 压缩Bitmap到字节数组
     * @param bitmap 原始Bitmap
     * @return 压缩后的字节数组
     */
    public static byte[] compressImage(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 压缩图片，质量80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, baos);
        return baos.toByteArray();
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot != -1 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 清除图片缓存
     */
    public static void clearImageCache(Context context) {
        File cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (cacheDir != null && cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        file.delete();
                    }
                }
            }
        }
    }
}