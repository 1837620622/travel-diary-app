package com.example.traildiary.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
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
import com.example.traildiary.activity.NotebookDetailActivity;
import com.example.traildiary.activity.NotebookSettingActivity;
import com.example.traildiary.model.Notebook;

import java.util.List;

public class NotebookAdapter extends RecyclerView.Adapter<NotebookAdapter.ViewHolder> {

    private Context context;
    private List<Notebook> notebookList;
    private boolean showSettings = false; // 是否显示设置图标
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onSettingsClick(int position);
    }

    public NotebookAdapter(Context context, List<Notebook> notebookList) {
        this.context = context;
        this.notebookList = notebookList;
    }

    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Notebook> notebookList) {
        this.notebookList = notebookList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notebook, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notebook notebook = notebookList.get(position);

        // 设置日记本名称
        holder.tvNotebookName.setText(notebook.getName());

        // 设置日记篇数
        holder.tvDiaryCount.setText("共" + notebook.getDiaryCount() + "篇");

        // 加载封面图片
        String coverPath = notebook.getCoverPath();
        
        Log.d("NotebookAdapter", "加载封面: name=" + notebook.getName() 
                + ", coverPath=" + coverPath);
        
        // 判断是否为自定义封面（文件路径以/开头）
        if (coverPath != null && coverPath.startsWith("/")) {
            // 自定义封面：使用Glide从文件加载图片
            Log.d("NotebookAdapter", "加载自定义封面文件: " + coverPath);
            java.io.File coverFile = new java.io.File(coverPath);
            if (coverFile.exists()) {
                Glide.with(context)
                        .load(coverFile)
                        .centerCrop()
                        .into(holder.ivCover);
            } else {
                // 文件不存在，使用默认封面
                Log.w("NotebookAdapter", "封面文件不存在: " + coverPath);
                holder.ivCover.setImageResource(CoverPagerAdapter.getCoverResourceId(null));
            }
        } else {
            // 预置封面：使用资源ID
            int resId = CoverPagerAdapter.getCoverResourceId(coverPath);
            holder.ivCover.setImageResource(resId);
        }

        // 设置点击事件
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            } else {
                Intent intent = new Intent(context, NotebookDetailActivity.class);
                intent.putExtra("notebook_id", notebook.getId());
                context.startActivity(intent);
            }
        });

        // 设置按钮点击事件
        holder.ivSettings.setVisibility(showSettings ? View.VISIBLE : View.GONE);
        holder.ivSettings.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSettingsClick(position);
            } else {
                Intent intent = new Intent(context, NotebookSettingActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("notebook_id", notebook.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notebookList != null ? notebookList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivCover;
        TextView tvNotebookName;
        TextView tvDiaryCount;
        ImageView ivSettings;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvNotebookName = itemView.findViewById(R.id.tv_notebook_name);
            tvDiaryCount = itemView.findViewById(R.id.tv_diary_count);
            ivSettings = itemView.findViewById(R.id.iv_settings);
            
            // 调试日志
            Log.d("NotebookAdapter", "ViewHolder创建: ivCover=" + (ivCover != null ? "找到" : "未找到"));
        }
    }
}