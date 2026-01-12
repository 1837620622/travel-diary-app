package com.example.traildiary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.traildiary.R;
import com.example.traildiary.adapter.DiaryListAdapter;
import com.example.traildiary.database.DiaryDAO;
import com.example.traildiary.database.NotebookDAO;
import com.example.traildiary.model.Diary;
import com.example.traildiary.model.Notebook;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.SharedPreferencesUtil;
import com.example.traildiary.widget.SearchDialog;

import java.util.ArrayList;
import java.util.List;

public class NotebookDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageButton btnSearch;
    private TextView tvNotebookName;
    private TextView tvEmptyHint, tvCreateDiary;
    private RecyclerView recyclerView;
    private FloatingActionButton fabCreate;

    private Notebook notebook;
    private DiaryListAdapter adapter;
    private DiaryDAO diaryDAO;
    private NotebookDAO notebookDAO;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notebook_detail);

        initView();
        initData();
        loadData();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_back);
        btnSearch = findViewById(R.id.btn_search);
        tvNotebookName = findViewById(R.id.tv_notebook_name);
        tvEmptyHint = findViewById(R.id.tv_empty_hint);
        recyclerView = findViewById(R.id.recyclerView);
        tvCreateDiary = findViewById(R.id.tv_create_diary);
        fabCreate = findViewById(R.id.fab_create);

        // 设置RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        // 修复：DiaryListAdapter需要两个参数，并且需要使用空的ArrayList初始化
        List<Diary> emptyDiaryList = new ArrayList<>();
        adapter = new DiaryListAdapter(this, emptyDiaryList);
        recyclerView.setAdapter(adapter);

        // 设置点击事件
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        // 搜索按钮点击事件：使用 SearchDialog
        btnSearch.setOnClickListener(v -> {
            // 初始化搜索对话框
            SearchDialog searchDialog = new SearchDialog(
                    NotebookDetailActivity.this,
                    new SearchDialog.OnSearchResultClickListener() {
                        @Override
                        public void onDiaryClicked(Diary diary) {
                            // 跳转到日记详情页
                            Intent intent = new Intent(NotebookDetailActivity.this, DiaryDetailActivity.class);
                            intent.putExtra("diary_id", diary.getDiaryId());
                            startActivity(intent);
                        }
                    });
            searchDialog.show(); // 显示搜索对话框
        });

        // 创建新日记按钮点击事件
        tvCreateDiary.setOnClickListener(v -> navigateToWriteDiary());

        // 悬浮按钮点击事件
        fabCreate.setOnClickListener(v -> navigateToWriteDiary());

        // 设置适配器的点击事件 - 使用正确的接口名
        adapter.setOnItemClickListener(diary -> {
            // 跳转到日记详情页
            Intent intent = new Intent(this, DiaryDetailActivity.class);
            intent.putExtra("diary_id", diary.getDiaryId());
            startActivity(intent);
        });
    }

    private void initData() {
        // 获取当前用户ID
        SharedPreferencesUtil spUtil = SharedPreferencesUtil.getInstance(this);
        currentUserId = spUtil.getCurrentUserId();

        // 初始化数据库操作对象 - 传入Context而不是DatabaseHelper
        diaryDAO = new DiaryDAO(this);
        notebookDAO = new NotebookDAO(this);

        // 获取传递的日记本ID
        int notebookId = getIntent().getIntExtra("notebook_id", -1);
        if (notebookId == -1) {
            // 尝试使用旧的常量名
            notebookId = getIntent().getIntExtra(Constants.INTENT_KEY_NOTEBOOK_ID, -1);
        }

        if (notebookId != -1) {
            notebook = notebookDAO.getNotebookById(notebookId);
            if (notebook != null) {
                tvNotebookName.setText(notebook.getName());
            }
        }
    }

    private void loadData() {
        if (notebook == null) {
            tvEmptyHint.setVisibility(View.VISIBLE);
            tvEmptyHint.setText("日记本不存在");
            recyclerView.setVisibility(View.GONE);
            return;
        }

        // 加载该日记本下的所有日记
        List<Diary> diaries = diaryDAO.getDiariesByNotebookId(notebook.getId());

        if (diaries.isEmpty()) {
            tvEmptyHint.setVisibility(View.VISIBLE);
            tvEmptyHint.setText("这个日记本里还没有作品！");
            tvCreateDiary.setVisibility(View.VISIBLE);
            tvCreateDiary.setText("去制作一篇吧~");
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyHint.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            // 修复：使用正确的更新数据方法名
            adapter.updateData(diaries);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载数据
        loadData();
    }

    /**
     * 跳转到写日记页面
     */
    private void navigateToWriteDiary() {
        Intent intent = new Intent(NotebookDetailActivity.this, WriteDiaryActivity.class);
        if (notebook != null) {
            intent.putExtra(Constants.INTENT_KEY_NOTEBOOK_ID, notebook.getId());
        }
        startActivity(intent);
    }
}