package com.example.traildiary.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traildiary.R;
import com.example.traildiary.model.DiaryContentItem;

import java.util.List;

public class DiaryContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DiaryContentItem> contentItems;
    private OnItemClickListener listener;

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_IMAGE = 1;

    public interface OnItemClickListener {
        void onAddImageClick(int position);
        void onDeleteClick(int position);
    }

    public DiaryContentAdapter(List<DiaryContentItem> contentItems) {
        this.contentItems = contentItems;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return contentItems.get(position).getType().ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TEXT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_content_text, parent, false);
            return new TextViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_content_image, parent, false);
            return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DiaryContentItem item = contentItems.get(position);

        if (holder instanceof TextViewHolder) {
            TextViewHolder textHolder = (TextViewHolder) holder;
            
            // 先移除旧的TextWatcher，防止重复添加导致内容覆盖
            if (textHolder.textWatcher != null) {
                textHolder.etContent.removeTextChangedListener(textHolder.textWatcher);
            }
            
            // 设置内容
            textHolder.etContent.setText(item.getContent());
            
            // 创建新的TextWatcher并保存引用
            textHolder.textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    item.setContent(s.toString());
                }
            };
            textHolder.etContent.addTextChangedListener(textHolder.textWatcher);

            textHolder.btnDelete.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(adapterPosition);
                }
            });

        } else if (holder instanceof ImageViewHolder) {
            ImageViewHolder imageHolder = (ImageViewHolder) holder;
            String imagePath = item.getImagePath();

            if (imagePath != null && !imagePath.trim().isEmpty()) {
                // 显示图片
                Glide.with(imageHolder.itemView.getContext())
                        .load(imagePath)
                        .into(imageHolder.ivImage);
                imageHolder.llAddImage.setVisibility(View.GONE);
                imageHolder.ivImage.setVisibility(View.VISIBLE);
            } else {
                // 显示添加按钮
                imageHolder.llAddImage.setVisibility(View.VISIBLE);
                imageHolder.ivImage.setVisibility(View.GONE);
            }

            imageHolder.llAddImage.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAddImageClick(adapterPosition);
                }
            });

            // 先移除旧的TextWatcher
            if (imageHolder.textWatcher != null) {
                imageHolder.etDescription.removeTextChangedListener(imageHolder.textWatcher);
            }
            
            imageHolder.etDescription.setText(item.getContent());
            
            // 创建新的TextWatcher并保存引用
            imageHolder.textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    item.setContent(s.toString());
                }
            };
            imageHolder.etDescription.addTextChangedListener(imageHolder.textWatcher);

            imageHolder.btnDelete.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(adapterPosition);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return contentItems.size();
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        EditText etContent;
        ImageButton btnDelete;
        TextWatcher textWatcher; // 保存TextWatcher引用，用于移除

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            etContent = itemView.findViewById(R.id.et_content);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llAddImage;
        ImageView ivImage;
        EditText etDescription;
        ImageButton btnDelete;
        TextWatcher textWatcher; // 保存TextWatcher引用，用于移除

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            llAddImage = itemView.findViewById(R.id.ll_add_image);
            ivImage = itemView.findViewById(R.id.iv_image);
            etDescription = itemView.findViewById(R.id.et_description);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}