package com.hjq.demo.utils;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.hjq.demo.R;
import com.hjq.demo.manager.ActivityManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: chenyao
 * @Describe: 本地检查工具类
 * @Date: 2020/4/11 3:58 PM
 */
public class CheckUtil {
    public static String RETURN_NULL_STRING = "";
    /**
     * 正则：URL
     */
    public static final String REGEX_URL = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";

    public static final String REGEX_URL2 = "^((https|http|ftp|rtsp|mms)?://)"  //https、http、ftp、rtsp、mms
            + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@
            + "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 例如：199.194.52.184
            + "|" // 允许IP和DOMAIN（域名）
            + "([0-9a-z_!~*'()-]+\\.)*" // 域名- www.
            + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
            + "[a-z]{2,6})" // first level domain- .com or .museum
            + "(:[0-9]{1,5})?" // 端口号最大为65535,5位数
            + "((/?)|" // a slash isn't required if there is no file name
            + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";

    /**
     * 验证身份证号码
     *
     * @param idCardNumber 身份证号码
     * @return true 验证通过
     */
    public static boolean isIdCardNumber(String idCardNumber) {
        String idCardNumberRegex = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$";
        if (TextUtils.isEmpty(idCardNumber)) {
            return false;
        } else {
            return idCardNumber.matches(idCardNumberRegex);
        }
    }

    /**
     * 判断字符串是否为空 为空即true
     *
     * @param str 字符串
     * @return
     */
    public static boolean isNullString(@Nullable String str) {
        return str == null || str.length() == 0 || "null".equals(str);
    }

    /**
     * null值返回空串
     *
     * @param str
     * @return
     */
    public static String getNotNullString(String str) {
        if (isNullString(str)) {
            return RETURN_NULL_STRING;
        } else {
            return str;
        }
    }

    /**
     * 返回默认数据
     *
     * @param str
     * @return
     */
    public static String getNullDefaultString(String str, String defaultStr) {
        if (isNullString(str)) {
            return defaultStr;
        } else {
            return str;
        }
    }

    /**
     * 获取性别图标
     *
     * @param xb
     * @return
     */
    /*public static int getGenderImgRes(String xb) {
        if ("男".equals(xb) || "1".equals(xb)) {
            return R.drawable.icon_male;
        }
        return R.drawable.icon_female;
    }*/

    /**
     * 判断textview edittext是否是空
     *
     * @param tv
     * @return true  内容是空的
     */
    public static boolean isNullTextView(TextView tv) {
        return TextUtils.isEmpty(tv.getText().toString().trim());
    }

    /**
     * 通过值查找键  确保值唯一
     *
     * @param value
     * @param map
     * @return
     */
    public static String findKeyByValue(String value, HashMap<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return "";
    }

    public static String getSeparatorString(String front, String back) {
        if (!isNullString(front)) {
            if (!isNullString(back)) {
                return front + "/" + back;
            } else {
                return front + "/暂无";
            }
        } else if (!isNullString(back)) {
            return "暂无/" + back;
        } else {
            return "";
        }
    }

    /**
     * 获取map中的值 null检查
     *
     * @param maps
     * @param key  传入的key
     * @return
     */
    public static String getValueForMap(HashMap<String, String> maps, String key) {
        if (isNullString(key)) {
            return "";
        }
        String str = maps.get(key);
        if (isNullString(str)) {
            return "";
        }
        return str;
    }


    /**
     * 验证手机号格式
     *
     * @param phoneNumber 手机号
     * @return 返回验证结果 true 通过 flase 不通过
     */
    public static boolean isPhoneNumber(String phoneNumber) {
        String phoneNumberRegex = "[1][3456789]\\d{9}";
        if (TextUtils.isEmpty(phoneNumber)) {
            return false;
        } else {
            return phoneNumber.matches(phoneNumberRegex);
        }
    }

    /**
     * 验证密码格式
     *
     * @param password 密码
     * @return 返回验证结果 true 通过 flase 不通过
     */
    public static boolean isPassword(String password) {
        String passwordRegex = "^[a-zA-Z].*[0-9]|.*[0-9].*[a-zA-Z]";
        if (TextUtils.isEmpty(password)) {
            return false;
        } else {
            return password.matches(passwordRegex);
        }
    }


    public static String getSerialNumber(int position) {
        if (position < 10) {
            return "0" + position;
        }
        return String.valueOf(position);
    }

    /**
     * 根据身份证号计算性别
     *
     * @param idCard
     * @return
     */
    /*public static int isSex(String idCard) {
        if (!TextUtils.isEmpty(idCard) && idCard.length() == 18) {
            if (Integer.parseInt(idCard.substring(16, 17)) % 2 == 0) {
                return R.drawable.icon_female;
            } else {
                return R.drawable.icon_male;
            }
        }
        return R.drawable.icon_male;
    }*/

    /**
     * 根据身份证号计算年龄
     *
     * @param idCardNumber 身份证号
     * @return 年龄
     */
    public static int getAge(String idCardNumber) {

        //截取身份证中出行人出生日期中的年、月、日
        int personYear = Integer.parseInt(idCardNumber.substring(6, 10));
        int personMonth = Integer.parseInt(idCardNumber.substring(10, 12));
        int personDay = Integer.parseInt(idCardNumber.substring(12, 14));

        Calendar cal = Calendar.getInstance();
        // 得到当前时间的年、月、日
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayNow = cal.get(Calendar.DATE);

        // 用当前年月日减去生日年月日
        int yearMinus = yearNow - personYear;
        int monthMinus = monthNow - personMonth;
        int dayMinus = dayNow - personDay;

        int age = yearMinus; //先大致赋值

        if (yearMinus == 0) { //出生年份为当前年份
            age = 0;
        } else { //出生年份大于当前年份
            if (monthMinus < 0) {//出生月份小于当前月份时，还没满周岁
                age = age - 1;
            }
            if (monthMinus == 0) {//当前月份为出生月份时，判断日期
                if (dayMinus < 0) {//出生日期小于当前月份时，没满周岁
                    age = age - 1;
                }
            }
        }
        return age;
    }

    /**
     * 出生日期获取年龄
     *
     * @param date
     * @return
     */
    public static int getAgeOfBirth(String date) {
        if (TextUtils.isEmpty(date)) {
            return 0;
        }
        String[] split = date.split("-");
        if (split.length < 3) {
            return 0;
        }
        int personYear = Integer.parseInt(split[0]);
        int personMonth = Integer.parseInt(split[1]);
        int personDay = Integer.parseInt(split[2]);

        Calendar cal = Calendar.getInstance();
        // 得到当前时间的年、月、日
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayNow = cal.get(Calendar.DATE);

        // 用当前年月日减去生日年月日
        int yearMinus = yearNow - personYear;
        int monthMinus = monthNow - personMonth;
        int dayMinus = dayNow - personDay;

        int age = yearMinus; //先大致赋值

        if (yearMinus == 0) { //出生年份为当前年份
            age = 0;
        } else { //出生年份大于当前年份
            if (monthMinus < 0) {//出生月份小于当前月份时，还没满周岁
                age = age - 1;
            }
            if (monthMinus == 0) {//当前月份为出生月份时，判断日期
                if (dayMinus < 0) {//出生日期小于当前月份时，没满周岁
                    age = age - 1;
                }
            }
        }
        return age;
    }


    public static void stopFlick(ImageView view) {
        if (null == view) {
            return;
        }
        view.clearAnimation();
    }

    /**
     * 十六进制串转化为byte数组
     */
    public static byte[] hex2byte(String hex) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    /**
     * 将byte数组化为十六进制串
     */
    public static StringBuilder byte2hex(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder(data.length);
        for (byte byteChar : data) {
            stringBuilder.append(String.format("%02X ", byteChar).trim());
        }
        return stringBuilder;
    }

    public static int getGenderValue(String genderData) {
        if (!TextUtils.isEmpty(genderData)) {
            if ("男".equals(genderData)) {
                return 1;
            } else if ("女".equals(genderData)) {
                return 2;
            }
        }
        return 3;
    }


    public static String getGenderText(int genderData) {
        return genderData == 1 ? "男" : genderData == 2 ? "女" : "未知";
    }

    /**
     * bmi
     *
     * @param tz 体重 kg
     * @param sg 身高 cm
     * @return
     */
    public static String getBmi(float tz, float sg) {
        if (tz <= 0 || sg <= 0) {
            return "";
        }
        double value = tz / Math.pow(sg / 100, 2);
        DecimalFormat decimalFormat = new DecimalFormat("0.0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format(value);
    }


    /**
     * 根据身份证号码得到出生日期
     *
     * @param cardID 身份证号码
     * @return
     */
    public static String getBirthday(String cardID) {
        String birth = "";
        StringBuffer tempStr = null;
        if (cardID != null && cardID.trim().length() > 0) {
            if (cardID.trim().length() == 15) { //2000年以前出生的
                tempStr = new StringBuffer(cardID.substring(6, 12));
                tempStr.insert(4, '-');
                tempStr.insert(2, '-');
                tempStr.insert(0, "19");
            } else if (cardID.trim().length() == 18) {
                tempStr = new StringBuffer(cardID.substring(6, 14));
                tempStr.insert(6, '-');
                tempStr.insert(4, '-');
            }
        }
        if (tempStr != null && tempStr.toString().trim().length() > 0) {
            birth = tempStr.toString();
        }
        return birth;
    }

    /**
     * 出生日期转Date
     *
     * @param birthday 出生日期
     * @return 日期格式的出生日期
     */
    public static Date birthdayToDate(String birthday) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(birthday);
    }

    /**
     * 出生日期转Date
     *
     * @return 日期格式的出生日期
     */
    public static Date getDateForIdcard(String idcard) {
        String birthday = getBirthday(idcard);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(birthday);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    /**
     * 根据出生日期得到年龄
     *
     * @param birthDay 出生日期
     * @return 年龄
     */
    public static int getAge(Date birthDay) throws Exception {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);

        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;

        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;
            } else {
                age--;
            }
        }
        return age;
    }

    /**
     * 根据身份证号码得到年龄
     *
     * @param idCardNum 身份证号码
     * @return 年龄
     */
    public static int birthdayToAge(String idCardNum) {
        int age = 0;
        try {
            age = getAge(birthdayToDate(getBirthday(idCardNum)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return age;
    }

    /**
     * 根据身份证号计算性别
     *
     * @param idCard
     * @return
     */
    public static String getGenderFromIdcard(String idCard) {
        if (!TextUtils.isEmpty(idCard) && idCard.length() == 18) {
            if (Integer.parseInt(idCard.substring(16, 17)) % 2 == 0) {
                return "女";
            } else {
                return "男";
            }
        }
        return "男";
    }

    /**
     * 在今天之后吗
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static boolean isAfterToday(int year, int month, int day) {
        // 如果不指定时分秒则默认为现在的时间
        Calendar cal = Calendar.getInstance();
        Calendar choose = Calendar.getInstance();
        choose.set(Calendar.YEAR, year);
        // 月份从零开始，所以需要减 1
        choose.set(Calendar.MONTH, month - 1);
        choose.set(Calendar.DAY_OF_MONTH, day);

        Trace.d("isAfterToday: " + cal);
        Trace.d("isAfterToday: " + choose);
        return choose.after(cal);
    }


    /**
     * 通过类对象，运行指定方法
     *
     * @param obj        类对象
     * @param methodName 方法名
     * @param params     参数值
     * @return 失败返回null
     */
    public static Object invokeDeclaredMethod(Object obj, String methodName, Object[] params) {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        Class<?> clazz = obj.getClass();
        try {
            Class<?>[] paramTypes = null;
            if (params != null) {
                paramTypes = new Class[params.length];
                for (int i = 0; i < params.length; ++i) {
                    paramTypes[i] = params[i].getClass();
                }
            }
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
            Trace.i("reflect", "method " + methodName + " not found in " + obj.getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return 设备序列号
     */
    public static String getSERIAL() {
        String serial = null;
        try {
            serial = String.valueOf(invokeDeclaredMethod(new Build(), "getString", new Object[]{"gsm.serial"}));
            Trace.d(">>>>", "getSERIAL: 已经获取" + serial);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(serial) && "unknown".equals(serial)) {
            try {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serial = (String) get.invoke(c, "ro.serialno");
                Trace.d(">>>>", "unknown: 已经获取SystemProperties" + serial);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (TextUtils.isEmpty(serial) || "unknown".equals(serial)) {
            serial = getOtherId(ActivityManager.getInstance().getApplication());
        }

        return serial;
    }

    public static String getAndroidId() {
        return Settings.System.getString(ActivityManager.getInstance().getApplication().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getOtherId(Context context) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            Trace.d("getOtherId: " + deviceId);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Trace.d("getOtherId: PERMISSION_GRANTED");
                    deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                } else {
                    assert mTelephony != null;
                    if (mTelephony.getDeviceId() != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            deviceId = mTelephony.getImei();
                            Trace.d(">>>>", "getImei: deviceId");
                        } else {
                            deviceId = mTelephony.getDeviceId();
                            Trace.d(">>>>", "getDeviceId: getDeviceId");
                        }
                    }
                }
            } else {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }
        Trace.d(">>>>", "getOtherId: androidid==" + deviceId);
        return deviceId;
    }

    @SuppressWarnings("deprecation")
    public static void copyTextToBoard(Context context, String string) {
        if (TextUtils.isEmpty(string))
            return;
        ClipboardManager clip = (ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(string);
    }


    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name))
                        return true;
                }
            }
        }
        return false;
    }

    public static String getFileNameFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return "";
    }


    /**
     * 脱敏身份证号
     *
     * @param idNumber
     * @return
     */
    public static String desensitizedIdNumber(String idNumber) {
        if (!TextUtils.isEmpty(idNumber)) {
            if (idNumber.length() == 15) {
                idNumber = idNumber.replaceAll("(\\w{6})\\w*(\\w{3})", "$1******$2");
            }
            if (idNumber.length() == 18) {
                idNumber = idNumber.replaceAll("(\\w{6})\\w*(\\w{3})", "$1*********$2");
            }
        }
        return idNumber;
    }

    public static String convertBr2NewLine(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return value.replace("<br />", "\n");
    }

    /**
     * @param editText
     * @param canEdit
     */
    public static void enableEditText(EditText editText, boolean canEdit) {
        if (canEdit) {
        } else {
            editText.setKeyListener(null);
            editText.setCursorVisible(false);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
        }

    }

    public static String getChildAgeFromBirthTime(Date checkTime, Date birthDay) {
        Calendar now = Calendar.getInstance();
        now.setTime(checkTime);
        Calendar birthday = Calendar.getInstance();
        birthday.setTime(birthDay);
        int day = now.get(Calendar.DAY_OF_MONTH) - birthday.get(Calendar.DAY_OF_MONTH) - 1;
        int month = now.get(Calendar.MONTH) - birthday.get(Calendar.MONTH);
        int year = now.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);
        if (day < 0 && year > 0) {
            month -= 1;
            now.add(Calendar.MONTH, -1);
            day = day + now.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        if (month < 0) {
            month = (month + 12) % 12;
            year--;
        }
        StringBuffer tag = new StringBuffer();
        if (year >= 0) {
            tag.append(year + "岁");
        }
        if (month > 0) {
            tag.append(month + "个月");
        }
        /*if (day > 0) {
            tag.append(day + "天");
        }*/
        if (year == 0 && month == 0 && day == 0) {
            tag.append("今日出生");
        }
        return String.valueOf(tag);


    }

    public static String deal16to10(String str) {
        String rightstr = str.substring(0, 1);
        String leftstr = str.substring(1, 2);
        String r = String.valueOf(Integer.valueOf(get10v(rightstr)) * 16 * 1 + Integer.valueOf(get10v(leftstr)) * 1);
        if (Integer.valueOf(r) < 10) {
            return "0" + r;
        } else {
            return r;
        }
    }

    private static int get10v(String str) {
        str = str.toLowerCase();
        if ("a".equals(str)) {
            return 10;
        } else if ("b".equals(str)) {
            return 11;
        } else if ("c".equals(str)) {
            return 12;
        } else if ("d".equals(str)) {
            return 13;
        } else if ("e".equals(str)) {
            return 14;
        } else if ("f".equals(str)) {
            return 15;
        } else {
            return Integer.valueOf(str);
        }
    }


    /**
     * 获取两个日期的月数差
     *
     * @param fromDate
     * @param toDate
     * @return
     */
    public static long getDifferMonth(Date fromDate, Date toDate) {

        Calendar fromDateCal = Calendar.getInstance();
        Calendar toDateCal = Calendar.getInstance();
        fromDateCal.setTime(fromDate);
        if (null == toDate) {
            toDateCal.setTime(new Date());
        } else {
            toDateCal.setTime(toDate);
        }

        int fromYear = fromDateCal.get(Calendar.YEAR);
        int toYear = toDateCal.get((Calendar.YEAR));
        if (fromYear == toYear) {
            return Math.abs(fromDateCal.get(Calendar.MONTH) - toDateCal.get(Calendar.MONTH));
        } else {
            int fromMonth = 12 - (fromDateCal.get(Calendar.MONTH) + 1);
            int toMonth = toDateCal.get(Calendar.MONTH) + 1;
            return Math.abs(toYear - fromYear - 1) * 12 + fromMonth + toMonth;
        }
    }

    /**
     * 获取前n天日期、后n天日期
     *
     * @param distanceDay 前几天 如获取前7天日期则传-7即可；如果后7天则传7
     * @return
     */
    public static Calendar getDateDistanceDay(int distanceDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, distanceDay);
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        String dateString = sdf.format(date);
        return calendar;
    }


    private static long lastClickTime = 0;

    /**
     * 每次点击button的时候，获取当前的时间，然后对比上一次的时间，两者的差值如果小于某个规定的时间，则判断为快速点击。
     *
     * @return
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String formatJson(String json) {
        if (json == null) {
            return "";
        } else {
            try {
                if (json.startsWith("{")) {
                    return unescapeJson((new JSONObject(json)).toString(4));
                }

                if (json.startsWith("[")) {
                    return unescapeJson((new JSONArray(json)).toString(4));
                }
            } catch (JSONException var2) {
                var2.printStackTrace();
            }

            return json;
        }
    }

    @NonNull
    public static String unescapeJson(String json) {
        return TextUtils.isEmpty(json) ? "" : json.replace("\\/", "/");
    }

    public static boolean isImgUrl(String urlString) {
        String regex = "(?i)\\.(jpg|jpeg|png|gif|bmp)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(urlString);
        return matcher.find();
        /*try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            String contentType = connection.getContentType();
            return contentType != null && contentType.startsWith("image");
        } catch (IOException e) {
            // URL无效或连接问题
            e.printStackTrace();
            return false;
        }*/
    }

    /**
     * 匹配包含在 #xxx# 中的内容
     *
     * @param text
     * @return
     */
    public static List<String> findSpannableStr(String text) {
        // 定义正则表达式，匹配包含在 #xxx# 中的内容
        String regex = "#(.*?)#";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        // 使用 List 存储匹配项
        List<String> matches = new ArrayList<>();

        // 循环匹配并存储结果到 List
        while (matcher.find()) {
            String match = matcher.group(1); // 获取第一个捕获组的内容
            matches.add(match); // 将匹配项添加到 List 中
        }
        return matches;
    }

    public static boolean isXmppLink(String urlString) {
        Matcher matcher = Patterns.XMPP_PATTERN.matcher(urlString);
        return matcher.find();
    }

    public static String getRoomIdFromUrl(String str) {
        // 如果字符串不以 "xmpp:" 开头，直接返回空字符串
        if (!str.startsWith("xmpp:")) {
            return "";
        }
        int startIndex = "xmpp:".length();
        int endIndex = str.indexOf("?join");
        if (endIndex == -1) {
            endIndex = str.length();
        }
        // 对提取的部分进行 URL 编码
        String substring = str.substring(startIndex, endIndex);
        try {
            return URLDecoder.decode(substring, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return substring;
        }
    }

    /**
     * 代码方式的
     *
     * @param tvChatContent
     * @param messageContent
     */
    private void checkContentType(Context context, TextView tvChatContent, String messageContent) {
        // 定义匹配 URL 的正则表达式
        Pattern urlPattern = Pattern.compile("\\b(https?://|www\\.)\\S+\\b");
        // 创建 SpannableString 对象
        SpannableString spannableString = new SpannableString(messageContent);
        // 在输入文本中查找匹配的 URL
        Matcher matcher = urlPattern.matcher(messageContent);
        while (matcher.find()) {
            final String url = matcher.group();
            Trace.d("checkContentType: " + url);
            // 创建 ClickableSpan
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    // 处理点击事件，例如跳转到浏览器打开 URL
                    // 这里演示将 URL 显示在 TextView 中
                    Intent openUrlIntent = OpenFileUtils.getOpenUrlIntent(url);
                    Trace.d("处理点击事件: " + url,
                            "intent>>>>" + openUrlIntent);
                    context.startActivity(openUrlIntent);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    // 设置链接文本的样式，例如颜色和下划线
                    ds.setColor(context.getResources().getColor(R.color.text_link_color)); // 设置淡蓝色或其他颜色
                    ds.setUnderlineText(true); // 添加下划线
                }
            };
            // 设置 ClickableSpan 的样式
            int start = matcher.start();
            int end = matcher.end();
            spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        // 设置 TextView 显示 SpannableString
        tvChatContent.setText(spannableString);
        // 设置 TextView 可以点击链接
        tvChatContent.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
