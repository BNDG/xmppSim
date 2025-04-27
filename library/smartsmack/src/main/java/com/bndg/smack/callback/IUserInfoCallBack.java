package com.bndg.smack.callback;

import org.jivesoftware.smackx.muc.Occupant;

/**
 * @author r
 * @date 2024/6/7
 * @description sdk使用
 */
public interface IUserInfoCallBack {
    default void getNickname(String nickName) {
    }

    default void getNicknameError(int code, String desc) {
    }

    default void getOccupantInfo(Occupant occupant) {
    }

    default void isOnLine(boolean isOnLine){}
}
