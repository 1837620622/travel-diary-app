package com.example.traildiary.model;

import org.threeten.bp.LocalDateTime;

import java.util.Date;

public class SearchHistory {
    private int searchId;
    private int userId;           // 用户ID
    private String keyword;       // 搜索关键词
    private int searchType;       // 搜索类型（1-作者,2-标题,3-类别,0-综合）
    private LocalDateTime searchTime;      // 搜索时间
    private String searchResult;  // 搜索结果（可存储JSON格式或摘要）

    // 搜索类型常量定义 - 与适配器中的映射保持一致
    public static final int TYPE_AUTHOR = 1;
    public static final int TYPE_TITLE = 2;
    public static final int TYPE_CATEGORY = 3;
    public static final int TYPE_GENERAL = 0;  // 新增：综合搜索类型

    public SearchHistory(int userId, String keyword) {
        this.userId = userId;
        this.keyword = keyword;
        this.searchTime = LocalDateTime.now();
    }

    // 全参构造方法（包含所有字段）
    public SearchHistory(int searchId, int userId, String keyword, int searchType, LocalDateTime searchTime, String searchResult) {
        this.searchId = searchId;
        this.userId = userId;
        this.keyword = keyword;
        this.searchType = searchType;
        this.searchTime = searchTime;
        this.searchResult = searchResult;
    }

    // 用于插入新记录的构造方法（不包含searchId和searchResult）
    public SearchHistory(int userId, String keyword, int searchType) {
        this.userId = userId;
        this.keyword = keyword;
        this.searchType = searchType;
        this.searchTime = LocalDateTime.now();
    }

    // 用于插入新记录的构造方法（包含searchResult）
    public SearchHistory(int userId, String keyword, int searchType, String searchResult) {
        this.userId = userId;
        this.keyword = keyword;
        this.searchType = searchType;
        this.searchResult = searchResult;
        this.searchTime = LocalDateTime.now();
    }

    // 添加适配器需要的getId()方法
    public int getId() {
        return searchId;
    }

    // 所有属性的 Getter 和 Setter 方法
    public int getSearchId() {
        return searchId;
    }

    public void setSearchId(int searchId) {
        this.searchId = searchId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public LocalDateTime getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(LocalDateTime searchTime) {
        this.searchTime = searchTime;
    }

    public String getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(String searchResult) {
        this.searchResult = searchResult;
    }

    // 辅助方法：获取搜索类型描述 - 与适配器中的映射保持一致
    public String getSearchTypeDescription() {
        switch (searchType) {
            case TYPE_AUTHOR:
                return "按作者";
            case TYPE_TITLE:
                return "按标题";
            case TYPE_CATEGORY:
                return "按类别";
            case TYPE_GENERAL:
                return "综合";
            default:
                return "未知";
        }
    }
}