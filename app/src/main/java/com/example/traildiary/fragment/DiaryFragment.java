package com.example.traildiary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traildiary.R;
import com.example.traildiary.activity.WriteDiaryActivity;
import com.example.traildiary.adapter.DiaryListAdapter;
import com.example.traildiary.model.Diary;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.threeten.bp.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiaryFragment extends Fragment {

    private EditText etSearch;
    private Spinner spinnerCategory, spinnerSort;
    private RecyclerView recyclerViewDiary;
    private LinearLayout layoutEmpty;
    private Button btnCreateDiary;
    private FloatingActionButton fabAddDiary;

    private DiaryListAdapter diaryAdapter;
    private List<Diary> diaryList = new ArrayList<>();
    private List<Diary> filteredDiaryList = new ArrayList<>();
    private boolean isGridView = true; // 默认网格布局

    private static final String[] CATEGORIES = {"全部分类", "国内游", "国际游", "亲子游", "美食之旅", "户外探险", "文化历史"};
    private static final String[] SORT_OPTIONS = {"最近更新", "最早更新", "标题A-Z", "标题Z-A"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);
        initView(view);
        setupSpinners();
        setupRecyclerView();
        setupListeners();
        loadDiaryData();
        return view;
    }

    private void initView(View view) {
        etSearch = view.findViewById(R.id.etSearch);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerSort = view.findViewById(R.id.spinnerSort);
        recyclerViewDiary = view.findViewById(R.id.recyclerViewDiary);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        btnCreateDiary = view.findViewById(R.id.btnCreateDiary);
        fabAddDiary = view.findViewById(R.id.fabAddDiary);
    }

    private void setupSpinners() {
        // 分类下拉框
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                CATEGORIES
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // 排序下拉框
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                SORT_OPTIONS
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
    }

    private void setupRecyclerView() {
        // 设置布局管理器 - 默认网格布局
        updateLayoutManager();

        // 修复：构造方法添加Context参数（requireContext()）
        diaryAdapter = new DiaryListAdapter(requireContext(), filteredDiaryList);
        diaryAdapter.setGridLayout(isGridView); // 设置布局模式
        recyclerViewDiary.setAdapter(diaryAdapter);
    }

    private void updateLayoutManager() {
        if (isGridView) {
            // 网格布局（每行2个）
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
            recyclerViewDiary.setLayoutManager(gridLayoutManager);
        } else {
            // 列表布局
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            recyclerViewDiary.setLayoutManager(linearLayoutManager);
        }
    }

    private void setupListeners() {
        // 搜索功能
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // 创建日记按钮
        btnCreateDiary.setOnClickListener(v -> navigateToWriteDiary());

        // 浮动按钮
        fabAddDiary.setOnClickListener(v -> navigateToWriteDiary());

        // 分类选择监听
        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterDiariesByCategory(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // 排序选择监听
        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                sortDiaries(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void navigateToWriteDiary() {
        Intent intent = new Intent(getActivity(), WriteDiaryActivity.class);
        startActivity(intent);
    }

    private void loadDiaryData() {
        // 清空现有数据
        diaryList.clear();

        // 模拟加载数据 - 使用正确的Diary构造方法
        LocalDateTime now = LocalDateTime.now();

        // 创建日记1
        Diary diary1 = new Diary(1, "海边日出", "清晨的海边格外宁静...", "国内游", 1, false);
        diary1.setDiaryId(1);
        diary1.setCoverImagePath("path/to/cover1.jpg");
        diary1.setCreateTime(now.minusDays(10));
        diary1.setUpdateTime(now.minusDays(1));
        diaryList.add(diary1);

        // 创建日记2
        Diary diary2 = new Diary(1, "山间徒步", "挑战自我的旅程...", "户外探险", 1, false);
        diary2.setDiaryId(2);
        diary2.setImages(Arrays.asList("path/to/image1.jpg", "path/to/image2.jpg"));
        diary2.setCreateTime(now.minusDays(7));
        diary2.setUpdateTime(now.minusDays(2));
        diaryList.add(diary2);

        // 创建日记3
        Diary diary3 = new Diary(1, "美食探店", "发现城市中的隐藏美味...", "美食之旅", 1, false);
        diary3.setDiaryId(3);
        diary3.setCoverImagePath("path/to/cover3.jpg");
        diary3.setCreateTime(now.minusDays(5));
        diary3.setUpdateTime(now.minusDays(3));
        diaryList.add(diary3);

        // 创建日记4
        Diary diary4 = new Diary(1, "古镇游记", "感受历史的沉淀...", "文化历史", 1, false);
        diary4.setDiaryId(4);
        diary4.setCoverImagePath("path/to/cover4.jpg");
        diary4.setCreateTime(now.minusDays(3));
        diary4.setUpdateTime(now.minusDays(1));
        diaryList.add(diary4);

        // 初始化筛选列表
        filteredDiaryList.clear();
        filteredDiaryList.addAll(diaryList);

        // 更新适配器
        diaryAdapter.updateData(filteredDiaryList);

        // 检查空状态
        checkEmptyState();
    }

    private void performSearch() {
        String keyword = etSearch.getText().toString().trim().toLowerCase();

        filteredDiaryList.clear();

        if (keyword.isEmpty()) {
            // 如果搜索关键词为空，显示所有日记
            filteredDiaryList.addAll(diaryList);
        } else {
            // 根据关键词筛选
            for (Diary diary : diaryList) {
                if (diary.getTitle().toLowerCase().contains(keyword) ||
                        (diary.getContent() != null && diary.getContent().toLowerCase().contains(keyword))) {
                    filteredDiaryList.add(diary);
                }
            }
        }

        diaryAdapter.updateData(filteredDiaryList);
        checkEmptyState();
    }

    private void filterDiariesByCategory(int categoryIndex) {
        if (categoryIndex == 0) {
            // "全部分类" - 显示所有日记
            filteredDiaryList.clear();
            filteredDiaryList.addAll(diaryList);
        } else {
            // 根据分类筛选
            String selectedCategory = CATEGORIES[categoryIndex];
            filteredDiaryList.clear();

            for (Diary diary : diaryList) {
                if (selectedCategory.equals(diary.getCategory())) {
                    filteredDiaryList.add(diary);
                }
            }
        }

        diaryAdapter.updateData(filteredDiaryList);
        checkEmptyState();
    }

    private void sortDiaries(int sortIndex) {
        switch (sortIndex) {
            case 0: // 最近更新
                filteredDiaryList.sort((d1, d2) -> {
                    // 修复：使用org.threeten.bp.LocalDateTime替换java.time.LocalDateTime
                    org.threeten.bp.LocalDateTime time1 = d2.getUpdateTime() != null ? d2.getUpdateTime() : d2.getCreateTime();
                    org.threeten.bp.LocalDateTime time2 = d1.getUpdateTime() != null ? d1.getUpdateTime() : d1.getCreateTime();
                    // 移除版本判断（threetenbp是兼容库）
                    return time1 != null && time2 != null ? time1.compareTo(time2) : 0;
                });
                break;

            case 1: // 最早更新
                filteredDiaryList.sort((d1, d2) -> {
                    // 修复：使用org.threeten.bp.LocalDateTime替换java.time.LocalDateTime
                    org.threeten.bp.LocalDateTime time1 = d1.getUpdateTime() != null ? d1.getUpdateTime() : d1.getCreateTime();
                    org.threeten.bp.LocalDateTime time2 = d2.getUpdateTime() != null ? d2.getUpdateTime() : d2.getCreateTime();
                    // 移除版本判断（threetenbp是兼容库）
                    return time1 != null && time2 != null ? time1.compareTo(time2) : 0;
                });
                break;

            case 2: // 标题A-Z
                filteredDiaryList.sort((d1, d2) ->
                        d1.getTitle().compareToIgnoreCase(d2.getTitle()));
                break;

            case 3: // 标题Z-A
                filteredDiaryList.sort((d1, d2) ->
                        d2.getTitle().compareToIgnoreCase(d1.getTitle()));
                break;
        }

        diaryAdapter.updateData(filteredDiaryList);
    }

    private void checkEmptyState() {
        if (filteredDiaryList.isEmpty()) {
            recyclerViewDiary.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerViewDiary.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    // 切换布局模式的方法（可在菜单或其他地方调用）
    public void toggleLayoutMode() {
        isGridView = !isGridView;
        updateLayoutManager();
        diaryAdapter.setGridLayout(isGridView);
    }
}