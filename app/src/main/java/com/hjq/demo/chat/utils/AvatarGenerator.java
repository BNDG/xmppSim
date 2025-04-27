package com.hjq.demo.chat.utils;// 导入必要的包

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.signature.ObjectKey;
import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.http.glide.GlideApp;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.OtherFileUtils;
import com.hjq.demo.utils.Trace;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class AvatarGenerator {
    /**
     * Maximum image width / height to be loaded.
     */
    private static final int MAX_SIZE = 256;

    /**
     * 通过jid获取用户头像文件
     *
     * @param userId
     * @return
     */
    public static File getAvatarFileByUserId(String userId, String avatarHash) {
        File avatarDir = new File(ActivityManager.getInstance().getApplication().getFilesDir(), "avatar");
        FileUtils.createOrExistsDir(avatarDir);
        return new File(avatarDir, OtherFileUtils.getFileMd5NameByString(userId + avatarHash) + ".png");
    }

    /**
     * 从vcard中保存头像
     */
    public static void saveAvatarFileByUserInfo(SmartUserInfo userInfo, boolean sendRefreshUserAvatar) {
        byte[] avatar = userInfo.getUserAvatar();
        if (null == avatar) {
            return;
        }
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getAvatarByUserIdOrHash(userInfo.getUserId(), userInfo.getUserAvatarHash())
                .subscribe(new Consumer<List<AvatarEntity>>() {
                    @Override
                    public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                        if (avatarEntities.isEmpty() || TextUtils.isEmpty(avatarEntities.get(0).getAvatarLocalPath())) {
                            // 没有存储过头像
                            Trace.w("accept: 没有存储过头像");
                            saveAvatarFileByByte(avatar, userInfo, sendRefreshUserAvatar);
                        } else {
                            AvatarEntity getAvatarEntity = avatarEntities.get(0);
                            String avatarHash = getAvatarEntity.getAvatarHash();
                            if (!TextUtils.isEmpty(avatarHash) && !userInfo.getUserAvatarHash().equals(avatarHash)) {
                                // 头像更新了
                                Trace.w("头像更新了 " + userInfo.getUserId());
                                saveAvatarFileByByte(avatar, userInfo, sendRefreshUserAvatar);
                            } else {
                                Trace.w("accept: 头像hash不变 只是id变了");
                                String userId = getAvatarEntity.getUserId();
                                if (!userId.contains(userInfo.getUserId())) {
                                    getAvatarEntity.setUserId(userId + "," + userInfo.getUserId());
                                    Trace.file("accept: 不包含 新增" + getAvatarEntity.getUserId());
                                }
                                DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                        .saveAvatarOrUpdate(getAvatarEntity)
                                        .subscribe(new CompletableObserver() {
                                            @Override
                                            public void onSubscribe(@NonNull Disposable d) {

                                            }

                                            @Override
                                            public void onComplete() {
                                            }

                                            @Override
                                            public void onError(@NonNull Throwable e) {

                                            }
                                        });
                            }
                        }
                    }
                });
    }

    /**
     * 根据钉钉头像规则截取用户名字符。
     * 中文用户名大于等于3个字符时，取后两位；
     * 英文用户名则取前两位。
     *
     * @param userName 用户名字符串
     * @return 按规则截取后的用户名字符
     */
    public static String getAvatarTextByName(String userName) {

        if (userName == null || userName.isEmpty()) {
            return ""; // 如果用户名为空，返回空字符串
        }
        if (userName.length() <= 2) {
            return userName;
        }
        StringBuilder extractedChars = new StringBuilder();

        boolean isChinese = false;
        for (char c : userName.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                isChinese = true;
                break;
            }
        }
        if (isChinese) {
            if (userName.length() >= 5) {
                // 长度小于3，取前两位
                int length = Math.min(userName.length(), 2);
                for (int i = 0; i < length; i++) {
                    extractedChars.append(userName.charAt(i));
                }
            } else {
                // 中文且长度大于等于3，取后两位
                extractedChars.append(userName.charAt(userName.length() - 2));
                extractedChars.append(userName.charAt(userName.length() - 1));
            }
        } else {
            // 非中文或长度小于3，取前两位（英文或混合情况）
            int length = Math.min(userName.length(), 2);
            for (int i = 0; i < length; i++) {
                extractedChars.append(userName.charAt(i));
            }
        }
        return extractedChars.toString();
    }

    /**
     * 加载
     *
     * @param userId
     * @param nickname
     * @param imageView
     */
    public static void loadAvatar(Context mContext, String userId, String nickname, ImageView imageView, boolean isLarge) {
        int cornerRadius = isLarge ? Constant.LARGE_CORNER : Constant.MIDDLE_CORNER;
        int fontSize = isLarge ? SizeUtils.sp2px(15) : SizeUtils.sp2px(14);
        Disposable subscribe = DBManager.Companion.getInstance(mContext)
                .getAvatarByUserId(userId)
                .subscribe(new Consumer<List<AvatarEntity>>() {
                    @Override
                    public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                        if (avatarEntities.isEmpty()) {
                            TextDrawable drawable = TextDrawable.builder()
                                    .beginConfig()
                                    .fontSize(fontSize)
                                    .bold()
                                    .endConfig()
                                    .buildRoundRect(AvatarGenerator.getAvatarTextByName(nickname),
                                            ActivityManager.getInstance().getApplication().getColor(R.color.primary_chat_user), cornerRadius);
                            imageView.setImageDrawable(drawable);
                        } else {
                            AvatarEntity avatarEntity = avatarEntities.get(0);
                            if (TextUtils.isEmpty(avatarEntities.get(0).getAvatarLocalPath())) {
                                // 没有本地文件 使用用户名 同时去请求头像
                                TextDrawable drawable = TextDrawable.builder()
                                        .beginConfig()
                                        .fontSize(fontSize)
                                        .bold()
                                        .endConfig()
                                        .buildRoundRect(AvatarGenerator.getAvatarTextByName(nickname),
                                                ActivityManager.getInstance().getApplication().getColor(R.color.primary_chat_user), cornerRadius);
                                imageView.setImageDrawable(drawable);
                                SmartIMClient.getInstance().getSmartCommUserManager().requestAvatarByUserId(userId);
                            } else {
                                String hashKey = avatarEntity.getAvatarHash();
                                if (TextUtils.isEmpty(hashKey)) {
                                    hashKey = String.valueOf(avatarEntity.getAvatarLocalPath().hashCode());
                                }
                                GlideApp.with(mContext)
                                        .load(new File(avatarEntity.getAvatarLocalPath()))
                                        .placeholder(R.mipmap.default_user_avatar) // 加载过程中的占位符
                                        .transform(new RoundedCorners(cornerRadius)) // 设置圆角
                                        .signature(new ObjectKey(hashKey))
                                        .into(imageView);
                            }
                        }
                    }
                });
    }

    /**
     * 加载
     *
     * @param userId
     * @param nickname
     * @param imageView
     */
    public static void loadRectAvatar(Context mContext, String userId, String nickname, ImageView imageView, boolean isLarge) {
        int fontSize = isLarge ? SizeUtils.sp2px(15) : SizeUtils.sp2px(14);
        Disposable subscribe = DBManager.Companion.getInstance(mContext)
                .getAvatarByUserId(userId)
                .subscribe(new Consumer<List<AvatarEntity>>() {
                    @Override
                    public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                        if (avatarEntities.isEmpty() || TextUtils.isEmpty(avatarEntities.get(0).getAvatarLocalPath())) {
                            TextDrawable drawable = TextDrawable.builder()
                                    .beginConfig()
                                    .fontSize(fontSize)
                                    .bold()
                                    .endConfig()
                                    .buildRect(AvatarGenerator.getAvatarTextByName(nickname),
                                            ActivityManager.getInstance().getApplication().getColor(R.color.primary_chat_user));
                            imageView.setImageDrawable(drawable);
                        } else {
                            AvatarEntity avatarEntity = avatarEntities.get(0);
                            String hashKey = avatarEntity.getAvatarHash();
                            if (TextUtils.isEmpty(hashKey)) {
                                hashKey = String.valueOf(avatarEntity.getAvatarLocalPath().hashCode());
                            }
                            Trace.d("loadAvatar " + avatarEntity.getUserId());
                            GlideApp.with(mContext)
                                    .load(new File(avatarEntity.getAvatarLocalPath()))
                                    .placeholder(R.mipmap.default_user_avatar) // 加载过程中的占位符
                                    .signature(new ObjectKey(hashKey))
                                    .into(imageView);
                        }
                    }
                });
    }

    /**
     * 通过byte保存头像
     *
     * @param avatar
     */
    public static void saveAvatarFileByByte(byte[] avatar, SmartUserInfo userInfo, boolean sendUserEvent) {
        File avatarFile = AvatarGenerator.getAvatarFileByUserId(userInfo.getUserId(), userInfo.getUserAvatarHash());
        try (FileOutputStream out = new FileOutputStream(avatarFile)) {
            out.write(avatar);
            out.flush();
            AvatarEntity userAvatar = new AvatarEntity();
            userAvatar.setUserId(userInfo.getUserId());
            userAvatar.setAvatarLocalPath(avatarFile.getAbsolutePath());
            userAvatar.setAvatarHash(userInfo.getUserAvatarHash());
            Trace.d("saveAvatarFileByByte " + userInfo.getUserId(),
                    userInfo.getUserAvatarHash() );
            DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                    .saveAvatarOrUpdate(userAvatar)
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            if (sendUserEvent) {
                                ChatEvent event = new ChatEvent(ChatEvent.REFRESH_USER_AVATAR);
                                event.obj = userInfo.getUserId();
                                EventBus.getDefault().post(event);
                            }
                            if (!TextUtils.isEmpty(userInfo.getGroupId())) {
                                ChatEvent event = new ChatEvent(ChatEvent.REFRESH_GROUP_MEMBER_AVATAR);
                                event.obj = userInfo.getUserId();
                                EventBus.getDefault().post(event);
                                // 得到头像后 如果没有群头像 并且成员是前9个的 需要发送通知群头像更新
                                checkGroupAvatar(userInfo.getGroupId(), userInfo.getUserId());
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkGroupAvatar(String groupId, String userJid) {
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getAvatarByUserId(groupId)
                .subscribe(new Consumer<List<AvatarEntity>>() {
                    @Override
                    public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                        if (!avatarEntities.isEmpty()) {
                        } else {
                            Disposable subscribe1 = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                                    .getGroupMemberByGroupId(groupId)
                                    .subscribe(new Consumer<List<GroupMember>>() {
                                        @Override
                                        public void accept(List<GroupMember> groupMembers) throws Throwable {
                                            // 应该做一个防抖
                                            List<GroupMember> subList = groupMembers.subList(0, Math.min(9, groupMembers.size()));
                                            for(GroupMember groupMember : subList) {
                                                if(groupMember.getMemberOriginId().equals(userJid)) {
                                                    ChatEvent event = new ChatEvent(ChatEvent.REFRESH_USER_AVATAR);
                                                    event.obj = groupId;
                                                    EventBus.getDefault().post(event);
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    public static void checkAvatar(String userId) {
        Disposable subscribe = DBManager.Companion.getInstance(ActivityManager.getInstance().getApplication())
                .getAvatarByUserId(userId)
                .subscribe(new Consumer<List<AvatarEntity>>() {
                    @Override
                    public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                        if (!avatarEntities.isEmpty() && TextUtils.isEmpty(avatarEntities.get(0).getAvatarLocalPath())) {
                            Trace.d("accept: 头像不存在-去获取 ");
                            SmartIMClient.getInstance().getSmartCommUserManager().requestAvatarByUserId(userId);
                        }
                    }
                });
    }
}