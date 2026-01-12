package com.example.traildiary.model;

import org.threeten.bp.LocalDateTime; // 替换Date为LocalDateTime
import java.util.List;

/**
 * 日记实体类，用于存储日记相关信息
 */
public class Diary {
    private int diaryId;
    private String avatar;      // 作者头像路径
    private int authorId;       // 作者ID
    private String authorName;  // 作者昵称
    private String title;       // 日记标题
    private String content;     // 日记内容
    private String category;    // 日记类别（国内游、亲子游、美食之旅、国际游等）
    private String coverImagePath; // 封面图片路径（修正命名：明确为单张封面图）
    private List<String> images;   // 插图列表（多图路径）
    private int notebookId;     // 所属日记本ID
    private boolean isDraft;    // 是否为草稿（true-是，false-否）
    private LocalDateTime createTime; // 创建时间（使用不可变的LocalDateTime替代Date）
    private LocalDateTime updateTime; // 更新时间（使用不可变的LocalDateTime替代Date）
    private int likeCount;      // 点赞数
    private int viewCount;      // 浏览数

    private String contentItemsJson; // 存储 DiaryContentItem 列表的 JSON

    // 无参构造
    public Diary() {
    }

    // 带常用属性的构造方法（方便对象初始化）
    public Diary(int authorId, String title, String content, String category, int notebookId, boolean isDraft) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.notebookId = notebookId;
        this.isDraft = isDraft;
    }

    // Getters and Setters

    public int getDiaryId() {
        return diaryId;
    }

    public void setDiaryId(int diaryId) {
        this.diaryId = diaryId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // 修正封面图路径的getter/setter（对应命名修改）
    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public int getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(int notebookId) {
        this.notebookId = notebookId;
    }

    // 修正boolean类型的getter（符合JavaBean规范：boolean类型用isXxx()）
    public boolean isDraft() {
        return isDraft;
    }

    public void setDraft(boolean draft) {
        isDraft = draft;
    }

    // 时间类型修改为LocalDateTime的getter/setter


    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * 获取内容摘要（前100个字符）
     * @return 内容摘要，若内容为null则返回空字符串
     */
    public String getSummary() {
        if (content == null) {
            return ""; // 避免返回null，减少空指针风险
        }
        if (content.length() > 100) {
            return content.substring(0, 100) + "...";
        }
        return content;
    }
    public String getContentItemsJson() { return contentItemsJson; }
    public void setContentItemsJson(String contentItemsJson) { this.contentItemsJson = contentItemsJson; }
}