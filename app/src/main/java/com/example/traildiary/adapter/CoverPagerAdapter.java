package com.example.traildiary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.traildiary.R;

import java.util.List;

/**
 * 封面选择适配器
 * 支持预置封面的左右滑动选择
 * 第一项为"自动生成封面"选项
 */
public class CoverPagerAdapter extends PagerAdapter {

    // 自动生成封面的标识
    public static final String AUTO_GENERATE_COVER = "auto";
    
    // 自定义封面的标识
    public static final String CUSTOM_COVER = "custom";

    // 预置封面资源数组
    private static final int[] COVER_RESOURCES = {
            R.drawable.cover1,
            R.drawable.cover2,
            R.drawable.cover3,
            R.drawable.cover4,
            R.drawable.cover5
    };

    private Context context;
    private List<String> coverPaths;
    private OnCoverClickListener listener;
    private OnCustomCoverClickListener customCoverListener;
    private int selectedPosition = 0;
    private String customCoverImagePath = null; // 自定义封面的图片路径

    public interface OnCoverClickListener {
        void onCoverClick(String coverPath);
    }
    
    // 自定义封面点击回调接口
    public interface OnCustomCoverClickListener {
        void onCustomCoverClick();
    }

    public CoverPagerAdapter(Context context, List<String> coverPaths) {
        this.context = context;
        this.coverPaths = coverPaths;
    }

    public void setOnCoverClickListener(OnCoverClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnCustomCoverClickListener(OnCustomCoverClickListener listener) {
        this.customCoverListener = listener;
    }
    
    /**
     * 设置自定义封面图片路径
     */
    public void setCustomCoverImagePath(String path) {
        this.customCoverImagePath = path;
        notifyDataSetChanged();
    }
    
    /**
     * 获取自定义封面图片路径
     */
    public String getCustomCoverImagePath() {
        return customCoverImagePath;
    }
    
    /**
     * 判断是否为自定义封面
     */
    public static boolean isCustomCover(String coverPath) {
        return CUSTOM_COVER.equals(coverPath);
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return coverPaths != null ? coverPaths.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cover, container, false);
        ImageView ivCover = view.findViewById(R.id.iv_cover);
        LinearLayout llAutoGenerate = view.findViewById(R.id.ll_auto_generate);
        LinearLayout llCustomCover = view.findViewById(R.id.ll_custom_cover);
        ImageView ivCustomPreview = view.findViewById(R.id.iv_custom_preview);
        LinearLayout llCustomPlaceholder = view.findViewById(R.id.ll_custom_placeholder);
        View viewSelected = view.findViewById(R.id.view_selected);

        String coverPath = coverPaths.get(position);

        // 判断是否为自动生成封面选项
        if (AUTO_GENERATE_COVER.equals(coverPath)) {
            // 显示自动生成UI
            ivCover.setVisibility(View.GONE);
            llAutoGenerate.setVisibility(View.VISIBLE);
            llCustomCover.setVisibility(View.GONE);
        } else if (CUSTOM_COVER.equals(coverPath)) {
            // 显示自定义封面UI
            ivCover.setVisibility(View.GONE);
            llAutoGenerate.setVisibility(View.GONE);
            llCustomCover.setVisibility(View.VISIBLE);
            
            // 如果已有自定义图片，显示预览
            if (customCoverImagePath != null && !customCoverImagePath.isEmpty()) {
                ivCustomPreview.setVisibility(View.VISIBLE);
                llCustomPlaceholder.setVisibility(View.GONE);
                // 加载自定义图片
                try {
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeFile(customCoverImagePath);
                    if (bitmap != null) {
                        ivCustomPreview.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ivCustomPreview.setVisibility(View.GONE);
                    llCustomPlaceholder.setVisibility(View.VISIBLE);
                }
            } else {
                // 显示占位符（+号）
                ivCustomPreview.setVisibility(View.GONE);
                llCustomPlaceholder.setVisibility(View.VISIBLE);
            }
        } else {
            // 显示预置封面图片
            ivCover.setVisibility(View.VISIBLE);
            llAutoGenerate.setVisibility(View.GONE);
            llCustomCover.setVisibility(View.GONE);
            int resId = getCoverResourceId(coverPath);
            ivCover.setImageResource(resId);
        }

        // 显示选中状态
        viewSelected.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);

        // 设置点击事件
        view.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();

            // 如果是自定义封面，触发自定义封面点击回调
            if (CUSTOM_COVER.equals(coverPath)) {
                if (customCoverListener != null) {
                    customCoverListener.onCustomCoverClick();
                }
            }
            
            if (listener != null) {
                listener.onCoverClick(coverPath);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    /**
     * 获取封面资源ID（供外部使用）
     * @param coverPath 封面路径（数字1-5或auto）
     * @return 资源ID
     */
    public static int getCoverResourceId(String coverPath) {
        if (coverPath == null || coverPath.isEmpty() || AUTO_GENERATE_COVER.equals(coverPath)) {
            // 自动生成时随机返回一个封面
            int randomIndex = (int) (Math.random() * COVER_RESOURCES.length);
            return COVER_RESOURCES[randomIndex];
        }
        
        // 提取数字
        String numStr = coverPath.replaceAll("[^0-9]", "");
        if (!numStr.isEmpty()) {
            try {
                int num = Integer.parseInt(numStr);
                // 数字1-5对应索引0-4
                if (num >= 1 && num <= 5) {
                    return COVER_RESOURCES[num - 1];
                }
                // 索引0-4直接使用
                if (num >= 0 && num < COVER_RESOURCES.length) {
                    return COVER_RESOURCES[num];
                }
            } catch (Exception ignored) {}
        }
        
        return COVER_RESOURCES[0];
    }

    /**
     * 判断是否为自动生成封面
     */
    public static boolean isAutoGenerate(String coverPath) {
        return AUTO_GENERATE_COVER.equals(coverPath);
    }

    /**
     * 获取随机封面路径（用于自动生成）
     */
    public static String getRandomCoverPath() {
        int randomNum = (int) (Math.random() * 5) + 1;
        return String.valueOf(randomNum);
    }

    @Override
    public float getPageWidth(int position) {
        return 0.85f;
    }
}