package com.hjq.demo.chat.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FileUtils;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.AppDatabase;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.ChatRoomEntity;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.ConversationHelper;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.utils.TimestampUtil;
import com.hjq.demo.http.glide.GlideApp;
import com.hjq.demo.ui.dialog.MessageDialog;
import com.hjq.demo.utils.Trace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import cn.mtjsoft.groupavatarslib.Builder;
import cn.mtjsoft.groupavatarslib.GroupAvatarsLib;
import cn.mtjsoft.groupavatarslib.layout.WechatLayoutManager;
import cn.mtjsoft.groupavatarslib.utils.MD5Util;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author r
 * @date 2024/6/21
 * @description Brief description of the file content.
 */
public class SmartConversationAdapter extends BaseMultiItemQuickAdapter<ConversationInfo, BaseViewHolder> {
    public static final int SINGLE = 1;
    public static final int GROUP = 2;
    // 刷新会话
    public static final String PAYLOADS = "PAYLOADS";
    // 刷新会话消息
    public static final String REFRESH_CONTENT = "REFRESH_CONTENT";
    // 刷新会话标题
    public static final String REFRESH_TITLE = "REFRESH_TITLE";
    // 刷新会话头像
    public static final String REFRESH_AVATAR = "REFRESH_AVATAR";
    // 置顶会话
    public static final String PINNED = "PINNED";
    HashMap<String, String> avatarKeys = new HashMap<>();

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private Activity activity;

    public SmartConversationAdapter(List<ConversationInfo> data) {
        super(data);
        addItemType(SINGLE, R.layout.item_conversation_single);
        addItemType(GROUP, R.layout.item_conversation_group_1);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ConversationInfo conversationItem) {
        int unReadMsgCnt = conversationItem.getUnReadMsgCnt();
        String unreadCount = unReadMsgCnt > 99 ? "99+" : String.valueOf(unReadMsgCnt);
        MenuHelper menuHelper = new MenuHelper(helper);
        helper.itemView.setOnCreateContextMenuListener(menuHelper);
        switch (helper.getItemViewType()) {
            case SINGLE:
                // 用户基础信息
                DBManager.Companion.getInstance(getContext())
                        .getUserById(conversationItem.getConversationId())
                        .subscribe(new Consumer<List<User>>() {
                            @Override
                            public void accept(List<User> users) throws Throwable {
                                if (!users.isEmpty()) {
                                    User user = users.get(0);
                                    helper.setText(R.id.tv_nick_name, conversationItem.getConversationTitle())
                                            .setText(R.id.tv_last_msg, conversationItem.getDigest())
                                            .setText(R.id.tv_unread, unreadCount)
                                            .setGone(R.id.tv_unread, unReadMsgCnt <= 0)
                                            .setText(R.id.tv_create_time, conversationItem.getLastMsgDate() == 0 ? TimestampUtil.getTimePoint(new Date().getTime())
                                                    : TimestampUtil.getTimeStringAutoShort2(conversationItem.getLastMsgDate(), false))
                                            .setBackgroundResource(R.id.rl_content, conversationItem.isPinned() ? R.drawable.item_tab_bg_pinned : R.drawable.item_tab_bg);
                                    ImageView mAvatarSdv = helper.getView(R.id.sdv_avatar);
                                    loadAvatar(user.getUserAvatar(), user.getUserId(), user.getUserNickName(), mAvatarSdv);
                                }
                            }
                        });
                break;
            case GROUP:
                // 群聊
                loadGroupAvatar(conversationItem, helper.getView(R.id.iv_group_avatar));
                String conversationTitle = conversationItem.getConversationTitle();
                if (TextUtils.isEmpty(conversationTitle)) {
                    getConversationTitle(helper.getView(R.id.tv_group_name), conversationItem.getConversationId());
                } else {
                    helper.setText(R.id.tv_group_name, conversationTitle);
                }

                helper.setText(R.id.tv_unread, unreadCount)
                        .setGone(R.id.tv_unread, unReadMsgCnt <= 0)
                        .setText(R.id.tv_create_time, conversationItem.getLastMsgDate() == 0 ? TimestampUtil.getTimePoint(new Date().getTime())
                                : TimestampUtil.getTimeStringAutoShort2(conversationItem.getLastMsgDate(), false))
                        .setText(R.id.tv_last_msg, conversationItem.getDigest())
                        .setBackgroundResource(R.id.rl_content, conversationItem.isPinned() ? R.drawable.item_tab_bg_pinned : R.drawable.item_tab_bg);
                break;
        }
    }

    /**
     * 加载群头像
     *
     * @param conversationItem
     * @param iv_avatar1
     */
    private void loadGroupAvatar(ConversationInfo conversationItem, ImageView iv_avatar1) {
        // 群头像获取
        Disposable subscribe = DBManager.Companion.getInstance(getContext())
                .getAvatarByConversationId(conversationItem.getConversationId())
                .subscribe(new Consumer<List<AvatarEntity>>() {
                    @Override
                    public void accept(List<AvatarEntity> avatarEntities) throws Throwable {
                        if (!avatarEntities.isEmpty()) {
                            AvatarEntity avatarEntity = avatarEntities.get(0);
                            File file = new File(avatarEntity.getAvatarLocalPath());
                            if (FileUtils.isFileExists(file)) {
                                GlideApp.with(getContext())
                                        .load(file)
                                        .transform(new RoundedCorners(Constant.MIDDLE_CORNER)) // 设置圆角
                                        .into(iv_avatar1);
                            } else {
                                generateDataByMember(conversationItem.getConversationId(), iv_avatar1);
                            }
                        } else {
                            generateDataByMember(conversationItem.getConversationId(), iv_avatar1);
                        }
                    }
                });
    }

    private void getConversationTitle(TextView view, String conversationId) {
        view.setText(getContext().getString(R.string.group_chats));
        Disposable subscribe = DBManager.Companion.getInstance(getContext())
                .getChatRoomByRoomId(conversationId)
                .subscribe(chatRoomEntities -> {
                    if (!chatRoomEntities.isEmpty()) {
                        ChatRoomEntity chatRoomEntity = chatRoomEntities.get(0);
                        String chatRoomName = chatRoomEntity.getChatRoomName();
                        if (!TextUtils.isEmpty(chatRoomName)) {
                            view.setText(chatRoomName);
                        }
                    }
                });
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ConversationInfo conversationItem, @NonNull List<?> payloads) {
        int unReadMsgCnt = conversationItem.getUnReadMsgCnt();
        String unreadCount = unReadMsgCnt > 99 ? "99+" : String.valueOf(unReadMsgCnt);
        Trace.w(conversationItem.getConversationId() + " 观察者: " + payloads + " 未读num " + unreadCount);
        // notifyItemChanged是观察者模式 android app后台会存储事件等到前台会把事件统一发送，所以此处需要对事件累计和过滤
        List<String> realPayloadList;
        if (!payloads.isEmpty()) {
            realPayloadList = new ArrayList<>();
            for (Object p : payloads) {
                if (p instanceof List) {
                    realPayloadList.addAll((Collection<? extends String>) p);
                }
            }
        } else {
            return;
        }
        Set<String> realPayloads = new HashSet<>(realPayloadList);
        switch (conversationItem.getItemType()) {
            case SINGLE:
                for (String payload : realPayloads) {
                    if (REFRESH_CONTENT.equals(payload)) {
                        helper.setText(R.id.tv_last_msg, conversationItem.getDigest())
                                .setText(R.id.tv_unread, unreadCount)
                                .setGone(R.id.tv_unread, unReadMsgCnt <= 0)
                                .setText(R.id.tv_create_time, conversationItem.getLastMsgDate() == 0 ? TimestampUtil.getTimePoint(new Date().getTime())
                                        : TimestampUtil.getTimeStringAutoShort2(conversationItem.getLastMsgDate(), false));
                    } else if (REFRESH_AVATAR.equals(payload)) {
                        AvatarGenerator.loadAvatar(getContext(), conversationItem.getConversationId(),
                                conversationItem.getConversationTitle(), helper.getView(R.id.sdv_avatar), true);
                    } else if (PINNED.equals(payload)) {
                        helper.setBackgroundResource(R.id.rl_content, conversationItem.isPinned() ? R.drawable.item_tab_bg_pinned : R.drawable.item_tab_bg);
                    } else if (REFRESH_TITLE.equals(payload)) {
                        helper.setText(R.id.tv_nick_name, conversationItem.getConversationTitle());
                    }
                }
                break;
            case GROUP:
                for (String payload : realPayloads) {
                    if (REFRESH_CONTENT.equals(payload)) {
                        Trace.w(" 进行刷新未读数 设置: " + unreadCount);
                        helper.setText(R.id.tv_unread, unreadCount)
                                .setGone(R.id.tv_unread, unReadMsgCnt <= 0)
                                .setText(R.id.tv_create_time, conversationItem.getLastMsgDate() == 0 ? TimestampUtil.getTimePoint(new Date().getTime())
                                        : TimestampUtil.getTimeStringAutoShort2(conversationItem.getLastMsgDate(), false))
                                .setText(R.id.tv_last_msg, conversationItem.getDigest())
                                .setGone(R.id.aitTv, !ConversationHelper.hasAit(conversationItem.getConversationId()));
                    } else if (REFRESH_AVATAR.equals(payload)) {
                        loadGroupAvatar(conversationItem, helper.getView(R.id.iv_group_avatar));
                    } else if (PINNED.equals(payload)) {
                        helper.setBackgroundResource(R.id.rl_content, conversationItem.isPinned() ? R.drawable.item_tab_bg_pinned : R.drawable.item_tab_bg);
                    } else if (REFRESH_TITLE.equals(payload)) {
                        String conversationTitle = conversationItem.getConversationTitle();
                        Trace.w(" 进行刷新标题 设置: " + conversationTitle);
                        if (TextUtils.isEmpty(conversationTitle)) {
                            getConversationTitle(helper.getView(R.id.tv_group_name), conversationItem.getConversationId());
                        } else {
                            helper.setText(R.id.tv_group_name, conversationTitle);
                        }
                    }
                }
                break;
        }
    }

    /**
     * 根据设置的群ID以及设置的头像数据，计算一个md5值，作为缓存的key
     * 当群ID和数据变化时，就会重新绘制，否则优先从内存缓存中获取
     *
     * @return
     */
    private String createKey(String conversationId, List<String> datas) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(conversationId).append("_");
        for (String s : datas) {
            stringBuilder.append(s).append("_");
        }
        return MD5Util.stringMD5(stringBuilder.toString());
    }

    /**
     * 根据群ID获取群成员头像
     *
     * @param groupId
     * @param imageView
     */
    private void generateDataByMember(String groupId, ImageView imageView) {
        Disposable subscribe = DBManager.Companion.getInstance(getContext())
                .getGroupMemberByGroupId(groupId)
                .subscribe(new Consumer<List<GroupMember>>() {
                    @Override
                    public void accept(List<GroupMember> groupMembers) throws Throwable {
                        checkAvatars(groupId, groupMembers, imageView);
                    }
                });
    }

    private void checkAvatars(String groupId, List<GroupMember> groupMembers, ImageView imageView) {
        Disposable subscribe = Single.fromCallable(new Callable<List<String>>() {
                    @Override
                    public List<String> call() throws Exception {
                        List<String> lists = new ArrayList<>();
                        if (groupMembers.isEmpty()) {
                            AvatarEntity avatar = AppDatabase.getInstance(getContext()).avatarDao().getAvatar(PreferencesUtil.getInstance().getUserId());
                            if (avatar != null && FileUtils.isFileExists(avatar.getAvatarLocalPath())) {
                                lists.add(avatar.getAvatarLocalPath());
                            } else {
                                lists.add(PreferencesUtil.getInstance().getUser().getUserNickName());
                            }
                        } else {
                            for (GroupMember groupMember : groupMembers) {
                                String avatarUserId = groupMember.getMemberOriginId();
                                String splitName = groupMember.getMemberName();
                                AvatarEntity avatar = AppDatabase.getInstance(getContext()).avatarDao().getAvatar(avatarUserId);
                                if (avatar != null && FileUtils.isFileExists(avatar.getAvatarLocalPath())) {
                                    lists.add(avatar.getAvatarLocalPath());
                                } else {
                                    lists.add(splitName);
                                }
                            }
                        }
                        return lists;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(lists -> {
                    /*String key = createKey(groupId, lists);
                    if (Objects.equals(avatarKeys.get(groupId), key)) {
                        // 缓存命中
                        return;
                    } else {
                        avatarKeys.put(groupId, key);
                    }*/
                    Trace.file(groupId,
                            "没有头像 使用宫格群头像 generateDataByMember: >>>> " + lists);
                    GroupAvatarsLib.init(getContext())
                            // 必选，设置最终生成的图片尺寸，单位dp（一般就是当前imageView的大小）
                            .setSize(80)
                            // 设置钉钉或者微信群头像类型 DingLayoutManager、 WechatLayoutManager
                            // 目前钉钉最多组合4个，微信最多9个。超出会自动截取前4或9个
                            .setLayoutManager(new WechatLayoutManager())
                            // 设置使用昵称生成头像时的背景颜色
                            .setNickAvatarColor(R.color.primary_chat_user)
                            // 设置昵称生成头像时的文字大小 ,单位dp （设置为0时 = 单个小头像的1/4大小）
                            .setNickTextSize(0)
                            // 设置群组ID，用于生成缓存key
                            .setGroupId(groupId)
                            // 设置加载最终图片的圆角大小，单位dp，默认0
                            .setRound(10)
                            // 设置内部单个图片的圆角，单位dp，默认0
                            .setChildAvatarRound(5)
                            // 单个图片之间的距离，单位dp，默认0dp
                            .setGap(1)
                            // 设置生成的图片背景色
                            .setGapColor(R.color.chat_gray_normal)
                            // 单个网络图片加载失败时，会展示默认图片
                            .setPlaceholder(R.drawable.group_default)
                            // 设置数据（可设置网络图片地址或者昵称）
                            .setDatas(lists)
                            // 设置要显示最终图片的ImageView
                            .setImageView(imageView)
                            .setDiskFileListener(new Builder.BitmapListener() {
                                @Override
                                public void getBitMap(Bitmap bitmap) {
                                }
                            })
                            .build();
                }, onError -> {

                });
    }

    private void loadAvatar(String avatar, String userId, String userNickName, ImageView iv) {
        iv.setVisibility(View.VISIBLE);
        AvatarGenerator.loadAvatar(getContext(), userId, userNickName, iv, true);
    }

    /**
     * 排序
     */
    public void sortConversation() {
        List<ConversationInfo> newData = new ArrayList<>(getData());
        newData.sort(new Comparator<ConversationInfo>() {
            @Override
            public int compare(ConversationInfo bean1, ConversationInfo bean2) {
                int result;
                if (bean1 == null) {
                    result = 1;
                } else if (bean2 == null) {
                    result = -1;
                } else if (bean1.isPinned() == bean2.isPinned()) {
                    long time = bean1.getLastMsgDate() - bean2.getLastMsgDate();
                    result = time == 0L ? 0 : (time > 0 ? -1 : 1);
                } else {
                    result = bean1.isPinned() ? -1 : 1;
                }
                return result;
            }
        });
        setDiffNewData(newData);
    }

    /**
     * 计算插入的位置
     *
     * @param dataConversation
     * @return
     */
    public int calcDataPosition(ConversationInfo dataConversation) {
        if (dataConversation.isPinned()) {
            return 0;
        }
        int currentPosition = 0;
        List<ConversationInfo> newData = new ArrayList<>(getData());
        newData.remove(dataConversation);
        for (int i = 0; i < newData.size(); i++) {
            ConversationInfo conversationInfo = newData.get(i);
            if (conversationInfo.isPinned()) {
                // 在置顶的后面
                currentPosition++;
                continue;
            }
            if (dataConversation.getLastMsgDate() >= conversationInfo.getLastMsgDate()) {
                return i;
            } else {
                currentPosition++;
            }
        }
        return currentPosition;
    }


    private class MenuHelper implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        private final BaseViewHolder helper;

        public MenuHelper(BaseViewHolder helper) {
            this.helper = helper;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int layoutPosition = helper.getLayoutPosition() - getHeaderLayoutCount();
            if (layoutPosition == RecyclerView.NO_POSITION) {
                return;
            }
            ConversationInfo conversationInfo = getItem(layoutPosition);
            if (conversationInfo == null) {
                return;
            }
            MenuInflater inflater = activity.getMenuInflater();
            inflater.inflate(R.menu.conversation_menu, menu);
            MenuItem item = menu.findItem(R.id.action_pinned_conference);
            if (conversationInfo.isPinned()) {
                item.setTitle(getContext().getString(R.string.cancel_pinned_this_chat));
            } else {
                item.setTitle(getContext().getString(R.string.pinned_this_chat));
            }
            item.setOnMenuItemClickListener(this);
            menu.findItem(R.id.action_delete_conference).setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(@NonNull MenuItem item) {
            int layoutDataPosition = helper.getLayoutPosition() - getHeaderLayoutCount();
            if (layoutDataPosition == RecyclerView.NO_POSITION) {
                return false;
            }
            ConversationInfo conversationInfo = getItem(layoutDataPosition);
            if (conversationInfo == null) {
                return false;
            }
            int itemId = item.getItemId();
            if (itemId == R.id.action_pinned_conference) {
                ConversationInfo cloneData = conversationInfo.clone();
                cloneData.setPinned(!cloneData.isPinned());
                if (conversationInfo.isPinned()) {
                    int calcDataPosition = calcDataPosition(cloneData);
                    Trace.d("onMenuItemClick: 当前是置顶状态 需要取消置顶 计算移动后的位置",
                            "data position" + layoutDataPosition,
                            "calc position" + calcDataPosition);
                    if (layoutDataPosition != calcDataPosition) {
                        setDiffNewData(getPinnedChangedData(layoutDataPosition, calcDataPosition, cloneData));
                    } else {
                        // 位置不变 刷新背景
                        conversationInfo.setPinned(!conversationInfo.isPinned());
                        ArrayList<String> refreshList = new ArrayList<>();
                        refreshList.add(SmartConversationAdapter.PINNED);
                        notifyItemChanged(layoutDataPosition + getHeaderLayoutCount(), refreshList);
                    }
                } else {
                    Trace.d("onMenuItemClick: 当前是取消置顶状态 需要置顶",
                            "data position" + layoutDataPosition);
                    if (layoutDataPosition != 0) {
                        setDiffNewData(getPinnedChangedData(layoutDataPosition, 0, cloneData));
                    } else {
                        conversationInfo.setPinned(!conversationInfo.isPinned());
                        ArrayList<String> refreshList = new ArrayList<>();
                        refreshList.add(SmartConversationAdapter.PINNED);
                        notifyItemChanged(getHeaderLayoutCount(), refreshList);
                    }
                }
                DBManager.Companion.getInstance(getContext())
                        .saveConversation(cloneData).subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                            }

                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                            }
                        });
                return true;
            } else if (itemId == R.id.action_delete_conference) {
                Trace.d("onMenuItemClick: >>> ");
                new MessageDialog.Builder(getContext())
                        .setTitle(getContext().getString(R.string.tips))
                        .setMessage(getContext().getString(R.string.confirm_delete))
                        .setConfirm(getContext().getString(R.string.delete))
                        .setCancel(getContext().getString(R.string.cancel))
                        .setListener(new MessageDialog.OnListener() {
                            @Override
                            public void onConfirm(BaseDialog dialog) {
                                DBManager.Companion.getInstance(getContext())
                                        .deleteConversation(PreferencesUtil.getInstance().getUserId(),
                                                conversationInfo.getConversationId()).subscribe();
                                remove(layoutDataPosition);
                            }

                            @Override
                            public void onCancel(BaseDialog dialog) {
                            }
                        })
                        .show();

                return true;
                // 添加更多菜单项的处理逻辑
            }
            return false;
        }
    }

    private List<ConversationInfo> getPinnedChangedData(int beforePosition, int afterPosition, ConversationInfo cloneData) {
        ArrayList<ConversationInfo> conversationInfos = new ArrayList<>(getData());
        conversationInfos.remove(beforePosition);
        conversationInfos.add(afterPosition, cloneData);
        return conversationInfos;
    }

}
