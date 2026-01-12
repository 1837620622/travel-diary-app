package com.example.traildiary.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.example.traildiary.R;
import com.example.traildiary.adapter.CoverPagerAdapter;
import com.example.traildiary.model.Notebook;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotebookDAO {
    private DatabaseHelper dbHelper;
    private DiaryDAO diaryDAO;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotebookDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        diaryDAO = new DiaryDAO(context);
    }

    // 添加日记本
    public long addNotebook(Notebook notebook) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        android.util.Log.d("NotebookDAO", "添加笔记本: name=" + notebook.getName() 
                + ", coverPath=" + notebook.getCoverPath());

        values.put(DatabaseHelper.COLUMN_NOTEBOOK_NAME, notebook.getName());
        values.put(DatabaseHelper.COLUMN_COVER, notebook.getCoverPath());
        values.put(DatabaseHelper.COLUMN_USER_ID, notebook.getUserId());
        values.put(DatabaseHelper.COLUMN_DIARY_COUNT, notebook.getDiaryCount());
        values.put(DatabaseHelper.COLUMN_SORT_ORDER, notebook.getSortOrder());

        // 处理时间
        if (notebook.getCreateTime() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                values.put(DatabaseHelper.COLUMN_CREATE_TIME, notebook.getCreateTime().format(formatter));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                values.put(DatabaseHelper.COLUMN_CREATE_TIME, LocalDateTime.now().format(formatter));
            }
        }

        long result = db.insert(DatabaseHelper.TABLE_NOTEBOOK, null, values);
        db.close();
        return result;
    }

    // 更新日记本
    public int updateNotebook(Notebook notebook) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        android.util.Log.d("NotebookDAO", "更新笔记本: id=" + notebook.getId() 
                + ", name=" + notebook.getName() 
                + ", coverPath=" + notebook.getCoverPath());

        values.put(DatabaseHelper.COLUMN_NOTEBOOK_NAME, notebook.getName());
        values.put(DatabaseHelper.COLUMN_COVER, notebook.getCoverPath());
        values.put(DatabaseHelper.COLUMN_SORT_ORDER, notebook.getSortOrder());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            values.put(DatabaseHelper.COLUMN_CREATE_TIME, LocalDateTime.now().format(formatter));
        }

        int result = db.update(DatabaseHelper.TABLE_NOTEBOOK, values,
                DatabaseHelper.COLUMN_NOTEBOOK_ID + " = ?",
                new String[]{String.valueOf(notebook.getId())});
        
        android.util.Log.d("NotebookDAO", "更新结果: " + result);
        
        db.close();
        return result;
    }

    // 更新日记本中的日记数量
    public int updateNotebookDiaryCount(int notebookId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 获取当前日记本中的日记数量
        String countQuery = "SELECT COUNT(*) as count FROM " + DatabaseHelper.TABLE_DIARY +
                " WHERE " + DatabaseHelper.COLUMN_NOTEBOOK_ID + " = ? AND " +
                DatabaseHelper.COLUMN_IS_DRAFT + " = 0";
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(notebookId)});

        int diaryCount = 0;
        if (cursor != null && cursor.moveToFirst()) {
            diaryCount = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            cursor.close();
        }

        // 更新日记本中的日记数量
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_DIARY_COUNT, diaryCount);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            values.put(DatabaseHelper.COLUMN_CREATE_TIME, LocalDateTime.now().format(formatter));
        }

        int result = db.update(DatabaseHelper.TABLE_NOTEBOOK, values,
                DatabaseHelper.COLUMN_NOTEBOOK_ID + " = ?",
                new String[]{String.valueOf(notebookId)});
        db.close();
        return result;
    }

    // 删除日记本
    public int deleteNotebook(int notebookId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 先删除该日记本中的所有日记
        db.delete(DatabaseHelper.TABLE_DIARY,
                DatabaseHelper.COLUMN_NOTEBOOK_ID + " = ?",
                new String[]{String.valueOf(notebookId)});

        // 然后删除日记本
        int result = db.delete(DatabaseHelper.TABLE_NOTEBOOK,
                DatabaseHelper.COLUMN_NOTEBOOK_ID + " = ?",
                new String[]{String.valueOf(notebookId)});
        db.close();
        return result;
    }

    // 根据ID获取日记本
    public Notebook getNotebookById(int notebookId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Notebook notebook = null;

        String[] columns = {
                DatabaseHelper.COLUMN_NOTEBOOK_ID,
                DatabaseHelper.COLUMN_NOTEBOOK_NAME,
                DatabaseHelper.COLUMN_COVER,
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_DIARY_COUNT,
                DatabaseHelper.COLUMN_SORT_ORDER,
                DatabaseHelper.COLUMN_CREATE_TIME
        };

        Cursor cursor = db.query(DatabaseHelper.TABLE_NOTEBOOK, columns,
                DatabaseHelper.COLUMN_NOTEBOOK_ID + " = ?",
                new String[]{String.valueOf(notebookId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            notebook = cursorToNotebook(cursor);
            cursor.close();
        }
        db.close();
        return notebook;
    }

    // 获取用户的所有日记本
    public List<Notebook> getNotebooksByUser(int userId) {
        List<Notebook> notebookList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {
                DatabaseHelper.COLUMN_NOTEBOOK_ID,
                DatabaseHelper.COLUMN_NOTEBOOK_NAME,
                DatabaseHelper.COLUMN_COVER,
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_DIARY_COUNT,
                DatabaseHelper.COLUMN_SORT_ORDER,
                DatabaseHelper.COLUMN_CREATE_TIME
        };

        String orderBy = DatabaseHelper.COLUMN_SORT_ORDER + " ASC, " +
                DatabaseHelper.COLUMN_CREATE_TIME + " DESC";

        Cursor cursor = db.query(DatabaseHelper.TABLE_NOTEBOOK, columns,
                DatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}, null, null, orderBy);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                notebookList.add(cursorToNotebook(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return notebookList;
    }

    // 获取日记本总数
    public int getNotebookCountByUser(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;

        String query = "SELECT COUNT(*) as count FROM " + DatabaseHelper.TABLE_NOTEBOOK +
                " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
            cursor.close();
        }
        db.close();
        return count;
    }

    // 检查日记本名称是否重复（同一用户内）
    public boolean isNotebookNameExists(String notebookName, int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_NOTEBOOK_NAME + " = ? AND " +
                DatabaseHelper.COLUMN_USER_ID + " = ?";

        Cursor cursor = db.query(DatabaseHelper.TABLE_NOTEBOOK,
                new String[]{DatabaseHelper.COLUMN_NOTEBOOK_ID},
                selection, new String[]{notebookName, String.valueOf(userId)},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    // 为用户创建默认日记本
    public long createDefaultNotebook(int userId) {
        Notebook defaultNotebook = new Notebook();
        defaultNotebook.setName("我的日记本");
        defaultNotebook.setCoverPath("1"); // 使用数字1作为默认封面
        defaultNotebook.setUserId(userId);
        defaultNotebook.setDiaryCount(0);
        defaultNotebook.setSortOrder(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            defaultNotebook.setCreateTime(LocalDateTime.now());
        }

        return addNotebook(defaultNotebook);
    }

    // 更新日记本排序
    public int updateNotebookOrder(List<Notebook> notebooks) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int updatedCount = 0;

        try {
            db.beginTransaction();

            for (int i = 0; i < notebooks.size(); i++) {
                Notebook notebook = notebooks.get(i);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_SORT_ORDER, i);

                int result = db.update(DatabaseHelper.TABLE_NOTEBOOK, values,
                        DatabaseHelper.COLUMN_NOTEBOOK_ID + " = ?",
                        new String[]{String.valueOf(notebook.getId())});

                if (result > 0) {
                    updatedCount++;
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }

        return updatedCount;
    }

    // 将Cursor转换为Notebook对象
    private Notebook cursorToNotebook(Cursor cursor) {
        Notebook notebook = new Notebook();
        notebook.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTEBOOK_ID)));
        notebook.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTEBOOK_NAME)));
        
        // 获取封面路径
        String coverPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COVER));
        notebook.setCoverPath(coverPath);
        
        // 添加调试日志
        android.util.Log.d("NotebookDAO", "从数据库读取笔记本: name=" + notebook.getName() + ", coverPath=" + coverPath);
        
        // 根据封面路径自动设置资源ID
        if (coverPath != null && !coverPath.isEmpty()) {
            int coverResId = CoverPagerAdapter.getCoverResourceId(coverPath);
            notebook.setCoverResId(coverResId);
            android.util.Log.d("NotebookDAO", "设置coverResId: " + coverResId);
        } else {
            // 如果没有封面路径，使用默认封面
            notebook.setCoverResId(R.drawable.cover1);
            android.util.Log.d("NotebookDAO", "使用默认封面: " + R.drawable.cover1);
        }
        
        notebook.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        notebook.setDiaryCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIARY_COUNT)));
        notebook.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SORT_ORDER)));

        // 处理时间
        String createTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME));
        if (createTimeStr != null && !createTimeStr.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notebook.setCreateTime(LocalDateTime.parse(createTimeStr, formatter));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notebook.setUpdateTime(LocalDateTime.parse(createTimeStr, formatter));
            }
        }

        return notebook;
    }
}
