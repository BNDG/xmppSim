package com.hjq.demo.chat.cons;

/**
 * 常量类
 */
public class Constant {
    // 会话类型
    public static final String CONVERSATION_TYPE = "CONVERSATION_TYPE";
    // 会话标题
    public static final String CONVERSATION_TITLE = "CONVERSATION_TITLE";
    // 会话id
    public static final String CONVERSATION_ID = "CONVERSATION_ID";
    // 会话昵称
    public static final String CONTACT_NICK_NAME = "contactNickName";
    // 联系人id
    public static final String CONTACT_ID = "contactId";
    public static final String GROUP_ID = "groupId";
    // 收到新消息
    public static final String RECEIVE_NEW_MSG = "RECEIVE_NEW_MSG";
    // Router
    public static final String FROM_WHERE = "FROM_WHERE";
    public static final String FROM_LOGIN = "FROM_LOGIN";
    public static final String FROM_REGISTER = "FROM_REGISTER";
    // 会话消息展示·标签
    public static final String IMAGE_LABEL = "[图片]";
    public static final String VOICE_LABEL = "[语音]";
    public static final String VIDEO_LABEL = "[视频]";
    public static final String CALL_LABEL = "[音视频通话]";
    public static final String SEND_CALL = "SEND_CALL";
    public static final String CALL_TYPE = "CALL_TYPE";
    public static final String CALL_ID = "CALL_ID";
    public static final String CURRENT_CALL_ID = "CURRENT_CALL_ID";
    // 通话发起者信息
    public static final String GROUP_CALL_CREATOR_INFO = "GROUP_CALL_CREATOR_INFO";
    // 搜索聊天记录 页码
    public static final String MESSAGE_PAGE = "MESSAGE_PAGE";
    // 搜索聊天记录 位置
    public static final String MESSAGE_LINENUM = "";
    // 好友添加
    public static final String FRIEND_ADDED = "FRIEND_ADDED";
    // 好友删除
    public static final String FRIEND_DELETED = "FRIEND_DELETED";
    // 好友信息
    public static final String FRIEND_USER_INFO = "FRIEND_USER_INFO";
    // 好友在线状态
    public static final String FRIEND_STATUS = "FRIEND_STATUS";
    public static final String ONLINE_STATUS = "ONLINE_STATUS";
    // 圆角(大)
    public static final int LARGE_CORNER = 20;
    // 圆角(中)
    public static final int MIDDLE_CORNER = 12;
    // 分隔符
    public static final String SEPARATOR_NICKNAME = "、";
    // 转发消息内容
    public static final String MESSAGE_CONTENT = "MESSAGE_CONTENT";
    // 消息类型
    public static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    // 消息id
    public static final String MESSAGE_ORIGIN_ID = "MESSAGE_ORIGINID";
    // 消息extra数据
    public static final String MESSAGE_EXTRA_DATA = "MESSAGE_EXTRA_DATA";
    public static final int REQUEST_CODE_SCAN_ONE = 0x01;
    // 多个会话
    public static final String CONVERSATION_IDS = "CONVERSATION_IDS";
    // 上传文件对象
    public static final String FILE_BEAN = "fileBean";
    // 上传文件成功
    public static final String UPLOAD_FILE_SUCCESS = "UPLOAD_FILE_SUCCESS";
    // 上传文件失败
    public static final String UPLOAD_FILE_FAILED = "UPLOAD_FILE_FAILED";
    public static final String TRANSITION = "TRANSITION";
    public static final String IMG_TRANSITION = "IMG_TRANSITION";
    // 消息spannable点击跳转自启动设置
    public static final String GO_AUTO_START = "go_auto_start";
    public static final String GO_POWER_MANAGER = "go_power_manager";
    public static final String GROUP_NAME = "groupName";
    public static final String SEPARATOR_CUSTOM = ":@#:";
    public static final String FILE_RECEIVED = "FILE_RECEIVED";
    public static final String PRIVACY_AGREEMENT = "PRIVACY_AGREEMENT";
    // 文件下载本地路径
    public static final String MESSAGE_FILE_LOCAL = "CHAT_MESSAGE_FILE_LOCAL";
    // 下载完成广播
    public static final String ACTION_DOWNLOAD_COMPLETE = "ACTION_DOWNLOAD_COMPLETE";
    // 下载进度
    public static final String ACTION_DOWNLOAD_PROGRESS = "ACTION_DOWNLOAD_PROGRESS";
    public static final String DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS";
    // 下载状态
    public static final String STATUS_DOWNLOADING = "DOWNLOADING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    // 发送文件大小限制
    public static final long FILE_LIMIT = 200;
    public static final String IS_FIRST = "IS_FIRST";
    // 好友申请介绍
    public static final String FRIEND_APPLY_INTRO = "FRIEND_APPLY_INTRO";
    // 刷新会话列表时间
    public static final String REFRESH_CONVERSATION_LIST_TIME = "REFRESH_CONVERSATION_LIST_TIME";
    // 静音
    public static final String MUTE_KEY = "_MUTE";
    // 阅读位置
    public static final String READ_POSITION = "_READ_POSITION";
    // 阅读时间戳
    public static final String READ_TIMESTAMP = "READ_TIMESTAMP";
    // 播放
    public static final String USE_SPEAKERPHONE = "USE_SPEAKERPHONE";
    // 排除的账户
    public static final String EXCLUDE_JIDS = "EXCLUDE_JIDS";
    // 输入框记录
    public static final String LAST_INPUT = "_last_input";
    // 好友类型
    public static final String FRIEND_TYPE = "FRIEND_TYPE";
    // 编辑图片
    public static final String EDIT_PHOTO = "EDIT_PHOTO";
    public static String PICTURE_DIR = "sdcard/wechat_/pictures/";
    // 用户信息
    public static final String USER_INFO = "USER_INFO";
    // 是否登录
    public static final String IS_LOGIN = "IS_LOGIN";
    public static final String USER_SEX_MALE = "1";
    public static final String USER_SEX_FEMALE = "2";
    // 好友标识0非好友
    public static final String IS_NOT_FRIEND = "0";
    // 好友标识1好友
    public static final String IS_FRIEND = "1";
    // 对方删除了我
    public static final String DELETE_ME = "4";
    // 自己
    public static final String IS_MYSELF = "10";
    // jitsi服务的地址
    public static final String JITSI_URL = "JITSI_URL";
    public static final String JITSI_URL_DEFAULT = "https://kmeet.infomaniak.com";
    public static final String JITSI_URL_DEFAULT_TEST = "https://jitsi.bndg.cn";
    // 音视频通话服务地址
    public static final String CALL_SERVICE_URL = "CALL_SERVICE_URL";
    // 同意好友请求
    public static final String FRIEND_APPLY_STATUS_ACCEPT = "1";
    // 未处理的好友请求
    public static final String FRIEND_APPLY_STATUS_NONE = "0";
    // 已查看好友请求
    public static final String FRIEND_APPLY_STATUS_CHECKED = "2";
    // 收到好友请求
    public static final String RECEIVED_FRIEND_APPLY = "RECEIVED_FRIEND_APPLY";
    // 传递参数 文件类型
    public static final String FILE_TYPE = "FILE_TYPE";
    public static final String FILE_TYPE_VIDEO = "VIDEO_FILE_TYPE";
    public static final String FILE_TYPE_IMAGE = "IMAGE_FILE_TYPE";
    // 名片信息
    public static final String MSG_TYPE_CARD_INFO = "CARD_INFO";
    // 位置
    public static final String MSG_TYPE_LOCATION = "LOCATION";
    // Spannable
    public static final String MSG_TYPE_SPANNABLE = "MSG_TYPE_SPANNABLE";
    // 语音通话
    public static final String MSG_TYPE_VIDEO_CALL = "VIDEO_CALL";
    // 视频通话
    public static final String MSG_TYPE_VOICE_CALL = "VOICE_CALL";
    // 结束通话
    public static final String MSG_TYPE_END_CALL = "CALL_END";
    // 取消通话
    public static final String MSG_TYPE_CANCEL_CALL = "CANCEL_CALL";
    // 忙线中
    public static final String MSG_TYPE_CALL_BUSY = "CALL_BUSY";
    // 拒绝接听
    public static final String MSG_TYPE_CALL_REFUSE = "CALL_REFUSE";
    // 同意接听
    public static final String MSG_TYPE_ACCEPT_CALL = "CALL_ACCEPT";
    public static final String MSG_TYPE_INTERACTIVE_BJ = "INTERACTIVE_BJ";
    // 创建群聊方式
    public static final String CREATE_GROUP_TYPE_FROM_NULL = "1";

    public static final String CREATE_GROUP_TYPE_FROM_SINGLE = "2";

    public static final String CREATE_GROUP_TYPE_FROM_GROUP = "3";
    // 语言
    public static final String LANGUAGE = "LANGUAGE";
    public static final String LANGUAGE_AUTO = "Auto";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_ZH_CN = "zh";
    public static final String LANGUAGE_ZH_TW = "zh_TW";

    // 好友来源
    //来自手机号搜索
    public static final String FRIENDS_SOURCE_BY_PHONE = "1";

    //来自号搜索
    public static final String FRIENDS_SOURCE_BY_WX_ID = "2";

    //来自手机号搜索
    public static final String CONTACTS_FROM_PHONE = "1";

    //来自号搜索
    public static final String CONTACTS_FROM_WX_ID = "2";

    //来自附近的人
    public static final String CONTACTS_FROM_PEOPLE_NEARBY = "3";

    //来自手机通讯录
    public static final String CONTACTS_FROM_CONTACT = "4";

    //朋友权限（所有权限：聊天、朋友圈、kptk运动等）
    public static final String PRIVACY_CHATS_MOMENTS_WERUN_ETC = "0";

    /**
     * 朋友权限（仅聊天）
     */
    public static final String PRIVACY_CHATS_ONLY = "1";

    /**
     * 朋友圈和视频动态-可以看我
     */
    public static final String SHOW_MY_POSTS = "0";

    /**
     * 朋友圈时视频动态-不让他看我
     */
    public static final String HIDE_MY_POSTS = "1";

    /**
     * 朋友圈和视频动态-可以看他
     */
    public static final String SHOW_HIS_POSTS = "0";

    /**
     * 朋友圈时视频动态-不看他
     */
    public static final String HIDE_HIS_POSTS = "1";

    /**
     * 非星标好友
     */
    public static final String CONTACT_IS_NOT_STARRED = "0";

    /**
     * 星标好友
     */
    public static final String CONTACT_IS_STARRED = "1";

    /**
     * 非黑名单
     */
    public static final String CONTACT_IS_NOT_BLOCKED = "0";

    /**
     * 黑名单
     */
    public static final String CONTACT_IS_BLOCKED = "1";

    /**
     * 星标好友分组title
     */
    public static final String STAR_FRIEND = "☆";

    /**
     * 用户名修改标记
     */
    public static final String USER_WX_ID_MODIFY_FLAG_TRUE = "1";

    /**
     * 地区类型-"省"
     */
    public static final String AREA_TYPE_PROVINCE = "1";

    /**
     * 地区类型-"市"
     */
    public static final String AREA_TYPE_CITY = "2";

    /**
     * 地区类型-"县"
     */
    public static final String AREA_TYPE_DISTRICT = "3";

    /**
     * 定位类型-地区信息
     * 获取省市区街道信息
     */
    public static final String LOCATION_TYPE_AREA = "0";

    /**
     * 定位类型-消息
     * 发送定位信息
     */
    public static final String LOCATION_TYPE_MSG = "1";

    public static final String DEFAULT_POST_CODE = "000000";

    // 登录方式
    /**
     * 手机号/密码登录
     */
    public static final String LOGIN_TYPE_PHONE_AND_PASSWORD = "0";

    /**
     * 手机号/验证码登录
     */
    public static final String LOGIN_TYPE_PHONE_AND_VERIFICATION_CODE = "1";

    /**
     * 用户名登录
     */
    public static final String LOGIN_TYPE_OTHER_ACCOUNTS_AND_PASSWORD = "2";

    /**
     * 验证码业务类型-"登录"
     */
    public static final String VERIFICATION_CODE_SERVICE_TYPE_LOGIN = "0";

    // 邮箱验证
    /**
     * 未绑定
     */
    public static final String EMAIL_NOT_LINK = "0";

    /**
     * 未验证
     */
    public static final String EMAIL_NOT_VERIFIED = "1";

    /**
     * 已验证
     */
    public static final String EMAIL_VERIFIED = "2";

    /**
     * 热词阈值
     */
    public static final Integer HOT_SEARCH_THRESHOLD = 8;

    /**
     * 普通注册用户
     */
    public static final String USER_TYPE_REG = "REG";

    /**
     * 团队
     */
    public static final String USER_TYPE_WEIXIN = "WEIXIN";

    /**
     * 文件传输助手
     */
    public static final String USER_TYPE_FILEHELPER = "FILEHELPER";

    // SharedPreferences key
    /**
     * 已选标签
     */
    public static final String SP_KEY_TAG_SELECTED = "tag_selected";
    // 会话是否可用
    public static String CONVERSATION_AVAILABLE = "CONVERSATION_AVAILABLE";
}
