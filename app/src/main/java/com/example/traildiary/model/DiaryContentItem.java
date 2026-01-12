package com.example.traildiary.model;

public class DiaryContentItem {
    public enum Type {
        TEXT, IMAGE
    }

    private Type type;
    private String content;        // 文字内容或图片描述
    private String imagePath;      // 图片路径，仅当 type == IMAGE 时有效

    // 私有构造方法
    private DiaryContentItem(Type type, String content, String imagePath) {
        this.type = type;
        this.content = content;
        this.imagePath = imagePath;
    }

    // 工厂方法：创建文本项（允许空内容，用于初始化空白编辑区域）
    public static DiaryContentItem createTextItem(String content) {
        return new DiaryContentItem(Type.TEXT, content != null ? content : "", null);
    }

    // 工厂方法：创建图片项（允许空路径，用于初始化空白图片占位）
    public static DiaryContentItem createImageItem(String description, String imagePath) {
        return new DiaryContentItem(Type.IMAGE, description != null ? description : "", imagePath != null ? imagePath : "");
    }

    // 获取类型
    public Type getType() {
        return type;
    }

    // 获取内容（文本内容或图片描述）
    public String getContent() {
        return content;
    }

    // 设置内容
    public void setContent(String content) {
        this.content = content != null ? content : "";
    }

    // 获取图片路径
    public String getImagePath() {
        return imagePath;
    }

    // 设置图片路径
    public void setImagePath(String imagePath) {
        if (type != Type.IMAGE) {
            throw new IllegalStateException("只有图片类型才能设置图片路径");
        }
        this.imagePath = imagePath != null ? imagePath : "";
    }

    @Override
    public String toString() {
        return "DiaryContentItem{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}