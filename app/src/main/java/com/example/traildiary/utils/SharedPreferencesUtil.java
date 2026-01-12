package com.example.traildiary.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {

    private static final String SP_NAME = "TrailDiaryPrefs";
    private static SharedPreferencesUtil instance;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public SharedPreferencesUtil(Context context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
    }

    public static synchronized SharedPreferencesUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesUtil(context.getApplicationContext());
        }
        return instance;
    }

    // 存储用户头像
    public void saveUserAvatar(String avatarPath) {
        putString(Constants.SP_KEY_AVATAR, avatarPath);
    }

    // 获取用户头像
    public String getUserAvatar() {
        return getString(Constants.SP_KEY_AVATAR, "");
    }

    // 存储字符串
    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // 获取字符串
    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    // 存储整数
    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // 获取整数
    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    // 存储长整数
    public void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    // 获取长整数
    public long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    // 存储布尔值
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    // 获取布尔值
    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    // 存储浮点数
    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    // 获取浮点数
    public float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    // 删除指定键的值
    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    // 清除所有数据
    public void clear() {
        editor.clear();
        editor.apply();
    }

    // 检查是否包含某个键
    public boolean contains(String key) {
        return sp.contains(key);
    }

    // 获取当前登录用户的ID
    public int getCurrentUserId() {
        return getInt(Constants.SP_KEY_USER_ID, -1);
    }

    // 获取当前登录用户的昵称
    public String getCurrentUserNickname() {
        return getString(Constants.SP_KEY_NICKNAME, "");
    }

    // 获取当前登录用户的途迹号
    public String getCurrentUserTrailNumber() {
        return getString(Constants.SP_KEY_TRAIL_NUMBER, "");
    }

    // 检查是否已登录
    public boolean isLoggedIn() {
        return getCurrentUserId() != -1;
    }

    // 退出登录
    public void logout() {
        remove(Constants.SP_KEY_USER_ID);
        remove(Constants.SP_KEY_NICKNAME);
        remove(Constants.SP_KEY_TRAIL_NUMBER);
        remove(Constants.SP_KEY_PASSWORD);
        // 不清除自动登录设置，由用户决定
    }
}