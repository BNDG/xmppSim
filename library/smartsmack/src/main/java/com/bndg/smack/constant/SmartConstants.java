package com.bndg.smack.constant;

public class SmartConstants {
    public static final String CONSTANT_DOMAIN = "CONSTANT_DOMAIN";
    public static final String CONSTANT_HOST = "CONSTANT_HOST";
    public static final String CONSTANT_PORT = "CONSTANT_PORT";

    public static final String CURRENT_STATUS = "CURRENT_STATUS";
    // 账户状态
    public static final String ACCOUNT_STATUS = "ACCOUNT_STATUS";
    public static final String SP_NAME = "smart_chat";
    public static final String USER_NAME = "USER_NAME";
    public static final String PASS_WORD = "PASS_WORD";
    public static final String NICKNAME = "_NICKNAME";
    public static final String KICKED_REASON = "normal";
    // 延迟时间(短)
    public static final long SHORT_DELAY = 125;
    public static final long DELAY_250 = 250;
    public static final String RECORD_KEY = "_recordMsg";
    public static final String SECURITY_MODE = "SECURITYMODE";
    public static final String SECURITY_MODE_REQUIRED = "SECURITYMODE_REQUIRED";
    public static final String SECURITY_MODE_DISABLED = "SECURITYMODE_DISABLED";
    public static final String DEVELOPER_MODE = "DEVELOPER_MODE";

    public interface Error {
        // 登录失败
        int LOGIN_FAILURE_OTHER = 1100;
        // 鉴权失败
        int AUTHENTICATION_FAILURE = 1101;
        // 账号被禁用
        int ACCOUNT_DISABLED = 1102;
        // 退出失败
        int LOGOUT_FAILED = 1110;

        //发送消息失败
        int SEND_MSG_FAILED = 1200;
        // 发送群消息失败
        int SEND_GROUP_MSG_FAILED = 1201;
        // 发送群消息失败 无法加入群
        int SEND_FAILED_CANT_JOIN_GROUP = 1202;
        // 发送消息失败 不是好友
        int SEND_MSG_FAILED_NO_FRIEND = 1203;
        // 禁止发言
        int FORBIDDEN = 1210;
        // 连接断开导致发送消息失败
        int SEND_MSG_FAILED_DISCONNECT = 1220;
        // 文件上传错误
        int UPLOAD_FILE_FAILED = 1300;
        // 删除好友失败
        int REMOVE_FRIEND_FAILED = 1400;
        // 同意好友请求失败
        int ACCEPT_PRESENCE_FAILED = 1401;
        // 拒绝好友请求失败
        int REJECT_PRESENCE_FAILED = 1402;
        // 获取好友列表失败
        int GET_FRIEND_LIST_FAILED = 1403;
        // 被用户屏蔽
        int BLOCKED_BY_USER = 1410;

        // 未找到用户
        int NO_USER_FOUND = 1500;
        // 连接失败
        int CONNECTION_FAILED = 1600;
        // 未连接
        int NO_CONNECTION = 1601;

        int CREATE_ACCOUNT_FAILED = 1700;
        int CREATE_ACCOUNT_EXISTS_FAILED = 1701;
        // 更新名片失败
        int UPDATE_VCARD_FAILED = 1800;
        int GET_AVATAR_FAILED = 1801;
        // 未能获取用户信息
        int NO_VCARD = 1802;
        // 不支持群聊
        int MUC_UNAVAILABLE = 1900;
        int LEAVE_ROOM_FAILED = 1901;
        int ROOM_JOIN_FAILED = 1902;
        int ROOM_DELETED_FAILED = 1903;
        // 群聊服务不可用
        int CREATE_MUC_FAILED = 1904;
        int ROOM_DESTROYED = 1905;
        int ROOM_FORBIDDEN = 1906;
        int CHANGE_NICK_IN_GROUP_FAILED = 1910;
    }
}
