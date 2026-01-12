package com.example.traildiary.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import java.io.File;

import com.example.traildiary.R;
import com.example.traildiary.adapter.CoverPagerAdapter;
import com.example.traildiary.database.NotebookDAO;
import com.example.traildiary.model.Notebook;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.ImageUtil;
import com.example.traildiary.utils.SharedPreferencesUtil;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class NotebookSettingActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitle;
    private ViewPager viewPager;
    private EditText etNotebookName;
    private Button btnSave;
    private Button btnDelete;
    private Button btnSort;

    private Notebook notebook;
    private CoverPagerAdapter coverAdapter;
    private NotebookDAO notebookDAO;
    private int currentUserId;
    private String selectedCoverPath;
    private List<String> coverPaths;
    
    // ------- 图片选择相关常量和变量 -------
    private static final int REQUEST_PICK_IMAGE = 2001;
    private static final int REQUEST_TAKE_PHOTO = 2002;
    private File photoFile; // 拍照时保存的文件
    private String customCoverImagePath; // 自定义封面图片路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook_setting);

        initView();
        initData();
        loadCoverOptions();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        viewPager = findViewById(R.id.viewPager);
        etNotebookName = findViewById(R.id.et_notebook_name);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        btnSort = findViewById(R.id.btn_sort);

        // 设置标题
        int notebookId = getIntent().getIntExtra(Constants.INTENT_KEY_NOTEBOOK_ID, -1);
        if (notebookId == -1) {
            // 新建日记本
            tvTitle.setText("新建日记本");
            btnDelete.setVisibility(View.GONE);
            btnSort.setVisibility(View.GONE);
        } else {
            // 编辑日记本
            tvTitle.setText("日记本设置");
        }

        // 设置点击事件
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveNotebook());

        btnDelete.setOnClickListener(v -> deleteNotebook());

        btnSort.setOnClickListener(v -> showSortOptions());
    }

    private void initData() {
        // 获取当前用户ID
        SharedPreferencesUtil spUtil = SharedPreferencesUtil.getInstance(this);
        currentUserId = spUtil.getCurrentUserId();

        // 初始化数据库DAO
        notebookDAO = new NotebookDAO(this);

        // 加载日记本数据
        int notebookId = getIntent().getIntExtra(Constants.INTENT_KEY_NOTEBOOK_ID, -1);
        if (notebookId != -1) {
            notebook = notebookDAO.getNotebookById(notebookId);
            if (notebook != null) {
                etNotebookName.setText(notebook.getName());
                selectedCoverPath = notebook.getCoverPath();
            }
        } else {
            // 新建日记本
            notebook = new Notebook();
            notebook.setUserId(currentUserId);
            notebook.setCreateTime(LocalDateTime.now());
            notebook.setDiaryCount(0);
        }
    }

    private void loadCoverOptions() {
        // 初始化封面列表
        coverPaths = new ArrayList<>();

        // 第一项：自定义封面选项（点击后可拍照或从相册选择）
        coverPaths.add(CoverPagerAdapter.CUSTOM_COVER);
        
        // 添加预置封面（使用简单的数字标识）
        coverPaths.add("1");
        coverPaths.add("2");
        coverPaths.add("3");
        coverPaths.add("4");
        coverPaths.add("5");

        // 创建适配器
        coverAdapter = new CoverPagerAdapter(this, coverPaths);
        viewPager.setAdapter(coverAdapter);

        // 设置页面间距，让相邻页面可见
        viewPager.setPageMargin(16);
        viewPager.setOffscreenPageLimit(3);

        // 设置页面切换监听，滑动时自动更新选中的封面
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // 更新选中的封面
                coverAdapter.setSelectedPosition(position);
                selectedCoverPath = coverPaths.get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // 设置封面选择监听（点击时触发）
        coverAdapter.setOnCoverClickListener(coverPath -> {
            selectedCoverPath = coverPath;
            if (CoverPagerAdapter.isCustomCover(coverPath)) {
                // 自定义封面，不显示Toast，等待回调处理
            } else {
                Toast.makeText(this, "封面已选择", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 设置自定义封面点击监听（弹出拍照/相册选择对话框）
        coverAdapter.setOnCustomCoverClickListener(this::showImagePicker);

        // 根据现有封面路径选中对应的封面
        String existingCoverPath = notebook.getCoverPath();
        int position = 0; // 默认选中"自定义"
        
        if (existingCoverPath != null && !existingCoverPath.isEmpty()) {
            // 检查是否为自定义封面（路径以/开头表示是文件路径）
            if (CoverPagerAdapter.isCustomCover(existingCoverPath) || existingCoverPath.startsWith("/")) {
                position = 0;
                // 如果是文件路径，设置自定义封面图片
                if (existingCoverPath.startsWith("/")) {
                    customCoverImagePath = existingCoverPath;
                    coverAdapter.setCustomCoverImagePath(customCoverImagePath);
                }
            } else {
                // 尝试直接匹配数字（需要+1因为第一项是自定义）
                int index = coverPaths.indexOf(existingCoverPath);
                if (index != -1) {
                    position = index;
                } else {
                    // 尝试从旧格式中提取数字
                    String numStr = existingCoverPath.replaceAll("[^0-9]", "");
                    if (!numStr.isEmpty()) {
                        try {
                            int num = Integer.parseInt(numStr);
                            if (num >= 1 && num <= 5) {
                                position = num; // 数字1对应索引1（因为索引0是自定义）
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        
        viewPager.setCurrentItem(position);
        coverAdapter.setSelectedPosition(position);
        selectedCoverPath = coverPaths.get(position);
    }
    
    // ------- 图片选择相关方法 -------
    
    /**
     * 显示图片选择对话框（拍照/从相册选择）
     */
    private void showImagePicker() {
        new AlertDialog.Builder(this)
                .setTitle("选择封面图片")
                .setItems(new String[]{"拍照", "从相册选择"}, (dialog, which) -> {
                    if (which == 0) {
                        // 拍照
                        photoFile = ImageUtil.takePhoto(this, REQUEST_TAKE_PHOTO);
                    } else {
                        // 从相册选择
                        ImageUtil.pickImageFromGallery(this, REQUEST_PICK_IMAGE);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            String imagePath = null;
            
            if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // 从相册选择
                Uri imageUri = data.getData();
                imagePath = ImageUtil.getRealPathFromUri(this, imageUri);
            } else if (requestCode == REQUEST_TAKE_PHOTO && photoFile != null) {
                // 拍照
                imagePath = photoFile.getAbsolutePath();
            }
            
            // 设置自定义封面图片
            if (imagePath != null && !imagePath.isEmpty()) {
                customCoverImagePath = imagePath;
                selectedCoverPath = CoverPagerAdapter.CUSTOM_COVER;
                coverAdapter.setCustomCoverImagePath(customCoverImagePath);
                Toast.makeText(this, "封面图片已选择", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveNotebook() {
        String notebookName = etNotebookName.getText().toString().trim();

        if (TextUtils.isEmpty(notebookName)) {
            etNotebookName.setError("请输入日记本名称");
            etNotebookName.requestFocus();
            return;
        }

        if (notebookName.length() > 20) {
            etNotebookName.setError("日记本名称不能超过20个字符");
            etNotebookName.requestFocus();
            return;
        }

        // 检查日记本名称是否重复（排除当前日记本）
        if (notebookDAO.isNotebookNameExists(notebookName, currentUserId)
                && (notebook.getId() == 0 || !notebook.getName().equals(notebookName))) {
            etNotebookName.setError("日记本名称已存在");
            etNotebookName.requestFocus();
            return;
        }

        // 设置笔记本信息
        notebook.setName(notebookName);
        
        // 处理封面路径：根据选择类型设置不同的封面
        String finalCoverPath = selectedCoverPath;
        int coverResId = 0;
        
        if (CoverPagerAdapter.isCustomCover(selectedCoverPath)) {
            // 自定义封面：使用用户选择的图片路径
            if (customCoverImagePath != null && !customCoverImagePath.isEmpty()) {
                finalCoverPath = customCoverImagePath;
                coverResId = 0; // 自定义图片不使用资源ID
            } else {
                // 未选择自定义图片，随机选择一个预置封面
                finalCoverPath = CoverPagerAdapter.getRandomCoverPath();
                coverResId = CoverPagerAdapter.getCoverResourceId(finalCoverPath);
            }
        } else {
            // 预置封面：使用对应的资源ID
            coverResId = CoverPagerAdapter.getCoverResourceId(finalCoverPath);
        }
        
        notebook.setCoverPath(finalCoverPath);
        
        // 添加调试日志
        android.util.Log.d("NotebookSetting", "保存笔记本: name=" + notebookName 
                + ", selectedCoverPath=" + selectedCoverPath
                + ", finalCoverPath=" + finalCoverPath
                + ", isCustom=" + (customCoverImagePath != null));
        
        // 设置封面资源ID
        notebook.setCoverResId(coverResId);
        
        android.util.Log.d("NotebookSetting", "coverResId=" + coverResId);
        
        notebook.setUpdateTime(LocalDateTime.now());

        long result;
        if (notebook.getId() == 0) {
            // 新建日记本
            result = notebookDAO.addNotebook(notebook);
            if (result > 0) {
                Toast.makeText(this, "日记本创建成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "日记本创建失败", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // 更新日记本
            int updateResult = notebookDAO.updateNotebook(notebook);
            if (updateResult > 0) {
                Toast.makeText(this, "日记本更新成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "日记本更新失败", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 返回上一页
        setResult(RESULT_OK);
        finish();
    }

    private void deleteNotebook() {
        new AlertDialog.Builder(this)
                .setTitle("删除日记本")
                .setMessage("确定要删除这个日记本吗？删除后所有日记将移到默认日记本。")
                .setPositiveButton("删除", (dialog, which) -> {
                    int result = notebookDAO.deleteNotebook(notebook.getId());
                    if (result > 0) {
                        Toast.makeText(this, "日记本已删除", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showSortOptions() {
        final String[] sortOptions = {"按创建时间", "按更新时间", "按名称", "自定义排序"};

        new AlertDialog.Builder(this)
                .setTitle("排序方式")
                .setItems(sortOptions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // 按创建时间排序
                            notebook.setSortOrder(1);
                            break;
                        case 1:
                            // 按更新时间排序
                            notebook.setSortOrder(2);
                            break;
                        case 2:
                            // 按名称排序
                            notebook.setSortOrder(3);
                            break;
                        case 3:
                            // 自定义排序
                            notebook.setSortOrder(0);
                            break;
                    }

                    // 保存排序设置
                    int result = notebookDAO.updateNotebook(notebook);
                    if (result > 0) {
                        Toast.makeText(this, "排序方式已更新", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "排序方式更新失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}