package com.example.traildiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.traildiary.R;
import com.example.traildiary.adapter.CoverPagerAdapter;
import com.example.traildiary.adapter.DiaryListAdapter;
import com.example.traildiary.adapter.NotebookAdapter;
import com.example.traildiary.database.DiaryDAO;
import com.example.traildiary.database.FavoriteDAO;
import com.example.traildiary.database.NotebookDAO;
import com.example.traildiary.database.UserDAO;
import com.example.traildiary.model.Diary;
import com.example.traildiary.model.Notebook;
import com.example.traildiary.model.User;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ImageView ivBack, ivCardAvatar;
    private TextView tvEdit, tvCardNickname, tvCardTrailNumber, tvCardSignature;
    private TextView tvDiaryCount, tvDiaryNumber;
    private TextView tabDiary, tabNotebook, tabFavorite;
    private View indicatorDiary, indicatorNotebook, indicatorFavorite;
    private RecyclerView rvContent;
    private ViewPager viewPager;

    private SharedPreferencesUtil spUtil;
    private UserDAO userDAO;
    private DiaryDAO diaryDAO;
    private NotebookDAO notebookDAO;
    private FavoriteDAO favoriteDAO;

    private DiaryListAdapter diaryAdapter;
    private NotebookAdapter notebookAdapter;
    private CoverPagerAdapter coverPagerAdapter;

    private List<Diary> diaryList = new ArrayList<>();
    private List<Diary> favoriteList = new ArrayList<>();
    private List<Notebook> notebookList = new ArrayList<>();
    private List<String> coverImages = new ArrayList<>();

    // 0: 日记, 1: 日记本, 2: 收藏
    private int currentTab = 0;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initData();
        setupListeners();
        loadUserData();
        loadDiaryData();
        loadNotebookData();
        // 设置默认显示日记标签
        switchToDiaryTab();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvEdit = findViewById(R.id.tv_edit);
        ivCardAvatar = findViewById(R.id.iv_card_avatar);
        tvCardNickname = findViewById(R.id.tv_card_nickname);
        tvCardTrailNumber = findViewById(R.id.tv_card_trail_number);
        tvCardSignature = findViewById(R.id.tv_card_signature);
        tabDiary = findViewById(R.id.tab_diary);
        tabNotebook = findViewById(R.id.tab_notebook);
        tabFavorite = findViewById(R.id.tab_favorite);
        indicatorDiary = findViewById(R.id.indicator_diary);
        indicatorNotebook = findViewById(R.id.indicator_notebook);
        indicatorFavorite = findViewById(R.id.indicator_favorite);
        tvDiaryCount = findViewById(R.id.tv_diary_count);
        tvDiaryNumber = findViewById(R.id.tv_diary_number);
        rvContent = findViewById(R.id.rv_content);
        viewPager = findViewById(R.id.viewPager);
    }

    private void initData() {
        spUtil = SharedPreferencesUtil.getInstance(this);

        // 初始化DAO
        userDAO = new UserDAO(this);
        diaryDAO = new DiaryDAO(this);
        notebookDAO = new NotebookDAO(this);
        favoriteDAO = new FavoriteDAO(this);

        currentUserId = spUtil.getCurrentUserId();

        // 初始化日记适配器
        diaryAdapter = new DiaryListAdapter(this, diaryList);
        diaryAdapter.setOnItemClickListener(diary -> {
            // 点击日记项的处理 - 跳转到日记详情页
            Intent intent = new Intent(HomeActivity.this, DiaryDetailActivity.class);
            intent.putExtra("diary_id", diary.getDiaryId());
            startActivity(intent);
        });

        // 初始化日记本适配器
        notebookAdapter = new NotebookAdapter(this, notebookList);
        notebookAdapter.setOnItemClickListener(new NotebookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // 点击日记本项的处理
                Notebook notebook = notebookList.get(position);
                Intent intent = new Intent(HomeActivity.this, NotebookDetailActivity.class);
                intent.putExtra("notebook_id", notebook.getId());
                startActivity(intent);
            }

            @Override
            public void onSettingsClick(int position) {
                // 点击设置按钮的处理
                Notebook notebook = notebookList.get(position);
                Intent intent = new Intent(HomeActivity.this, NotebookSettingActivity.class);
                intent.putExtra("notebook_id", notebook.getId());
                startActivity(intent);
            }
        });

        // 初始化封面图片
        coverImages.add("cover1");
        coverImages.add("cover2");
        coverImages.add("cover3");
        coverImages.add("cover4");
        coverImages.add("cover5");
        coverPagerAdapter = new CoverPagerAdapter(this, coverImages);

        // 设置ViewPager适配器
        if (viewPager != null) {
            viewPager.setAdapter(coverPagerAdapter);
            viewPager.setPageMargin(16);
        }
    }

    private void setupListeners() {
        // 返回按钮点击事件
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // 编辑资料按钮点击事件
        if (tvEdit != null) {
            tvEdit.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProfileEditActivity.class);
                intent.putExtra("source", "home_activity");
                startActivity(intent);
            });
        }

        // 卡片头像点击事件
        if (ivCardAvatar != null) {
            ivCardAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProfileEditActivity.class);
                intent.putExtra("source", "home_activity");
                startActivity(intent);
            });
        }

        // 标签切换
        if (tabDiary != null) {
            tabDiary.setOnClickListener(v -> switchToDiaryTab());
        }

        if (tabNotebook != null) {
            tabNotebook.setOnClickListener(v -> switchToNotebookTab());
        }

        if (tabFavorite != null) {
            tabFavorite.setOnClickListener(v -> switchToFavoriteTab());
        }
    }

    private void loadUserData() {
        if (currentUserId != -1) {
            User user = userDAO.getUserById(currentUserId);
            if (user != null) {
                // 设置卡片昵称
                tvCardNickname.setText(user.getNickname());

                // 设置途号
                String trailNumber = user.getTrailNumber();
                if (trailNumber != null && !trailNumber.isEmpty()) {
                    tvCardTrailNumber.setText("途号：" + trailNumber);
                } else {
                    tvCardTrailNumber.setText("途号：未设置");
                }

                // 卡片签名完整显示
                String cardSignature = user.getSignature();
                if (cardSignature == null || cardSignature.isEmpty()) {
                    cardSignature = "山川为印，时光为笔。";
                }
                tvCardSignature.setText(cardSignature);

                // 加载用户头像 - 使用圆形裁剪
                loadAvatar(user);
            }
        }
    }

    private void loadAvatar(User user) {
        // 优先尝试从数据库获取头像字节数组
        byte[] avatarBytes = userDAO.getUserAvatarBytes(currentUserId);

        if (avatarBytes != null && avatarBytes.length > 0) {
            // 如果有字节数组，使用Glide加载并圆形裁剪
            Glide.with(this)
                    .asBitmap()
                    .load(avatarBytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivCardAvatar);
        } else if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            // 如果avatar字段是路径字符串
            Glide.with(this)
                    .load(user.getAvatar())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivCardAvatar);
        } else {
            // 使用默认头像（随机选择）
            int defaultAvatarResId = spUtil.getInt(Constants.SP_KEY_AVATAR_RES_ID, R.drawable.avatar1);
            Glide.with(this)
                    .load(defaultAvatarResId)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivCardAvatar);
        }
    }

    private void loadDiaryData() {
        if (currentUserId != -1) {
            diaryList.clear();
            // 获取用户的日记
            diaryList.addAll(diaryDAO.getDiariesByUserId(currentUserId, false));
            diaryAdapter.notifyDataSetChanged();

            tvDiaryNumber.setText("." + diaryList.size());
        }
    }

    private void loadNotebookData() {
        if (currentUserId != -1) {
            notebookList.clear();
            // 获取用户的日记本
            notebookList.addAll(notebookDAO.getNotebooksByUser(currentUserId));
            notebookAdapter.notifyDataSetChanged();

            // 更新封面ViewPager
            if (coverPagerAdapter != null) {
                coverPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void loadFavoriteData() {
        if (currentUserId != -1) {
            favoriteList.clear();
            // 获取用户收藏的日记
            favoriteList.addAll(favoriteDAO.getFavoritesByUserId(currentUserId));
        }
    }

    private void switchToDiaryTab() {
        if (currentTab != 0) {
            currentTab = 0;
            // 更新标签颜色
            tabDiary.setTextColor(getResources().getColor(R.color.tab_selected));
            tabNotebook.setTextColor(getResources().getColor(R.color.tab_unselected));
            tabFavorite.setTextColor(getResources().getColor(R.color.tab_unselected));
            // 更新指示器
            indicatorDiary.setBackgroundColor(getResources().getColor(R.color.tab_selected));
            indicatorNotebook.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            indicatorFavorite.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            // 切换内容显示
            rvContent.setLayoutManager(new GridLayoutManager(this, 2));
            rvContent.setAdapter(diaryAdapter);
            tvDiaryCount.setText("全部");
            tvDiaryNumber.setText("." + diaryList.size());

            if (viewPager != null) {
                viewPager.setVisibility(View.GONE);
            }
        }
    }

    private void switchToNotebookTab() {
        if (currentTab != 1) {
            currentTab = 1;
            // 更新标签颜色
            tabDiary.setTextColor(getResources().getColor(R.color.tab_unselected));
            tabNotebook.setTextColor(getResources().getColor(R.color.tab_selected));
            tabFavorite.setTextColor(getResources().getColor(R.color.tab_unselected));
            // 更新指示器
            indicatorDiary.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            indicatorNotebook.setBackgroundColor(getResources().getColor(R.color.tab_selected));
            indicatorFavorite.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            // 切换内容显示
            rvContent.setLayoutManager(new GridLayoutManager(this, 2));
            rvContent.setAdapter(notebookAdapter);
            tvDiaryCount.setText("日记本");
            tvDiaryNumber.setText("." + notebookList.size());

            if (viewPager != null) {
                viewPager.setVisibility(View.GONE);
            }
        }
    }

    private void switchToFavoriteTab() {
        if (currentTab != 2) {
            currentTab = 2;
            // 更新标签颜色
            tabDiary.setTextColor(getResources().getColor(R.color.tab_unselected));
            tabNotebook.setTextColor(getResources().getColor(R.color.tab_unselected));
            tabFavorite.setTextColor(getResources().getColor(R.color.tab_selected));
            // 更新指示器
            indicatorDiary.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            indicatorNotebook.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            indicatorFavorite.setBackgroundColor(getResources().getColor(R.color.tab_selected));

            // 加载收藏数据
            loadFavoriteData();

            // 创建收藏日记适配器
            DiaryListAdapter favoriteAdapter = new DiaryListAdapter(this, favoriteList);
            favoriteAdapter.setOnItemClickListener(diary -> {
                // 点击收藏的日记 - 跳转到日记详情页
                Intent intent = new Intent(HomeActivity.this, DiaryDetailActivity.class);
                intent.putExtra("diary_id", diary.getDiaryId());
                startActivity(intent);
            });

            // 切换内容显示
            rvContent.setLayoutManager(new GridLayoutManager(this, 2));
            rvContent.setAdapter(favoriteAdapter);
            tvDiaryCount.setText("收藏");
            tvDiaryNumber.setText("." + favoriteList.size());

            if (viewPager != null) {
                viewPager.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当从其他页面返回时刷新数据
        loadUserData();
        loadDiaryData();
        loadNotebookData();
        loadFavoriteData();

        // 根据当前选中的标签刷新对应内容
        switch (currentTab) {
            case 0:
                diaryAdapter.notifyDataSetChanged();
                tvDiaryNumber.setText("." + diaryList.size());
                break;
            case 1:
                notebookAdapter.notifyDataSetChanged();
                tvDiaryNumber.setText("." + notebookList.size());
                break;
            case 2:
                switchToFavoriteTab();
                break;
        }
    }
}