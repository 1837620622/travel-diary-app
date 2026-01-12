package com.example.traildiary.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traildiary.R;
import com.example.traildiary.activity.DiaryDetailActivity;
import com.example.traildiary.model.Diary;
import com.example.traildiary.utils.DateUtil;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;

public class DiaryListAdapter extends RecyclerView.Adapter<DiaryListAdapter.ViewHolder> {

    private Context context;
    private List<Diary> diaryList;
    private boolean isGridLayout = false; // 是否网格布局（首页用网格，日记本详情用列表）

    // 添加点击监听器接口
    private OnItemClickListener onItemClickListener;

    // 修复：构造方法添加Context参数并正确赋值
    public DiaryListAdapter(Context context, List<Diary> diaryList) {
        this.context = context;
        this.diaryList = diaryList;
    }

    public void setGridLayout(boolean gridLayout) {
        isGridLayout = gridLayout;
    }

    // 设置点击监听器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 修复：区分网格/列表布局（根据实际布局文件名调整）
        int layoutResId = isGridLayout ? R.layout.item_diary : R.layout.item_diary;
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Diary diary = diaryList.get(position);

        // 封面图片 - 使用 coverImagePath
        if (diary.getCoverImagePath() != null && !diary.getCoverImagePath().isEmpty()) {
            Glide.with(context)
                    .load(diary.getCoverImagePath())
                    .placeholder(R.drawable.ic_default_cover)
                    .error(R.drawable.ic_default_cover)
                    .into(holder.ivCover);
        } else {
            // 如果没有封面图，检查是否有插图
            if (diary.getImages() != null && !diary.getImages().isEmpty()) {
                Glide.with(context)
                        .load(diary.getImages().get(0))
                        .placeholder(R.drawable.ic_default_cover)
                        .error(R.drawable.ic_default_cover)
                        .into(holder.ivCover);
            } else {
                holder.ivCover.setImageResource(R.drawable.ic_default_cover);
            }
        }

        // 标题
        holder.tvTitle.setText(diary.getTitle());

        // 分类 - 将类别代码转换为类别名称
        holder.tvCategory.setText(getCategoryName(diary.getCategory()));

        // 作者信息
        String authorName = diary.getAuthorName();
        if (authorName != null && !authorName.isEmpty()) {
            holder.tvAuthor.setText(authorName);
        } else {
            holder.tvAuthor.setText("匿名用户");
        }

        // 编辑时间 - 修复：适配org.threeten.bp.LocalDateTime
        if (diary.getUpdateTime() != null) {
            String time = formatLocalDateTime(diary.getUpdateTime(), "MM/dd");
            holder.tvTime.setText(time);
        } else if (diary.getCreateTime() != null) {
            String time = formatLocalDateTime(diary.getCreateTime(), "MM/dd");
            holder.tvTime.setText(time);
        } else {
            holder.tvTime.setText("");
        }

        // 点击事件 - 使用接口回调
        holder.cardView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(diary);
            } else {
                // 如果没有设置监听器，使用默认的跳转逻辑
                Intent intent = new Intent(context, DiaryDetailActivity.class);
                intent.putExtra("diary_id", diary.getDiaryId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return diaryList == null ? 0 : diaryList.size();
    }

    public void updateData(List<Diary> newList) {
        this.diaryList = newList;
        notifyDataSetChanged();
    }

    public void addData(List<Diary> newItems) {
        if (diaryList == null) {
            diaryList = newItems;
        } else {
            diaryList.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    // 点击监听器接口
    public interface OnItemClickListener {
        void onItemClick(Diary diary);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivCover;
        TextView tvTitle;
        TextView tvCategory;
        TextView tvAuthor;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }

    // 新增：适配org.threeten.bp.LocalDateTime的格式化方法
    private String formatLocalDateTime(org.threeten.bp.LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    // 将类别代码转换为类别名称
    private String getCategoryName(String categoryCode) {
        if (categoryCode == null || categoryCode.isEmpty()) {
            return "未分类";
        }
        
        try {
            int code = Integer.parseInt(categoryCode);
            switch (code) {
                case 1:
                    return "国内游";
                case 2:
                    return "国际游";
                case 3:
                    return "亲子游";
                case 4:
                    return "美食之旅";
                case 5:
                    return "探险之旅";
                case 6:
                    return "文化之旅";
                default:
                    return "未分类";
            }
        } catch (NumberFormatException e) {
            // 如果不是数字，可能已经是类别名称，直接返回
            return categoryCode;
        }
    }
}