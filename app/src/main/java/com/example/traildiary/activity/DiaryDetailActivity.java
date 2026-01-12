package com.example.traildiary.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.traildiary.R;
import com.example.traildiary.database.DiaryDAO;
import com.example.traildiary.database.FavoriteDAO;
import com.example.traildiary.database.NotebookDAO;
import com.example.traildiary.database.UserDAO;
import com.example.traildiary.model.Diary;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.DateUtil;
import com.example.traildiary.utils.SharedPreferencesUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DiaryDetailActivity extends AppCompatActivity {

    private ImageView ivBack, ivAvatar, ivMore, ivFavorite;
    private TextView tvUsername, tvTitle, tvCategory, tvEditTime;
    private LinearLayout llImages;
    private TextView tvContent;

    private int diaryId;
    private Diary diary;
    private DiaryDAO diaryDAO;
    private UserDAO userDAO;
    private FavoriteDAO favoriteDAO; // 收藏功能
    private SharedPreferencesUtil spUtil;
    private boolean isFavorite = false; // 是否已收藏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        initData();
        initViews();
        loadDiaryData();
    }

    private void initData() {
        spUtil = SharedPreferencesUtil.getInstance(this);
        diaryDAO = new DiaryDAO(this);
        userDAO = new UserDAO(this);
        favoriteDAO = new FavoriteDAO(this); // 初始化收藏DAO

        Intent intent = getIntent();
        if (intent != null) {
            diaryId = intent.getIntExtra(Constants.INTENT_KEY_DIARY_ID, -1);
        }
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivAvatar = findViewById(R.id.iv_avatar);
        ivMore = findViewById(R.id.iv_more);
        ivFavorite = findViewById(R.id.iv_favorite); // 收藏按钮
        tvUsername = findViewById(R.id.tv_username);
        tvTitle = findViewById(R.id.tv_title);
        tvCategory = findViewById(R.id.tv_category);
        tvEditTime = findViewById(R.id.tv_edit_time);
        llImages = findViewById(R.id.ll_images);
        tvContent = findViewById(R.id.tv_content);

        // 设置当前用户信息
        String nickname = spUtil.getCurrentUserNickname();
        tvUsername.setText(nickname);

        // 加载用户头像
        loadUserAvatar();

        // 返回按钮
        ivBack.setOnClickListener(v -> finish());

        // 更多菜单
        ivMore.setOnClickListener(v -> showMoreMenu());
        
        // 收藏按钮点击事件
        ivFavorite.setOnClickListener(v -> toggleFavorite());
    }

    //加载用户头像
    private void loadUserAvatar() {
        int userId = spUtil.getCurrentUserId();
        if (userId > 0 && userDAO != null) {
            byte[] avatarBytes = userDAO.getUserAvatarBytes(userId);
            if (avatarBytes != null && avatarBytes.length > 0) {
                Glide.with(this)
                        .asBitmap()
                        .load(avatarBytes)
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(ivAvatar);
            } else {
                // 使用默认头像
                Glide.with(this)
                        .load(R.drawable.ic_default_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivAvatar);
            }
        }
    }

    private void loadDiaryData() {
        if (diaryId > 0) {
            diary = diaryDAO.getDiaryById(diaryId);
            if (diary != null) {
                displayDiaryData();
                // 检查收藏状态
                checkFavoriteStatus();
            } else {
                Toast.makeText(this, "日记不存在", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    // ------- 收藏功能相关方法（拓展功能） -------
    
    /**
     * 检查收藏状态
     */
    private void checkFavoriteStatus() {
        int userId = spUtil.getCurrentUserId();
        isFavorite = favoriteDAO.isFavorite(userId, diaryId);
        updateFavoriteIcon();
    }
    
    /**
     * 更新收藏图标状态
     */
    private void updateFavoriteIcon() {
        if (isFavorite) {
            ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
            ivFavorite.setColorFilter(getResources().getColor(R.color.colorAccent));
        } else {
            ivFavorite.setImageResource(R.drawable.ic_favorite_border);
            ivFavorite.setColorFilter(getResources().getColor(R.color.text_hint));
        }
    }
    
    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        int userId = spUtil.getCurrentUserId();
        
        if (isFavorite) {
            // 取消收藏
            int result = favoriteDAO.removeFavorite(userId, diaryId);
            if (result > 0) {
                isFavorite = false;
                updateFavoriteIcon();
                Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 添加收藏
            long result = favoriteDAO.addFavorite(userId, diaryId);
            if (result > 0) {
                isFavorite = true;
                updateFavoriteIcon();
                Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //显示更多菜单

    private void displayDiaryData() {
        // 设置标题
        tvTitle.setText(diary.getTitle());

        // 设置分类
        String category = getCategoryName(diary.getCategory());
        tvCategory.setText(category);

        // 设置编辑时间
        String editTime = DateUtil.getEditTimeString(diary.getUpdateTime());
        tvEditTime.setText(editTime);

        // 设置内容
        String content = diary.getContent();
        if (content != null && !content.isEmpty()) {
            tvContent.setText(content);
            tvContent.setVisibility(View.VISIBLE);
        }

        // 显示图片（如果有）
        List<String> images = diary.getImages();
        if (images != null && !images.isEmpty()) {
            displayImages(images);
        }
    }

    private void displayImages(List<String> imagePaths) {
        llImages.removeAllViews();

        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }

        for (String imagePath : imagePaths) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400
            );
            params.setMargins(0, 0, 0, 16);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            // 使用Glide加载图片，添加错误处理和fallback
            RequestBuilder<Drawable> requestBuilder;

            if (isFileAccessible(imagePath)) {
                // 如果文件可以直接访问
                requestBuilder = Glide.with(this)
                        .load(new File(imagePath));
            } else {
                // 如果文件无法直接访问，尝试通过ContentResolver加载
                requestBuilder = Glide.with(this)
                        .load(Uri.fromFile(new File(imagePath)));
            }

            requestBuilder
                    .placeholder(R.drawable.ic_default_cover)
                    .error(R.drawable.ic_default_cover)
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .skipMemoryCache(false))
                    .into(imageView);

            llImages.addView(imageView);
        }
    }

    //检查文件是否可以直接访问
    private boolean isFileAccessible(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }

        // 检查文件是否在应用专属目录中
        if (isFileInAppDirectory(filePath)) {
            return true;
        }

        // 对于Android 10+，检查是否有权限访问外部存储
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return false; // Android 10+ 默认无法直接访问外部存储
        }

        // 对于Android 10以下，检查读写权限
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    //检查文件是否在应用专属目录中
    private boolean isFileInAppDirectory(String filePath) {
        File file = new File(filePath);
        File appDir = getExternalFilesDir(null);

        if (appDir == null) {
            return false;
        }

        try {
            String fileCanonicalPath = file.getCanonicalPath();
            String appDirCanonicalPath = appDir.getCanonicalPath();

            return fileCanonicalPath.startsWith(appDirCanonicalPath) ||
                    fileCanonicalPath.startsWith(getFilesDir().getCanonicalPath()) ||
                    fileCanonicalPath.startsWith(getCacheDir().getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showMoreMenu() {
        PopupMenu popupMenu = new PopupMenu(this, ivMore);
        popupMenu.getMenuInflater().inflate(R.menu.menu_diary_detail, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                editDiary();
                return true;
            } else if (itemId == R.id.menu_delete) {
                deleteDiary();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    //编辑日记 - 跳转到写日记页面进行编辑
    private void editDiary() {
        if (diary == null || diaryId <= 0) {
            Toast.makeText(this, "日记数据异常，无法编辑", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, WriteDiaryActivity.class);
        intent.putExtra(Constants.INTENT_KEY_DIARY_ID, diaryId);
        intent.putExtra(Constants.INTENT_KEY_NOTEBOOK_ID, diary.getNotebookId());
        intent.putExtra(Constants.INTENT_KEY_IS_EDIT_MODE, true);
        startActivity(intent);
        finish();
    }

    private void deleteDiary() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这篇日记吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    int deleteRows = diaryDAO.deleteDiary(diaryId);
                    boolean success = deleteRows > 0;

                    if (success) {
                        Toast.makeText(this, "日记删除成功", Toast.LENGTH_SHORT).show();

                        // 更新日记本中的日记数量
                        int notebookId = diary.getNotebookId();
                        if (notebookId > 0) {
                            NotebookDAO notebookDAO = new NotebookDAO(this);
                            notebookDAO.updateNotebookDiaryCount(notebookId);
                        }

                        finish();
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }


    // 将int常量转换为String进行比较
    private String getCategoryName(String categoryCode) {
        if (categoryCode == null) {
            return "国内游";
        }

        // 将int常量转换为String进行比较
        switch (categoryCode) {
            case "1": // 国内游
                return "国内游";
            case "2": // 国际游
                return "国际游";
            case "3": // 亲子游
                return "亲子游";
            case "4": // 美食之旅
                return "美食之旅";
            case "5": // 探险之旅
                return "探险之旅";
            case "6": // 文化之旅
                return "文化之旅";
            default:
                return "国内游";
        }
    }
}