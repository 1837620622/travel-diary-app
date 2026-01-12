package com.example.traildiary.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.traildiary.R;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.PermissionUtil;
import com.example.traildiary.utils.SharedPreferencesUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    private Button btnAgree;
    private TextView tvDisagree;
    private TextView tvAgreementContent;

    private SharedPreferencesUtil spUtil;
    private PermissionUtil permissionUtil;

    // 使用线程池执行后台任务
    private ExecutorService executorService;
    private Handler mainHandler;

    private static final int PERMISSION_REQUEST_CODE = 1001;
    // 精简权限列表，只保留必要的权限
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 先显示UI，让用户能够看到界面
        initView();
        setAgreementTextWithClickableLinks();
        setListeners();

        // 初始化工具类（轻量级）
        spUtil = SharedPreferencesUtil.getInstance(this);
        permissionUtil = PermissionUtil.getInstance(this);

        // 初始化线程池和主线程Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // 延迟检查首次运行，避免阻塞UI线程
        new Handler().postDelayed(() -> {
            checkFirstRunAsync();
        }, 500); // 延迟500ms确保UI完全加载
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    /**
     * 初始化视图
     */
    private void initView() {
        btnAgree = findViewById(R.id.btn_agree);
        tvDisagree = findViewById(R.id.tv_disagree);
        tvAgreementContent = findViewById(R.id.tv_agreement_content);
    }

    /**
     * 设置带有可点击链接的协议文本
     */
    private void setAgreementTextWithClickableLinks() {
        try {
            String fullText = getString(R.string.splash_text);
            SpannableString spannableString = new SpannableString(fullText);
            addClickableSpans(spannableString);
            tvAgreementContent.setText(spannableString);
            tvAgreementContent.setMovementMethod(LinkMovementMethod.getInstance());
            tvAgreementContent.setHighlightColor(Color.TRANSPARENT);
        } catch (Exception e) {
            // 设置默认文本，避免因资源问题导致ANR
            tvAgreementContent.setText("欢迎使用途迹日记");
        }
    }

    /**
     * 为SpannableString添加可点击的协议链接
     */
    private void addClickableSpans(SpannableString spannableString) {
        String text = spannableString.toString();

        // 用户协议点击事件
        int userAgreementStart = text.indexOf("《用户协议》");
        int userAgreementEnd = userAgreementStart + "《用户协议》".length();

        if (userAgreementStart >= 0) {
            spannableString.setSpan(new CustomClickableSpan(this, UserAgreementActivity.class),
                    userAgreementStart, userAgreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 隐私政策点击事件
        int privacyPolicyStart = text.indexOf("《隐私政策》");
        int privacyPolicyEnd = privacyPolicyStart + "《隐私政策》".length();

        if (privacyPolicyStart >= 0) {
            spannableString.setSpan(new CustomClickableSpan(this, PrivacyPolicyActivity.class),
                    privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 自定义 ClickableSpan 类
     */
    private class CustomClickableSpan extends ClickableSpan {
        private Context context;
        private Class<?> targetActivity;

        public CustomClickableSpan(Context context, Class<?> targetActivity) {
            this.context = context;
            this.targetActivity = targetActivity;
        }

        @Override
        public void onClick(View widget) {
            // 防止快速多次点击
            widget.setEnabled(false);
            new Handler().postDelayed(() -> widget.setEnabled(true), 1000);

            Intent intent = new Intent(context, targetActivity);
            startActivity(intent);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(Color.parseColor("#2196F3"));
            ds.setUnderlineText(true);
        }
    }

    /**
     * 异步检查是否是第一次运行
     */
    private void checkFirstRunAsync() {
        executorService.execute(() -> {
            try {
                boolean isFirstRun = spUtil.getBoolean(Constants.SP_KEY_FIRST_RUN, true);
                boolean isAgreed = spUtil.getBoolean(Constants.SP_KEY_AGREEMENT_AGREED, false);

                // 如果不是第一次运行且已经同意协议，直接跳转到登录注册页
                if (!isFirstRun && isAgreed) {
                    mainHandler.postDelayed(() -> {
                        navigateToLoginRegister();
                    }, 1000);
                } else {
                    // 标记为第一次运行
                    spUtil.putBoolean(Constants.SP_KEY_FIRST_RUN, false);
                }
            } catch (Exception e) {
                // 异常处理，确保不会阻塞主线程
                mainHandler.post(() -> {
                    navigateToLoginRegister();
                });
            }
        });
    }

    /**
     * 设置监听器
     */
    private void setListeners() {
        // 同意按钮点击事件
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 防止快速多次点击
                v.setEnabled(false);
                onAgreeClicked();
                new Handler().postDelayed(() -> v.setEnabled(true), 2000);
            }
        });

        // 不同意文本点击事件
        tvDisagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 防止快速多次点击
                v.setEnabled(false);
                showDisagreeDialog();
                new Handler().postDelayed(() -> v.setEnabled(true), 1000);
            }
        });
    }

    /**
     * 同意按钮点击处理
     */
    private void onAgreeClicked() {
        // 保存用户同意状态
        spUtil.putBoolean(Constants.SP_KEY_AGREEMENT_AGREED, true);

        // 异步检查权限
        executorService.execute(() -> {
            boolean hasAllPermissions = true;
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(SplashActivity.this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermissions = false;
                    break;
                }
            }

            if (hasAllPermissions) {
                mainHandler.post(() -> {
                    navigateToLoginRegister();
                });
            } else {
                mainHandler.post(() -> {
                    requestPermissions();
                });
            }
        });
    }

    /**
     * 显示不同意对话框
     */
    private void showDisagreeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("咩～加入我们吧！")
                .setMessage("亲爱的途友，若不同意《用户协议》、《隐私政策》，我们将无法为您提供途迹的完整功能哦～")
                .setCancelable(false)
                .setPositiveButton("同意条款并进入", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onAgreeClicked();
                    }
                })
                .setNegativeButton("放弃使用", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 请求权限
     */
    private void requestPermissions() {
        if (permissionUtil != null) {
            permissionUtil.requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE,
                    new PermissionUtil.PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            navigateToLoginRegister();
                        }

                        @Override
                        public void onDenied(List<String> deniedPermissions) {
                            // 即使权限被拒绝也允许进入，但提示用户
                            Toast.makeText(SplashActivity.this,
                                    "部分权限未授予，可能影响功能使用", Toast.LENGTH_LONG).show();
                            navigateToLoginRegister();
                        }

                        @Override
                        public void onPermanentlyDenied(List<String> permanentlyDeniedPermissions) {
                            // 权限被永久拒绝，提示但不阻塞
                            Toast.makeText(SplashActivity.this,
                                    "部分权限被永久拒绝，请在设置中手动开启", Toast.LENGTH_LONG).show();
                            navigateToLoginRegister();
                        }
                    });
        } else {
            navigateToLoginRegister();
        }
    }

    /**
     * 跳转到登录注册页
     */
    private void navigateToLoginRegister() {
        Intent intent = new Intent(SplashActivity.this, LoginRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && permissionUtil != null) {
            permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}