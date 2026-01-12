package com.example.traildiary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.traildiary.R;
import com.example.traildiary.activity.NotebookDetailActivity;
import com.example.traildiary.activity.NotebookSettingActivity;
import com.example.traildiary.adapter.CoverPagerAdapter;
import com.example.traildiary.adapter.NotebookAdapter;
import com.example.traildiary.database.NotebookDAO;
import com.example.traildiary.model.Notebook;
import com.example.traildiary.utils.SharedPreferencesUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotebookFragment extends Fragment {

    private static final String TAG = "NotebookFragment";

    private ViewPager viewPagerNotebook;
    private LinearLayout layoutIndicator;
    private RecyclerView recyclerViewNotebook;
    private LinearLayout layoutEmptyNotebook;
    private Button btnCreateNotebook;
    private FloatingActionButton fabAddNotebook;
    public TextView tvNotebookCount;

    private CoverPagerAdapter coverPagerAdapter;
    private NotebookAdapter notebookAdapter;
    private List<Notebook> notebookList = new ArrayList<>();
    
    // 数据库操作
    private NotebookDAO notebookDAO;
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notebook, container, false);
        
        // 初始化数据库和用户ID
        notebookDAO = new NotebookDAO(getContext());
        SharedPreferencesUtil spUtil = SharedPreferencesUtil.getInstance(getContext());
        currentUserId = spUtil.getCurrentUserId();
        
        initView(view);
        setupViewPager();
        setupRecyclerView();
        setupListeners();
        loadNotebookData();
        return view;
    }

    private void initView(View view) {
        viewPagerNotebook = view.findViewById(R.id.viewPagerNotebook);
        layoutIndicator = view.findViewById(R.id.layoutIndicator);
        recyclerViewNotebook = view.findViewById(R.id.recyclerViewNotebook);
        layoutEmptyNotebook = view.findViewById(R.id.layoutEmptyNotebook);
        btnCreateNotebook = view.findViewById(R.id.btnCreateNotebook);
        fabAddNotebook = view.findViewById(R.id.fabAddNotebook);
        tvNotebookCount = view.findViewById(R.id.tvNotebookCount);
    }

    private void setupViewPager() {
        // 修复：CoverPagerAdapter需要List<String>，将资源ID转为字符串
        List<String> coverList = new ArrayList<>();
        coverList.add(String.valueOf(R.drawable.cover1));
        coverList.add(String.valueOf(R.drawable.cover2));
        coverList.add(String.valueOf(R.drawable.cover3));
        coverList.add(String.valueOf(R.drawable.cover4));

        // 修复构造函数参数类型不匹配问题
        coverPagerAdapter = new CoverPagerAdapter(getContext(), coverList);
        viewPagerNotebook.setAdapter(coverPagerAdapter);

        // 设置页面切换动画和监听
        viewPagerNotebook.setPageMargin(20);
        viewPagerNotebook.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // 创建页面指示器
        createIndicators(coverList.size());
    }

    private void createIndicators(int count) {
        layoutIndicator.removeAllViews();

        for (int i = 0; i < count; i++) {
            View indicator = new View(getContext());
            int size = 8;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(i == 0 ? R.drawable.bg_indicator_selected : R.drawable.bg_indicator_unselected);
            layoutIndicator.addView(indicator);
        }
    }

    private void updateIndicator(int position) {
        for (int i = 0; i < layoutIndicator.getChildCount(); i++) {
            View indicator = layoutIndicator.getChildAt(i);
            indicator.setBackgroundResource(i == position ? R.drawable.bg_indicator_selected : R.drawable.bg_indicator_unselected);
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerViewNotebook.setLayoutManager(layoutManager);

        // 修复：NotebookAdapter构造函数缺少Context参数
        notebookAdapter = new NotebookAdapter(getContext(), notebookList);
        recyclerViewNotebook.setAdapter(notebookAdapter);
    }

    private void setupListeners() {
        // 创建日记本按钮
        btnCreateNotebook.setOnClickListener(v -> createNewNotebook());

        // 浮动按钮
        fabAddNotebook.setOnClickListener(v -> createNewNotebook());

        // 日记本点击监听
        notebookAdapter.setOnItemClickListener(new NotebookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Notebook notebook = notebookList.get(position);
                openNotebookDetail(notebook);
            }

            @Override
            public void onSettingsClick(int position) {
                Notebook notebook = notebookList.get(position);
                openNotebookSettings(notebook);
            }
        });
    }

    private void createNewNotebook() {
        Intent intent = new Intent(getActivity(), NotebookSettingActivity.class);
        intent.putExtra("mode", "create");
        startActivity(intent);
    }

    private void openNotebookDetail(Notebook notebook) {
        Intent intent = new Intent(getActivity(), NotebookDetailActivity.class);
        intent.putExtra("notebook_id", notebook.getId());
        startActivity(intent);
    }

    private void openNotebookSettings(Notebook notebook) {
        Intent intent = new Intent(getActivity(), NotebookSettingActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("notebook_id", notebook.getId());
        startActivity(intent);
    }

    /**
     * 从数据库加载笔记本数据
     */
    private void loadNotebookData() {
        notebookList.clear();
        
        // 从数据库获取当前用户的所有笔记本
        List<Notebook> notebooks = notebookDAO.getNotebooksByUser(currentUserId);
        
        if (notebooks != null && !notebooks.isEmpty()) {
            notebookList.addAll(notebooks);
            Log.d(TAG, "从数据库加载了 " + notebooks.size() + " 个笔记本");
            
            // 打印每个笔记本的封面信息用于调试
            for (Notebook notebook : notebookList) {
                Log.d(TAG, "笔记本: " + notebook.getName() 
                        + ", coverPath=" + notebook.getCoverPath() 
                        + ", coverResId=" + notebook.getCoverResId());
            }
        } else {
            Log.d(TAG, "数据库中没有笔记本数据");
        }

        notebookAdapter.notifyDataSetChanged();
        updateNotebookCount();
        checkEmptyState();
    }

    private void updateNotebookCount() {
        tvNotebookCount.setText(String.format("本子 %d 本", notebookList.size()));
    }

    private void checkEmptyState() {
        if (notebookList.isEmpty()) {
            viewPagerNotebook.setVisibility(View.GONE);
            layoutIndicator.setVisibility(View.GONE);
            recyclerViewNotebook.setVisibility(View.GONE);
            layoutEmptyNotebook.setVisibility(View.VISIBLE);
        } else {
            viewPagerNotebook.setVisibility(View.VISIBLE);
            layoutIndicator.setVisibility(View.VISIBLE);
            recyclerViewNotebook.setVisibility(View.VISIBLE);
            layoutEmptyNotebook.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 重新加载数据
        loadNotebookData();
    }
}