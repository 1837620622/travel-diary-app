package com.example.traildiary.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

import com.example.traildiary.R;
import com.example.traildiary.adapter.DiaryListAdapter;
import com.example.traildiary.database.DiaryDAO;
import com.example.traildiary.database.SearchHistoryDAO;
import com.example.traildiary.model.Diary;
import com.example.traildiary.model.SearchHistory;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchDialog extends Dialog {

    private Context context;
    private SharedPreferencesUtil spUtil;
    private DiaryDAO diaryDAO;
    private SearchHistoryDAO searchHistoryDAO;

    // 视图组件
    private EditText etSearch;
    private ImageView btnClear;
    private RadioGroup rgSearchType;
    private RadioButton rbAuthor, rbTitle, rbCategory;
    private Button btnSearch, btnCancel;
    private LinearLayout layoutSearchHistory;
    private com.google.android.flexbox.FlexboxLayout flexboxHistory;
    private RecyclerView rvSearchResult;
    private TextView tvNoHistory, tvNoResult, tvSearchHistoryTitle;
    private LinearLayout llEmpty;

    // 适配器
    private DiaryListAdapter diaryAdapter;

    // 数据
    private List<SearchHistory> searchHistoryList;
    private List<Diary> searchResultList;

    // 回调接口
    private OnSearchResultClickListener listener;

    public interface OnSearchResultClickListener {
        void onDiaryClicked(Diary diary);
    }

    public SearchDialog(Context context, OnSearchResultClickListener listener) {
        super(context, R.style.Theme_TrailDiary_Dialog);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search);

        // 设置对话框尺寸
        Window window = getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        initViews();
        initData();
        setupListeners();
        loadSearchHistory();
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnClear = findViewById(R.id.iv_clear);
        rgSearchType = findViewById(R.id.rg_search_type);
        rbAuthor = findViewById(R.id.rb_author);
        rbTitle = findViewById(R.id.rb_title);
        rbCategory = findViewById(R.id.rb_category);
        btnSearch = findViewById(R.id.btn_search);
        btnCancel = findViewById(R.id.btn_cancel);
        layoutSearchHistory = findViewById(R.id.layout_search_history);
        flexboxHistory = findViewById(R.id.flexbox_history);
        rvSearchResult = findViewById(R.id.rv_search_result);
        tvNoHistory = findViewById(R.id.tv_no_history);
        tvSearchHistoryTitle = findViewById(R.id.tv_recent_title);
        llEmpty = findViewById(R.id.ll_empty);

        // 设置搜索结果RecyclerView
        rvSearchResult.setLayoutManager(new LinearLayoutManager(context));

        // 初始化搜索结果适配器
        searchResultList = new ArrayList<>();
        diaryAdapter = new DiaryListAdapter(context, searchResultList);
        diaryAdapter.setOnItemClickListener(diary -> {
            if (listener != null) {
                listener.onDiaryClicked(diary);
                dismiss();
            }
        });
        rvSearchResult.setAdapter(diaryAdapter);

        // 初始化搜索历史数据
        searchHistoryList = new ArrayList<>();
    }

    private void initData() {
        spUtil = SharedPreferencesUtil.getInstance(context);
        diaryDAO = new DiaryDAO(context);
        searchHistoryDAO = new SearchHistoryDAO(context);
        // 移除：DiaryDAO和SearchHistoryDAO没有open()方法，删除以下两行
        // diaryDAO.open();
        // searchHistoryDAO.open();
    }

    private void setupListeners() {
        // 清除按钮
        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            btnClear.setVisibility(View.GONE);
            showSearchHistory();
        });

        // 搜索框文本变化监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (s.length() == 0) {
                    showSearchHistory();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 搜索按钮
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (keyword.isEmpty()) {
                Toast.makeText(context, "请输入搜索内容", Toast.LENGTH_SHORT).show();
                return;
            }

            performSearch(keyword);
            hideKeyboard();
        });

        // 取消按钮
        btnCancel.setOnClickListener(v -> dismiss());

        // 点击外部关闭
        setCanceledOnTouchOutside(true);

        // 键盘搜索键监听
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                performSearch(keyword);
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void performSearch(String keyword) {
        // 获取搜索类型
        int searchType = getSelectedSearchType();

        // 执行搜索
        searchDiaries(keyword, searchType);

        // 保存搜索历史
        saveSearchHistory(keyword, searchType);

        // 显示搜索结果
        showSearchResult();
    }

    private void searchDiaries(String keyword, int searchType) {
        int userId = spUtil.getCurrentUserId();
        searchResultList.clear();

        List<Diary> result = new ArrayList<>();

        // 修改：修正DiaryDAO的方法名（原方法名是searchByAuthor/searchByTitle/searchByCategory）
        switch (searchType) {
            case Constants.SEARCH_BY_AUTHOR:
                result = diaryDAO.searchByAuthor(keyword, userId); // 修正方法名
                break;
            case Constants.SEARCH_BY_TITLE:
                result = diaryDAO.searchByTitle(keyword, userId); // 修正方法名
                break;
            case Constants.SEARCH_BY_CATEGORY:
                result = diaryDAO.searchByCategory(keyword, userId); // 修正方法名
                break;
            default:
                // 综合搜索（搜索所有字段）
                result = diaryDAO.searchAll(keyword, userId); // 修正为searchAll
                break;
        }

        searchResultList.addAll(result);
        diaryAdapter.notifyDataSetChanged();

        // 修改：显示/隐藏无结果提示（适配布局里的ll_empty）
        llEmpty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        rvSearchResult.setVisibility(result.isEmpty() ? View.GONE : View.VISIBLE);
        // 兼容旧tvNoResult（可选保留）
        if (tvNoResult != null) {
            tvNoResult.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private int getSelectedSearchType() {
        int checkedId = rgSearchType.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_author) {
            return Constants.SEARCH_BY_AUTHOR;
        } else if (checkedId == R.id.rb_title) {
            return Constants.SEARCH_BY_TITLE;
        } else if (checkedId == R.id.rb_category) {
            return Constants.SEARCH_BY_CATEGORY;
        }
        return Constants.SEARCH_BY_TITLE; // 默认按标题搜索
    }

    private void saveSearchHistory(String keyword, int searchType) {
        int userId = spUtil.getCurrentUserId();

        // 检查是否已存在相同搜索记录
        boolean exists = searchHistoryDAO.isSearchHistoryExist(userId, keyword);

        if (exists) {
            // 如果已存在，更新搜索时间（需要在DAO中添加相应方法）
            // 这里简化为不保存重复记录
            return;
        }

        // 添加新的搜索历史
        SearchHistory history = new SearchHistory(userId, keyword, searchType);
        long id = searchHistoryDAO.addSearchHistory(history);

        if (id > 0) {
            // 重新加载搜索历史
            loadSearchHistory();
        }
    }

    private void loadSearchHistory() {
        int userId = spUtil.getCurrentUserId();
        if (userId == -1) return;

        searchHistoryList.clear();
        List<SearchHistory> history = searchHistoryDAO.getSearchHistoryByUser(
                userId, Constants.MAX_SEARCH_HISTORY);
        searchHistoryList.addAll(history);

        // 清空FlexboxLayout并重新添加历史标签
        flexboxHistory.removeAllViews();
        for (SearchHistory item : searchHistoryList) {
            addHistoryTag(item);
        }

        // 显示/隐藏无历史提示
        boolean hasHistory = !history.isEmpty();
        tvNoHistory.setVisibility(hasHistory ? View.GONE : View.VISIBLE);
        tvSearchHistoryTitle.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
        flexboxHistory.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
    }

    /**
     * 添加搜索历史标签到FlexboxLayout
     */
    private void addHistoryTag(SearchHistory history) {
        TextView tag = new TextView(context);
        tag.setText(history.getKeyword());
        tag.setTextSize(14);
        tag.setTextColor(context.getResources().getColor(R.color.text_secondary));
        tag.setBackgroundResource(R.drawable.bg_tag);
        tag.setPadding(24, 12, 24, 12);

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 16, 16);
        tag.setLayoutParams(params);

        // 点击搜索
        tag.setOnClickListener(v -> {
            etSearch.setText(history.getKeyword());
            performSearch(history.getKeyword());
        });

        // 长按删除
        tag.setOnLongClickListener(v -> {
            deleteSearchHistory(history.getId(), searchHistoryList.indexOf(history));
            return true;
        });

        flexboxHistory.addView(tag);
    }

    private void deleteSearchHistory(int historyId, int position) {
        int result = searchHistoryDAO.deleteSearchHistory(historyId);
        if (result > 0) {
            // 重新加载搜索历史
            loadSearchHistory();
            Toast.makeText(context, "已删除搜索记录", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSearchHistory() {
        layoutSearchHistory.setVisibility(View.VISIBLE);
        rvSearchResult.setVisibility(View.GONE);
        llEmpty.setVisibility(View.GONE); // 隐藏无结果布局
        if (tvNoResult != null) {
            tvNoResult.setVisibility(View.GONE);
        }
    }

    private void showSearchResult() {
        layoutSearchHistory.setVisibility(View.GONE);
        rvSearchResult.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void dismiss() {
        // 移除：DiaryDAO和SearchHistoryDAO没有close()方法，删除以下两行
        // if (diaryDAO != null) {
        //     diaryDAO.close();
        // }
        // if (searchHistoryDAO != null) {
        //     searchHistoryDAO.close();
        // }
        hideKeyboard();
        super.dismiss();
    }
}