package com.bndg.smack.callback;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

/**
 * @author r
 * @date 2024/5/28
 * @description 登陆回调
 */
public interface ILoginCallback {
    void onSuccess();
    void onError(int code, String desc);
}
