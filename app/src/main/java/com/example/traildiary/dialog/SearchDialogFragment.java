package com.example.traildiary.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traildiary.R;
import com.example.traildiary.adapter.SearchHistoryAdapter;
import com.example.traildiary.adapter.DiaryListAdapter;
import com.example.traildiary.model.Diary;
import com.example.traildiary.model.SearchHistory;
import com.example.traildiary.database.SearchHistoryDAO;
import com.example.traildiary.database.DiaryDAO;
import com.example.traildiary.utils.SharedPreferencesUtil;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SearchDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_SEARCH_TYPE = "search_type";

    private EditText etSearch;
    private ImageView ivClear;
    private TextView tvRecentTitle;
    private FlexboxLayout flexboxHistory;
    private TextView tvResultTitle;
    private RecyclerView rvSearchResult;
    private LinearLayout llEmpty;

    private SearchHistoryAdapter historyAdapter;
    private DiaryListAdapter resultAdapter;
    private List<SearchHistory> historyList = new ArrayList<>();
    private List<Diary> resultList = new ArrayList<>();

    private SearchHistoryDAO searchHistoryDAO;
    private DiaryDAO diaryDAO;
    private SharedPreferencesUtil spUtil;

    private OnSearchResultClickListener listener;

    public interface OnSearchResultClickListener {
        void onDiaryClick(Diary diary);
        void onSearchHistoryClick(String keyword);
    }

    public static SearchDialogFragment newInstance(int searchType) {
        SearchDialogFragment fragment = new SearchDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SEARCH_TYPE, searchType);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnSearchResultClickListener(OnSearchResultClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initData();
        initAdapters();
        loadSearchHistory();
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        ivClear = view.findViewById(R.id.iv_clear);
        tvRecentTitle = view.findViewById(R.id.tv_recent_title);
        flexboxHistory = view.findViewById(R.id.flexbox_history);
        tvResultTitle = view.findViewById(R.id.tv_result_title);
        rvSearchResult = view.findViewById(R.id.rv_search_result);
        llEmpty = view.findViewById(R.id.ll_empty);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        ivClear.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
        });
    }

    private void initData() {
        searchHistoryDAO = new SearchHistoryDAO(requireContext());
        diaryDAO = new DiaryDAO(requireContext());
        spUtil = SharedPreferencesUtil.getInstance(requireContext());
    }

    private void initAdapters() {
        // 核心修复：补充Context参数（requireContext()）
        resultAdapter = new DiaryListAdapter(requireContext(), resultList);
        resultAdapter.setOnItemClickListener(diary -> {
            if (listener != null) {
                listener.onDiaryClick(diary);
            }
            dismiss();
        });

        rvSearchResult.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearchResult.setAdapter(resultAdapter);
    }

    private void loadSearchHistory() {
        historyList = searchHistoryDAO.getRecentSearchHistory(spUtil.getCurrentUserId());
        if (!historyList.isEmpty()) {
            tvRecentTitle.setVisibility(View.VISIBLE);
            flexboxHistory.removeAllViews();

            for (SearchHistory history : historyList) {
                TextView tvHistory = (TextView) LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_search_history, flexboxHistory, false);
                tvHistory.setText(history.getKeyword());
                tvHistory.setOnClickListener(v -> {
                    etSearch.setText(history.getKeyword());
                    etSearch.setSelection(history.getKeyword().length());
                    performSearch();
                });

                ImageView ivDelete = tvHistory.findViewById(R.id.btn_delete);
                ivDelete.setOnClickListener(v -> {
                    searchHistoryDAO.deleteSearchHistory(history.getId());
                    flexboxHistory.removeView(tvHistory);
                    if (flexboxHistory.getChildCount() == 0) {
                        tvRecentTitle.setVisibility(View.GONE);
                    }
                });

                flexboxHistory.addView(tvHistory);
            }
        } else {
            tvRecentTitle.setVisibility(View.GONE);
        }
    }

    private void performSearch() {
        String keyword = etSearch.getText().toString().trim();
        if (keyword.isEmpty()) {
            return;
        }

        // 保存搜索记录
        int userId = spUtil.getCurrentUserId();
        searchHistoryDAO.addSearchHistory(new SearchHistory(userId, keyword));

        // 执行搜索
        int searchType = getArguments() != null ? getArguments().getInt(ARG_SEARCH_TYPE, 0) : 0;
        resultList.clear();

        switch (searchType) {
            case 1: // 按作者
                resultList = diaryDAO.searchByAuthor(keyword, userId);
                break;
            case 2: // 按标题
                resultList = diaryDAO.searchByTitle(keyword, userId);
                break;
            case 3: // 按类别
                resultList = diaryDAO.searchByCategory(keyword, userId);
                break;
            default: // 综合搜索
                resultList = diaryDAO.searchAll(keyword, userId);
                break;
        }

        updateResultUI();
    }

    private void updateResultUI() {
        if (resultList.isEmpty()) {
            llEmpty.setVisibility(View.VISIBLE);
            tvResultTitle.setVisibility(View.GONE);
            rvSearchResult.setVisibility(View.GONE);
        } else {
            llEmpty.setVisibility(View.GONE);
            tvResultTitle.setVisibility(View.VISIBLE);
            rvSearchResult.setVisibility(View.VISIBLE);
            resultAdapter.updateData(resultList);
        }
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        listener = null;
    }
}