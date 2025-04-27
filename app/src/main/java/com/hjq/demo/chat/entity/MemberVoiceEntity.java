package com.hjq.demo.chat.entity;

import com.bndg.smack.model.SmartUserInfo;

/**
 * @author r
 * @date 2024/11/25
 * @description Brief description of the file content.
 */
public class MemberVoiceEntity {
    public String role;
    public boolean moderated;
    public MemberVoiceEntity(boolean moderated, String role) {
        this.moderated = moderated;
        this.role = role;
    }

    public boolean noRightToSpeak() {
        return moderated && SmartUserInfo.VISITOR.equals(role);
    }
}
