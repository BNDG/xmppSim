package com.hjq.demo.chat.utils;

import android.text.TextUtils;

import com.hjq.demo.chat.entity.User;

import java.util.Comparator;

public class PinyinComparator implements Comparator<User> {
    @Override
    public int compare(User u1, User u2) {
        String py1 = u1.getUserHeader();
        String py2 = u2.getUserHeader();
        // 判断是否为空""
        if (TextUtils.isEmpty(py1) && TextUtils.isEmpty(py2)) {
            return 0;
        }
        if (TextUtils.isEmpty(py1)) {
            return -1;
        }
        if (TextUtils.isEmpty(py2)) {
            return 1;
        }
        String str1 = "";
        String str2 = "";
        try {
            str1 = ((u1.getUserHeader()).toUpperCase()).substring(0, 1);
            str2 = ((u2.getUserHeader()).toUpperCase()).substring(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str1.compareTo(str2);
    }

    private boolean isEmpty(String str) {
        return "".equals(str.trim());
    }
}
