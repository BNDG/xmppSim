package com.hjq.demo.chat.entity.enums;

public enum MessageStatus {
    /**
     * 消息状态
     */
    SENDING(1),
    SEND_SUCCESS(2),
    SEND_FAIL(3),
    DISABLE(4);
    private final int status;

    MessageStatus(int status) {
        this.status = status;
    }

    public int value() {
        return status;
    }
}
