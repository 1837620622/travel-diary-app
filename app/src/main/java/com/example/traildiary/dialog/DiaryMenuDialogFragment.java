package com.example.traildiary.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.traildiary.R;
import com.example.traildiary.model.Diary;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.Serializable;

public class DiaryMenuDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_DIARY = "diary";

    private Diary diary;
    private OnMenuClickListener listener;

    public interface OnMenuClickListener {
        void onEditClick(Diary diary);
        void onDeleteClick(Diary diary);
    }

    public static DiaryMenuDialogFragment newInstance(Diary diary) {
        DiaryMenuDialogFragment fragment = new DiaryMenuDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DIARY, (Serializable) diary);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_diary_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            diary = (Diary) getArguments().getSerializable(ARG_DIARY);
        }

        Button btnEdit = view.findViewById(R.id.btn_edit);
        Button btnDelete = view.findViewById(R.id.btn_delete);

        btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(diary);
            }
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(diary);
            }
            dismiss();
        });
    }
}