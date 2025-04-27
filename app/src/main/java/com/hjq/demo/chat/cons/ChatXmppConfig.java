package com.hjq.demo.chat.cons;

import android.util.Log;

import com.blankj.utilcode.util.SPUtils;

import com.bndg.smack.BuildConfig;
import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.contract.ISmartCommConfig;

/**
 * @author r
 * @date 2024/5/21
 * @description xmpp服务器配置
 */
public class ChatXmppConfig implements ISmartCommConfig {
    private static volatile ChatXmppConfig instance;

    private ChatXmppConfig() {

    }

    public static ChatXmppConfig getInstance() {
        if (instance == null) {
            synchronized (ChatXmppConfig.class) {
                if (instance == null) {
                    instance = new ChatXmppConfig();
                }
            }
        }
        return instance;
    }

    @Override
    public String getDomainName() {
        String string = SPUtils.getInstance().getString(SmartConstants.CONSTANT_DOMAIN, BuildConfig.xmppDomain);
        Log.w(">>>>", "getDomainName: ------------------" + string);
        return string;
    }

    @Override
    public String getHostAddress() {
        return SPUtils.getInstance().getString(SmartConstants.CONSTANT_HOST, com.bndg.smack.BuildConfig.xmppDomain);
    }

    @Override
    public int getPort() {
        return SPUtils.getInstance().getInt(SmartConstants.CONSTANT_PORT, com.bndg.smack.BuildConfig.xmppPort);
    }


}
