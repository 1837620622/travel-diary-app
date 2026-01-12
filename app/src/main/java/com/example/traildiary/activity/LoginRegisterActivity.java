
package com.example.traildiary.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.traildiary.R;
import com.example.traildiary.database.UserDAO;
import com.example.traildiary.model.User;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.ImageUtil;
import com.example.traildiary.utils.SharedPreferencesUtil;
import com.example.traildiary.utils.ValidationUtil;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class LoginRegisterActivity extends AppCompatActivity {

    // 登录界面组件
    private EditText etLoginAccount, etLoginPassword;
    private CheckBox cbAutoLogin;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegisterLink;

    // 注册界面组件
    private TextView tvLoginLink;
    private EditText etRegisterNickname, etRegisterTrailNumber, etRegisterPassword;
    private EditText etRegisterConfirmPassword, etRegisterPhone, etRegisterSignature;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnRegister;
    private Spinner spYear, spMonth, spDay;

    // 布局引用
    private View loginView, registerView;

    // 工具类
    private UserDAO userDAO;
    private SharedPreferencesUtil spUtil;

    // 变量
    private boolean isLoginMode = true;
    private byte[] avatarBytes = null;
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_CAMERA = 101;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 102;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 103;

    // 默认头像资源ID数组
    private int[] defaultAvatarResIds = {
            R.drawable.avatar1,
            R.drawable.avatar2,
            R.drawable.avatar3,
            R.drawable.avatar4,
            R.drawable.avatar5
    };

    // 年、月、日数据
    private List<String> yearList = new ArrayList<>();
    private List<String> monthList = new ArrayList<>();
    private List<String> dayList = new ArrayList<>();

    // 当前选择的年月日
    private int selectedYear = 2000;
    private int selectedMonth = 1;
    private int selectedDay = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoginViews();
        initRegisterViews();
        // 初始化数据库和工具类
        userDAO = new UserDAO(this);
        spUtil = new SharedPreferencesUtil(this);
        // 检查是否已登录
        checkAutoLogin();
        // 设置事件监听器
        // 根据当前模式初始化界面
        if (isLoginMode) {
            initLoginViews();
            setupLoginListeners();
        } else {
            initRegisterViews();
            setupRegisterListeners();
            initBirthdaySpinners();  // 只在注册界面需要
        }
        // 初始化生日下拉菜单
        initBirthdaySpinners();
    }

    private void initLoginViews() {
        setContentView(R.layout.activity_login);

        // 获取登录界面的组件
        etLoginAccount = findViewById(R.id.et_login_account);
        etLoginPassword = findViewById(R.id.et_login_password);
        cbAutoLogin = findViewById(R.id.cb_auto_login);
        btnLogin = findViewById(R.id.btn_login);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvRegisterLink = findViewById(R.id.tv_register_link);

        loginView = findViewById(android.R.id.content);
    }

    private void initRegisterViews() {
        setContentView(R.layout.activity_register);

        // 获取注册界面的组件
        tvLoginLink = findViewById(R.id.tv_login_link);
        etRegisterNickname = findViewById(R.id.et_register_nickname);
        etRegisterTrailNumber = findViewById(R.id.et_register_trail_number);
        etRegisterPassword = findViewById(R.id.et_register_password);
        etRegisterConfirmPassword = findViewById(R.id.et_register_confirm_password);
        etRegisterPhone = findViewById(R.id.et_register_phone);
        etRegisterSignature = findViewById(R.id.et_register_signature);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        btnRegister = findViewById(R.id.btn_register);

        // 生日下拉菜单组件
        spYear = findViewById(R.id.sp_year);
        spMonth = findViewById(R.id.sp_month);
        spDay = findViewById(R.id.sp_day);

        registerView = findViewById(android.R.id.content);

        // 设置默认签名
        if (TextUtils.isEmpty(etRegisterSignature.getText().toString())) {
            etRegisterSignature.setText("山川为印，时光为笔。");
        }
    }

    private void setupLoginListeners() {
        // 登录按钮
        btnLogin.setOnClickListener(v -> performLogin());

        // 忘记密码
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // 注册链接
        tvRegisterLink.setOnClickListener(v -> switchToRegisterMode());
    }

    private void setupRegisterListeners() {
        // 注册按钮
        btnRegister.setOnClickListener(v -> performRegistration());

        // 登录链接
        tvLoginLink.setOnClickListener(v -> switchToLoginMode());
    }

    private void checkAutoLogin() {
        boolean isAutoLogin = spUtil.getBoolean(Constants.SP_KEY_AUTO_LOGIN, false);
        String savedUsername = spUtil.getString(Constants.SP_KEY_USERNAME, "");
        String savedPassword = spUtil.getString(Constants.SP_KEY_PASSWORD, "");

        if (isAutoLogin && !TextUtils.isEmpty(savedUsername) && !TextUtils.isEmpty(savedPassword)) {
            if (userDAO == null) {
                userDAO = new UserDAO(this);
            }
            // 尝试自动登录
            User user = userDAO.login(savedUsername, savedPassword);
            if (user != null) {
                // 登录成功，跳转到首页
                startMainActivity(user);
            }
        }
    }

    private void switchToLoginMode() {
        if (isLoginMode) return;

        isLoginMode = true;
        if (userDAO == null) {
            userDAO = new UserDAO(this);
        }
        initLoginViews();
    }

    private void switchToRegisterMode() {
        if (!isLoginMode) return;

        isLoginMode = false;
        if (userDAO == null) {
            userDAO = new UserDAO(this);
        }
        initRegisterViews();
        setupRegisterListeners();
        initBirthdaySpinners();
        // 设置随机默认头像
        setRandomDefaultAvatar();
    }

    private void initBirthdaySpinners() {
        // 初始化年份数据（当前年份往前推100年）
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        for (int year = currentYear - 100; year <= currentYear; year++) {
            yearList.add(String.valueOf(year));
        }

        // 初始化月份数据
        for (int month = 1; month <= 12; month++) {
            monthList.add(String.format("%02d", month));
        }

        // 设置年份适配器
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, yearList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYear.setAdapter(yearAdapter);

        // 设置月份适配器
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, monthList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);

        // 默认选择当前年份和1月份
        int defaultYearIndex = yearList.indexOf(String.valueOf(currentYear - 20)); // 默认选择20岁的年龄
        if (defaultYearIndex != -1) {
            spYear.setSelection(defaultYearIndex);
            selectedYear = currentYear - 20;
        }

        spMonth.setSelection(0); // 1月份

        // 初始化日期数据
        updateDayList();

        // 设置日期适配器
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdapter);

        spDay.setSelection(0); // 1号

        // 设置年份选择监听
        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(yearList.get(position));
                updateDayList();
                updateDaySpinnerAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不处理
            }
        });

        // 设置月份选择监听
        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = Integer.parseInt(monthList.get(position));
                updateDayList();
                updateDaySpinnerAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不处理
            }
        });

        // 设置日期选择监听
        spDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDay = Integer.parseInt(dayList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不处理
            }
        });
    }

    private void updateDayList() {
        dayList.clear();

        // 根据年月计算天数
        int daysInMonth = getDaysInMonth(selectedYear, selectedMonth);

        for (int day = 1; day <= daysInMonth; day++) {
            dayList.add(String.format("%02d", day));
        }
    }

    private void updateDaySpinnerAdapter() {
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, dayList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDay.setAdapter(dayAdapter);

        // 如果之前选择的天数大于当前月份的天数，选择最后一天
        if (selectedDay > dayList.size()) {
            spDay.setSelection(dayList.size() - 1);
            selectedDay = dayList.size();
        } else if (selectedDay >= 1) {
            spDay.setSelection(selectedDay - 1);
        } else {
            spDay.setSelection(0);
        }
    }

    private int getDaysInMonth(int year, int month) {
        switch (month) {
            case 2:
                // 闰年判断
                if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
                    return 29;
                } else {
                    return 28;
                }
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            default:
                return 31;
        }
    }

    private void setRandomDefaultAvatar() {
        // 从五张默认头像中随机选择一张
        Random random = new Random();
        int randomIndex = random.nextInt(defaultAvatarResIds.length);
        int selectedAvatarResId = defaultAvatarResIds[randomIndex];

        // 将选中的头像转换为字节数组
        Bitmap defaultAvatarBitmap = BitmapFactory.decodeResource(getResources(), selectedAvatarResId);
        // 压缩并保存为字节数组
        avatarBytes = ImageUtil.compressImage(defaultAvatarBitmap);

        // 同时保存资源ID到SharedPreferences，以便后续使用
        spUtil.putInt(Constants.SP_KEY_AVATAR_RES_ID, selectedAvatarResId);
    }

    private void performLogin() {
        String account = etLoginAccount.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        // 验证输入
        if (TextUtils.isEmpty(account)) {
            showToast("请输入账号");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            showToast("请输入密码");
            return;
        }

        // 执行登录
        User user = userDAO.login(account, password);

        if (user != null) {
            // 登录成功
            showToast("登录成功");

            // 保存自动登录信息
            boolean autoLogin = cbAutoLogin.isChecked();
            spUtil.putBoolean(Constants.SP_KEY_AUTO_LOGIN, autoLogin);
            if (autoLogin) {
                spUtil.putString(Constants.SP_KEY_USERNAME, account);
                spUtil.putString(Constants.SP_KEY_PASSWORD, password);
            }

            // 保存当前用户信息
            spUtil.putInt(Constants.SP_KEY_USER_ID, user.getUserId());
            spUtil.putString(Constants.SP_KEY_NICKNAME, user.getNickname());
            spUtil.putString(Constants.SP_KEY_TRAIL_NUMBER, user.getTrailNumber());

            // 跳转到首页
            startMainActivity(user);
        } else {
            showToast("账号或密码错误");
        }
    }

    private void performRegistration() {
        // 获取用户输入
        String nickname = etRegisterNickname.getText().toString().trim();
        String trailNumber = etRegisterTrailNumber.getText().toString().trim();
        String password = etRegisterPassword.getText().toString().trim();
        String confirmPassword = etRegisterConfirmPassword.getText().toString().trim();
        String phone = etRegisterPhone.getText().toString().trim();
        String signature = etRegisterSignature.getText().toString().trim();

        // 从下拉菜单获取生日
        String yearStr = (String) spYear.getSelectedItem();
        String monthStr = (String) spMonth.getSelectedItem();
        String dayStr = (String) spDay.getSelectedItem();

        String birthday = String.format("%s-%s-%s", yearStr, monthStr, dayStr);

        int gender = rbMale.isChecked() ? 1 : 0; // 1:男, 0:女

        // 验证输入
        if (!validateRegistrationInput(nickname, trailNumber, password, confirmPassword, phone)) {
            return;
        }

        // 创建用户对象
        User user = new User();
        user.setNickname(nickname);
        user.setTrailNumber(trailNumber);
        user.setPassword(password);
        user.setPhone(phone);
        user.setSignature(TextUtils.isEmpty(signature) ? "山川为印，时光为笔。" : signature);
        user.setGender(String.valueOf(gender));
        user.setBirthday(LocalDate.parse(birthday));
        user.setCreateTime(LocalDateTime.now());

        // 保存到数据库 - 使用支持字节数组的方法
        long userId = userDAO.addUser(user, avatarBytes);

        if (userId != -1) {
            showToast("注册成功");
            user.setUserId((int) userId);

            // 自动登录
            spUtil.putBoolean(Constants.SP_KEY_AUTO_LOGIN, true);
            spUtil.putString(Constants.SP_KEY_USERNAME, trailNumber);
            spUtil.putString(Constants.SP_KEY_PASSWORD, password);
            spUtil.putInt(Constants.SP_KEY_USER_ID, user.getUserId());
            spUtil.putString(Constants.SP_KEY_NICKNAME, user.getNickname());
            spUtil.putString(Constants.SP_KEY_TRAIL_NUMBER, user.getTrailNumber());

            // 跳转到首页
            startMainActivity(user);
        } else {
            showToast("注册失败，请重试");
        }
    }

    private boolean validateRegistrationInput(String nickname, String trailNumber,
                                              String password, String confirmPassword, String phone) {

        // 验证昵称
        if (TextUtils.isEmpty(nickname)) {
            showToast("请输入昵称");
            return false;
        }

        if (!ValidationUtil.isNicknameValid(nickname)) {
            showToast("昵称格式不正确（2-20位汉字、字母或数字）");
            return false;
        }

        // 检查昵称是否已存在
        if (userDAO.isNicknameExists(nickname)) {
            showToast("该昵称已存在");
            return false;
        }

        // 验证途迹号
        if (TextUtils.isEmpty(trailNumber)) {
            showToast("请输入途迹号");
            return false;
        }

        if (!ValidationUtil.isTrailNumberValid(trailNumber)) {
            showToast("途迹号格式不正确（6-20位字母或数字）");
            return false;
        }

        // 检查途迹号是否已存在
        if (userDAO.isTrailNumberExists(trailNumber)) {
            showToast("该途迹号已存在");
            return false;
        }

        // 验证密码
        if (TextUtils.isEmpty(password)) {
            showToast("请输入密码");
            return false;
        }

        if (!ValidationUtil.isPasswordValid(password)) {
            showToast("密码格式不正确（6-20位字母或数字）");
            return false;
        }

        // 验证确认密码
        if (!password.equals(confirmPassword)) {
            showToast("两次输入的密码不一致");
            return false;
        }

        // 验证手机号
        if (!TextUtils.isEmpty(phone) && !ValidationUtil.isPhoneValid(phone)) {
            showToast("手机号格式不正确");
            return false;
        }

        return true;
    }

    private void showAvatarSelectionDialog() {
        // 检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
            return;
        }

        // 创建选择对话框
        String[] options = {"拍照", "从相册选择", "取消"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("选择头像");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // 拍照
                    openCamera();
                    break;
                case 1: // 从相册选择
                    openGallery();
                    break;
                case 2: // 取消
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA_PERMISSION);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void showForgotPasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("找回密码");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etVerificationCode = dialogView.findViewById(R.id.et_verification_code);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        Button btnSendCode = dialogView.findViewById(R.id.btn_send_code);

        builder.setView(dialogView);
        builder.setPositiveButton("确定", (dialog, which) -> {
            // 1. 提取输入内容
            String phone = etPhone.getText().toString().trim();
            String code = etVerificationCode.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();

            // 2. 分步非空校验（更友好的提示）
            if (TextUtils.isEmpty(phone)) {
                showToast("请输入手机号");
                return;
            }
            if (TextUtils.isEmpty(code)) {
                showToast("请输入验证码");
                return;
            }
            if (TextUtils.isEmpty(newPassword)) {
                showToast("请输入新密码");
                return;
            }

            // 3. 可选：密码复杂度校验（提升安全性）
            if (newPassword.length() < 6) {
                showToast("新密码至少6位，请重新设置");
                return;
            }

            // 4. 验证码验证（真实项目替换为短信SDK的校验接口）
            // 简化模拟：正确验证码暂定为123456
            boolean isCodeValid = "123456".equals(code);
            if (!isCodeValid) {
                showToast("验证码错误，请重新输入");
                return;
            }

            // 5. 调用DAO通过手机号重置密码的方法（核心逻辑）
            int resetResult = userDAO.resetPassword(phone, newPassword);
            if (resetResult > 0) {
                showToast("密码重置成功");
                dialog.dismiss(); // 重置成功后关闭弹窗
            } else {
                showToast("手机号不存在");
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
        btnSendCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                showToast("请输入手机号");
                return;
            }

            // 这里应该调用短信验证码发送接口
            // 暂时简化处理
            btnSendCode.setEnabled(false);
            btnSendCode.setText("60s后重发");

            new Thread(() -> {
                for (int i = 60; i > 0; i--) {
                    final int count = i;
                    runOnUiThread(() -> btnSendCode.setText(count + "s后重发"));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText("发送验证码");
                });
            }).start();

            showToast("验证码已发送");
        });
    }

    private void startMainActivity(User user) {
        Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && !isLoginMode) {
            switch (requestCode) {
                case REQUEST_CODE_PICK_IMAGE:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    getContentResolver(), selectedImage);

                            // 压缩图片并保存为字节数组
                            avatarBytes = ImageUtil.compressImage(bitmap);


                        } catch (IOException e) {
                            e.printStackTrace();
                            showToast("图片选择失败");
                        }
                    }
                    break;

                case REQUEST_CODE_CAMERA:
                    if (data != null && data.getExtras() != null) {
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                        // 压缩图片并保存为字节数组
                        avatarBytes = ImageUtil.compressImage(bitmap);

                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CODE_STORAGE_PERMISSION:
                    openGallery();
                    break;
                case REQUEST_CODE_CAMERA_PERMISSION:
                    openCamera();
                    break;
            }
        } else {
            showToast("需要相关权限才能使用该功能");
        }
    }
}