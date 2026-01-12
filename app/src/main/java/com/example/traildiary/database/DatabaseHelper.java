package com.example.traildiary.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 数据库信息
    private static final String DATABASE_NAME = "TrailDiary.db";
    private static final int DATABASE_VERSION = 3; // 版本升级：添加收藏表

    // 用户表
    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_AVATAR = "avatar";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_TRAIL_NUMBER = "trail_number";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_SIGNATURE = "signature";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_BIRTHDAY = "birthday";
    public static final String COLUMN_CREATE_TIME = "create_time";

    // 日记本表
    public static final String TABLE_NOTEBOOK = "notebook";
    public static final String COLUMN_NOTEBOOK_ID = "notebook_id";
    public static final String COLUMN_NOTEBOOK_NAME = "notebook_name";
    public static final String COLUMN_COVER = "cover";
    public static final String COLUMN_DIARY_COUNT = "diary_count";
    public static final String COLUMN_SORT_ORDER = "sort_order";
    // COLUMN_USER_ID 已定义

    // 日记表 - 更新字段以匹配Diary模型
    public static final String TABLE_DIARY = "diary";
    public static final String COLUMN_DIARY_ID = "diary_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_AUTHOR_ID = "author_id";
    public static final String COLUMN_AUTHOR_NAME = "author_name"; // 新增
    public static final String COLUMN_COVER_IMAGE_PATH = "cover_image_path"; // 修正命名
    public static final String COLUMN_IMAGES = "images"; // 多图路径，存储为JSON字符串
    public static final String COLUMN_IS_DRAFT = "is_draft";
    public static final String COLUMN_CREATE_TIME_DIARY = "create_time_diary"; // 重命名避免冲突
    public static final String COLUMN_UPDATE_TIME = "update_time"; // 更新时间
    public static final String COLUMN_LIKE_COUNT = "like_count"; // 新增
    public static final String COLUMN_VIEW_COUNT = "view_count"; // 新增
    // COLUMN_NOTEBOOK_ID 已定义

    // 搜索历史表
    public static final String TABLE_SEARCH_HISTORY = "search_history";
    public static final String COLUMN_SEARCH_ID = "search_id";
    public static final String COLUMN_KEYWORD = "keyword";
    public static final String COLUMN_SEARCH_TYPE = "search_type";
    public static final String COLUMN_SEARCH_RESULT = "search_result";
    // COLUMN_USER_ID 已定义
    // 使用COLUMN_CREATE_TIME作为搜索时间

    // 收藏表（拓展功能）
    public static final String TABLE_FAVORITE = "favorite";
    public static final String COLUMN_FAVORITE_ID = "favorite_id";
    // COLUMN_USER_ID 已定义
    // COLUMN_DIARY_ID 已定义
    public static final String COLUMN_FAVORITE_TIME = "favorite_time";

    // 创建用户表SQL
    private static final String CREATE_TABLE_USER =
            "CREATE TABLE " + TABLE_USER + "(" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_AVATAR + " TEXT," +
                    COLUMN_NICKNAME + " TEXT UNIQUE NOT NULL," +
                    COLUMN_TRAIL_NUMBER + " TEXT UNIQUE NOT NULL," +
                    COLUMN_PASSWORD + " TEXT NOT NULL," +
                    COLUMN_PHONE + " TEXT," +
                    COLUMN_SIGNATURE + " TEXT," +
                    COLUMN_GENDER + " TEXT," +
                    COLUMN_BIRTHDAY + " TEXT," + // 存储为字符串格式 yyyy-MM-dd
                    COLUMN_CREATE_TIME + " TEXT" + // 存储为字符串格式 yyyy-MM-dd HH:mm:ss
                    ")";

    // 创建日记本表SQL
    private static final String CREATE_TABLE_NOTEBOOK =
            "CREATE TABLE " + TABLE_NOTEBOOK + "(" +
                    COLUMN_NOTEBOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NOTEBOOK_NAME + " TEXT NOT NULL," +
                    COLUMN_COVER + " TEXT," +
                    COLUMN_USER_ID + " INTEGER NOT NULL," +
                    COLUMN_DIARY_COUNT + " INTEGER DEFAULT 0," +
                    COLUMN_SORT_ORDER + " INTEGER DEFAULT 0," +
                    COLUMN_CREATE_TIME + " TEXT," + // 存储为字符串格式
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE" +
                    ")";

    // 创建日记表SQL - 更新以匹配Diary模型
    private static final String CREATE_TABLE_DIARY =
            "CREATE TABLE " + TABLE_DIARY + "(" +
                    COLUMN_DIARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TITLE + " TEXT NOT NULL," +
                    COLUMN_CONTENT + " TEXT," +
                    COLUMN_CATEGORY + " TEXT," +
                    COLUMN_AUTHOR_ID + " INTEGER NOT NULL," +
                    COLUMN_AUTHOR_NAME + " TEXT," + // 新增
                    COLUMN_COVER_IMAGE_PATH + " TEXT," + // 修正命名
                    COLUMN_IMAGES + " TEXT," + // 存储为JSON数组字符串
                    COLUMN_NOTEBOOK_ID + " INTEGER," +
                    COLUMN_IS_DRAFT + " INTEGER DEFAULT 0," + // 0-不是草稿, 1-是草稿
                    COLUMN_CREATE_TIME_DIARY + " TEXT," + // 创建时间
                    COLUMN_UPDATE_TIME + " TEXT," + // 更新时间
                    COLUMN_LIKE_COUNT + " INTEGER DEFAULT 0," + // 点赞数
                    COLUMN_VIEW_COUNT + " INTEGER DEFAULT 0," + // 浏览数
                    "FOREIGN KEY(" + COLUMN_AUTHOR_ID + ") REFERENCES " +
                    TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE," +
                    "FOREIGN KEY(" + COLUMN_NOTEBOOK_ID + ") REFERENCES " +
                    TABLE_NOTEBOOK + "(" + COLUMN_NOTEBOOK_ID + ") ON DELETE SET NULL" +
                    ")";

    // 创建搜索历史表SQL
    private static final String CREATE_TABLE_SEARCH_HISTORY =
            "CREATE TABLE " + TABLE_SEARCH_HISTORY + "(" +
                    COLUMN_SEARCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_KEYWORD + " TEXT NOT NULL," +
                    COLUMN_SEARCH_TYPE + " INTEGER DEFAULT 0," +
                    COLUMN_USER_ID + " INTEGER NOT NULL," +
                    COLUMN_CREATE_TIME + " TEXT," + // 使用TEXT存储时间字符串
                    COLUMN_SEARCH_RESULT + " TEXT," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE" +
                    ")";

    // 创建收藏表SQL（拓展功能）
    private static final String CREATE_TABLE_FAVORITE =
            "CREATE TABLE " + TABLE_FAVORITE + "(" +
                    COLUMN_FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USER_ID + " INTEGER NOT NULL," +
                    COLUMN_DIARY_ID + " INTEGER NOT NULL," +
                    COLUMN_FAVORITE_TIME + " TEXT," +
                    "UNIQUE(" + COLUMN_USER_ID + ", " + COLUMN_DIARY_ID + ")," +
                    "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " +
                    TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE," +
                    "FOREIGN KEY(" + COLUMN_DIARY_ID + ") REFERENCES " +
                    TABLE_DIARY + "(" + COLUMN_DIARY_ID + ") ON DELETE CASCADE" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建所有表
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_NOTEBOOK);
        db.execSQL(CREATE_TABLE_DIARY);
        db.execSQL(CREATE_TABLE_SEARCH_HISTORY);
        db.execSQL(CREATE_TABLE_FAVORITE); // 收藏表
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级数据库时删除旧表并创建新表
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH_HISTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTEBOOK);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            onCreate(db);
        }
        // 版本3：添加收藏表
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE);
            db.execSQL(CREATE_TABLE_FAVORITE);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // 启用外键约束
        db.setForeignKeyConstraintsEnabled(true);
    }
}
