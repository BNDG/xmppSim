package com.bndg.smack.callback;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.List;
import java.util.Set;

import com.bndg.smack.model.SmartUserInfo;

/**
 * @author r
 * @date 2024/5/29
 * @description 好友列表的回调
 */
public interface IFriendListCallback {
    void onSuccess(Set<SmartUserInfo> entries);

    void onFailed(int code, String desc);
}
