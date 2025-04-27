package com.hjq.demo.chat.manager;

import com.hjq.demo.R;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.chat.activity.ChatActivity;
import com.hjq.demo.chat.activity.MainActivity;
import com.hjq.demo.manager.ActivityManager;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.enums.SmartConversationType;

/**
 * @author r
 * @date 2024/11/7
 * @description Brief description of the file content.
 */
public class ChatManager {
    private static volatile ChatManager instance;

    private ChatManager() {
    }

    public static ChatManager getInstance() {
        if (instance == null) {
            synchronized (ChatManager.class) {
                if (instance == null) {
                    instance = new ChatManager();
                }
            }
        }
        return instance;
    }

    public void joinGroup(String groupId, AppActivity activity) {
        activity.showDialog();
        SmartIMClient.getInstance().getSmartCommChatRoomManager().realJoinRoom(groupId, new IChatRoomCallback() {
            @Override
            public void joinRoomSuccess(String groupId) {
                activity.hideDialog();
                ChatActivity.start(activity, SmartConversationType.GROUP.name(), groupId, activity.getString(R.string.group_chats));
                ActivityManager.getInstance().finishAllActivities(MainActivity.class, ChatActivity.class);
            }

            @Override
            public void joinRoomFailed(int code, String groupId, String desc) {
                activity.hideDialog();
                activity.toast(desc);
            }
        });
    }
}
