# TrailDiary - 旅行日记App

一款精美的Android旅行日记应用，支持创建日记本、编写图文日记、自定义封面等功能。

---

## 功能特性

### 基本功能

#### 1. 引导页
- 首次运行显示App介绍
- 再次运行直接跳转首页
- 使用SharedPreferences实现状态记录

#### 2. 用户信息管理
- **注册功能**：支持用户名、密码、手机号等信息注册，包含有效性检查
- **登录功能**：账户验证，支持自动登录（SharedPreferences实现）
- **个人资料编辑**：修改签名、头像、性别、生日等信息（用户名不可修改）

#### 3. 日记信息管理
- **创建日记**：支持标题、内容、类别、插图等
- **编辑日记**：修改已发布的日记内容
- **删除日记**：删除指定日记并更新数据库
- **分类筛选**：国内游、国际游、亲子游、美食之旅、探险之旅、文化之旅
- **模糊搜索**：支持按作者、类别、标题等关键字检索
- **列表展示**：卡片式展示日记，显示封面、标题、作者、时间

#### 4. 日记本管理
- **创建日记本**：自定义名称和封面
- **编辑日记本**：修改名称、更换封面
- **删除日记本**：删除日记本及关联日记
- **排序功能**：按创建时间、更新时间、名称排序

### 拓展功能

#### 1. 自定义封面
- 日记本封面支持自定义图片
- 可从相册选择或拍照
- 预览已选择的封面图片

#### 2. 收藏日记
- 在日记详情页点击收藏按钮
- 已收藏日记显示填充心形图标
- 取消收藏功能

---

## 技术栈

| 技术 | 说明 |
|------|------|
| Java | 主要开发语言 |
| SQLite | 本地数据库存储 |
| SharedPreferences | 轻量级数据存储 |
| ViewPager | 封面滑动选择 |
| RecyclerView | 列表展示 |
| Glide | 图片加载框架 |
| FileProvider | 安全文件共享 |
| ThreeTenABP | 日期时间处理 |

---

## 项目结构

```
app/src/main/java/com/example/traildiary/
├── activity/                    # Activity页面
│   ├── MainActivity.java        # 主页面
│   ├── LoginActivity.java       # 登录页面
│   ├── RegisterActivity.java    # 注册页面
│   ├── WriteDiaryActivity.java  # 写日记页面
│   ├── DiaryDetailActivity.java # 日记详情页面
│   ├── NotebookSettingActivity.java # 日记本设置页面
│   ├── NotebookDetailActivity.java  # 日记本详情页面
│   └── ProfileEditActivity.java     # 个人资料编辑页面
│
├── adapter/                     # 适配器
│   ├── NotebookAdapter.java     # 日记本列表适配器
│   ├── DiaryListAdapter.java    # 日记列表适配器
│   ├── CoverPagerAdapter.java   # 封面选择适配器
│   └── DiaryContentAdapter.java # 日记内容适配器
│
├── database/                    # 数据库
│   ├── DatabaseHelper.java      # 数据库帮助类
│   ├── UserDAO.java             # 用户数据访问对象
│   ├── DiaryDAO.java            # 日记数据访问对象
│   ├── NotebookDAO.java         # 日记本数据访问对象
│   └── FavoriteDAO.java         # 收藏数据访问对象（拓展功能）
│
├── model/                       # 数据模型
│   ├── User.java                # 用户模型
│   ├── Diary.java               # 日记模型
│   ├── Notebook.java            # 日记本模型
│   └── DiaryContentItem.java    # 日记内容项模型
│
├── fragment/                    # Fragment
│   ├── HomeFragment.java        # 首页Fragment
│   └── NotebookFragment.java    # 日记本Fragment
│
├── utils/                       # 工具类
│   ├── ImageUtil.java           # 图片处理工具
│   ├── DateUtil.java            # 日期工具
│   ├── ValidationUtil.java      # 验证工具
│   ├── SharedPreferencesUtil.java # SP工具
│   └── Constants.java           # 常量定义
│
└── widget/                      # 自定义控件
    └── SearchDialog.java        # 搜索对话框
```

---

## 数据库设计

### 用户表 (user)
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | INTEGER | 主键，自增 |
| nickname | TEXT | 用户昵称（唯一） |
| trail_number | TEXT | 足迹号（唯一） |
| password | TEXT | 密码 |
| phone | TEXT | 手机号 |
| avatar | TEXT | 头像路径 |
| signature | TEXT | 个性签名 |
| gender | TEXT | 性别 |
| birthday | TEXT | 生日 |
| create_time | TEXT | 创建时间 |

### 日记本表 (notebook)
| 字段 | 类型 | 说明 |
|------|------|------|
| notebook_id | INTEGER | 主键，自增 |
| notebook_name | TEXT | 日记本名称 |
| cover | TEXT | 封面路径 |
| user_id | INTEGER | 用户ID（外键） |
| diary_count | INTEGER | 日记数量 |
| sort_order | INTEGER | 排序顺序 |
| create_time | TEXT | 创建时间 |

### 日记表 (diary)
| 字段 | 类型 | 说明 |
|------|------|------|
| diary_id | INTEGER | 主键，自增 |
| title | TEXT | 标题 |
| content | TEXT | 内容 |
| category | TEXT | 类别 |
| author_id | INTEGER | 作者ID（外键） |
| author_name | TEXT | 作者名称 |
| cover_image_path | TEXT | 封面图片路径 |
| images | TEXT | 插图列表（JSON） |
| notebook_id | INTEGER | 日记本ID（外键） |
| is_draft | INTEGER | 是否草稿 |
| create_time_diary | TEXT | 创建时间 |
| update_time | TEXT | 更新时间 |
| like_count | INTEGER | 点赞数 |
| view_count | INTEGER | 浏览数 |

### 收藏表 (favorite) - 拓展功能
| 字段 | 类型 | 说明 |
|------|------|------|
| favorite_id | INTEGER | 主键，自增 |
| user_id | INTEGER | 用户ID（外键） |
| diary_id | INTEGER | 日记ID（外键） |
| favorite_time | TEXT | 收藏时间 |

---

## 使用说明

### Mac系统

#### 1. 环境准备
```bash
# 安装Android Studio
# 下载地址：https://developer.android.com/studio

# 确保已安装JDK 11+
java -version
```

#### 2. 打开项目
1. 启动Android Studio
2. 选择 `Open` → 定位到项目目录
3. 等待Gradle同步完成

#### 3. 运行预览
**模拟器方式：**
1. `Tools` → `Device Manager` → `Create Device`
2. 选择设备（如Pixel 4）→ 选择系统镜像（如API 30）
3. 点击运行按钮 ▶️

**真机方式：**
1. 手机开启USB调试
2. 连接手机到电脑
3. 点击运行按钮 ▶️

#### 4. 打包APK
```bash
# Debug版本
./gradlew assembleDebug
# APK位置：app/build/outputs/apk/debug/app-debug.apk

# Release版本（需要签名）
./gradlew assembleRelease
```

### Windows系统

#### 1. 环境准备
```powershell
# 安装Android Studio
# 下载地址：https://developer.android.com/studio

# 确保已安装JDK 11+
java -version
```

#### 2. 打开项目
1. 启动Android Studio
2. 选择 `Open` → 定位到项目目录
3. 等待Gradle同步完成

#### 3. 运行预览
与Mac系统相同

#### 4. 打包APK
```powershell
# Debug版本
.\gradlew.bat assembleDebug

# Release版本（需要签名）
.\gradlew.bat assembleRelease
```

---

## 截图预览

> 请在运行App后自行截图添加

---

## 注意事项

1. **权限要求**：App需要相机和存储权限才能使用拍照和图片选择功能
2. **Android版本**：最低支持Android 6.0 (API 23)
3. **数据存储**：所有数据存储在本地SQLite数据库中

---

## 作者信息

- **微信**: 1837620622（传康kk）
- **邮箱**: 2040168455@qq.com
- **咸鱼/B站**: 万能程序员

---

## 许可证

本项目仅供学习交流使用，请勿用于商业用途。
