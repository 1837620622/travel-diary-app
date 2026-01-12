package com.example.traildiary.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traildiary.R;
import com.example.traildiary.adapter.DiaryContentAdapter;
import com.example.traildiary.database.DiaryDAO;
import com.example.traildiary.database.UserDAO;
import com.example.traildiary.model.Diary;
import com.example.traildiary.model.User;
import com.example.traildiary.model.DiaryContentItem;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.ImageUtil;
import com.example.traildiary.utils.SharedPreferencesUtil;
import com.example.traildiary.utils.ValidationUtil;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class WriteDiaryActivity extends AppCompatActivity {

    private EditText etTitle;
    private TextView tvCategory;
    private RecyclerView rvContent;
    private DiaryContentAdapter contentAdapter;
    private List<DiaryContentItem> contentItems = new ArrayList<>();

    private ImageView ivBack, ivSave;
    private LinearLayout llAddContent;

    private int currentUserId;
    private int diaryId = -1; // -1表示新建，>0表示编辑现有日记
    private int notebookId = -1; // 所属日记本ID
    private boolean isEditMode = false;
    private boolean hasContent = false;

    private DiaryDAO diaryDAO;
    private UserDAO userDAO;
    private SharedPreferencesUtil spUtil;
    private String currentUserName; // 当前用户昵称

    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_TAKE_PHOTO = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary);

        initData();
        initViews();
        initContentList();

        // 编辑模式下加载日记数据
        loadDiaryDataIfNeeded();

        // 只有在非编辑模式下才检查草稿
        if (!isEditMode) {
            checkDraft();
        }
    }

    private void initData() {
        spUtil = SharedPreferencesUtil.getInstance(this);
        currentUserId = spUtil.getCurrentUserId();
        diaryDAO = new DiaryDAO(this);
        userDAO = new UserDAO(this);
        
        // 获取当前用户昵称
        User currentUser = userDAO.getUserById(currentUserId);
        if (currentUser != null && currentUser.getNickname() != null) {
            currentUserName = currentUser.getNickname();
        } else {
            currentUserName = "匿名用户";
        }

        Intent intent = getIntent();
        if (intent != null) {
            diaryId = intent.getIntExtra(Constants.INTENT_KEY_DIARY_ID, -1);
            notebookId = intent.getIntExtra(Constants.INTENT_KEY_NOTEBOOK_ID, -1);
            isEditMode = intent.getBooleanExtra(Constants.INTENT_KEY_IS_EDIT_MODE, false);

            // 如果有diaryId但没有设置isEditMode，自动设为编辑模式
            if (diaryId > 0 && !isEditMode) {
                isEditMode = true;
            }
        }
    }

    /**
     * 初始化完成后加载日记数据（编辑模式）
     */
    private void loadDiaryDataIfNeeded() {
        if (isEditMode && diaryId > 0) {
            loadDiaryData(diaryId);
        }
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        tvCategory = findViewById(R.id.tv_category);
        rvContent = findViewById(R.id.rv_content);
        ivBack = findViewById(R.id.iv_back);
        ivSave = findViewById(R.id.iv_save);
        llAddContent = findViewById(R.id.ll_add_content);

        // 设置标题提示
        etTitle.setHint(getString(R.string.diary_title_hint));

        // 返回按钮点击事件
        ivBack.setOnClickListener(v -> onBackPressed());

        // 保存按钮点击事件
        ivSave.setOnClickListener(v -> saveDiary());

        // 添加内容按钮点击事件
        llAddContent.setOnClickListener(v -> showAddContentDialog());

        // 分类选择
        tvCategory.setOnClickListener(v -> showCategoryDialog());

        // 监听内容变化
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                checkContentChanged();
            }
        });
    }

    private void initContentList() {
        contentAdapter = new DiaryContentAdapter(contentItems);
        contentAdapter.setOnItemClickListener(new DiaryContentAdapter.OnItemClickListener() {
            @Override
            public void onAddImageClick(int position) {
                showImagePicker(position);
            }

            @Override
            public void onDeleteClick(int position) {
                deleteContentItem(position);
            }
        });

        rvContent.setLayoutManager(new LinearLayoutManager(this));
        rvContent.setAdapter(contentAdapter);

        // 只有在非编辑模式下才添加空白内容项
        // 编辑模式下由loadDiaryData()负责填充内容
        if (!isEditMode) {
            addEmptyContentItem();
        }
    }

    /**
     * 加载日记数据（编辑模式）
     */
    private void loadDiaryData(int diaryId) {
        Diary diary = diaryDAO.getDiaryById(diaryId);
        if (diary != null) {
            // 设置标题和分类
            etTitle.setText(diary.getTitle());
            tvCategory.setText(getCategoryName(diary.getCategory()));

            // 保存日记本ID，用于更新时使用
            notebookId = diary.getNotebookId();
            isEditMode = true;

            // 清空内容列表
            contentItems.clear();

            // 解析文本内容
            String content = diary.getContent();
            if (content != null && !content.isEmpty()) {
                // 按段落分割内容
                String[] paragraphs = content.split("\n\n");
                for (String paragraph : paragraphs) {
                    if (!paragraph.trim().isEmpty()) {
                        DiaryContentItem textItem = DiaryContentItem.createTextItem(paragraph.trim());
                        contentItems.add(textItem);
                    }
                }
                hasContent = true;
            }

            // 加载图片 - 注意参数顺序：第一个是描述，第二个是图片路径
            List<String> images = diary.getImages();
            if (images != null && !images.isEmpty()) {
                for (String imagePath : images) {
                    if (imagePath != null && !imagePath.trim().isEmpty()) {
                        // createImageItem(description, imagePath) - 描述为空，图片路径为实际路径
                        DiaryContentItem imageItem = DiaryContentItem.createImageItem("", imagePath);
                        contentItems.add(imageItem);
                        hasContent = true;
                    }
                }
            }

            // 如果没有任何内容，添加一个空文本项
            if (contentItems.isEmpty()) {
                DiaryContentItem textItem = DiaryContentItem.createTextItem("");
                contentItems.add(textItem);
            }

            contentAdapter.notifyDataSetChanged();
        }
    }

    private void showCategoryDialog() {
        String[] categories = {
                getString(R.string.domestic_travel),
                getString(R.string.international_travel),
                getString(R.string.family_travel),
                getString(R.string.food_travel),
                "探险之旅",
                "文化之旅"
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.category)
                .setItems(categories, (dialog, which) -> {
                    tvCategory.setText(categories[which]);
                    checkContentChanged();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAddContentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("添加内容")
                .setItems(new String[]{"文字", "图片"}, (dialog, which) -> {
                    if (which == 0) {
                        addTextContentItem();
                    } else {
                        addImageContentItem();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void addTextContentItem() {
        DiaryContentItem item = DiaryContentItem.createTextItem("");
        contentItems.add(item);
        contentAdapter.notifyItemInserted(contentItems.size() - 1);
        checkContentChanged();
    }

    private void addImageContentItem() {
        DiaryContentItem item = DiaryContentItem.createImageItem("", "");
        contentItems.add(item);
        contentAdapter.notifyItemInserted(contentItems.size() - 1);
        checkContentChanged();
    }

    private void addEmptyContentItem() {
        DiaryContentItem item = DiaryContentItem.createTextItem("");
        contentItems.add(item);
        contentAdapter.notifyItemInserted(0);
    }

    private void showImagePicker(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("选择图片")
                .setItems(new String[]{"拍照", "从相册选择"}, (dialog, which) -> {
                    if (which == 0) {
                        ImageUtil.takePhoto(this, REQUEST_TAKE_PHOTO);
                    } else {
                        ImageUtil.pickImageFromGallery(this, REQUEST_PICK_IMAGE);
                    }
                    // 保存位置信息，在onActivityResult中处理
                    spUtil.putInt("temp_image_position", position);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            int position = spUtil.getInt("temp_image_position", -1);
            if (position >= 0 && position < contentItems.size()) {
                DiaryContentItem item = contentItems.get(position);

                if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                    Uri imageUri = data.getData();
                    String imagePath = ImageUtil.getRealPathFromUri(this, imageUri);
                    try {
                        item.setImagePath(imagePath);
                        contentAdapter.notifyItemChanged(position);
                        checkContentChanged();
                    } catch (IllegalStateException e) {
                        Toast.makeText(this, "只能为图片类型设置图片路径", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == REQUEST_TAKE_PHOTO) {
                    // 处理拍照返回
                    // 这里需要获取拍照返回的图片路径
                    // 需要在takePhoto方法中保存文件路径
                }
            }
            spUtil.remove("temp_image_position");
        }
    }

    private void deleteContentItem(int position) {
        if (contentItems.size() > 1) {
            contentItems.remove(position);
            contentAdapter.notifyItemRemoved(position);
            checkContentChanged();
        } else {
            Toast.makeText(this, "至少保留一项内容", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkContentChanged() {
        boolean titleNotEmpty = !etTitle.getText().toString().trim().isEmpty();
        boolean categorySelected = !tvCategory.getText().toString().equals(getString(R.string.category));
        boolean hasItems = false;

        for (DiaryContentItem item : contentItems) {
            if (item.getType() == DiaryContentItem.Type.TEXT) {
                if (item.getContent() != null && !item.getContent().trim().isEmpty()) {
                    hasItems = true;
                    break;
                }
            } else if (item.getType() == DiaryContentItem.Type.IMAGE) {
                String imagePath = item.getImagePath();
                if (imagePath != null && !imagePath.trim().isEmpty()) {
                    hasItems = true;
                    break;
                }
            }
        }

        hasContent = titleNotEmpty || categorySelected || hasItems;
    }

    /**
     * 保存日记
     */
    private void saveDiary() {
        String title = etTitle.getText().toString().trim();
        String category = tvCategory.getText().toString();

        // 验证标题
        if (!ValidationUtil.isDiaryTitleValid(title)) {
            Toast.makeText(this, "请输入有效的日记标题（1-50个字符）", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证分类
        if (category.equals(getString(R.string.category))) {
            Toast.makeText(this, "请选择日记分类", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构建内容字符串，合并所有内容项（文本和图片描述）
        StringBuilder contentBuilder = new StringBuilder();
        for (DiaryContentItem item : contentItems) {
            if (item.getType() == DiaryContentItem.Type.TEXT) {
                // 文本内容
                if (item.getContent() != null && !item.getContent().trim().isEmpty()) {
                    if (contentBuilder.length() > 0) {
                        contentBuilder.append("\n\n");
                    }
                    contentBuilder.append(item.getContent().trim());
                }
            } else if (item.getType() == DiaryContentItem.Type.IMAGE) {
                // 图片描述也加入内容
                if (item.getContent() != null && !item.getContent().trim().isEmpty()) {
                    if (contentBuilder.length() > 0) {
                        contentBuilder.append("\n\n");
                    }
                    contentBuilder.append(item.getContent().trim());
                }
            }
        }
        String content = contentBuilder.toString();

        // 验证：必须插入至少一张图片
        boolean hasImages = false;
        for (DiaryContentItem item : contentItems) {
            if (item.getType() == DiaryContentItem.Type.IMAGE &&
                    item.getImagePath() != null && !item.getImagePath().trim().isEmpty()) {
                hasImages = true;
                break;
            }
        }

        if (!hasImages) {
            Toast.makeText(this, "请至少添加一张图片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构建图片路径列表
        List<String> imagePaths = new ArrayList<>();
        for (DiaryContentItem item : contentItems) {
            if (item.getType() == DiaryContentItem.Type.IMAGE &&
                    item.getImagePath() != null && !item.getImagePath().trim().isEmpty()) {
                imagePaths.add(item.getImagePath());
            }
        }

        // 创建Diary对象
        Diary diary = new Diary();
        diary.setTitle(title);
        diary.setContent(content);
        diary.setCategory(String.valueOf(getCategoryCode(category)));
        diary.setImages(imagePaths);
        diary.setAuthorId(currentUserId);
        diary.setAuthorName(currentUserName); // 设置作者名称
        diary.setNotebookId(notebookId > 0 ? notebookId : 1);
        diary.setUpdateTime(LocalDateTime.now());

        boolean success;
        if (isEditMode && diaryId > 0) {
            // 编辑模式：更新日记
            diary.setDiaryId(diaryId);
            int result = diaryDAO.updateDiary(diary);
            success = result > 0;
        } else {
            // 新建模式：添加日记
            diary.setCreateTime(LocalDateTime.now());
            long result = diaryDAO.addDiary(diary);
            success = result > 0;
        }

        if (success) {
            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
            clearDraft();
            finish();
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkDraft() {
        String draftTitle = spUtil.getString("draft_title", "");
        String draftCategory = spUtil.getString("draft_category", "");

        if (!draftTitle.isEmpty() || !draftCategory.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("恢复草稿")
                    .setMessage("是否基于上次存储在草稿箱里的继续写？")
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        etTitle.setText(draftTitle);
                        if (!draftCategory.isEmpty()) {
                            tvCategory.setText(draftCategory);
                        }
                        // 这里还可以恢复更多内容
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                        clearDraft();
                    })
                    .show();
        }
    }

    private void saveDraft() {
        String title = etTitle.getText().toString().trim();
        String category = tvCategory.getText().toString();

        spUtil.putString("draft_title", title);
        spUtil.putString("draft_category",
                category.equals(getString(R.string.category)) ? "" : category);
    }

    private void clearDraft() {
        spUtil.remove("draft_title");
        spUtil.remove("draft_category");
    }

    @Override
    public void onBackPressed() {
        if (hasContent) {
            showSaveDraftDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showSaveDraftDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.save_draft_prompt)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    saveDraft();
                    finish();
                })
                .setNegativeButton(R.string.dont_save, (dialog, which) -> {
                    clearDraft();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private int getCategoryCode(String categoryName) {
        switch (categoryName) {
            case "国内游": return Constants.DIARY_CATEGORY_DOMESTIC;
            case "国际游": return Constants.DIARY_CATEGORY_INTERNATIONAL;
            case "亲子游": return Constants.DIARY_CATEGORY_FAMILY;
            case "美食之旅": return Constants.DIARY_CATEGORY_FOOD;
            case "探险之旅": return Constants.DIARY_CATEGORY_ADVENTURE;
            case "文化之旅": return Constants.DIARY_CATEGORY_CULTURE;
            default: return Constants.DIARY_CATEGORY_DOMESTIC;
        }
    }

    private String getCategoryName(String categoryCode) {
        try {
            int code = Integer.parseInt(categoryCode);
            switch (code) {
                case Constants.DIARY_CATEGORY_DOMESTIC: return "国内游";
                case Constants.DIARY_CATEGORY_INTERNATIONAL: return "国际游";
                case Constants.DIARY_CATEGORY_FAMILY: return "亲子游";
                case Constants.DIARY_CATEGORY_FOOD: return "美食之旅";
                case Constants.DIARY_CATEGORY_ADVENTURE: return "探险之旅";
                case Constants.DIARY_CATEGORY_CULTURE: return "文化之旅";
                default: return "国内游";
            }
        } catch (NumberFormatException e) {
            return categoryCode; // 如果已经是字符串，直接返回
        }
    }
}