package com.hjq.demo.chat.utils;

import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.entity.PositionInfo;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.Trace;

public class PreferencesUtil {
    private User mUser;
    public static PreferencesUtil preferencesUtil;

    public static PreferencesUtil getInstance() {
        if (preferencesUtil == null) {
            synchronized (PreferencesUtil.class) {
                if (preferencesUtil == null) {
                    // 使用双重同步锁
                    preferencesUtil = new PreferencesUtil();
                }
            }
        }
        return preferencesUtil;
    }

    /**
     * 问题在于，这个Context哪来的我们不能确定，很大的可能性，你在某个Activity里面为了方便，直接传了个this;
     * 这样问题就来了，我们的这个类中的sInstance是一个static且强引用的，在其内部引用了一个Activity作为Context，也就是说，
     * 我们的这个Activity只要我们的项目活着，就没有办法进行内存回收。而我们的Activity的生命周期肯定没这么长，造成了内存泄漏。
     * 所以这里使用context.getApplicationContext()
     */
    private PreferencesUtil() {

    }


    /**
     * Whether to use for the first time
     *
     * @return
     */
    public boolean isFirst() {
        return SPUtils.getInstance().getBoolean(Constant.IS_FIRST, true);
    }

    /**
     * set user first use is false
     *
     * @return
     */
    public void setFirst(Boolean isFirst) {
        SPUtils.getInstance().put(Constant.IS_FIRST, isFirst);
    }

    /**
     * Set up the first time login
     *
     * @return
     */
    public boolean isLogin() {
        return SPUtils.getInstance().getBoolean(Constant.IS_LOGIN);
    }

    /**
     * @return
     */
    public void setLogin(Boolean isLogin) {
        SPUtils.getInstance().put(Constant.IS_LOGIN, isLogin);
    }


    public void setUser(User user) {
        mUser = null;
        if (user == null) {
            SPUtils.getInstance().remove(Constant.USER_INFO);
        } else {
            String jsonString = JsonParser.serializeToJson(user);
            Trace.d("-------保存用户setUser:==------= " + jsonString);
            SPUtils.getInstance().put(Constant.USER_INFO, jsonString);
        }
    }

    public User getUser() {
        if (null == mUser) {
            String userInfo = SPUtils.getInstance().getString(Constant.USER_INFO);
            if (TextUtils.isEmpty(userInfo)) {
                return null;
            }
            mUser = JsonParser.deserializeByJson(userInfo, User.class);
        }
        return mUser;
    }

    public void setPickedProvince(String provinceName) {
        SPUtils.getInstance().put("pickedProvince", provinceName);
    }

    public String getPickedProvince() {
        return SPUtils.getInstance().getString("pickedProvince", "");
    }

    public void setPickedCity(String cityName) {
        SPUtils.getInstance().put("pickedCity", cityName);
    }

    public String getPickedCity() {
        return SPUtils.getInstance().getString("pickedCity", "");
    }

    public void setPickedDistrict(String districtName) {
        SPUtils.getInstance().put("pickedDistrict", districtName);
    }

    public String getPickedDistrict() {
        return SPUtils.getInstance().getString("pickedDistrict", "");
    }

    public void setPickedPostCode(String postCode) {
        SPUtils.getInstance().put("pickedPostCode", postCode);
    }

    public String getPickedPostCode() {
        return SPUtils.getInstance().getString("pickedPostCode", "");
    }

    /**
     * 设置位置信息
     *
     * @param positionInfo 位置信息
     */
    public void setPositionInfo(PositionInfo positionInfo) {
        SPUtils.getInstance().put("positionInfo", JsonParser.serializeToJson(positionInfo));
    }

    /**
     * 获取位置信息
     *
     * @return 位置信息
     */
    public PositionInfo getPositionInfo() {
        PositionInfo positionInfo;
        try {
            String string = SPUtils.getInstance().getString("positionInfo", "");
            positionInfo = JsonParser.deserializeByJson(string, PositionInfo.class);
        } catch (Exception e) {
            positionInfo = new PositionInfo();
        }
        return positionInfo;
    }

    /**
     * 是否开启"附近的人"
     *
     * @return true:是  false:否
     */
    public boolean isOpenPeopleNearby() {
        return SPUtils.getInstance().getBoolean("isOpenPeopleNearby", false);
    }

    /**
     * 设置是否开启附近的人
     *
     * @param isOpenPeopleNearby 是否开启附近的人
     */
    public void setOpenPeopleNearby(Boolean isOpenPeopleNearby) {
        SPUtils.getInstance().put("isOpenPeopleNearby", isOpenPeopleNearby);
    }

    public void setUserId(String tusername) {
        SPUtils.getInstance().put("userId", tusername);
    }

    public String getUserId() {
        return SPUtils.getInstance().getString("userId", "");
    }

    public void logOut() {
        setLogin(false);
        setUser(null);
        SPUtils.getInstance().put("userId", "");
    }
}
