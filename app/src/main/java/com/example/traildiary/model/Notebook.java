package com.example.traildiary.model;

import java.io.Serializable;
import org.threeten.bp.LocalDateTime;

public class Notebook implements Serializable {
    private static final long serialVersionUID = 1L;  // 添加序列化版本ID

    private int id;
    private String name;
    private String coverPath;  // 封面图片路径，可以为null
    private int coverResId;    // 封面资源ID，为0表示未设置
    private int diaryCount;    // 日记篇数
    private int userId;        // 所属用户ID
    private LocalDateTime createTime;  // 使用java.time替代Date
    private LocalDateTime updateTime;  // 使用java.time替代Date
    private int sortOrder;     // 排序顺序，默认0表示未排序

    // 无参构造
    public Notebook() {
        this.diaryCount = 0;
        this.sortOrder = 0;
        this.coverResId = 0;
    }

    // 全参构造（包含封面路径和资源ID）
    public Notebook(int id, String name, String coverPath, int coverResId, int diaryCount,
                    int userId, LocalDateTime createTime, LocalDateTime updateTime, int sortOrder) {
        this.id = id;
        this.name = name;
        this.coverPath = coverPath;
        this.coverResId = coverResId;
        this.diaryCount = diaryCount;
        this.userId = userId;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath; // 允许为null
    }

    public int getCoverResId() { return coverResId; }
    public void setCoverResId(int coverResId) {
        this.coverResId = coverResId;
    }

    public int getDiaryCount() { return diaryCount; }
    public void setDiaryCount(int diaryCount) {
        this.diaryCount = Math.max(diaryCount, 0); // 确保非负
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    // 简化获取封面显示文本的方法
    public String getDisplayDiaryCount() {
        return "共" + diaryCount + "篇";
    }

    // 添加toString方法便于调试
    @Override
    public String toString() {
        return "Notebook{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coverPath='" + coverPath + '\'' +
                ", coverResId=" + coverResId +
                ", diaryCount=" + diaryCount +
                ", userId=" + userId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", sortOrder=" + sortOrder +
                '}';
    }

    // 辅助方法：获取封面路径或默认值
    public String getCoverPathOrDefault(String defaultPath) {
        return coverPath != null && !coverPath.isEmpty() ? coverPath : defaultPath;
    }
}