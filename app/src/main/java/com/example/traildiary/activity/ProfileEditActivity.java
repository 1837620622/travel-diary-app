package com.example.traildiary.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.traildiary.R;
import com.example.traildiary.database.DatabaseHelper;
import com.example.traildiary.database.UserDAO;
import com.example.traildiary.model.User;
import com.example.traildiary.utils.Constants;
import com.example.traildiary.utils.SharedPreferencesUtil;
import com.example.traildiary.utils.ValidationUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

public class ProfileEditActivity extends AppCompatActivity {

    private ImageView ivBack, ivProfileAvatar, ivEditAvatar;
    private TextView tvProfileNickname, tvProfileTrailNumber, tvConstellation;
    private EditText etSignature, etPhone, etBirthday;
    private TextView tvGender; // 修改：将RadioGroup改为TextView
    private Button btnSave, btnLogout;

    private SharedPreferencesUtil spUtil;
    private UserDAO userDAO;
    private User currentUser;
    private int currentUserId;

    private String selectedAvatarPath;
    private Uri selectedAvatarUri; // 新增：保存选中的头像Uri
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private String sourceFrom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        // 获取来源，默认为main（首页）
        sourceFrom = getIntent().getStringExtra("source");
        if (sourceFrom == null) {
            sourceFrom = "main"; // 默认来源
        }
        initViews();
        initData();
        setupListeners();
        loadUserData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        ivEditAvatar = findViewById(R.id.iv_edit_avatar);
        tvProfileNickname = findViewById(R.id.tv_profile_nickname);
        tvProfileTrailNumber = findViewById(R.id.tv_profile_trail_number);
        tvConstellation = findViewById(R.id.tv_constellation);
        etSignature = findViewById(R.id.et_signature);
        etPhone = findViewById(R.id.et_phone);
        etBirthday = findViewById(R.id.et_birthday);
        tvGender = findViewById(R.id.rg_gender); // XML中的id是rg_gender，但它是TextView
        btnSave = findViewById(R.id.btn_save);
        btnLogout = findViewById(R.id.btn_logout);

        // 添加性别点击事件
        View genderLayout = findViewById(R.id.gender_layout); // 需要给性别LinearLayout添加id
        if (genderLayout != null) {
            genderLayout.setOnClickListener(v -> showGenderDialog());
        }

        // 如果上面的方法不行，直接给tvGender设置点击事件
        tvGender.setOnClickListener(v -> showGenderDialog());
    }

    private void initData() {
        spUtil = SharedPreferencesUtil.getInstance(this);

        // 修改：使用正确的UserDAO构造函数
        userDAO = new UserDAO(this);

        currentUserId = spUtil.getCurrentUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        // 返回按钮 - 根据来源决定返回哪个页面
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBackButton();
            }
        });

        // 编辑头像
        ivEditAvatar.setOnClickListener(v -> showImagePickerDialog());
        ivProfileAvatar.setOnClickListener(v -> showImagePickerDialog());

        // 生日选择
        etBirthday.setOnClickListener(v -> showDatePickerDialog());

        // 保存按钮
        btnSave.setOnClickListener(v -> saveUserData());

        // 退出登录
        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
    }
    // 处理返回按钮点击的方法
    private void handleBackButton() {
        if ("home_activity".equals(sourceFrom)) {
            // 如果是从我的小屋进入，返回到HomeActivity（我的小屋页面）
            Intent intent = new Intent(ProfileEditActivity.this, HomeActivity.class);
            // 清除返回栈，确保直接返回到HomeActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            // 如果是从MainActivity进入，返回到MainActivity（首页）
            Intent intent = new Intent(ProfileEditActivity.this, MainActivity.class);
            // 清除返回栈，确保直接返回到MainActivity
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        // 结束当前Activity
        finish();
    }

    private void loadUserData() {
        if (currentUserId != -1) {
            currentUser = userDAO.getUserById(currentUserId);
            if (currentUser != null) {
                // 昵称和途迹号（不可编辑）
                tvProfileNickname.setText(currentUser.getNickname());
                tvProfileTrailNumber.setText(currentUser.getTrailNumber());

                // 签名
                String signature = currentUser.getSignature();
                if (signature != null) {
                    etSignature.setText(signature);
                }

                // 手机号
                String phone = currentUser.getPhone();
                if (phone != null) {
                    etPhone.setText(phone);
                }

                // 性别 - 修复：将数字转换为中文显示
                String gender = currentUser.getGender();
                if (gender != null) {
                    // 将 "1" 或 "0" 转换为 "男" 或 "女"
                    if ("1".equals(gender)) {
                        tvGender.setText("男");
                    } else if ("0".equals(gender)) {
                        tvGender.setText("女");
                    } else {
                        // 如果是其他格式，直接显示
                        tvGender.setText(gender);
                    }
                }

                // 生日
                org.threeten.bp.LocalDate birthday = currentUser.getBirthday();
                if (birthday != null) {
                    try {
                        org.threeten.bp.format.DateTimeFormatter formatter =
                                org.threeten.bp.format.DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
                        String birthdayStr = birthday.format(formatter);
                        etBirthday.setText(birthdayStr);

                        // 计算星座
                        String constellation = calculateConstellation(birthday);
                        tvConstellation.setText(constellation);
                    } catch (Exception e) {
                        e.printStackTrace();
                        etBirthday.setText(birthday.toString());
                    }
                }

                // 加载头像 - 使用圆形裁剪
                loadProfileAvatar();
            }
        }
    }

    private void loadProfileAvatar() {
        if (currentUser == null) return;

        // 优先尝试从数据库获取头像字节数组
        byte[] avatarBytes = userDAO.getUserAvatarBytes(currentUserId);

        if (avatarBytes != null && avatarBytes.length > 0) {
            // 如果有字节数组，使用Glide加载并圆形裁剪
            Glide.with(this)
                    .asBitmap()
                    .load(avatarBytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileAvatar);
        } else if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
            // 如果avatar字段是路径字符串
            Glide.with(this)
                    .load(currentUser.getAvatar())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileAvatar);
        } else {
            // 使用默认头像
            Glide.with(this)
                    .load(R.drawable.ic_default_avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileAvatar);
        }
    }

    private void saveUserData() {
        if (currentUser == null) {
            return;
        }

        // 获取表单数据
        String signature = etSignature.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String genderDisplay = tvGender.getText().toString().trim(); // 显示的中文

        // 将中文性别转换为数字字符串存储
        String gender = "";
        if ("男".equals(genderDisplay)) {
            gender = "1";
        } else if ("女".equals(genderDisplay)) {
            gender = "0";
        } else {
            gender = genderDisplay;
        }

        // 生日处理
        org.threeten.bp.LocalDate birthday = null;
        String birthdayStr = etBirthday.getText().toString().trim();
        if (!birthdayStr.isEmpty()) {
            try {
                org.threeten.bp.format.DateTimeFormatter formatter =
                        org.threeten.bp.format.DateTimeFormatter.ofPattern(Constants.DATE_FORMAT);
                birthday = org.threeten.bp.LocalDate.parse(birthdayStr, formatter);
            } catch (Exception e) {
                e.printStackTrace();
                etBirthday.setError("日期格式不正确");
                return;
            }
        }

        // 验证手机号
        if (!TextUtils.isEmpty(phone) && !ValidationUtil.isPhoneValid(phone)) {
            etPhone.setError("手机号格式不正确");
            return;
        }

        // 验证签名
        if (!TextUtils.isEmpty(signature) && !ValidationUtil.isSignatureValid(signature)) {
            etSignature.setError("签名不能超过100个字符");
            return;
        }

        // 更新用户数据
        currentUser.setSignature(signature);
        currentUser.setPhone(phone);
        currentUser.setGender(gender);
        currentUser.setBirthday(birthday);

        // 处理头像更新
        byte[] avatarBytes = null;
        if (selectedAvatarUri != null) {
            try {
                avatarBytes = uriToBytes(selectedAvatarUri);
                // 同时设置avatar字段为Uri字符串，保持兼容性
                currentUser.setAvatar(selectedAvatarUri.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "头像处理失败", Toast.LENGTH_SHORT).show();
            }
        } else if (selectedAvatarPath == null && currentUser.getAvatar() == null) {
            // 用户选择了使用默认头像
            currentUser.setAvatar("");
        }

        // 更新数据库 - 使用新的方法支持头像字节数组
        int result;
        if (avatarBytes != null) {
            // 使用支持字节数组的方法
            result = userDAO.updateUserWithAvatar(currentUser, avatarBytes);
        } else {
            // 使用原来的方法
            result = userDAO.updateUser(currentUser);
        }

        boolean success = result > 0;
        if (success) {
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();

            // 更新SharedPreferences中的昵称
            spUtil.putString(Constants.SP_KEY_NICKNAME, currentUser.getNickname());

            // 返回结果
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] uriToBytes(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;

        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        inputStream.close();
        return byteBuffer.toByteArray();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dateStr = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    etBirthday.setText(dateStr);

                    // 计算星座
                    try {
                        org.threeten.bp.LocalDate localDate = org.threeten.bp.LocalDate.of(
                                selectedYear, selectedMonth + 1, selectedDay
                        );
                        String constellation = calculateConstellation(localDate);
                        tvConstellation.setText(constellation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showGenderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择性别");
        builder.setItems(new String[]{"男", "女"}, (dialog, which) -> {
            String gender = which == 0 ? "男" : "女";
            tvGender.setText(gender);
        });
        builder.show();
    }

    private String calculateConstellation(org.threeten.bp.LocalDate birthday) {
        if (birthday == null) return "";

        int month = birthday.getMonthValue();
        int day = birthday.getDayOfMonth();

        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座";
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return "双鱼座";
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "白羊座";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return "双子座";
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return "巨蟹座";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "狮子座";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "处女座";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) return "天秤座";
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return "天蝎座";
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return "射手座";
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "摩羯座";

        return "";
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择头像");
        builder.setItems(new String[]{"拍照", "从相册选择", "使用默认头像"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    // 拍照
                    // ImageUtil.takePhoto(this, REQUEST_CODE_PICK_IMAGE);
                    Toast.makeText(this, "拍照功能暂未实现", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    // 从相册选择
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                    break;
                case 2:
                    // 使用默认头像
                    selectedAvatarPath = null;
                    selectedAvatarUri = null;
                    Glide.with(this)
                            .load(R.drawable.ic_default_avatar)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivProfileAvatar);
                    break;
            }
        });
        builder.show();
    }

    private void showLogoutConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认退出登录");
        builder.setMessage("退出登录后需要重新登录才能使用");
        builder.setPositiveButton("确定", (dialog, which) -> {
            logout();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void logout() {
        spUtil.logout();

        Intent intent = new Intent(this, LoginRegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                selectedAvatarUri = data.getData();
                if (selectedAvatarUri != null) {
                    // 显示图片
                    Glide.with(this)
                            .load(selectedAvatarUri)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivProfileAvatar);

                    // 保存路径字符串，保持兼容性
                    selectedAvatarPath = selectedAvatarUri.toString();
                }
            }
        }
    }
}