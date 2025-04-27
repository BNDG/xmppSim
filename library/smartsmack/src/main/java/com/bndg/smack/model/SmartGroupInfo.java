//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.bndg.smack.model;

import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.util.HashMap;
import java.util.List;

import com.bndg.smack.SmartCommHelper;

/**
 * @author r
 * @date 2024/5/20
 * @description 用户信息类
 */

public class SmartGroupInfo {

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    // 群id
    protected String groupID = "";
    // 群名称
    protected String groupName = "";

    public String getSubject() {
        return subject;
    }

    // 群公告
    private String subject;

    public boolean isMembersOnly() {
        return membersOnly;
    }

    public void setMembersOnly(boolean membersOnly) {
        this.membersOnly = membersOnly;
    }

    // 只有成员可以进入聊天室，则返回 true。
    private boolean membersOnly;

    public boolean isModerated() {
        return moderated;
    }

    public void setModerated(boolean moderated) {
        this.moderated = moderated;
    }

    //仅允许参与者发言，则返回 true。
    private boolean moderated;

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }

    // 密码才能加入聊天室，则返回 true。
    private boolean passwordProtected;

    public boolean isNonanonymous() {
        return nonanonymous;
    }

    public void setNonanonymous(boolean nonanonymous) {
        this.nonanonymous = nonanonymous;
    }
    // 是否公开jid地址，则返回 true。
    private boolean nonanonymous;

    private HashMap<String, List<String>> conf;

    public SmartGroupInfo() {
    }


    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setConf(DataForm form) {
        List<FormField> fields = form.getFields();
        HashMap<String, List<String>> conf = new HashMap<>();
        for (FormField field : fields) {
            conf.put(SmartCommHelper.getInstance().getMucFormItemDesc(field.getFieldName()), field.getValuesAsString());
        }
        this.conf = conf;
    }
}
