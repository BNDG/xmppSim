package com.bndg.smack.contract;

import org.jivesoftware.smack.ConnectionConfiguration;

/**
 * Xmpp 配置接口
 */
public interface ISmartCommConfig {
    /**
     * 获取域名
     *
     * @return
     */
    String getDomainName();

    /**
     * 获取服务器地址
     *
     * @return
     */
    String getHostAddress();

    /**
     * 获取连接端口
     *
     * @return
     */
    int getPort();

    default ConnectionConfiguration.SecurityMode getSecurityMode() {
        return ConnectionConfiguration.SecurityMode.ifpossible;
    }

}
