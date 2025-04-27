package com.bndg.smack.callback;

import java.util.List;

import com.bndg.smack.model.SmartUserInfo;

/**
 * @author r
 * @date 2024/6/14
 * @description 群成员列表回调
 */
public interface IGroupMemberCallback {
    void onSuccess(List<SmartUserInfo> smartUserInfoList);

    default void onFailed(int code, String desc) {
    }
}
