package com.example.traildiary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.traildiary.model.User;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private DatabaseHelper dbHelper;
    // 直接使用 ThreeTenABP 的 DateTimeFormatter，无需系统版本判断
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 添加用户（注册） - 重载版本，支持直接传入字节数组
    public long addUser(User user) {
        return addUser(user, null);
    }

    // 添加用户（注册）- 支持头像字节数组
    public long addUser(User user, byte[] avatarBytes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_NICKNAME, user.getNickname());
        values.put(DatabaseHelper.COLUMN_TRAIL_NUMBER, user.getTrailNumber());
        values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COLUMN_PHONE, user.getPhone());
        values.put(DatabaseHelper.COLUMN_SIGNATURE,
                user.getSignature() != null ? user.getSignature() : "山川为印，时光为笔。");
        values.put(DatabaseHelper.COLUMN_GENDER, user.getGender());

        // 处理头像：如果有字节数组，转换为十六进制字符串存储
        if (avatarBytes != null && avatarBytes.length > 0) {
            String avatarHex = bytesToHex(avatarBytes);
            values.put(DatabaseHelper.COLUMN_AVATAR, avatarHex);
        } else if (user.getAvatar() != null) {
            values.put(DatabaseHelper.COLUMN_AVATAR, user.getAvatar());
        } else {
            values.put(DatabaseHelper.COLUMN_AVATAR, "");
        }

        // 处理生日
        if (user.getBirthday() != null) {
            values.put(DatabaseHelper.COLUMN_BIRTHDAY, user.getBirthday().format(dateFormatter));
        }

        // 处理创建时间
        if (user.getCreateTime() != null) {
            values.put(DatabaseHelper.COLUMN_CREATE_TIME, user.getCreateTime().format(timeFormatter));
        } else {
            values.put(DatabaseHelper.COLUMN_CREATE_TIME, LocalDateTime.now().format(timeFormatter));
        }

        long result = db.insert(DatabaseHelper.TABLE_USER, null, values);
        db.close();
        return result;
    }

    // 更新用户信息 - 支持头像字节数组
    public int updateUserWithAvatar(User user, byte[] avatarBytes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_NICKNAME, user.getNickname());
        values.put(DatabaseHelper.COLUMN_SIGNATURE, user.getSignature());
        values.put(DatabaseHelper.COLUMN_GENDER, user.getGender());

        // 处理头像：如果有字节数组，转换为十六进制字符串存储
        if (avatarBytes != null && avatarBytes.length > 0) {
            String avatarHex = bytesToHex(avatarBytes);
            values.put(DatabaseHelper.COLUMN_AVATAR, avatarHex);
        } else if (user.getAvatar() != null) {
            values.put(DatabaseHelper.COLUMN_AVATAR, user.getAvatar());
        }

        // 处理生日
        if (user.getBirthday() != null) {
            values.put(DatabaseHelper.COLUMN_BIRTHDAY, user.getBirthday().format(dateFormatter));
        }

        int result = db.update(DatabaseHelper.TABLE_USER, values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getUserId())});
        db.close();
        return result;
    }

    // 更新用户信息（保持原有方法兼容性）
    public int updateUser(User user) {
        return updateUserWithAvatar(user, null);
    }

    // 更新用户头像（字节数组版本）
    public int updateUserAvatar(int userId, byte[] avatarBytes) {
        if (avatarBytes == null || avatarBytes.length == 0) {
            return 0;
        }

        String avatarHex = bytesToHex(avatarBytes);
        return updateUserAvatar(userId, avatarHex);
    }

    // 更新用户头像（字符串版本）
    public int updateUserAvatar(int userId, String avatarPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AVATAR, avatarPath);

        int result = db.update(DatabaseHelper.TABLE_USER, values,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
        return result;
    }

    // 获取用户头像字节数组
    public byte[] getUserAvatarBytes(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        byte[] avatarBytes = null;

        String[] columns = {DatabaseHelper.COLUMN_AVATAR};

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, columns,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String avatarHex = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVATAR));
            if (avatarHex != null && !avatarHex.isEmpty()) {
                avatarBytes = hexToBytes(avatarHex);
            }
            cursor.close();
        }
        db.close();
        return avatarBytes;
    }

    // 仅通过手机号重置密码
    public int resetPassword(String phone, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASSWORD, newPassword);

        // 条件：仅匹配手机号
        String selection = DatabaseHelper.COLUMN_PHONE + " = ?";
        String[] selectionArgs = {phone};

        int result = db.update(DatabaseHelper.TABLE_USER, values, selection, selectionArgs);
        db.close();
        return result;
    }

    // 根据ID获取用户
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String[] columns = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_NICKNAME,
                DatabaseHelper.COLUMN_TRAIL_NUMBER,
                DatabaseHelper.COLUMN_PASSWORD,
                DatabaseHelper.COLUMN_PHONE,
                DatabaseHelper.COLUMN_SIGNATURE,
                DatabaseHelper.COLUMN_GENDER,
                DatabaseHelper.COLUMN_BIRTHDAY,
                DatabaseHelper.COLUMN_AVATAR,
                DatabaseHelper.COLUMN_CREATE_TIME
        };

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, columns,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    // 根据手机号获取用户
    public User getUserByPhone(String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String[] columns = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_NICKNAME,
                DatabaseHelper.COLUMN_TRAIL_NUMBER,
                DatabaseHelper.COLUMN_PASSWORD,
                DatabaseHelper.COLUMN_PHONE,
                DatabaseHelper.COLUMN_SIGNATURE,
                DatabaseHelper.COLUMN_GENDER,
                DatabaseHelper.COLUMN_BIRTHDAY,
                DatabaseHelper.COLUMN_AVATAR,
                DatabaseHelper.COLUMN_CREATE_TIME
        };

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, columns,
                DatabaseHelper.COLUMN_PHONE + " = ?",
                new String[]{phone}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    // 登录验证
    public User login(String identifier, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;

        String[] columns = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_NICKNAME,
                DatabaseHelper.COLUMN_TRAIL_NUMBER,
                DatabaseHelper.COLUMN_PASSWORD,
                DatabaseHelper.COLUMN_PHONE,
                DatabaseHelper.COLUMN_SIGNATURE,
                DatabaseHelper.COLUMN_GENDER,
                DatabaseHelper.COLUMN_BIRTHDAY,
                DatabaseHelper.COLUMN_AVATAR,
                DatabaseHelper.COLUMN_CREATE_TIME
        };

        // 支持途迹号或昵称登录
        String selection = "(" + DatabaseHelper.COLUMN_TRAIL_NUMBER + " = ? OR " +
                DatabaseHelper.COLUMN_NICKNAME + " = ?) AND " +
                DatabaseHelper.COLUMN_PASSWORD + " = ?";

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, columns, selection,
                new String[]{identifier, identifier, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        db.close();
        return user;
    }

    // 检查途迹号是否已存在
    public boolean isTrailNumberExists(String trailNumber) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_TRAIL_NUMBER + " = ?";

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                selection, new String[]{trailNumber}, null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    // 检查昵称是否已存在
    public boolean isNicknameExists(String nickname) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_NICKNAME + " = ?";

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                selection, new String[]{nickname}, null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    // 检查手机号是否已存在
    public boolean isPhoneExists(String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.COLUMN_PHONE + " = ?";

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
                selection, new String[]{phone}, null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    // 获取所有用户（可用于搜索功能）
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_NICKNAME,
                DatabaseHelper.COLUMN_TRAIL_NUMBER,
                DatabaseHelper.COLUMN_AVATAR,
                DatabaseHelper.COLUMN_SIGNATURE
        };

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, columns,
                null, null, null, null,
                DatabaseHelper.COLUMN_CREATE_TIME + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
                user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NICKNAME)));
                user.setTrailNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRAIL_NUMBER)));
                user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVATAR)));
                user.setSignature(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNATURE)));
                userList.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return userList;
    }

    // 根据关键词搜索用户（用于搜索功能）
    public List<User> searchUsers(String keyword) {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_NICKNAME,
                DatabaseHelper.COLUMN_TRAIL_NUMBER,
                DatabaseHelper.COLUMN_AVATAR,
                DatabaseHelper.COLUMN_SIGNATURE
        };

        String selection = DatabaseHelper.COLUMN_NICKNAME + " LIKE ? OR " +
                DatabaseHelper.COLUMN_TRAIL_NUMBER + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + keyword + "%", "%" + keyword + "%"};

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER, columns,
                selection, selectionArgs, null, null,
                DatabaseHelper.COLUMN_NICKNAME);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
                user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NICKNAME)));
                user.setTrailNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRAIL_NUMBER)));
                user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVATAR)));
                user.setSignature(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNATURE)));
                userList.add(user);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return userList;
    }

    // 将Cursor转换为User对象
    private User cursorToUser(Cursor cursor) {
        User user = new User();

        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        user.setNickname(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NICKNAME)));
        user.setTrailNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRAIL_NUMBER)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)));
        user.setSignature(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SIGNATURE)));
        user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GENDER)));
        user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVATAR)));

        // 处理生日：移除 SDK 版本判断，直接用 ThreeTenABP 解析
        String birthdayStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BIRTHDAY));
        if (birthdayStr != null && !birthdayStr.isEmpty()) {
            user.setBirthday(LocalDate.parse(birthdayStr, dateFormatter));
        }

        // 处理创建时间：移除 SDK 版本判断
        String createTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME));
        if (createTimeStr != null && !createTimeStr.isEmpty()) {
            user.setCreateTime(LocalDateTime.parse(createTimeStr, timeFormatter));
        }

        return user;
    }

    // 字节数组转十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // 十六进制字符串转字节数组
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}