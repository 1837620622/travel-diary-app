package com.example.traildiary.utils;

public class Constants {

    // SharedPreferences 键名
    public static final String SP_KEY_AUTO_LOGIN = "auto_login";
    public static final String SP_KEY_USERNAME = "username";
    public static final String SP_KEY_PASSWORD = "password";
    public static final String SP_KEY_USER_ID = "user_id";
    public static final String SP_KEY_NICKNAME = "nickname";
    public static final String SP_KEY_TRAIL_NUMBER = "trail_number";
    public static final String SP_KEY_FIRST_RUN = "first_run";
    public static final String SP_KEY_AGREEMENT_AGREED = "agreement_agreed";

    // 用户相关常量
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 0;
    public static final String DEFAULT_SIGNATURE = "山川为印，时光为笔。";
    public static final String SP_KEY_AVATAR_RES_ID = "avatar_res_id";

    // 头像相关
    public static final String SP_KEY_AVATAR = "avatar";

    // 日记相关常量
    public static final int DIARY_CATEGORY_DOMESTIC = 1;      // 国内游
    public static final int DIARY_CATEGORY_INTERNATIONAL = 2; // 国际游
    public static final int DIARY_CATEGORY_FAMILY = 3;        // 亲子游
    public static final int DIARY_CATEGORY_FOOD = 4;          // 美食之旅
    public static final int DIARY_CATEGORY_ADVENTURE = 5;     // 探险之旅
    public static final int DIARY_CATEGORY_CULTURE = 6;       // 文化之旅

    // 数据库相关常量
    public static final String DB_NAME = "trail_diary.db";
    public static final int DB_VERSION = 1;

    // 日期时间格式
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // 权限请求码
    public static final int PERMISSION_REQUEST_LOCATION = 1001;
    public static final int PERMISSION_REQUEST_STORAGE = 1002;
    public static final int PERMISSION_REQUEST_CAMERA = 1003;

    // Intent 键名
    public static final String INTENT_KEY_USER = "user";
    public static final String INTENT_KEY_DIARY = "diary";
    public static final String INTENT_KEY_NOTEBOOK = "notebook";
    public static final String INTENT_KEY_DIARY_ID = "diary_id";
    public static final String INTENT_KEY_NOTEBOOK_ID = "notebook_id";
    public static final String INTENT_KEY_IS_EDIT_MODE = "is_edit_mode";
    public static final String INTENT_KEY_FROM_DRAFT = "from_draft";

    // 搜索相关
    public static final int MAX_SEARCH_HISTORY = 10;          // 最大保存搜索历史条数
    public static final int SEARCH_BY_AUTHOR = 1;             // 按作者搜索
    public static final int SEARCH_BY_TITLE = 2;              // 按标题搜索
    public static final int SEARCH_BY_CATEGORY = 3;           // 按类别搜索
}