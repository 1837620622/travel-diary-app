package com.example.traildiary.utils;

import android.text.TextUtils;
import java.util.regex.Pattern;

public class ValidationUtil {

    // 昵称验证：2-20位汉字、字母或数字
    public static boolean isNicknameValid(String nickname) {
        if (TextUtils.isEmpty(nickname)) {
            return false;
        }
        String pattern = "^[\\u4e00-\\u9fa5a-zA-Z0-9]{2,20}$";
        return Pattern.matches(pattern, nickname);
    }

    // 途迹号验证：6-20位字母或数字
    public static boolean isTrailNumberValid(String trailNumber) {
        if (TextUtils.isEmpty(trailNumber)) {
            return false;
        }
        String pattern = "^[a-zA-Z0-9]{6,20}$";
        return Pattern.matches(pattern, trailNumber);
    }

    // 密码验证：6-20位字母或数字
    public static boolean isPasswordValid(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        String pattern = "^[a-zA-Z0-9]{6,20}$";
        return Pattern.matches(pattern, password);
    }

    // 手机号验证：11位数字，1开头
    public static boolean isPhoneValid(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        String pattern = "^1[3-9]\\d{9}$";
        return Pattern.matches(pattern, phone);
    }

    // 邮箱验证
    public static boolean isEmailValid(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        String pattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(pattern, email);
    }

    // 签名验证：不超过100个字符
    public static boolean isSignatureValid(String signature) {
        if (signature == null) {
            return false;
        }
        return signature.length() <= 100;
    }

    // 日记标题验证：1-50个字符，不能为空
    public static boolean isDiaryTitleValid(String title) {
        if (TextUtils.isEmpty(title)) {
            return false;
        }
        return title.length() >= 1 && title.length() <= 50;
    }

    // 日记内容验证：不能为空
    public static boolean isDiaryContentValid(String content) {
        return !TextUtils.isEmpty(content);
    }
}