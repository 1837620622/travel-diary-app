package com.example.traildiary.utils;

import android.os.Build;

import java.text.SimpleDateFormat;
import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// 导入 ThreeTenBP 的转换工具
import org.threeten.bp.DateTimeUtils;

public class DateUtil {

    /**
     * 格式化日期
     * @param date 日期对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 格式化 LocalDateTime
     * @param localDateTime LocalDateTime 对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String formatLocalDateTime(org.threeten.bp.LocalDateTime localDateTime, String pattern) {
        if (localDateTime == null) {
            return "";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return localDateTime.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 将字符串转换为日期
     * @param dateString 日期字符串
     * @param pattern 格式模式
     * @return 日期对象
     */
    public static Date parseDate(String dateString, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            return sdf.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字符串转换为 LocalDateTime
     * @param dateString 日期字符串
     * @param pattern 格式模式
     * @return LocalDateTime 对象
     */
    public static LocalDateTime parseLocalDateTime(String dateString, String pattern) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取当前时间的格式化字符串
     *
     * @return 格式化后的当前时间
     */
    public static String getCurrentTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取当前时间戳
     * @return 当前时间戳（毫秒）
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前 LocalDateTime
     * @return 当前 LocalDateTime
     */
    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 根据生日计算年龄
     * @param birthday 生日日期
     * @return 年龄
     */
    public static int calculateAge(Date birthday) {
        if (birthday == null) {
            return 0;
        }

        Calendar birth = Calendar.getInstance();
        birth.setTime(birthday);
        Calendar now = Calendar.getInstance();

        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        // 如果还没过生日，年龄减1
        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    /**
     * 根据生日计算星座
     * @param birthday 生日日期
     * @return 星座名称
     */
    public static String calculateZodiac(Date birthday) {
        if (birthday == null) {
            return "未知";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthday);

        int month = calendar.get(Calendar.MONTH) + 1; // 月份从0开始
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return getZodiac(month, day);
    }

    /**
     * 根据月份和日期计算星座
     */
    private static String getZodiac(int month, int day) {
        String[] zodiacArr = {"魔羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座",
                "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "魔羯座"};

        int[] dayArr = {20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22}; // 两个星座分割日

        if (day <= dayArr[month - 1]) {
            return zodiacArr[month - 1];
        } else {
            return zodiacArr[month];
        }
    }

    /**
     * 获取星期几
     * @param date 日期
     * @return 星期几（一、二、三...）
     */
    public static String getWeekday(Date date) {
        if (date == null) {
            return "";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};

        return "星期" + weekdays[dayOfWeek - 1];
    }

    /**
     * 获取星期几（LocalDateTime 版本）
     * @param localDateTime LocalDateTime 对象
     * @return 星期几（一、二、三...）
     */
    public static String getWeekday(org.threeten.bp.LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }

        // 使用 ThreeTenBP 的 DateTimeUtils 将 ThreeTenBP 类型转换为 java.util.Date
        Date date = DateTimeUtils.toDate(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return getWeekday(date);
    }

    /**
     * 计算两个日期之间的天数差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static int getDaysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }

        long diff = endDate.getTime() - startDate.getTime();
        return (int) (diff / (1000 * 60 * 60 * 24));
    }

    /**
     * 计算两个 LocalDateTime 之间的天数差
     * @param startDateTime 开始 LocalDateTime
     * @param endDateTime 结束 LocalDateTime
     * @return 天数差
     */
    public static int getDaysBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }

        long diff = Duration.between(startDateTime, endDateTime).toDays();
        return (int) diff;
    }

    /**
     * 获取友好时间显示（刚刚、几分钟前、几小时前等）
     * @param date 日期
     * @return 友好时间字符串
     */
    public static String getFriendlyTime(Date date) {
        if (date == null) {
            return "";
        }

        long time = date.getTime();
        long now = System.currentTimeMillis();
        long diff = now - time;

        // 转换为秒
        long seconds = diff / 1000;

        if (seconds < 60) {
            return "刚刚";
        }

        // 转换为分钟
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "分钟前";
        }

        // 转换为小时
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "小时前";
        }

        // 转换为天
        long days = hours / 24;
        if (days < 7) {
            return days + "天前";
        }

        // 超过一周，显示具体日期
        return formatDate(date, "yyyy-MM-dd");
    }

    /**
     * 获取友好时间显示（LocalDateTime 版本）
     * @param localDateTime LocalDateTime 对象
     * @return 友好时间字符串
     */
    public static String getFriendlyTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }

        // 使用 ThreeTenBP 的 DateTimeUtils 将 ThreeTenBP 类型转换为 java.util.Date
        Date date = DateTimeUtils.toDate(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return getFriendlyTime(date);
    }

    /**
     * 获取日记编辑时间的格式化显示
     * @param updateTime 最后编辑时间
     * @return 格式化的编辑时间字符串
     */
    public static String getEditTimeString(Date updateTime) {
        if (updateTime == null) {
            return "";
        }
        return "编辑于 " + formatDate(updateTime, "yyyy年MM月dd日 HH:mm:ss");
    }

    /**
     * 获取日记编辑时间的格式化显示（LocalDateTime 版本）
     * @param updateTime 最后编辑时间
     * @return 格式化的编辑时间字符串
     */
    public static String getEditTimeString(LocalDateTime updateTime) {
        if (updateTime == null) {
            return "";
        }
        return "编辑于 " + formatLocalDateTime(updateTime, "yyyy年MM月dd日 HH:mm:ss");
    }
}