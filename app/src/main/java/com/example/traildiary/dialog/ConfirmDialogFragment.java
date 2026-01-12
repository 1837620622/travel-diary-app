package com.example.traildiary.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.traildiary.R;

public class ConfirmDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_POSITIVE_TEXT = "positive_text";
    private static final String ARG_NEGATIVE_TEXT = "negative_text";

    private OnDialogClickListener listener;

    public interface OnDialogClickListener {
        void onPositiveClick();
        void onNegativeClick();
    }

    public static ConfirmDialogFragment newInstance(String title, String message,
                                                    String positiveText, String negativeText) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_POSITIVE_TEXT, positiveText);
        args.putString(ARG_NEGATIVE_TEXT, negativeText);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDialogClickListener(OnDialogClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);

        Bundle args = getArguments();
        if (args != null) {
            tvTitle.setText(args.getString(ARG_TITLE, "提示"));
            tvMessage.setText(args.getString(ARG_MESSAGE, ""));
            btnCancel.setText(args.getString(ARG_NEGATIVE_TEXT, "取消"));
            btnConfirm.setText(args.getString(ARG_POSITIVE_TEXT, "确定"));
        }

        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNegativeClick();
            }
            dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPositiveClick();
            }
            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        listener = null;
    }
}