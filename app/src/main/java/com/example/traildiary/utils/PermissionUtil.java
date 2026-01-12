package com.example.traildiary.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    // 单例实例
    private static PermissionUtil instance;
    // 关联的Activity
    private Activity activity;
    // 权限回调接口
    private PermissionCallback callback;
    // 权限请求码
    private int requestCode;

    /**
     * 获取单例实例（确保全局唯一）
     * @param activity 关联的Activity
     * @return PermissionUtil单例
     */
    public static PermissionUtil getInstance(Activity activity) {
        if (instance == null) {
            instance = new PermissionUtil(activity);
        }
        instance.setActivity(activity);
        return instance;
    }

    /**
     * 私有构造方法（禁止外部直接实例化，确保单例特性）
     * @param activity 关联的Activity
     */
    private PermissionUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * 更新关联的Activity
     * @param activity 新的关联Activity
     */
    private void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * 权限回调接口（精细化区分权限状态）
     */
    public interface PermissionCallback {
        // 所有权限都已授予
        void onAllGranted();
        // 权限被普通拒绝（可再次请求）
        void onDenied(List<String> deniedPermissions);
        // 权限被永久拒绝（需引导用户到设置页面开启）
        void onPermanentlyDenied(List<String> permanentlyDeniedPermissions);
    }

    /**
     * 检查单个权限是否已授予
     * @param permission 要检查的权限（如Manifest.permission.CAMERA）
     * @return true=已授予，false=未授予
     */
    public boolean isPermissionGranted(String permission) {
        // Android 6.0（M）以下默认权限已授予
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * 检查多个权限是否全部已授予
     * @param permissions 权限数组
     * @return true=全部授予，false=至少一个未授予
     */
    public boolean arePermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 请求多个权限
     * @param permissions 要请求的权限数组
     * @param requestCode 权限请求码（用于区分不同的权限请求）
     * @param callback 权限回调
     */
    public void requestPermissions(String[] permissions, int requestCode, PermissionCallback callback) {
        this.callback = callback;
        this.requestCode = requestCode;

        List<String> permissionList = new ArrayList<>();
        // 筛选出未授予的权限
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                permissionList.add(permission);
            }
        }

        if (permissionList.isEmpty()) {
            // 所有权限都已授予，直接回调成功
            if (callback != null) {
                callback.onAllGranted();
            }
        } else {
            // 只请求未授予的权限
            ActivityCompat.requestPermissions(activity,
                    permissionList.toArray(new String[0]), requestCode);
        }
    }

    /**
     * 处理权限请求结果（需在Activity的onRequestPermissionsResult中调用）
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults 权限授予结果数组
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 匹配请求码且回调不为空时才处理
        if (this.requestCode != requestCode || callback == null) {
            return;
        }

        List<String> deniedPermissions = new ArrayList<>();
        List<String> permanentlyDeniedPermissions = new ArrayList<>();

        // 遍历权限结果，区分普通拒绝和永久拒绝
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
                // 判断是否永久拒绝：用户勾选了"不再询问"
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    permanentlyDeniedPermissions.add(permissions[i]);
                }
            }
        }

        // 根据权限状态分发回调
        if (deniedPermissions.isEmpty()) {
            // 所有权限都授予
            callback.onAllGranted();
        } else {
            if (!permanentlyDeniedPermissions.isEmpty()) {
                // 存在永久拒绝的权限
                callback.onPermanentlyDenied(permanentlyDeniedPermissions);
            } else {
                // 仅普通拒绝（可再次请求）
                callback.onDenied(deniedPermissions);
            }
        }
    }

    /**
     * 显示权限说明对话框（引导用户授予权限）
     * @param title 对话框标题
     * @param message 权限说明信息
     * @param permissions 要请求的权限数组
     */
    public void showPermissionRationale(String title, String message, final String[] permissions) {
        new android.app.AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("去授权", (dialog, which) -> {
                    // 关闭对话框后再次请求权限
                    requestPermissions(permissions, requestCode, callback);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 跳转到当前应用的设置页面（用于权限永久拒绝时引导用户手动开启）
     */
    public void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    /**
     * 快捷请求相机权限（含存储权限，兼容旧版本）
     * @param callback 权限回调
     */
    public void requestCameraPermission(PermissionCallback callback) {
        requestPermissions(new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, Constants.PERMISSION_REQUEST_CAMERA, callback);
    }

    /**
     * 快捷请求存储权限（根据Android版本自动适配权限列表）
     * @param callback 权限回调
     */
    public void requestStoragePermission(PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 无需WRITE_EXTERNAL_STORAGE权限
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, Constants.PERMISSION_REQUEST_STORAGE, callback);
        } else {
            // Android 10以下需要读写双重权限
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, Constants.PERMISSION_REQUEST_STORAGE, callback);
        }
    }

    /**
     * 快捷请求定位权限（精确定位+粗略定位）
     * @param callback 权限回调
     */
    public void requestLocationPermission(PermissionCallback callback) {
        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, Constants.PERMISSION_REQUEST_LOCATION, callback);
    }

    /**
     * 快捷请求所有必要权限（定位+相机+存储+通知（Android 13+））
     * @param callback 权限回调
     */
    public void requestAllPermissions(PermissionCallback callback) {
        List<String> permissions = new ArrayList<>();
        // 定位权限
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        // 相机权限
        permissions.add(Manifest.permission.CAMERA);
        // 存储读取权限
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        // Android 10以下添加存储写入权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        // Android 13+ 添加通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // 统一请求所有权限
        requestPermissions(permissions.toArray(new String[0]), 1000, callback);
    }
}