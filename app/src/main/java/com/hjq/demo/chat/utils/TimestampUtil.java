package com.hjq.demo.chat.utils;


import com.blankj.utilcode.util.StringUtils;
import com.hjq.demo.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TimestampUtil {
    private static int hour;
    private static int day;
    private static Date date;
    private static DateFormat df;
    private static String pointText;
    private static Long now;

    public TimestampUtil() {
        hour = 60 * 60 * 1000;
        day = (60 * 60 * 24) * 1000;
        now = new Date().getTime();
        pointText = "1970-01-01";
    }

    // 获得当天0点时间
    public static Long getTimesMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    // 获取星期几
    public static String getWeekOfDate(Date dt) {
        // 获取星期的索引（1-7）
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 星期天是0，星期一是1，以此类推
        // 根据星期的索引获取对应的字符串资源
        return getWeekOfIndex(w);
    }

    private static String getWeekOfIndex(int w) {
        switch (w) {
            case 0:
                return StringUtils.getString(R.string.sunday);
            case 1:
                return StringUtils.getString(R.string.monday);
            case 2:
                return StringUtils.getString(R.string.tuesday);
            case 3:
                return StringUtils.getString(R.string.wednesday);
            case 4:
                return StringUtils.getString(R.string.thursday);
            case 5:
                return StringUtils.getString(R.string.friday);
            case 6:
                return StringUtils.getString(R.string.saturday);
            default:
                return "";
        }
    }

    // 获取时间点
    public static String getTimePoint(Long time) {
        // 现在时间
        now = new Date().getTime();

        // 时间点比当天零点早
        if (time <= now) {
            date = new Date(time);
            if (time < getTimesMorning()) {
                if (time >= getTimesMorning() - day) {
                    // 比昨天零点晚
                    pointText = StringUtils.getString(R.string.yesterday);
                    return pointText;
                } else {
                    // 比昨天零点早
                    if (time >= getTimesMorning() - 6 * day) {
                        // 比七天前的零点晚，显示星期几
                        return getWeekOfDate(date);
                    } else {
                        // 显示具体日期
                        df = new SimpleDateFormat("yyyy-MM-dd");
                        pointText = df.format(date);
                        return pointText;
                    }
                }
            } else {
                // 无日期时间,当天内具体时间
                df = new SimpleDateFormat("HH:mm");
                pointText = df.format(date);
                int hour = Integer.parseInt(pointText.split(":")[0]);
                String period = "";
                if (hour >= 0 && hour <= 6) {
                    period = StringUtils.getString(R.string.midnight);
                }
                if (hour > 6 && hour <= 12) {
                    period = StringUtils.getString(R.string.morning);
                }
                if (hour == 13) {
                    period = StringUtils.getString(R.string.noon);
                }
                if (hour > 13 && hour <= 18) {
                    period = StringUtils.getString(R.string.afternoon);
                }
                if (hour > 18 && hour <= 24) {
                    period = StringUtils.getString(R.string.evening);
                }

                return period + pointText;
            }
        }
        return pointText;
    }

    public static String[] getYearMonthAndDay(long timeStamp) {
        try {
            Date date = new Date(timeStamp);
            String[] time = new SimpleDateFormat("yyyy-MM-dd").format(date).split("-");
            return time;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(getYearMonthAndDay(1574151206223L)[0]);
        System.out.println(getYearMonthAndDay(1574151206223L)[1]);
        System.out.println(getYearMonthAndDay(1574151206223L)[2]);
    }

    /**
     * 返回指定pattern样的日期时间字符串。
     *
     * @param dt
     * @param pattern
     * @return 如果时间转换成功则返回结果，否则返回空字符串""
     * @author 即时通讯网([url = http : / / www.52im.net]http : / / www.52im.net[ / url])
     */
    public static String getTimeString(Date dt, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);//"yyyy-MM-dd HH:mm:ss"
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(dt);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 仿照微信中的消息时间显示逻辑，将时间戳（单位：毫秒）转换为友好的显示格式.
     * <p>
     * 1）7天之内的日期显示逻辑是：今天、昨天(-1d)、前天(-2d)、星期？（只显示总计7天之内的星期数，即<=-4d）；<br>
     * 2）7天之外（即>7天）的逻辑：直接显示完整日期时间。
     *
     * @param time         要处理的源日期时间
     * @param mustIncludeTime true表示输出的格式里一定会包含“时间:分钟”，否则不包含（参考微信，不包含时分的情况，用于首页“消息”中显示时）
     * @return 输出格式形如：“10:30”、“昨天 12:04”、“前天 20:51”、“星期二”、“2019/2/21 12:09”等形式
     * @author 即时通讯网([url = http : / / www.52im.net]http : / / www.52im.net[ / url])
     * @since 4.5
     */
    public static String getTimeStringAutoShort2(Long time, boolean mustIncludeTime) {
        Date srcDate = new Date(time);
        String ret = "";

        try {
            GregorianCalendar gcCurrent = new GregorianCalendar();
            gcCurrent.setTime(new Date());
            int currentYear = gcCurrent.get(GregorianCalendar.YEAR);
            int currentMonth = gcCurrent.get(GregorianCalendar.MONTH) + 1;
            int currentDay = gcCurrent.get(GregorianCalendar.DAY_OF_MONTH);

            GregorianCalendar gcSrc = new GregorianCalendar();
            gcSrc.setTime(srcDate);
            int srcYear = gcSrc.get(GregorianCalendar.YEAR);
            int srcMonth = gcSrc.get(GregorianCalendar.MONTH) + 1;
            int srcDay = gcSrc.get(GregorianCalendar.DAY_OF_MONTH);

            // 要额外显示的时间分钟
            String timeExtraStr = (mustIncludeTime ? " " + getTimeString(srcDate, "HH:mm") : "");

            // 当年
            if (currentYear == srcYear) {
                long currentTimestamp = gcCurrent.getTimeInMillis();
                long srcTimestamp = gcSrc.getTimeInMillis();

                // 相差时间（单位：毫秒）
                long delta = (currentTimestamp - srcTimestamp);

                // 当天（月份和日期一致才是）
                if (currentMonth == srcMonth && currentDay == srcDay) {
                  /*  // 时间相差60秒以内
                    if (delta < 60 * 1000)
                        ret = "刚刚";
                        // 否则当天其它时间段的，直接显示“时:分”的形式
                    else*/
                        ret = getTimeString(srcDate, "HH:mm");
                }
                // 当年 && 当天之外的时间（即昨天及以前的时间）
                else {
                    // 昨天（以“现在”的时候为基准-1天）
                    GregorianCalendar yesterdayDate = new GregorianCalendar();
                    yesterdayDate.add(GregorianCalendar.DAY_OF_MONTH, -1);

                    // 前天（以“现在”的时候为基准-2天）
                    GregorianCalendar beforeYesterdayDate = new GregorianCalendar();
                    beforeYesterdayDate.add(GregorianCalendar.DAY_OF_MONTH, -2);

                    // 用目标日期的“月”和“天”跟上方计算出来的“昨天”进行比较，是最为准确的（如果用时间戳差值
                    // 的形式，是不准确的，比如：现在时刻是2019年02月22日1:00、而srcDate是2019年02月21日23:00，
                    // 这两者间只相差2小时，直接用“delta/(3600 * 1000)” > 24小时来判断是否昨天，就完全是扯蛋的逻辑了）
                    if (srcMonth == (yesterdayDate.get(GregorianCalendar.MONTH) + 1)
                            && srcDay == yesterdayDate.get(GregorianCalendar.DAY_OF_MONTH)) {
                        ret =  StringUtils.getString(R.string.yesterday) + timeExtraStr;// -1d
                    }
                    // “前天”判断逻辑同上
//                    else if (srcMonth == (beforeYesterdayDate.get(GregorianCalendar.MONTH) + 1)
//                            && srcDay == beforeYesterdayDate.get(GregorianCalendar.DAY_OF_MONTH)) {
//                        ret = "前天" + timeExtraStr;// -2d
//                    }
                    else {
                        // 跟当前时间相差的小时数
                        long deltaHour = (delta / (3600 * 1000));

                        // 如果小于 7*24小时就显示星期几
                        if (deltaHour < 7 * 24) {
                            // 取出当前是星期几
                            int index = gcSrc.get(GregorianCalendar.DAY_OF_WEEK) - 1;
                            String weedayDesc = getWeekOfIndex(index);

                            ret = weedayDesc + timeExtraStr;
                        }
                        // 否则直接显示完整日期时间
                        else
                            ret = getTimeString(srcDate, "yyyy/M/d") + timeExtraStr;
                    }
                }
            } else
                ret = getTimeString(srcDate, "yyyy/M/d") + timeExtraStr;
        } catch (Exception e) {
            System.err.println("【DEBUG-getTimeStringAutoShort】计算出错：" + e.getMessage() + " 【NO】");
        }

        return ret;
    }

}
