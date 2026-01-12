package com.example.traildiary.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.traildiary.R;
import com.example.traildiary.adapter.CoverPagerAdapter;
import com.example.traildiary.database.NotebookDAO;
import com.example.traildiary.database.UserDAO;
import com.example.traildiary.model.Notebook;
import com.example.traildiary.model.User;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.SharedPreferencesUtil;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout tabHome, tabSettings;
    private ImageView ivAvatar;
    private TextView tvNickname, tvSignature, tvNotebookCount, tvNickname1;

    private SharedPreferencesUtil spUtil;
    private UserDAO userDAO;
    private NotebookDAO notebookDAO;
    private User currentUser;

    private boolean isDataLoaded = false; // 添加标志位，防止重复加载

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 在主线程中初始化 SharedPreferencesUtil
        spUtil = SharedPreferencesUtil.getInstance(MainActivity.this);

        // 立即检查登录状态（不在后台线程中）
        if (!isUserLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginRegisterActivity.class));
            finish();
            return; // 如果未登录，直接返回，不执行后续代码
        }

        // 初始化UI
        initView();
        setupTabListeners();

        // 初始化数据库DAO
        userDAO = new UserDAO(MainActivity.this);
        notebookDAO = new NotebookDAO(MainActivity.this);

        // 延迟加载数据，确保UI先显示
        new Handler().postDelayed(() -> {
            loadUserData();
            loadNotebookData();
            isDataLoaded = true; // 标记数据已加载
        }, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 检查 spUtil 是否已初始化
        if (spUtil == null) {
            spUtil = SharedPreferencesUtil.getInstance(this);
        }

        // 重新检查登录状态
        if (!isUserLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginRegisterActivity.class));
            finish();
            return;
        }

        // 如果数据已经加载过，才重新加载（避免重复加载）
        if (isDataLoaded && spUtil != null) {
            // 重新加载用户数据（如编辑资料后返回）
            loadUserData();
            loadNotebookData();
        }
    }

    private boolean isUserLoggedIn() {
        if (spUtil == null) {
            spUtil = SharedPreferencesUtil.getInstance(this);
        }

        boolean isLoggedIn = spUtil.isLoggedIn();

        // 简化检查，避免过多的数据库查询
        if (isLoggedIn) {
            int userId = spUtil.getCurrentUserId();
            String nickname = spUtil.getCurrentUserNickname();
            return userId > 0 && nickname != null && !nickname.isEmpty();
        }
        return false;
    }

    private void initView() {
        viewPager = findViewById(R.id.viewPager);
        tabHome = findViewById(R.id.tabHome);
        tabSettings = findViewById(R.id.tabSettings);

        // 初始化用户信息相关的视图
        ivAvatar = findViewById(R.id.iv_avatar);
        tvNickname = findViewById(R.id.tv_nickname);
        tvSignature = findViewById(R.id.tv_signature);
        tvNotebookCount = findViewById(R.id.tv_notebook_count);
        tvNickname1 = findViewById(R.id.tv_nickname1);
    }

    private void setupTabListeners() {
        // 小屋tab点击事件
        tabHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // 设置tab点击事件
        tabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileEditActivity.class);
                intent.putExtra("source", "main");
                startActivity(intent);
            }
        });

        // 头像点击事件 - 跳转到我的小屋页面
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
        });

        // 昵称区域点击事件 - 跳转到我的小屋页面
        findViewById(R.id.tv_nickname).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
            }
        });
    }

    private void loadUserData() {
        // 检查 spUtil 是否已初始化
        if (spUtil == null) {
            spUtil = SharedPreferencesUtil.getInstance(this);
            return; // 如果仍未初始化，则直接返回
        }

        int userId = spUtil.getCurrentUserId();

        if (userId > 0 && userDAO != null) {
            currentUser = userDAO.getUserById(userId);

            if (currentUser != null) {
                displayUserInfo(currentUser);
                loadUserAvatar(userId);
                spUtil.putString(Constants.SP_KEY_NICKNAME, currentUser.getNickname());
                spUtil.putString(Constants.SP_KEY_TRAIL_NUMBER, currentUser.getTrailNumber());
            }
        }
    }

    private void loadNotebookData() {
        // 检查 spUtil 是否已初始化
        if (spUtil == null) {
            spUtil = SharedPreferencesUtil.getInstance(this);
            return;
        }

        int userId = spUtil.getCurrentUserId();

        if (userId > 0 && notebookDAO != null) {
            int notebookCount = getNotebookCount(userId);
            displayNotebookCount(notebookCount);
            loadLatestNotebook(userId);
        }
    }

    private int getNotebookCount(int userId) {
        if (notebookDAO != null) {
            return notebookDAO.getNotebookCountByUser(userId);
        }
        return 0;
    }

    private void displayNotebookCount(int count) {
        if (tvNotebookCount == null) {
            return;
        }

        String displayText;
        if (count == 0) {
            displayText = "日记本.0本";
        } else if (count == 1) {
            displayText = "日记本.1本";
        } else {
            displayText = String.format("日记本.%d本", count);
        }

        tvNotebookCount.setText(displayText);
    }

    private void loadLatestNotebook(int userId) {
        if (notebookDAO == null) {
            return;
        }

        List<Notebook> notebooks = notebookDAO.getNotebooksByUser(userId);

        if (notebooks == null || notebooks.isEmpty()) {
            createDefaultNotebookWithRandomCover(userId);
            notebooks = notebookDAO.getNotebooksByUser(userId);
        }

        if (notebooks != null && !notebooks.isEmpty()) {
            // 更新每个日记本的日记数量
            for (Notebook notebook : notebooks) {
                notebookDAO.updateNotebookDiaryCount(notebook.getId());
            }
            // 重新获取更新后的数据
            notebooks = notebookDAO.getNotebooksByUser(userId);

            sortNotebooksByCreateTime(notebooks);

            List<Notebook> finalNotebooks = notebooks;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateNotebookViewPager(finalNotebooks);
                    displayNotebookCount(finalNotebooks.size());
                }
            }, 100);
        }
    }

    private void sortNotebooksByCreateTime(List<Notebook> notebooks) {
        if (notebooks == null || notebooks.size() <= 1) {
            return;
        }

        notebooks.sort(new java.util.Comparator<Notebook>() {
            @Override
            public int compare(Notebook n1, Notebook n2) {
                if (n1.getCreateTime() != null && n2.getCreateTime() != null) {
                    return n2.getCreateTime().compareTo(n1.getCreateTime());
                }
                return 0;
            }
        });
    }

    private void createDefaultNotebookWithRandomCover(int userId) {
        Notebook defaultNotebook = new Notebook();
        defaultNotebook.setName("我的日记本");
        defaultNotebook.setUserId(userId);
        defaultNotebook.setDiaryCount(0);
        defaultNotebook.setSortOrder(0);

        // 使用数字1-5作为封面标识
        String randomCover = getRandomCover();
        defaultNotebook.setCoverPath(randomCover);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            defaultNotebook.setCreateTime(LocalDateTime.now());
        }

        if (notebookDAO != null) {
            notebookDAO.addNotebook(defaultNotebook);
        }
    }

    private String getRandomCover() {
        // 使用数字1-5作为封面标识
        String[] covers = {"1", "2", "3", "4", "5"};
        Random random = new Random();
        int index = random.nextInt(covers.length);
        return covers[index];
    }

    private class NotebookPagerAdapter extends androidx.viewpager.widget.PagerAdapter {
        private Context context;
        private List<Notebook> notebooks;
        private OnAddNotebookClickListener addNotebookClickListener;

        public NotebookPagerAdapter(Context context, List<Notebook> notebooks) {
            this.context = context;
            this.notebooks = notebooks != null ? notebooks : new ArrayList<>();
        }

        public void setOnAddNotebookClickListener(OnAddNotebookClickListener listener) {
            this.addNotebookClickListener = listener;
        }

        @Override
        public int getCount() {
            return notebooks.size() + 1;
        }

        @Override
        public boolean isViewFromObject(android.view.View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(android.view.ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(context);

            View addView;
            android.view.ViewGroup.LayoutParams params;
            if (position == notebooks.size()) {
                // 添加笔记本项，大小与笔记本预览一致
                addView = inflater.inflate(R.layout.item_add_notebook, container, false);

                params = new android.view.ViewGroup.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.notebook_width),
                        getResources().getDimensionPixelSize(R.dimen.notebook_height)
                );
                addView.setLayoutParams(params);

                View ivCreateNotebook = addView.findViewById(R.id.iv_create_notebook);
                ivCreateNotebook.setOnClickListener(v -> {
                    Intent intent = new Intent(context, NotebookSettingActivity.class);
                    intent.putExtra("is_new_notebook", true);
                    context.startActivity(intent);
                });

            } else {
                addView = inflater.inflate(R.layout.item_notebook_preview, container, false);

                params = new android.view.ViewGroup.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.notebook_width),
                        getResources().getDimensionPixelSize(R.dimen.notebook_height)
                );
                addView.setLayoutParams(params);

                Notebook notebook = notebooks.get(position);
                bindNotebookView(addView, notebook, position);
            }

            container.addView(addView);
            return addView;
        }

        @Override
        public void destroyItem(android.view.ViewGroup container, int position, Object object) {
            container.removeView((android.view.View) object);
        }

        private void bindNotebookView(android.view.View view, Notebook notebook, int position) {
            ImageView ivCover = view.findViewById(R.id.iv_notebook_cover);
            TextView tvName = view.findViewById(R.id.tv_notebook_name);
            TextView tvCount = view.findViewById(R.id.tv_diary_count);

            tvName.setText(notebook.getName());
            tvCount.setText(notebook.getDisplayDiaryCount());
            setNotebookCover(ivCover, notebook.getCoverPath());

            view.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    Intent intent = new Intent(context, NotebookDetailActivity.class);
                    intent.putExtra("notebook_id", notebook.getId());
                    context.startActivity(intent);
                }
            });

            ImageView notebookSetting = view.findViewById(R.id.NotebookSetting);
            if (notebookSetting != null) {
                notebookSetting.setOnClickListener(v -> {
                    Intent intent = new Intent(context, NotebookSettingActivity.class);
                    intent.putExtra("notebook_id", notebook.getId());
                    context.startActivity(intent);
                });
            }
        }

        private void setNotebookCover(ImageView imageView, String coverPath) {
            // 使用CoverPagerAdapter的方法获取封面资源ID
            int resourceId = CoverPagerAdapter.getCoverResourceId(coverPath);
            imageView.setImageResource(resourceId);
            imageView.setVisibility(View.VISIBLE);
        }

        public void updateData(List<Notebook> newNotebooks) {
            this.notebooks = newNotebooks != null ? newNotebooks : new ArrayList<>();
            notifyDataSetChanged();
        }

        @Override
        public float getPageWidth(int position) {
            // 每个页面占据80%的宽度，让下一本笔记本的左边露出来
            return 0.8f;
        }
    }

    public interface OnAddNotebookClickListener {
        void onAddNotebookClick();
    }

    /**
     * 更新日记本ViewPager
     * 效果：当前笔记本完整显示，下一本笔记本的左边部分露出在右侧
     */
    private void updateNotebookViewPager(List<Notebook> notebooks) {
        if (viewPager == null) {
            return;
        }

        if (notebooks == null || notebooks.isEmpty()) {
            return;
        }

        NotebookPagerAdapter adapter = new NotebookPagerAdapter(this, notebooks);
        viewPager.setAdapter(adapter);

        // 设置页面间距
        int pageMargin = getResources().getDimensionPixelSize(R.dimen.page_margin);
        viewPager.setPageMargin(pageMargin);

        // 简单的滑动效果，不做特殊变换，让下一本笔记本的左边自然露出
        viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                if (position < -1) {
                    // 左侧屏幕外
                    page.setAlpha(0);
                } else if (position <= 1) {
                    // 可见范围内的页面
                    page.setAlpha(1);
                    page.setTranslationX(0);
                    page.setScaleX(1);
                    page.setScaleY(1);
                } else {
                    // 右侧屏幕外
                    page.setAlpha(0);
                }
            }
        });

        viewPager.setOffscreenPageLimit(3);
    }

    private void displayUserInfo(User user) {
        if (tvNickname != null) {
            tvNickname.setText(user.getNickname());
        }

        if (tvNickname1 != null) {
            tvNickname1.setText(user.getNickname());
        }

        if (tvSignature != null) {
            String signature = user.getSignature();
            if (signature == null || signature.trim().isEmpty()) {
                signature = "山川为印，时光为笔。";
            }
            tvSignature.setText(signature);
        }
    }

    private void loadUserAvatar(int userId) {
        if (userDAO == null || ivAvatar == null) {
            return;
        }

        byte[] avatarBytes = userDAO.getUserAvatarBytes(userId);

        if (avatarBytes != null && avatarBytes.length > 0) {
            try {
                Glide.with(this)
                        .asBitmap()
                        .load(avatarBytes)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivAvatar);
            } catch (Exception e) {
                e.printStackTrace();
                setDefaultAvatar();
            }
        } else {
            setDefaultAvatar();
        }
    }

    private void setDefaultAvatar() {
        if (ivAvatar == null) {
            return;
        }

        Glide.with(this)
                .load(R.drawable.ic_default_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(ivAvatar);
    }

    public User getCurrentUser() {
        return currentUser;
    }
}