package com.example.traildiary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.traildiary.model.Diary;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DiaryDAO {
    private DatabaseHelper dbHelper;
    private Gson gson = new Gson();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DiaryDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 添加日记
    public long addDiary(Diary diary) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_TITLE, diary.getTitle());
        values.put(DatabaseHelper.COLUMN_CONTENT, diary.getContent());
        values.put(DatabaseHelper.COLUMN_CATEGORY, diary.getCategory());
        values.put(DatabaseHelper.COLUMN_AUTHOR_ID, diary.getAuthorId());
        values.put(DatabaseHelper.COLUMN_AUTHOR_NAME, diary.getAuthorName());
        values.put(DatabaseHelper.COLUMN_COVER_IMAGE_PATH, diary.getCoverImagePath());
        values.put(DatabaseHelper.COLUMN_NOTEBOOK_ID, diary.getNotebookId());
        values.put(DatabaseHelper.COLUMN_IS_DRAFT, diary.isDraft() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_LIKE_COUNT, diary.getLikeCount());
        values.put(DatabaseHelper.COLUMN_VIEW_COUNT, diary.getViewCount());

        // 处理图片列表
        if (diary.getImages() != null) {
            values.put(DatabaseHelper.COLUMN_IMAGES, gson.toJson(diary.getImages()));
        }

        // 处理时间（使用 org.threeten.bp 包的 LocalDateTime）
        if (diary.getCreateTime() != null) {
            values.put(DatabaseHelper.COLUMN_CREATE_TIME_DIARY, diary.getCreateTime().format(formatter));
        }
        if (diary.getUpdateTime() != null) {
            values.put(DatabaseHelper.COLUMN_UPDATE_TIME, diary.getUpdateTime().format(formatter));
        } else if (diary.getCreateTime() != null) {
            values.put(DatabaseHelper.COLUMN_UPDATE_TIME, diary.getCreateTime().format(formatter));
        }

        long result = db.insert(DatabaseHelper.TABLE_DIARY, null, values);
        db.close();
        return result;
    }

    // 更新日记
    public int updateDiary(Diary diary) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_TITLE, diary.getTitle());
        values.put(DatabaseHelper.COLUMN_CONTENT, diary.getContent());
        values.put(DatabaseHelper.COLUMN_CATEGORY, diary.getCategory());
        values.put(DatabaseHelper.COLUMN_AUTHOR_NAME, diary.getAuthorName());
        values.put(DatabaseHelper.COLUMN_COVER_IMAGE_PATH, diary.getCoverImagePath());
        values.put(DatabaseHelper.COLUMN_NOTEBOOK_ID, diary.getNotebookId());
        values.put(DatabaseHelper.COLUMN_IS_DRAFT, diary.isDraft() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_LIKE_COUNT, diary.getLikeCount());
        values.put(DatabaseHelper.COLUMN_VIEW_COUNT, diary.getViewCount());

        // 处理图片列表
        if (diary.getImages() != null) {
            values.put(DatabaseHelper.COLUMN_IMAGES, gson.toJson(diary.getImages()));
        }

        // 更新时间为当前时间（使用 org.threeten.bp 包的 LocalDateTime）
        values.put(DatabaseHelper.COLUMN_UPDATE_TIME, LocalDateTime.now().format(formatter));

        int result = db.update(DatabaseHelper.TABLE_DIARY, values,
                DatabaseHelper.COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(diary.getDiaryId())});
        db.close();
        return result;
    }

    // 删除日记
    public int deleteDiary(int diaryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_DIARY,
                DatabaseHelper.COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(diaryId)});
        db.close();
        return result;
    }

    // 根据ID获取日记
    public Diary getDiaryById(int diaryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Diary diary = null;

        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE d." + DatabaseHelper.COLUMN_DIARY_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(diaryId)});

        if (cursor != null && cursor.moveToFirst()) {
            diary = cursorToDiary(cursor);
            cursor.close();
        }
        db.close();
        return diary;
    }

    // 获取用户的所有日记
    public List<Diary> getDiariesByUserId(int userId, boolean includeDrafts) {
        List<Diary> diaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE d." + DatabaseHelper.COLUMN_AUTHOR_ID + " = ?";

        if (!includeDrafts) {
            query += " AND d." + DatabaseHelper.COLUMN_IS_DRAFT + " = 0";
        }

        query += " ORDER BY d." + DatabaseHelper.COLUMN_CREATE_TIME_DIARY + " DESC";

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

    // 根据日记本ID获取日记
    public List<Diary> getDiariesByNotebookId(int notebookId) {
        List<Diary> diaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE d." + DatabaseHelper.COLUMN_NOTEBOOK_ID + " = ? AND d." +
                DatabaseHelper.COLUMN_IS_DRAFT + " = 0" +
                " ORDER BY d." + DatabaseHelper.COLUMN_CREATE_TIME_DIARY + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(notebookId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                diaryList.add(cursorToDiary(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return diaryList;
    }

    // 按搜索类型（作者/类别/标题/综合）搜索所有用户日记
    public List<Diary> searchDiaries(String keyword, int searchType) {
        List<Diary> diaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE d." + DatabaseHelper.COLUMN_IS_DRAFT + " = 0 AND (";

        switch (searchType) {
            case 0: // 按作者搜索
                query += "u." + DatabaseHelper.COLUMN_NICKNAME + " LIKE ?)";
                break;
            case 1: // 按类别搜索
                query += "d." + DatabaseHelper.COLUMN_CATEGORY + " LIKE ?)";
                break;
            case 2: // 按标题搜索
                query += "d." + DatabaseHelper.COLUMN_TITLE + " LIKE ?)";
                break;
            default: // 综合搜索
                query += "u." + DatabaseHelper.COLUMN_NICKNAME + " LIKE ? OR " +
                        "d." + DatabaseHelper.COLUMN_CATEGORY + " LIKE ? OR " +
                        "d." + DatabaseHelper.COLUMN_TITLE + " LIKE ?)";
        }

        query += " ORDER BY d." + DatabaseHelper.COLUMN_CREATE_TIME_DIARY + " DESC";

        Cursor cursor;
        if (searchType == 3) { // 综合搜索
            cursor = db.rawQuery(query, new String[]{"%" + keyword + "%",
                    "%" + keyword + "%", "%" + keyword + "%"});
        } else {
            cursor = db.rawQuery(query, new String[]{"%" + keyword + "%"});
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                diaryList.add(cursorToDiary(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return diaryList;
    }

    // 按关键词+用户ID 综合搜索（标题/内容/类别）
    public List<Diary> searchDiariesByUser(String keyword, int userId) {
        List<Diary> diaries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE (d." + DatabaseHelper.COLUMN_TITLE + " LIKE ? OR d." +
                DatabaseHelper.COLUMN_CONTENT + " LIKE ? OR d." + DatabaseHelper.COLUMN_CATEGORY +
                " LIKE ?) AND d." + DatabaseHelper.COLUMN_AUTHOR_ID + " = ? " +
                "ORDER BY d." + DatabaseHelper.COLUMN_CREATE_TIME_DIARY + " DESC";

        String searchPattern = "%" + keyword + "%";
        Cursor cursor = db.rawQuery(query, new String[]{
                searchPattern, searchPattern, searchPattern, String.valueOf(userId)
        });

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Diary diary = cursorToDiary(cursor);
                diaries.add(diary);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return diaries;
    }

    // 按作者搜索（搜索作者昵称包含关键词的日记）
    public List<Diary> searchByAuthor(String keyword, int userId) {
        List<Diary> diaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 搜索作者昵称包含关键词的日记
        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE u." + DatabaseHelper.COLUMN_NICKNAME + " LIKE ? AND d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = ? AND d." +
                DatabaseHelper.COLUMN_IS_DRAFT + " = 0" +
                " ORDER BY d." + DatabaseHelper.COLUMN_CREATE_TIME_DIARY + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{"%" + keyword + "%", String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                diaryList.add(cursorToDiary(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return diaryList;
    }

    // 按标题搜索
    public List<Diary> searchByTitle(String keyword, int userId) {
        List<Diary> diaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE d." + DatabaseHelper.COLUMN_TITLE + " LIKE ? AND d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = ? AND d." +
                DatabaseHelper.COLUMN_IS_DRAFT + " = 0" +
                " ORDER BY d." + DatabaseHelper.COLUMN_CREATE_TIME_DIARY + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{"%" + keyword + "%", String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                diaryList.add(cursorToDiary(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return diaryList;
    }

    // 按类别搜索（只支持类别名称搜索，如"国内游"）
    public List<Diary> searchByCategory(String keyword, int userId) {
        List<Diary> diaryList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 将类别名称转换为类别代码，只有匹配到有效类别名称才搜索
        String categoryCode = getCategoryCodeFromName(keyword);
        
        // 如果输入的不是有效的类别名称，返回空列表
        if (categoryCode.isEmpty()) {
            db.close();
            return diaryList;
        }
        
        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE d." + DatabaseHelper.COLUMN_CATEGORY + " = ? AND d." +
                DatabaseHelper.COLUMN_AUTHOR_ID + " = ? AND d." +
                DatabaseHelper.COLUMN_IS_DRAFT + " = 0" +
                " ORDER BY d." + DatabaseHelper.COLUMN_CREATE_TIME_DIARY + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{
                categoryCode,
                String.valueOf(userId)
        });

        if (cursor != null && cursor.moveToFirst()) {
            do {
                diaryList.add(cursorToDiary(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return diaryList;
    }
    
    // 根据类别名称获取类别代码（只匹配完整的类别名称）
    private String getCategoryCodeFromName(String categoryName) {
        if (categoryName == null) return "";
        
        // 去除空格后匹配
        String trimmed = categoryName.trim();
        
        switch (trimmed) {
            case "国内游":
                return "1";
            case "国际游":
                return "2";
            case "亲子游":
                return "3";
            case "美食之旅":
                return "4";
            case "探险之旅":
                return "5";
            case "文化之旅":
                return "6";
            default:
                // 不匹配任何类别名称，返回空字符串
                return "";
        }
    }

    // 综合搜索（所有字段）
    public List<Diary> searchAll(String keyword, int userId) {
        return searchDiariesByUser(keyword, userId);
    }

    // 获取用户的草稿（修复 SQL 语法错误）
    public List<Diary> getUserDrafts(int userId) {
        List<Diary> drafts = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT d.*, u." + DatabaseHelper.COLUMN_NICKNAME + " as author_name, " +
                "u." + DatabaseHelper.COLUMN_AVATAR + " as avatar " +
                "FROM " + DatabaseHelper.TABLE_DIARY + " d " +
                "LEFT JOIN " + DatabaseHelper.TABLE_USER + " u ON d." + DatabaseHelper.COLUMN_AUTHOR_ID + " = u." + DatabaseHelper.COLUMN_USER_ID +
                " WHERE d." + DatabaseHelper.COLUMN_AUTHOR_ID + " = ? AND d." +
                DatabaseHelper.COLUMN_IS_DRAFT + " = 1" +
                " ORDER BY d." + DatabaseHelper.COLUMN_UPDATE_TIME + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                drafts.add(cursorToDiary(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return drafts;
    }

    // 删除用户的草稿
    public int deleteUserDrafts(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_DIARY,
                DatabaseHelper.COLUMN_AUTHOR_ID + " = ? AND " +
                        DatabaseHelper.COLUMN_IS_DRAFT + " = 1",
                new String[]{String.valueOf(userId)});
        db.close();
        return result;
    }

    // 获取日记总数
    public int getDiaryCountByUser(int userId, boolean includeDrafts) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;

        String query = "SELECT COUNT(*) as count FROM " + DatabaseHelper.TABLE_DIARY +
                " WHERE " + DatabaseHelper.COLUMN_AUTHOR_ID + " = ?";

        if (!includeDrafts) {
            query += " AND " + DatabaseHelper.COLUMN_IS_DRAFT + " = 0";
        }

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            cursor.close();
        }
        db.close();
        return count;
    }

    // 增加点赞数
    public int incrementLikeCount(int diaryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "UPDATE " + DatabaseHelper.TABLE_DIARY +
                " SET " + DatabaseHelper.COLUMN_LIKE_COUNT + " = " +
                DatabaseHelper.COLUMN_LIKE_COUNT + " + 1 WHERE " +
                DatabaseHelper.COLUMN_DIARY_ID + " = ?";

        db.execSQL(query, new String[]{String.valueOf(diaryId)});

        // 获取更新后的点赞数
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_LIKE_COUNT +
                        " FROM " + DatabaseHelper.TABLE_DIARY +
                        " WHERE " + DatabaseHelper.COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(diaryId)});

        int likeCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            likeCount = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return likeCount;
    }

    // 增加浏览数
    public int incrementViewCount(int diaryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String query = "UPDATE " + DatabaseHelper.TABLE_DIARY +
                " SET " + DatabaseHelper.COLUMN_VIEW_COUNT + " = " +
                DatabaseHelper.COLUMN_VIEW_COUNT + " + 1 WHERE " +
                DatabaseHelper.COLUMN_DIARY_ID + " = ?";

        db.execSQL(query, new String[]{String.valueOf(diaryId)});

        // 获取更新后的浏览数
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_VIEW_COUNT +
                        " FROM " + DatabaseHelper.TABLE_DIARY +
                        " WHERE " + DatabaseHelper.COLUMN_DIARY_ID + " = ?",
                new String[]{String.valueOf(diaryId)});

        int viewCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            viewCount = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return viewCount;
    }

    // 将Cursor转换为Diary对象
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

        // 处理时间（使用 org.threeten.bp 包的 LocalDateTime）
        String createTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME_DIARY));
        if (createTimeStr != null && !createTimeStr.isEmpty()) {
            diary.setCreateTime(LocalDateTime.parse(createTimeStr, formatter));
        }

        String updateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATE_TIME));
        if (updateTimeStr != null && !updateTimeStr.isEmpty()) {
            diary.setUpdateTime(LocalDateTime.parse(updateTimeStr, formatter));
        }

        // 从JOIN查询中获取的作者信息
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