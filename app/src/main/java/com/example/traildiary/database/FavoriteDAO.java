package com.example.traildiary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.traildiary.model.Diary;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 收藏功能数据访问对象（拓展功能）
 * 用于管理用户收藏的日记
 */
public class FavoriteDAO {
    
    private DatabaseHelper dbHelper;
    private Gson gson = new Gson();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FavoriteDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ------- 添加收藏 -------
    public long addFavorite(int userId, int diaryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(DatabaseHelper.COLUMN_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_DIARY_ID, diaryId);
        values.put(DatabaseHelper.COLUMN_FAVORITE_TIME, LocalDateTime.now().format(formatter));
        
        // 使用INSERT OR IGNORE避免重复收藏报错
        long result = db.insertWithOnConflict(DatabaseHelper.TABLE_FAVORITE, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result;
    }

    // ------- 取消收藏 -------
    public int removeFavorite(int userId, int diaryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_FAVORITE,
                DatabaseHelper.COLUMN_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(diaryId)});
        db.close();
        return result;
    }

    // ------- 判断是否已收藏 -------
    public boolean isFavorite(int userId, int diaryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean isFav = false;
        
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_FAVORITE +
                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_DIARY_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(diaryId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            isFav = cursor.getInt(0) > 0;
            cursor.close();
        }
        db.close();
        return isFav;
    }

    // ------- 获取用户收藏的所有日记 -------
    public List<Diary> getFavoritesByUserId(int userId) {
        List<Diary> diaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 联表查询获取收藏的日记详情
        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar, " +
                "f." + DatabaseHelper.COLUMN_FAVORITE_TIME + " as fav_time " +
                "FROM " + DatabaseHelper.TABLE_FAVORITE + " f " +
                "INNER JOIN " + DatabaseHelper.TABLE_DIARY + " d ON f." +
                DatabaseHelper.COLUMN_DIARY_ID + " = d." + DatabaseHelper.COLUMN_DIARY_ID +
                " LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE f." + DatabaseHelper.COLUMN_USER_ID + " = ?" +
                " ORDER BY f." + DatabaseHelper.COLUMN_FAVORITE_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                diaryList.add(cursorToDiary(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return diaryList;
    }

    // ------- 获取用户收藏数量 -------
    public int getFavoriteCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_FAVORITE +
                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return count;
    }

    // ------- 将Cursor转换为Diary对象 -------
    private Diary cursorToDiary(Cursor cursor) {
        Diary diary = new Diary();

        diary.setDiaryId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_ID)));
        diary.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
        diary.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTENT)));
        diary.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)));
        diary.setAuthorId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTHOR_ID)));
        diary.setNotebookId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTEBOOK_ID)));
        diary.setCoverImagePath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COVER_IMAGE_PATH)));
        diary.setDraft(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IS_DRAFT)) == 1);
        diary.setLikeCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LIKE_COUNT)));
        diary.setViewCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VIEW_COUNT)));

        // 处理图片列表
        String imagesJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGES));
        if (imagesJson != null && !imagesJson.isEmpty()) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> images = gson.fromJson(imagesJson, listType);
            diary.setImages(images);
        }

        // 处理时间
        String createTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME_DIARY));
        if (createTimeStr != null && !createTimeStr.isEmpty()) {
            diary.setCreateTime(LocalDateTime.parse(createTimeStr, formatter));
        }

        String updateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATE_TIME));
        if (updateTimeStr != null && !updateTimeStr.isEmpty()) {
            diary.setUpdateTime(LocalDateTime.parse(updateTimeStr, formatter));
        }

        // 作者信息
        if (cursor.getColumnIndex("author_name") != -1) {
            String authorName = cursor.getString(cursor.getColumnIndexOrThrow("author_name"));
            if (authorName != null) {
                diary.setAuthorName(authorName);
            }
        }

        if (cursor.getColumnIndex("avatar") != -1) {
            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
            if (avatar != null) {
                diary.setAvatar(avatar);
            }
        }

        return diary;
    }
}
