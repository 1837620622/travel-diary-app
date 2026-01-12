package com.example.traildiary.model;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class User implements Parcelable {  // 添加 Parcelable 接口
    private int userId;
    private String avatar;
    private String nickname;
    private String trailNumber;
    private String password;
    private String phone;
    private String signature;
    private String gender;
    private LocalDate birthday;
    private LocalDateTime createTime;

    // 构造方法
    public User() {
        this.signature = "山川为印，时光为笔。";
        this.createTime = LocalDateTime.now();
    }

    public User(String nickname, String trailNumber, String password, String phone) {
        this();
        this.nickname = nickname;
        this.trailNumber = trailNumber;
        this.password = password;
        this.phone = phone;
    }

    // 更完整的构造方法
    public User(String nickname, String trailNumber, String password, String phone,
                String gender, LocalDate birthday) {
        this(nickname, trailNumber, password, phone);
        this.gender = gender;
        this.birthday = birthday;
    }

    // Parcelable 构造方法
    protected User(Parcel in) {
        userId = in.readInt();
        avatar = in.readString();
        nickname = in.readString();
        trailNumber = in.readString();
        password = in.readString();
        phone = in.readString();
        signature = in.readString();
        gender = in.readString();

        // 读取生日字符串并转换为 LocalDate
        String birthdayStr = in.readString();
        if (birthdayStr != null && !birthdayStr.isEmpty()) {
            birthday = LocalDate.parse(birthdayStr);
        }

        // 读取创建时间字符串并转换为 LocalDateTime
        String createTimeStr = in.readString();
        if (createTimeStr != null && !createTimeStr.isEmpty()) {
            createTime = LocalDateTime.parse(createTimeStr);
        }
    }

    // Parcelable 相关方法
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(avatar);
        dest.writeString(nickname);
        dest.writeString(trailNumber);
        dest.writeString(password);
        dest.writeString(phone);
        dest.writeString(signature);
        dest.writeString(gender);
        dest.writeString(birthday != null ? birthday.toString() : "");
        dest.writeString(createTime != null ? createTime.toString() : "");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // Getters and Setters (保持不变)
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTrailNumber() {
        return trailNumber;
    }

    public void setTrailNumber(String trailNumber) {
        this.trailNumber = trailNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    // 兼容旧Date类型的setter
    public void setBirthday(Date birthday) {
        if (birthday != null) {
            // 使用 ThreeTenABP 的 DateTimeUtils 进行转换
            this.birthday = org.threeten.bp.DateTimeUtils.toInstant(birthday)
                    .atZone(org.threeten.bp.ZoneId.systemDefault())
                    .toLocalDate();
        }
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    // 兼容旧Date类型的setter
    public void setCreateTime(Date createTime) {
        if (createTime != null) {
            // 使用 ThreeTenABP 的 DateTimeUtils 进行转换
            this.createTime = org.threeten.bp.DateTimeUtils.toInstant(createTime)
                    .atZone(org.threeten.bp.ZoneId.systemDefault())
                    .toLocalDateTime();
        }
    }

    // 根据生日计算星座
    public String getZodiac() {
        if (birthday == null) {
            return "未知";
        }

        int month = birthday.getMonthValue();
        int day = birthday.getDayOfMonth();

        // 星座日期划分
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) {
            return "白羊座";
        } else if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) {
            return "金牛座";
        } else if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) {
            return "双子座";
        } else if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) {
            return "巨蟹座";
        } else if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) {
            return "狮子座";
        } else if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) {
            return "处女座";
        } else if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) {
            return "天秤座";
        } else if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) {
            return "天蝎座";
        } else if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) {
            return "射手座";
        } else if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) {
            return "摩羯座";
        } else if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) {
            return "水瓶座";
        } else {
            return "双鱼座";
        }
    }

    // 根据生日计算年龄
    public int getAge() {
        if (birthday == null) {
            return 0;
        }
        return LocalDate.now().getYear() - birthday.getYear();
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", nickname='" + nickname + '\'' +
                ", trailNumber='" + trailNumber + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", birthday=" + (birthday != null ?
                birthday.format(DateTimeFormatter.ISO_LOCAL_DATE) : "null") +
                ", createTime=" + (createTime != null ?
                createTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "null") +
                '}';
    }
}