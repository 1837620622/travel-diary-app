package com.example.traildiary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.traildiary.R;
import com.example.traildiary.activity.NotebookDetailActivity;
import com.example.traildiary.activity.NotebookSettingActivity;
import com.example.traildiary.activity.ProfileEditActivity;
import com.example.traildiary.adapter.NotebookAdapter;
import com.example.traildiary.database.NotebookDAO;
import com.example.traildiary.database.UserDAO;
import com.example.traildiary.model.Notebook;
import com.example.traildiary.model.User;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.SharedPreferencesUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页Fragment - 显示用户信息和日记本列表
 */
public class HomeFragment extends Fragment {

    // 视图组件
    private TextView tvNickname, tvSignature, tvNotebookCount;
    private ImageView ivAvatar;
    private RecyclerView rvNotebooks;
    private FloatingActionButton fabCreate;

    // 数据
    private SharedPreferencesUtil spUtil;
    private UserDAO userDAO;
    private NotebookDAO notebookDAO;
    private NotebookAdapter notebookAdapter;
    private List<Notebook> notebookList = new ArrayList<>();
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        initData();
        setupListeners();
        loadUserData();
        loadNotebookData();
        return view;
    }

    /**
     * 初始化视图组件
     */
    private void initViews(View view) {
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvSignature = view.findViewById(R.id.tv_signature);
        tvNotebookCount = view.findViewById(R.id.tv_notebook_count);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        rvNotebooks = view.findViewById(R.id.rv_notebooks);
        fabCreate = view.findViewById(R.id.fab_create);

        // 设置RecyclerView为水平滑动布局，露出右侧笔记本
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvNotebooks.setLayoutManager(layoutManager);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        spUtil = SharedPreferencesUtil.getInstance(requireContext());
        userDAO = new UserDAO(requireContext());
        notebookDAO = new NotebookDAO(requireContext());
        currentUserId = spUtil.getCurrentUserId();

        // 初始化日记本适配器
        notebookAdapter = new NotebookAdapter(requireContext(), notebookList);
        notebookAdapter.setShowSettings(true); // 显示设置图标
        rvNotebooks.setAdapter(notebookAdapter);
    }

    /**
     * 设置点击事件监听
     */
    private void setupListeners() {
        // 头像点击 - 跳转到个人资料编辑
        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
            startActivity(intent);
        });

        // 悬浮按钮点击 - 创建日记本
        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotebookSettingActivity.class);
            intent.putExtra("mode", "create");
            startActivity(intent);
        });

        // 日记本列表点击事件
        notebookAdapter.setOnItemClickListener(new NotebookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Notebook notebook = notebookList.get(position);
                Intent intent = new Intent(getActivity(), NotebookDetailActivity.class);
                intent.putExtra("notebook_id", notebook.getId());
                startActivity(intent);
            }

            @Override
            public void onSettingsClick(int position) {
                Notebook notebook = notebookList.get(position);
                Intent intent = new Intent(getActivity(), NotebookSettingActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("notebook_id", notebook.getId());
                startActivity(intent);
            }
        });
    }

    /**
     * 加载用户数据
     */
    private void loadUserData() {
        if (currentUserId != -1) {
            User user = userDAO.getUserById(currentUserId);
            if (user != null) {
                // 设置昵称
                tvNickname.setText(user.getNickname() != null ? user.getNickname() : "用户");

                // 设置签名
                String signature = user.getSignature();
                if (signature == null || signature.isEmpty()) {
                    signature = Constants.DEFAULT_SIGNATURE;
                }
                tvSignature.setText(signature);

                // 加载头像
                loadAvatar(user);
            }
        }
    }

    /**
     * 加载用户头像
     */
    private void loadAvatar(User user) {
        byte[] avatarBytes = userDAO.getUserAvatarBytes(currentUserId);

        if (avatarBytes != null && avatarBytes.length > 0) {
            Glide.with(this)
                    .asBitmap()
                    .load(avatarBytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar);
        } else if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatar())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar);
        } else {
            int defaultAvatarResId = spUtil.getInt(Constants.SP_KEY_AVATAR_RES_ID, R.drawable.avatar1);
            Glide.with(this)
                    .load(defaultAvatarResId)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivAvatar);
        }
    }

    /**
     * 加载日记本数据
     */
    private void loadNotebookData() {
        if (currentUserId != -1) {
            notebookList.clear();
            List<Notebook> notebooks = notebookDAO.getNotebooksByUser(currentUserId);
            
            // 添加调试日志
            for (Notebook notebook : notebooks) {
                Log.d("HomeFragment", "加载笔记本: name=" + notebook.getName() 
                        + ", coverPath=" + notebook.getCoverPath() 
                        + ", coverResId=" + notebook.getCoverResId());
            }
            
            notebookList.addAll(notebooks);
            notebookAdapter.notifyDataSetChanged();

            // 更新日记本数量显示
            tvNotebookCount.setText("本子." + notebookList.size() + "本");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 刷新数据
        loadUserData();
        loadNotebookData();
    }
}
