package com.example.traildiary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traildiary.R;
import com.example.traildiary.database.SearchHistoryDAO;
import com.example.traildiary.model.SearchHistory;
import com.example.traildiary.utils.SharedPreferencesUtil;
import com.example.traildiary.utils.DateUtil;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {

    private Context context;
    private List<SearchHistory> historyList;
    private OnSearchHistoryClickListener listener;
    private SharedPreferencesUtil spUtil;
    private SearchHistoryDAO searchHistoryDAO;

    public interface OnSearchHistoryClickListener {
        void onSearchHistoryClick(String keyword);
        void onDeleteHistoryClick(int historyId, int position);
    }

    public SearchHistoryAdapter(Context context, List<SearchHistory> historyList,
                                OnSearchHistoryClickListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
        this.spUtil = SharedPreferencesUtil.getInstance(context);
    }

    public void setSearchHistoryDAO(SearchHistoryDAO dao) {
        this.searchHistoryDAO = dao;
    }

    public void updateData(List<SearchHistory> newHistoryList) {
        this.historyList = newHistoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchHistory history = historyList.get(position);

        // 设置搜索关键词
        holder.tvKeyword.setText(history.getKeyword());

        // 设置搜索类型
        String searchType = getSearchTypeText(history.getSearchType());
        holder.tvSearchType.setText(searchType);

        // 修复：替换getFriendlyTime为适配org.threeten.bp.LocalDateTime的实现
        String timeStr = getFriendlyTime(history.getSearchTime());
        holder.tvSearchTime.setText(timeStr);

        // 点击整个item进行搜索
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSearchHistoryClick(history.getKeyword());
            }
        });

        // 点击删除按钮
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteHistoryClick(history.getId(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList == null ? 0 : historyList.size();
    }

    // 获取搜索类型对应的文本
    private String getSearchTypeText(int searchType) {
        switch (searchType) {
            case SearchHistory.TYPE_AUTHOR:
                return "按作者";
            case SearchHistory.TYPE_TITLE:
                return "按标题";
            case SearchHistory.TYPE_CATEGORY:
                return "按类别";
            default:
                return "综合";
        }
    }

    // 新增：适配org.threeten.bp.LocalDateTime的友好时间格式化方法
    private String getFriendlyTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(time, now);
        long hours = ChronoUnit.HOURS.between(time, now);
        long days = ChronoUnit.DAYS.between(time, now);

        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (hours < 24) {
            return hours + "小时前";
        } else if (days < 7) {
            return days + "天前";
        } else {
            // 超过一周显示具体日期
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");
            return time.format(formatter);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyword;
        TextView tvSearchType;
        TextView tvSearchTime;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKeyword = itemView.findViewById(R.id.tv_keyword);
            tvSearchType = itemView.findViewById(R.id.tv_search_type);
            tvSearchTime = itemView.findViewById(R.id.tv_search_time);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}