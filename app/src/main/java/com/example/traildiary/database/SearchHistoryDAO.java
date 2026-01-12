package com.example.traildiary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.example.traildiary.model.SearchHistory;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryDAO {
    private DatabaseHelper dbHelper;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SearchHistoryDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 添加搜索记录
    public long addSearchHistory(SearchHistory searchHistory) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_USER_ID, searchHistory.getUserId());
        values.put(DatabaseHelper.COLUMN_KEYWORD, searchHistory.getKeyword());
        values.put(DatabaseHelper.COLUMN_SEARCH_TYPE, searchHistory.getSearchType());
        values.put(DatabaseHelper.COLUMN_SEARCH_RESULT, searchHistory.getSearchResult());

        // 处理时间
        if (searchHistory.getSearchTime() != null) {
            values.put(DatabaseHelper.COLUMN_CREATE_TIME, searchHistory.getSearchTime().format(formatter));
        } else {
            values.put(DatabaseHelper.COLUMN_CREATE_TIME, LocalDateTime.now().format(formatter));
        }

        long result = db.insert(DatabaseHelper.TABLE_SEARCH_HISTORY, null, values);
        db.close();
        return result;
    }

    // 按搜索ID删除单条记录
    public int deleteSearchHistory(int searchId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_SEARCH_HISTORY,
                DatabaseHelper.COLUMN_SEARCH_ID + " = ?",
                new String[]{String.valueOf(searchId)});
        db.close();
        return result;
    }

    public List<SearchHistory> getRecentSearchHistory(int userId) {
        return getSearchHistoryByUser(userId, 10); // 获取最近10条记录
    }

    // 按【关键词+用户ID】删除记录
    public int deleteSearchHistoryByKeyword(String keyword, int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_SEARCH_HISTORY,
                DatabaseHelper.COLUMN_KEYWORD + " = ? AND " +
                        DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{keyword, String.valueOf(userId)});
        db.close();
        return result;
    }

    // 清空指定用户的所有搜索历史
    public int clearUserSearchHistory(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_SEARCH_HISTORY,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
        return result;
    }

    // 获取指定用户的搜索历史（按时间倒序，支持条数限制）
    public List<SearchHistory> getSearchHistoryByUser(int userId, int limit) {
        List<SearchHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_SEARCH_HISTORY +
                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? " +
                "ORDER BY " + DatabaseHelper.COLUMN_CREATE_TIME + " DESC LIMIT ?";

        Cursor cursor = db.rawQuery(query,
                new String[]{String.valueOf(userId), String.valueOf(limit)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                historyList.add(cursorToSearchHistory(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return historyList;
    }

    // 获取热门搜索关键词（按搜索次数排序，支持条数限制）
    public List<String> getPopularKeywords(int limit) {
        List<String> keywords = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + DatabaseHelper.COLUMN_KEYWORD + ", COUNT(*) as count " +
                "FROM " + DatabaseHelper.TABLE_SEARCH_HISTORY +
                " GROUP BY " + DatabaseHelper.COLUMN_KEYWORD +
                " ORDER BY count DESC LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                keywords.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEYWORD)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return keywords;
    }

    // 检查【关键词+用户ID+搜索类型】是否存在
    public boolean isSearchHistoryExists(String keyword, int userId, int searchType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SEARCH_HISTORY +
                " WHERE " + DatabaseHelper.COLUMN_KEYWORD + " = ? AND " +
                DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_SEARCH_TYPE + " = ?";

        Cursor cursor = db.rawQuery(query,
                new String[]{keyword, String.valueOf(userId), String.valueOf(searchType)});

        boolean exists = false;
        if (cursor != null && cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
            cursor.close();
        }
        db.close();
        return exists;
    }

    // 检查【关键词+用户ID】是否存在
    public boolean isSearchHistoryExist(int userId, String keyword) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SEARCH_HISTORY +
                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                DatabaseHelper.COLUMN_KEYWORD + " = ?";

        Cursor cursor = db.rawQuery(query,
                new String[]{String.valueOf(userId), keyword});

        boolean exists = false;
        if (cursor != null && cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
            cursor.close();
        }
        db.close();
        return exists;
    }

    // Cursor转换为SearchHistory对象
    private SearchHistory cursorToSearchHistory(Cursor cursor) {
        // 从cursor中获取userId和keyword
        int userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
        String keyword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEYWORD));

        SearchHistory history = new SearchHistory(userId, keyword);

        history.setSearchId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SEARCH_ID)));
        history.setSearchType(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SEARCH_TYPE)));
        history.setSearchResult(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SEARCH_RESULT)));

        // 处理时间
        String searchTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME));
        if (searchTimeStr != null && !searchTimeStr.isEmpty()) {
            history.setSearchTime(LocalDateTime.parse(searchTimeStr, formatter));
        }

        return history;
    }
}