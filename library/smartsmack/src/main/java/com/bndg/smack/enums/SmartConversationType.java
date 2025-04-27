package com.bndg.smack.enums;

public enum SmartConversationType {
    /**
     * 单聊会话类型
     */
    SINGLE(0, "单聊"),

    /**
     * 群聊会话类型
     */
    GROUP(1, "群聊"),

    /**
     * 频道会话类型
     */
    CHANNEL(2, "频道");

    // 注意：实际使用时应根据实际需求和SDK支持来定义类型

    private final int code;
    private final String description;

    SmartConversationType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // 从int值转换到SmartConversationType的静态方法
    public static SmartConversationType fromCode(int code) {
        for (SmartConversationType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid conversation type code: " + code);
    }
}
