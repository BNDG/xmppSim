package com.hjq.demo.chat.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.StringUtils;
import com.bndg.smack.OmemoHelper;
import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.extensions.base.IExtension;
import com.bndg.smack.model.SmartUserInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.chad.library.adapter.base.listener.OnUpFetchListener;
import com.effective.android.panel.PanelSwitchHelper;
import com.effective.android.panel.interfaces.ContentScrollMeasurer;
import com.effective.android.panel.interfaces.PanelHeightMeasurer;
import com.effective.android.panel.interfaces.TriggerViewClickInterceptor;
import com.effective.android.panel.interfaces.listener.OnPanelChangeListener;
import com.effective.android.panel.view.panel.IPanelView;
import com.effective.android.panel.view.panel.PanelView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.bean.DialogItemBean;
import com.hjq.demo.chat.adapter.EmojiGridAdapter;
import com.hjq.demo.chat.adapter.SmartMessageAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.ConversationDao;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.GroupMemberDao;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.dao.UserDao;
import com.hjq.demo.chat.entity.CallCreatorInfo;
import com.hjq.demo.chat.entity.CardInfoBean;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.entity.ChatFileBean;
import com.hjq.demo.chat.entity.ChatImageBean;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ChatVideoBean;
import com.hjq.demo.chat.entity.ChatVoiceBean;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.entity.MemberVoiceEntity;
import com.hjq.demo.chat.entity.User;
import com.hjq.demo.chat.entity.enums.MessageStatus;
import com.hjq.demo.chat.extensions.AitExtension;
import com.hjq.demo.chat.extensions.CallExtension;
import com.hjq.demo.chat.listener.ChatEdittextAction;
import com.hjq.demo.chat.listener.ChatMsgCallback;
import com.hjq.demo.chat.listener.ChatRoomListener;
import com.hjq.demo.chat.listener.ContactCallback;
import com.hjq.demo.chat.listener.DownloadFileListener;
import com.hjq.demo.chat.listener.SimpleResultCallback;
import com.hjq.demo.chat.manager.CallManager;
import com.hjq.demo.chat.manager.ChatMessageManager;
import com.hjq.demo.chat.service.DownloadService;
import com.hjq.demo.chat.service.UploadService;
import com.hjq.demo.chat.utils.ConversationHelper;
import com.hjq.demo.chat.utils.EmojiSource;
import com.hjq.demo.chat.utils.FileUtil;
import com.hjq.demo.chat.utils.JimUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.views.AutoHidePanelRecyclerView;
import com.hjq.demo.chat.views.ait.AitManager;
import com.hjq.demo.chat.views.ait.AitTextviewManager;
import com.hjq.demo.chat.voice.MediaManager;
import com.hjq.demo.chat.voice.RecordButton;
import com.hjq.demo.chat.widget.ConfirmDialog;
import com.hjq.demo.chat.widget.selecttext.SelectTextEvent;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.other.AppConfig;
import com.hjq.demo.other.PermissionCallback;
import com.hjq.demo.ui.activity.ImagePreviewMsgActivity;
import com.hjq.demo.ui.activity.ImageSelectActivity;
import com.hjq.demo.ui.activity.VideoPlayActivity;
import com.hjq.demo.ui.dialog.DemoListDialogFragment;
import com.hjq.demo.ui.dialog.MessageDialog;
import com.hjq.demo.ui.popup.ListPopup;
import com.hjq.demo.utils.CheckUtil;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.OpenFileUtils;
import com.hjq.demo.utils.Trace;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.rxjava.rxlife.RxLife;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * 聊天界面 有可能是单聊 群聊
 *
 * @author zhou
 */
public class ChatActivity extends ChatBaseActivity implements View.OnClickListener, ChatMsgCallback, OnLoadMoreListener, IChatRoomCallback {
    public static final int REQUEST_CODE_VOICE = 5;
    public static final int REQUEST_CODE_IMAGE_ALBUM = 6;
    public static final int REQUEST_CODE_IMAGE_CAMERA = 7;
    public static final int REQUEST_CODE_LOCATION = 8;
    public static final int REQUEST_CODE_FILE = 10;

    InputMethodManager mManager;

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.ll_video_call)
    View ll_video_call;

    // 切换成语音
    @BindView(R.id.btn_set_mode_voice)
    Button btn_switch_voice;
    // 按住说话
    @BindView(R.id.ll_press_to_speak)
    LinearLayout mPressToSpeakLl;

    // 发送图片-"相册"
    @BindView(R.id.ll_image_album)
    LinearLayout ll_image_extension;

    // 发送图片-"拍照"
    @BindView(R.id.ll_image_camera)
    LinearLayout ll_tack_photo_extension;

    // 位置
    @BindView(R.id.ll_chat_location)
    LinearLayout ll_location_extension;

    @BindView(R.id.iv_emoji_control)
    ImageView ivShowEmoji;

    @BindView(R.id.btn_more_send)
    Button btn_more_send;

    @BindView(R.id.btn_send)
    Button mSendBtn;

    @BindView(R.id.rl_text_msg)
    RelativeLayout mTextMsgRl;

    @BindView(R.id.et_text_msg)
    EditText mTextMsgEt;
    @BindView(R.id.btnAudio)
    RecordButton recordButton;
    @BindView(R.id.iv_chat_video)
    View ivChatVideo;

    @BindView(R.id.iv_more)
    ImageView ivMore;
    @BindView(R.id.ll_conversation_unavailable)
    View ll_conversation_unavailable;

    @BindView(R.id.rv_message)
    AutoHidePanelRecyclerView mMessageRv;
    @BindView(R.id.ll_group_call)
    View ll_group_call;
    @BindView(R.id.tv_group_call_tips)
    TextView tv_group_call_tips;
    @BindView(R.id.tv_unread_tips)
    TextView tv_unread_tips;
    @BindView(R.id.tv_reason)
    TextView tvUnAvailableReason;
    @BindView(R.id.scroll_to_bottom_button)
    FloatingActionButton mScrollToBottomButton;
    @BindView(R.id.iv_lock)
    ImageView ivLock;

    List<ChatMessage> mChatMessageList;

    // 会话id
    private String conversationId;
    // 会话类型
    private String conversationType;
    // 会话标题
    private String conversationTitle;
    // 通话消息索引
    private int callMessageIndex = -1;
    // 适配器
    SmartMessageAdapter smartMessageAdapter;
    // 页码
    private int pageCount = 0;
    // 跳转行号
    int lineNum = -1;
    // 是否是单聊
    private boolean isSingle;
    private boolean isGroup;
    private boolean isOnPause;
    // 是否滚动到了底部
    private boolean needScrollToEnd = true;
    // 是否有新消息
    private boolean hasNewMsg;
    private LinearLayoutManager layoutManager;
    // 会话是否可用
    private boolean conversationAvailable = true;
    private ChatMessage latestCallMsg;
    private int unfilledHeight = 0;
    private PanelSwitchHelper mHelper;
    // @消息管理器
    private AitManager mAitManager;
    private AitTextviewManager aitTextviewManager;
    // 文件发送队列
    private LinkedList<String> mFileSendQueue = new LinkedList<>();
    private LinkedList<ChatFileBean> mFileBeanSendQueue = new LinkedList<>();
    private boolean isOriginalImg;
    // 是搜索记录
    private boolean isFromSearchRecord;
    private String lastOriginId;
    private long lastTimestamp;
    private boolean isLoadingMore;
    private boolean isUploading;

    public static void start(Context context, String conversationType,
                             String conversationId, String conversationTitle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constant.CONVERSATION_TYPE, conversationType);
        intent.putExtra(Constant.CONVERSATION_ID, conversationId);
        intent.putExtra(Constant.CONVERSATION_TITLE, conversationTitle);

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        context.startActivity(intent);
    }


    public static void startFromSearch(Context context, String targetType,
                                       String contactId, String conversationTitle, int page, int lineNum) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constant.CONVERSATION_TYPE, targetType);
        intent.putExtra(Constant.CONVERSATION_ID, contactId);
        intent.putExtra(Constant.CONVERSATION_TITLE, conversationTitle);
        intent.putExtra(Constant.MESSAGE_PAGE, page);
        intent.putExtra(Constant.MESSAGE_LINENUM, lineNum);

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    public void initView() {
        setTitleStrokeWidth(tv_title);
        ivMore.setVisibility(View.VISIBLE);
        ivChatVideo.setOnLongClickListener(v -> {
            return true;
        });
        mManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ImmersionBar.with(this)
                .statusBarColor(R.color.titlebar_bg)
                .navigationBarColor(R.color.titlebar_bg)
                .keyboardEnable(true)
                .autoDarkModeEnable(true)
                .init();
        EventBus.getDefault().register(this);
        initEmoji();
        mTextMsgEt.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                MMKV.defaultMMKV().putString(conversationId + Constant.LAST_INPUT, s.toString());
            }
        });
        aitTextviewManager = new AitTextviewManager(mTextMsgEt);
        aitTextviewManager.setmProxy(new ChatEdittextAction() {
            @Override
            public void onTypeStateChange(boolean isTyping) {
                if (isTyping) {
                    btn_more_send.setVisibility(View.GONE);
                    mSendBtn.setVisibility(View.VISIBLE);
                } else {
                    btn_more_send.setVisibility(View.VISIBLE);
                    mSendBtn.setVisibility(View.GONE);
                }
            }
        });
        setOnClickListener(R.id.iv_more, R.id.ll_image_album, R.id.ll_image_camera, R.id.scroll_to_bottom_button,
                R.id.ll_chat_location, R.id.btn_set_mode_voice, R.id.iv_chat_video, R.id.tv_join, R.id.tv_unread_tips,
                R.id.iv_lock);
    }

    /**
     * 初始化表情
     */
    private void initEmoji() {
        List<String> stringList = Arrays.asList(EmojiSource.people);
        ArrayList<String> arrayList = new ArrayList<>(stringList);
        RecyclerView rv_emoji_list = findViewById(R.id.rv_emoji_list);
        // 设置布局管理器，这里将列数设置为 8
        GridLayoutManager layoutManager = new GridLayoutManager(this, 8);
        rv_emoji_list.setLayoutManager(layoutManager);
        EmojiGridAdapter emojiGridAdapter = new EmojiGridAdapter(arrayList);
        emojiGridAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int position) {
                String emoji = String.valueOf(baseQuickAdapter.getItem(position));
                // 获取当前光标位置
                int cursorPosition = mTextMsgEt.getSelectionStart();
                // 插入 Emoji 到光标位置
                mTextMsgEt.getText().insert(cursorPosition, emoji);
                // 在需要的时候请求焦点并显示光标
                mTextMsgEt.requestFocus();
                mTextMsgEt.setCursorVisible(true);
            }
        });
        rv_emoji_list.setAdapter(emojiGridAdapter);
    }

    @Override
    protected boolean isStatusBarEnabled() {
        return false;
    }

    @Override
    public void initListener() {
    }

    @Override
    public void initData() {
        ChatMessageManager.getInstance().addMsgCallback(this);
        ChatRoomListener.getInstance().addListener(this);
        smartMessageAdapter = new SmartMessageAdapter(new ArrayList<>());
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mMessageRv.setLayoutManager(layoutManager);
        mMessageRv.setAdapter(smartMessageAdapter);
        smartMessageAdapter.setEditText(mTextMsgEt);
        smartMessageAdapter.getUpFetchModule().setOnUpFetchListener(new OnUpFetchListener() {
            @Override
            public void onUpFetch() {
                startUpFetch();
            }
        });
        smartMessageAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
        smartMessageAdapter.getLoadMoreModule().setAutoLoadMore(true);
        smartMessageAdapter.getLoadMoreModule().setEnableLoadMore(false);
        smartMessageAdapter.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(false);
        smartMessageAdapter.getLoadMoreModule().loadMoreEnd();
        smartMessageAdapter.getUpFetchModule().setStartUpFetchPosition(1);
        smartMessageAdapter.setOnItemChildClickListener(
                (baseQuickAdapter, view, position) -> {
                    ChatMessage item = smartMessageAdapter.getItem(position);
                    if (null == item) {
                        return;
                    }
                    boolean isSent = item.getIsSent();
                    String messageContent = item.getMessageContent();
                    if (view.getId() == R.id.iv_msg_status) {
                        if (CheckUtil.isFastDoubleClick()) {
                            return;
                        }
                        // 重发文字消息
                        if (SmartContentType.TEXT.equals(item.getMessageType())) {
                            sendSmackTextMsg(conversationType,
                                    conversationId,
                                    item.getMessageType(),
                                    messageContent,
                                    new ArrayList<>(),
                                    item.getOriginId(),
                                    position);
                        } else if (SmartContentType.IMAGE.equals(item.getMessageType())) {
                            if (FileUtils.isFileExists(item.getFileLocalPath())) {
                                item.setStatus(MessageStatus.SENDING.value());
                                smartMessageAdapter.notifyItemChanged(position);
                                // 重发图片消息
                                mFileBeanSendQueue.add(prepareImageFileMsg(item.getFileLocalPath(), item.getOriginId()));
                                uploadImageFile();
                            } else {
                                toast(R.string.file_not_exist);
                            }
                        }
                    } else if (view.getId() == R.id.sdv_image_content) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.MESSAGE_FILE_LOCAL, item.getFileLocalPath());
                        bundle.putString(Constant.MESSAGE_ORIGIN_ID, item.getOriginId());
                        bundle.putString(Constant.MESSAGE_CONTENT, item.getMessageContent());
                        bundle.putString(Constant.CONVERSATION_ID, conversationId);
                        ImagePreviewMsgActivity.start(getActivity(), bundle);
                    } else if (view.getId() == R.id.cv_voice_content) {
                        Trace.d("onItemChildClick: play audio");
                        View iv_chat_voice = view.findViewById(R.id.iv_chat_voice);
                        ChatVoiceBean chatVoiceMessage = JsonParser.deserializeByJson(item.getExtraData(), ChatVoiceBean.class);
                        if (chatVoiceMessage == null || TextUtils.isEmpty(item.getMessageContent())) {
                            return;
                        }
                        //点击正在播放的 停止播放
                        if (MediaManager.getInstance(ChatActivity.this)
                                .isCurentPlayFile(item.getMessageContent())) {
                            iv_chat_voice.setBackgroundResource(
                                    isSent ? R.drawable.voice_sent_static : R.drawable.voice_receive_static);
                            MediaManager.getInstance(ChatActivity.this).reset();
                            return;
                        }
                        MediaManager.getInstance(ChatActivity.this)
                                .setCurrentPlayFile(item.getMessageContent());
                        // 将逐帧动画资源设置到
                        iv_chat_voice.setBackgroundResource(
                                isSent ? R.drawable.voice_sent_anim : R.drawable.voice_receive_anim);
                        // 获取 AnimationDrawable 对象
                        AnimationDrawable animationDrawable = (AnimationDrawable) iv_chat_voice.getBackground();
                        // 开始动画
                        animationDrawable.start();
                        String voiceFilePath = item.getFileLocalPath();
                        if (isSent && FileUtils.isFileExists(voiceFilePath)) {
                            playVoice(iv_chat_voice, voiceFilePath, true);
                        } else {
                            File voiceFile = FileUtil.getVoiceFile(ChatActivity.this,
                                    item.getMessageContent());
                            // todo 改成service下载
                            ChatMessageManager.downLoadFile(ChatActivity.this, voiceFile, item.getMessageContent(), new DownloadFileListener() {
                                @Override
                                public void onComplete(File file) {
                                    // 应该保存下载之后的文件地址
                                    item.setFileLocalPath(file.getAbsolutePath());
                                    MessageDao.getInstance().save(item);
                                    playVoice(iv_chat_voice, file.getPath(), isSent);
                                }

                                @Override
                                public void onError(File var1, Throwable var2) {

                                }
                            });
                        }
                    } else if (view.getId() == R.id.iv_video_thumbnail) {
                        ChatVideoBean chatVideoBean = JsonParser.deserializeByJson(item.getExtraData(), ChatVideoBean.class);
                        if (chatVideoBean == null || TextUtils.isEmpty(chatVideoBean.getThumbnailUrl())) {
                            if (SmartCommHelper.getInstance().isDeveloperMode()) {
                                new VideoPlayActivity.Builder()
                                        .setVideoTitle("")
                                        .setVideoSource(item.getMessageContent())
                                        .setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                        .setAutoOver(false)
                                        .start(ChatActivity.this);
                            }
                            return;
                        }
                        String thumbnailLocalPath = chatVideoBean.getThumbnailLocalPath();
                        if (!TextUtils.isEmpty(thumbnailLocalPath)) {
                            File file = new File(thumbnailLocalPath);
                            if (file.exists()) {
                                if (isSent) {
                                    new VideoPlayActivity.Builder()
                                            .setVideoTitle("")
                                            .setVideoSource(new File(chatVideoBean.getVideoLocalPath()))
                                            .setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                            .setAutoOver(false)
                                            .start(ChatActivity.this);
                                } else {
                                    new VideoPlayActivity.Builder()
                                            .setVideoTitle("")
                                            .setVideoSource(item.getMessageContent())
                                            .setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                            .setAutoOver(false)
                                            .start(ChatActivity.this);
                                }
                                return;
                            }
                        }
                        // 下载完封面后再去播放
                        if (!TextUtils.isEmpty(chatVideoBean.getThumbnailUrl())) {
                            File videoThumbnailFile = FileUtil.getThumbnailFile(ChatActivity.this,
                                    chatVideoBean.getThumbnailUrl());
                            ChatMessageManager.downLoadFile(ChatActivity.this, videoThumbnailFile, chatVideoBean.getThumbnailUrl(), new DownloadFileListener() {
                                @Override
                                public void onComplete(File file) {
                                    chatVideoBean.setThumbnailLocalPath(file.getAbsolutePath());
                                    item.setExtraData(JsonParser.serializeToJson(chatVideoBean));
                                    MessageDao.getInstance().save(item);
                                    playVideoFile(item.getMessageContent());
                                }

                                @Override
                                public void onError(File var1, Throwable var2) {

                                }
                            });
                        } else {
                            playVideoFile(item.getMessageContent());
                        }
                    } else if (view.getId() == R.id.cv_file_content) {
                        ChatFileBean chatFileBean = JsonParser.deserializeByJson(item.getExtraData(), ChatFileBean.class);
                        if (chatFileBean == null) {
                            return;
                        }
                        if (isSent) {
                            // 这里的路径是？
                            String fileLocalPath = item.getFileLocalPath();
                            Trace.d("onItemChildClick: 本机发送的文件路径 " + fileLocalPath);
                            OpenFileUtils.chooseOpenFile(ChatActivity.this, chatFileBean.fileName, fileLocalPath);
                        } else {
                            // 下载 应该进度显示
                            if (TextUtils.isEmpty(item.getFileLocalPath())) {
                                DownloadService.Companion.startService(ChatActivity.this,
                                        item.getMessageContent(),
                                        item.getOriginId(),
                                        item.getMessageType());
                            } else {
                                OpenFileUtils.chooseOpenFile(ChatActivity.this, chatFileBean.fileName, item.getFileLocalPath());
                            }
                        }
                    } else if (view.getId() == R.id.sdv_avatar) {
                        // xmpp 群聊无法获取真实id
                        String fromUserId = item.getFromUserId();
                        goUserInfoActivity(fromUserId);
                    } else if (view.getId() == R.id.cv_card_info) {
                        CardInfoBean cardInfoBean = JsonParser.deserializeByJson(item.getExtraData(), CardInfoBean.class);
                        if (cardInfoBean != null) {
                            goUserInfoActivity(cardInfoBean.getUserId());
                        }
                    }
                });
        //使用mp3格式 录制语音消息
        recordButton.setUseMP3(false);
        recordButton.setOnFinishedRecordListener((audioPath, audioDuration) -> {
            Trace.d("onFinishedRecord: " + audioPath + ">>> audioDuration>>>" + audioDuration);
            ChatVoiceBean voiceBean = ChatMessageManager.getInstance().createVoiceBean(conversationType, conversationId, audioPath, audioDuration);
            voiceBean.originId = addFileMsg(voiceBean.fileLocalPath, SmartContentType.VOICE, voiceBean);
            Intent intent = new Intent(ChatActivity.this, UploadService.class);
            intent.putExtra(Constant.FILE_BEAN, voiceBean);
            intent.putExtra(Constant.MESSAGE_FILE_LOCAL, voiceBean.fileLocalPath);
            intent.putExtra(Constant.FILE_TYPE, SmartContentType.VOICE);
            startService(intent);
        });
        smartMessageAdapter.setOnItemChildLongClickListener(new OnItemChildLongClickListener() {

            @Override
            public boolean onItemChildLongClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                ChatMessage item = smartMessageAdapter.getItem(position);
                if (item.isGroupMsg()) {
                    // 查看我能对对方做什么操作的菜单
                    if (item.getIsSent()) {
                        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
                        map.put(getString(R.string.view_speech_record), 100);
                        showOperationMenu(item, map, view, null);
                    } else {
                        // 此处用userName 也就是memberAccount去匹配
                        GroupMemberDao.getInstance().getOperationList(conversationId, item.getFromUserId(), new GroupMemberDao.GroupMemberCallback() {

                            @Override
                            public void getOperationList(LinkedHashMap<String, Integer> strings, GroupMember targetMember) {
                                post(() -> {
                                    if (SmartCommHelper.getInstance().isDeveloperMode()) {
                                        strings.put("屏蔽对方所有消息", 110);
                                    }
                                    showOperationMenu(item, strings, view, targetMember);
                                });
                            }
                        });
                    }
                }
                return true;
            }
        });
        initOtherData();
        tv_title.setOnClickListener(v -> {
      /*      toast("laq");
            String archivedId = "";
            ChatMessage message = MessageDao.getInstance()
                    .getEarliestMessageByConversationId(conversationId);
            if (null != message) {
                archivedId = message.getArchivedId();
                Trace.d(archivedId + "joinRoom: >>>>>" + message.getMessageContent());
            }*/
        });
    }

    private void playVideoFile(String source) {
        new VideoPlayActivity.Builder()
                .setVideoTitle("")
                .setVideoSource(source)
                .setActivityOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .setAutoOver(false)
                .start(ChatActivity.this);
    }

    private void goUserInfoActivity(String fromUserId) {
        if (!TextUtils.isEmpty(fromUserId)) {
            if (!PreferencesUtil.getInstance().getUserId().equals(fromUserId)) {
                UserDao.getInstance().getUserById(ChatActivity.this, fromUserId, new ContactCallback() {
                    @Override
                    public void getUser(@Nullable User userById) {
                        if (userById != null) {
                            if (userById.isFriend()) {
                                UserInfoActivity.start(getContext(), fromUserId);
                            } else {
                                UserInfoActivity.start(getContext(), fromUserId, Constant.CONTACTS_FROM_WX_ID);
                            }
                        } else {
                            // 查询然后跳转
                            if (SmartCommHelper.getInstance().isDeveloperMode()) {
                                showDialog();
                                SmartIMClient.getInstance().getSmartCommUserManager().getUserInfo(fromUserId, new IUserInfoCallback2() {
                                    @Override
                                    public void onSuccess(SmartUserInfo userInfo) {
                                        hideDialog();
                                        if (userInfo != null) {
                                            User userById = new User();
                                            userById.setBelongAccount(PreferencesUtil.getInstance().getUserId());
                                            userById.setUserNickName(userInfo.getNickname());
                                            userById.setUserId(fromUserId);
                                            UserDao.getInstance().saveOrUpdateContact(userById);
                                            UserInfoActivity.start(ChatActivity.this, fromUserId, Constant.CONTACTS_FROM_WX_ID);
                                        } else {
                                            toast(getString(R.string.user_not_found));
                                        }
                                    }

                                    @Override
                                    public void onFailed(int code, String desc) {
                                        hideDialog();
                                        toast(getString(R.string.user_not_found));
                                    }
                                });
                            }
                        }
                    }
                });
            } else {
                Intent intent = new Intent(this, UserInfoMyActivity.class);
                startActivity(intent);
            }
        }
    }

    private void showOperationMenu(ChatMessage item, LinkedHashMap<String, Integer> map, View view, GroupMember targetMember) {
        int delay = 0;
        if (!mHelper.isResetState()) {
            mHelper.hookSystemBackByPanelSwitcher();
            delay = 250;
        }
        postDelayed(() -> {
            new ListPopup.Builder(ChatActivity.this, Gravity.LEFT)
                    .setList(new ArrayList<>(map.keySet()))
                    .setListener((ListPopup.OnListener<String>) (popupWindow, popPosition, s) -> {
                        if (map.get(s) == 100) {
                            // 查找聊天记录
                            SearchRecordActivity.start(ChatActivity.this,
                                    item.getConversationType(), item.getConversationId(),
                                    item.getFromUserName(),
                                    item.getFromUserId());
                        } else if (map.get(s) == 101) {
                            // @某人 由于当前没有群昵称功能 所以此处会显示成员的真实昵称
                            if (targetMember != null) {
                                mAitManager.insertReplyAit(targetMember.getMemberOriginId(), targetMember.getMemberName());
                            }
                        } else if (map.get(s) == 102 || map.get(s) == 103) {
                            // 禁言某人
                            if (targetMember != null) {
                                SmartIMClient.getInstance().getSmartCommChatRoomManager()
                                        .muteMember(map.get(s) == 102, conversationId, targetMember.getMemberAccount(), new IChatRoomCallback() {
                                            @Override
                                            public void muteMemberSuccess() {
                                                toast(StringUtils.getString(R.string.success));
                                            }
                                        });
                            }
                        } else if (map.get(s) == 110) {
                            // 屏蔽消息
                            MessageDao.getInstance().deleteMessageByUserId(ChatActivity.this,
                                    item.getFromUserId(), new SimpleResultCallback() {
                                        @Override
                                        public void onResult(boolean isSuccess) {
                                            toast(StringUtils.getString(R.string.success));
                                            loadChatRecord(false);
                                        }
                                    });
                        }
                    })
                    .showAsDropDown(view);
        }, delay);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mHelper == null) {
            mHelper = new PanelSwitchHelper.Builder(this)
                    //可选
                    .addKeyboardStateListener((visible, height) -> Trace.d("系统键盘是否可见 : " + visible + " 高度为：" + height))
                    .addEditTextFocusChangeListener((view, hasFocus) -> {
                        Trace.d("输入框是否获得焦点 : " + hasFocus);
                        if (hasFocus) {
                            // 聊天页拉至最下
                            scrollToEnd(null, true);
                        }
                    })
                    .setTriggerViewClickInterceptor((TriggerViewClickInterceptor) triggerId -> {
                        // return true 可拦截
                        return false;
                    })
                    //可选
                    .addViewClickListener(view -> {
                        switch (view.getId()) {
                            case R.id.et_text_msg:
                            case R.id.btn_more_send:
                            case R.id.iv_emoji_control: {
                                scrollToEnd(null, true);
                            }
                            break;
                        }
                    })
                    //可选
                    .addPanelChangeListener(new OnPanelChangeListener() {

                        @Override
                        public void onKeyboard() {
                            Trace.d("唤起系统输入法");
                            ivShowEmoji.setSelected(false);
                            if (pageCount != 0) {
                                pageCount = 0;
                                MessageDao.getInstance().getMessagesByConversationId(ChatActivity.this, conversationId, pageCount, new MessageDao.MessageDaoCallback() {
                                    @Override
                                    public void getMessagesByConversationId(List<ChatMessage> chatMessages) {
                                        smartMessageAdapter.setList(chatMessages);
                                        mChatMessageList = smartMessageAdapter.getData();
                                        needScrollToEnd = true;
                                        scrollToEnd(null, false);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onNone() {
                            Trace.d("隐藏所有面板");
                            ivShowEmoji.setSelected(false);
                        }

                        @Override
                        public void onPanel(IPanelView view) {
                            Trace.d("唤起面板 : " + view);
                            switchVoiceBtn(false);
                            if (view instanceof PanelView) {
                                ivShowEmoji.setSelected(((PanelView) view).getId() == R.id.panel_emotion ? true : false);
                                scrollToEnd(null, true);
                            }
                        }

                        @Override
                        public void onPanelSizeChange(IPanelView panelView, boolean portrait, int oldWidth, int oldHeight, int width, int height) {
                            if (panelView instanceof PanelView) {
                                switch (((PanelView) panelView).getId()) {
                                    case R.id.panel_emotion: {
                                        // 只会进来一次
                                        int viewPagerSize = height - SizeUtils.dp2px(30f);
                                        break;
                                    }
                                    case R.id.panel_addition: {
                                        //auto center,nothing to do
                                        break;
                                    }
                                }
                            }
                        }
                    })
                    .addContentScrollMeasurer(new ContentScrollMeasurer() {
                        @Override
                        public int getScrollDistance(int i) {
                            return 0;
                        }

                        @Override
                        public int getScrollViewId() {
                            return R.id.imageView;
                        }
                    })
                    .addContentScrollMeasurer(new ContentScrollMeasurer() {
                        @Override
                        public int getScrollDistance(int defaultDistance) {
                            return defaultDistance - unfilledHeight;
                        }

                        @Override
                        public int getScrollViewId() {
                            return R.id.rv_message;
                        }
                    })
                    /**
                     * 可选，可不设置
                     * 面板默认高度设置,输入法显示后会采纳输入法高度为面板高度，否则则以框架内部默认值为主
                     */
                    .addPanelHeightMeasurer(new PanelHeightMeasurer() {

                        /**
                         * false 为不同步输入法高度
                         * @return
                         */
                        @Override
                        public boolean synchronizeKeyboardHeight() {
                            return false;
                        }

                        @Override
                        public int getTargetPanelDefaultHeight() {
                            return SizeUtils.dp2px(122f);
                        }

                        @Override
                        public int getPanelTriggerId() {
                            return R.id.btn_more_send;
                        }
                    })
                    .logTrack(true)             //output log
                    .build();
            mMessageRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    if (tv_unread_tips.getVisibility() == View.VISIBLE) {
                        ChatMessage item = smartMessageAdapter.getItem(firstVisibleItemPosition);
                        if (item != null) {
                            if (item.getTimestamp() < lastTimestamp) {
                                tv_unread_tips.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    // 判断是否滚动到底部
                    if (lastVisibleItemPosition >= totalItemCount - 1) {
                        // 这里可以执行相应的操作，比如加载更多数据
                        needScrollToEnd = true;
                        mScrollToBottomButton.hide();
                    }

                    if (dy < 0) {
                        // 向上滚动
                        // 可以执行相应的操作
                        needScrollToEnd = !recyclerView.canScrollVertically(1);
                        // 不是搜索聊天记录 才显示
                        if (!isFromSearchRecord && dy < -30) {
                            mScrollToBottomButton.show();
                        }
                    } else if (dy > 0) {
                        // 向下滚动
                        // 可以执行相应的操作
                    }

                    View lastChildView = layoutManager.findViewByPosition(lastVisibleItemPosition);
                    if (lastChildView != null) {
                        int bottom = lastChildView.getBottom();
                        int listHeight = mMessageRv.getHeight() - mMessageRv.getPaddingBottom();
                        unfilledHeight = listHeight - bottom;
                    }
                }
            });
        }
        mMessageRv.setPanelSwitchHelper(mHelper);
    }

    /**
     * 切换语音按钮
     *
     * @param isShow
     */
    private void switchVoiceBtn(boolean isShow) {
        if (isShow) {
            // 切换为键盘图标
            btn_switch_voice.setSelected(true);
            // 显示"按住说话"
            mPressToSpeakLl.setVisibility(View.VISIBLE);
            // 隐藏文本输入框
            mTextMsgRl.setVisibility(View.GONE);
            // 隐藏所有面板
            mHelper.hookSystemBackByPanelSwitcher();
        } else {
            // 切换为语音图标
            btn_switch_voice.setSelected(false);
            // 隐藏按住说话
            mPressToSpeakLl.setVisibility(View.GONE);
            // 显示输入框
            mTextMsgRl.setVisibility(View.VISIBLE);
        }
    }

    private void initOtherData() {
        conversationAvailable = true;
        pageCount = getIntent().getIntExtra(Constant.MESSAGE_PAGE, 0);
        lineNum = getIntent().getIntExtra(Constant.MESSAGE_LINENUM, -1);
        isFromSearchRecord = lineNum != -1;
        conversationId = getIntent().getStringExtra(Constant.CONVERSATION_ID);
        conversationTitle = getIntent().getStringExtra(Constant.CONVERSATION_TITLE);
        conversationType = getIntent().getStringExtra(Constant.CONVERSATION_TYPE);
        isSingle = SmartConversationType.SINGLE.name().equals(conversationType);
        isGroup = SmartConversationType.GROUP.name().equals(conversationType);
        tv_title.setText(conversationTitle);
        if (OmemoHelper.getInstance().isEnableEncrypt(conversationId)) {
            ivLock.setImageResource(R.drawable.ic_lock_24dp);
        } else {
            ivLock.setImageResource(R.drawable.ic_lock_open_outline_24dp);
        }
        if (isSingle) {
            UserDao.getInstance().getUserById(this, conversationId, new ContactCallback() {
                @Override
                public void getUser(@Nullable User userById) {
                    if (userById != null && Constant.USER_TYPE_WEIXIN.equals(userById.getUserType())) {
                        ivMore.setVisibility(View.INVISIBLE);
                    }
                }
            });
        } else if (isGroup) {
            // 初始化AitManager
            SmartIMClient.getInstance().getSmartCommChatRoomManager().checkGroupStatus(conversationId);
            mAitManager = new AitManager(this, conversationId);
            mAitManager.setAitTextChangeListener(aitTextviewManager);
            aitTextviewManager.setAitTextWatcher(mAitManager);
        }
        DBManager.Companion.getInstance(this)
                .getConversationByConversationId(myUserInfo.getUserId(), conversationId)
                .to(RxLife.to(this))
                .subscribe(conversationInfoList -> {
                    ConversationInfo conversationInfo;
                    if (conversationInfoList.isEmpty()) {
                        conversationInfo = null;
                    } else {
                        conversationInfo = conversationInfoList.get(0);
                    }
                    if (conversationInfo != null) {
                        conversationAvailable = conversationInfo.isAvailable();
                        if (!conversationAvailable) {
                            // 已不在群聊时
                            if (isGroup) {
                                ll_conversation_unavailable.setVisibility(View.VISIBLE);
                            }
                        }
                        if (isGroup) {
                            String subTitle;
                            conversationTitle = conversationInfo.getConversationTitle();
                            if (TextUtils.isEmpty(conversationTitle)) {
                                subTitle = ActivityManager.getInstance().getApplication().getString(R.string.normal_group_name);
                            } else {
                                subTitle = JimUtil.truncateConversationTitle(conversationTitle);
                            }
                            DBManager.Companion.getInstance(this).getGroupMemberByGroupId(conversationId)
                                    .to(RxLife.to(this))
                                    .subscribe(new Consumer<List<GroupMember>>() {
                                        @Override
                                        public void accept(List<GroupMember> groupMembers) throws Throwable {
                                            int size = groupMembers.size();
                                            tv_title.setText(subTitle + "(" + size + ")");
                                        }
                                    });
                        }
                    }
                });
        DBManager.Companion.getInstance(getContext())
                .getMemberVoiceInGroup(conversationId, SmartCommHelper.getInstance().getAccountIdInGroup(conversationId))
                .to(RxLife.to(this))
                .subscribe(memberVoiceEntities -> {
                    if (!memberVoiceEntities.isEmpty()) {
                        MemberVoiceEntity memberVoiceEntity = memberVoiceEntities.get(0);
                        if (memberVoiceEntity.noRightToSpeak()) {
                            ll_conversation_unavailable.setVisibility(View.VISIBLE);
                            tvUnAvailableReason.setText(getString(R.string.no_right_to_speak));
                        }
                    }
                });
        ConversationDao.getInstance().clearUnreadMsgCount(conversationId);
        if (SmartCommHelper.getInstance().isDeveloperMode() && isSingle) {
            findViewById(R.id.iv_lock).setVisibility(View.VISIBLE);
        }
        post(() -> {
            loadChatRecord(true);
            String lastInputStr = MMKV.defaultMMKV().getString(conversationId + Constant.LAST_INPUT, "");
            if (!TextUtils.isEmpty(lastInputStr) && !lastInputStr.contains("@")) {
                // 只恢复普通文本
                mTextMsgEt.setText(lastInputStr);
                mTextMsgEt.setSelection(lastInputStr.length());
            }
        });
    }

    /**
     * 加载聊天记录
     *
     * @param isStart 是否是第一次加载
     */
    private void loadChatRecord(boolean isStart) {
        Trace.d("loadChatRecord: load Start " + System.currentTimeMillis());
        MessageDao.getInstance().getMessagesByConversationId(this, conversationId, pageCount, new MessageDao.MessageDaoCallback() {
            @Override
            public void getMessagesByConversationId(List<ChatMessage> chatMessages) {
                Trace.d("loadChatRecord: load end " + System.currentTimeMillis());
                smartMessageAdapter.setList(chatMessages);
                smartMessageAdapter.getUpFetchModule().setUpFetchEnable(true);
                if (pageCount == 0) {
                    smartMessageAdapter.getLoadMoreModule().setEnableLoadMore(false);
                    smartMessageAdapter.getLoadMoreModule().loadMoreEnd(true);
                } else {
                    smartMessageAdapter.getLoadMoreModule().setEnableLoadMore(true);
                }
                mChatMessageList = smartMessageAdapter.getData();
                if (mChatMessageList.isEmpty()) {
                    return;
                }
                ChatMessage lastMsg = mChatMessageList.get(mChatMessageList.size() - 1);
                // 初始化滚动到底部
                if (lineNum != -1) {
                    // 是查找聊天记录 滚动到指定位置
                    Trace.d("run: 滚动到指定位置 " + lineNum);
                    mMessageRv.scrollToPosition(lineNum);
                } else {
                    // 查询上次阅读位置 是否显示消息图标
                    if (isStart) {
                        queryLastPosition();
                        MMKV.defaultMMKV().putString(conversationId + Constant.READ_POSITION, lastMsg.getOriginId());
                        MMKV.defaultMMKV().putLong(conversationId + Constant.READ_TIMESTAMP, lastMsg.getTimestamp());
                        if (isSingle) {
                            // 从后往前遍历 查询最新一条对方发送的消息是否已读
                            for (int i = mChatMessageList.size() - 1; i >= 0; i--) {
                                ChatMessage chatMessage = mChatMessageList.get(i);
                                if (!chatMessage.getIsSent()) {
                                    if (!chatMessage.isRead()) {
                                        MessageDao.getInstance().markAsRead(chatMessage);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    scrollToEnd(null, false);
                }
            }
        });
    }

    /**
     * 查询上次阅读位置 显示有多少未读消息
     */
    private void queryLastPosition() {
        lastTimestamp = MMKV.defaultMMKV().getLong(conversationId + Constant.READ_TIMESTAMP, -1);
        lastOriginId = MMKV.defaultMMKV().getString(conversationId + Constant.READ_POSITION, "");
        if (lastTimestamp > 0) {
            MessageDao.getInstance().queryUnreadMsgCount(ChatActivity.this, conversationId, lastTimestamp, new MessageDao.MessageDaoCallback() {
                @Override
                public void queryUnreadMsgCount(int unreadCount) {
                    if (layoutManager != null && unreadCount > 5) {
                        // 获取可见的范围
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                        // 计算可见的 item 数量
                        int visibleItemCount = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                        if (unreadCount > visibleItemCount) {
                            tv_unread_tips.setVisibility(View.VISIBLE);
                            tv_unread_tips.setText(getString(R.string.unread_tips, unreadCount - visibleItemCount));
                        }
                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChatEvent event) {
        switch (event.getWhat()) {
            case ChatEvent.REFRESH_CHAT_UI:
                // 清空聊天记录后
                smartMessageAdapter.setList(new ArrayList<>());
                mChatMessageList = smartMessageAdapter.getData();
                break;
            case ChatEvent.SEND_CALL_MSG: {
                // 呼叫方发起一次通话
                Bundle bundle = event.bundle;
                if (bundle != null) {
                    boolean isVoiceCall = bundle.getBoolean(Constant.MSG_TYPE_VOICE_CALL);
                    ArrayList<String> ids = bundle.getStringArrayList(Constant.CONVERSATION_IDS);
                    CallManager.CURRENT_CREATOR_INFO = CallCreatorInfo.create(myUserInfo.getUserId(), myUserInfo.getUserNickName(), AppConfig.getJitsiUrl());
                    CallManager.CURRENT_CALL_ID = bundle.getString(Constant.CALL_ID);
                    CallExtension callExtension = new CallExtension(CallManager.CURRENT_CALL_ID,
                            isVoiceCall ? Constant.MSG_TYPE_VOICE_CALL : Constant.MSG_TYPE_VIDEO_CALL,
                            TextUtils.join(",", ids),
                            CallManager.CURRENT_CREATOR_INFO, AppConfig.getJitsiUrl());
                    CallManager.getInstance().initiateCall(conversationId, conversationTitle, ids, conversationType, callExtension);
                }
                break;
            }
            case ChatEvent.END_CALL_MSG: {
                // 主动正常结束通话 更新通话时长-不要显示在UI上
                Bundle bundle = event.bundle;
                if (bundle != null) {
                    boolean isGroup = bundle.getBoolean(SmartConversationType.GROUP.name());
                    CallManager.CURRENT_CREATOR_INFO = CallCreatorInfo.create(myUserInfo.getUserId(), myUserInfo.getUserNickName(), AppConfig.getJitsiUrl());
                    CallExtension callExtension = new CallExtension(CallManager.CURRENT_CALL_ID,
                            Constant.MSG_TYPE_END_CALL,
                            myUserInfo.getUserId(),
                            CallManager.CURRENT_CREATOR_INFO, AppConfig.getJitsiUrl());
                    CallManager.getInstance().endCall(conversationId, !isGroup, callExtension);
                }
                break;
            }
            case ChatEvent.CANCEL_CALL_MSG: {
                // 主动取消通话
                Bundle bundle = event.bundle;
                if (bundle != null) {
                    boolean isGroup = bundle.getBoolean(SmartConversationType.GROUP.name());
                    CallManager.CURRENT_CREATOR_INFO = CallCreatorInfo.create(myUserInfo.getUserId(), myUserInfo.getUserNickName(),
                            AppConfig.getJitsiUrl());
                    CallExtension callExtension = new CallExtension(
                            CallManager.CURRENT_CALL_ID,
                            Constant.MSG_TYPE_CANCEL_CALL,
                            "",
                            CallManager.CURRENT_CREATOR_INFO,
                            AppConfig.getJitsiUrl());
                    CallManager.getInstance().endCall(conversationId, !isGroup, callExtension);
                }
                break;
            }
            case ChatEvent.GROUP_DESTROYED:
            case ChatEvent.KICKED_ME: {
                // 被踢出群聊
                Bundle bundle = event.bundle;
                if (bundle != null) {
                    String eventGroupId = bundle.getString(Constant.GROUP_ID);
                    String originId = bundle.getString(Constant.MESSAGE_ORIGIN_ID);
                    if (isGroup && conversationId.equals(eventGroupId)) {
                        // 是当前
                        MessageDao.getInstance().getMessageByOriginId(this, originId, new MessageDao.MessageDaoCallback() {
                            @Override
                            public void getMessageByOriginId(ChatMessage chatMessage) {
                                needScrollToEnd = true;
                                scrollToEnd(chatMessage, false);
                            }
                        });
                        ll_conversation_unavailable.setVisibility(View.VISIBLE);
                        mHelper.hookSystemBackByPanelSwitcher();
                    }
                }
                break;
            }
            case ChatEvent.FILE_DOWNLOAD_COMPLETE: {
                Bundle bundle = event.bundle;
                String eventOriginId = bundle.getString(Constant.MESSAGE_ORIGIN_ID);
                String fileLocalPath = bundle.getString(Constant.MESSAGE_FILE_LOCAL);
                refreshFileMsg(eventOriginId, fileLocalPath, "");
                break;
            }
            case ChatEvent.FILE_DOWNLOAD_PROGRESS: {
                Bundle bundle = event.bundle;
                String eventOriginId = bundle.getString(Constant.MESSAGE_ORIGIN_ID);
                int progress = bundle.getInt(Constant.DOWNLOAD_PROGRESS);
                smartMessageAdapter.refreshDownloadProgress(eventOriginId, Constant.DOWNLOAD_PROGRESS, progress);
                break;
            }
            case ChatEvent.UPLOAD_FILE_SUCCESS:
                // 继续上传下一个
                Trace.d(">>>>", "onEvent: UPLOAD_FILE_SUCCESS");
                isUploading = false;
                uploadImageFile();
                break;
        }
    }

    /**
     * 播放语音
     *
     * @param view
     * @param voiceFilePath
     * @param isSent
     */
    private void playVoice(View view, String voiceFilePath, boolean isSent) {
        if (!TextUtils.isEmpty(voiceFilePath)) {
            boolean fileExists = FileUtils.isFileExists(new File(voiceFilePath));
            if (fileExists) {
                MediaManager.getInstance(ChatActivity.this).playSound(voiceFilePath, mp -> {
                    view.setBackgroundResource(
                            isSent ? R.drawable.voice_sent_static : R.drawable.voice_receive_static);
                    MediaManager.getInstance(ChatActivity.this).release();
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mHelper != null && mHelper.hookSystemBackByPanelSwitcher()) {
            return;
        }
        // 离开之前清除一下会话的未读数
        Trace.d("onBackPressed: 离开之前清除一下会话的未读数");
        ConversationDao.getInstance().clearUnreadMsgCount(conversationId);
        MediaManager.getInstance(ChatActivity.this).release();
        super.onBackPressed();
    }

    /**
     * 加载聊天记录
     */
    private void startUpFetch() {
        Trace.d("startUpFetch: 触发了上拉加载");
        pageCount++;
        smartMessageAdapter.getUpFetchModule().setUpFetching(true);
        MessageDao.getInstance().getMessagesByConversationId(ChatActivity.this, conversationId, pageCount, new MessageDao.MessageDaoCallback() {
            @Override
            public void getMessagesByConversationId(List<ChatMessage> chatMessages) {
                smartMessageAdapter.addData(0, chatMessages);
                smartMessageAdapter.getUpFetchModule().setUpFetching(false);
                if (chatMessages.size() < MessageDao.PAGE_SIZE) {
                    Trace.d("run: 没有更多了");
                    pageCount--;
                    smartMessageAdapter.getUpFetchModule().setUpFetchEnable(false);
                }
            }
        });
    }

    /**
     * 滚动到底部
     *
     * @param chatMessage 不为空添加一条
     * @param isSmooth    是否平滑
     */
    private void scrollToEnd(ChatMessage chatMessage, boolean isSmooth) {
        // 将RecyclerView平滑滚动到最后一个位置
        if (null != chatMessage) {
            smartMessageAdapter.addData(chatMessage);
            if (needScrollToEnd) {
                if (isSmooth) {
                    mMessageRv.smoothScrollToPosition(smartMessageAdapter.getData().size() - 1);
                } else {
                    mMessageRv.scrollToPosition(smartMessageAdapter.getData().size() - 1);
                }
            } else {
                if (hasNewMsg) {

                }
            }
        } else if (!smartMessageAdapter.getData().isEmpty()) {
            if (needScrollToEnd) {
                if (isSmooth) {
                    mMessageRv.smoothScrollToPosition(smartMessageAdapter.getData().size() - 1);
                } else {
                    mMessageRv.scrollToPosition(smartMessageAdapter.getData().size() - 1);
                }
            } else {
                if (hasNewMsg) {

                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                if (CheckUtil.isFastDoubleClick()) {
                    return;
                }
                String content = mTextMsgEt.getText().toString();
                prepareTextMsg(content, SmartContentType.TEXT);
                break;
            case R.id.iv_more:
                if (SmartConversationType.SINGLE.name().equals(conversationType)) {
                    // 单聊设置
                    ChatSingleSettingActivity.start(ChatActivity.this, conversationId, conversationTitle);
                } else {
                    // 群聊设置
                    ChatGroupSettingActivity.start(ChatActivity.this, conversationId,
                            TextUtils.isEmpty(conversationTitle) ? "" : conversationTitle);
                }
                break;
            case R.id.btn_set_mode_voice:
                if (mPressToSpeakLl.getVisibility() == View.VISIBLE) {
                    // 切换为键盘模式
                    mTextMsgEt.setFocusable(true);
                    mTextMsgEt.setFocusableInTouchMode(true);
                    mTextMsgEt.requestFocus();
                    // 显示软键盘
                    showKeyboard();
                    switchVoiceBtn(false);
                } else {
                    checkPermission(REQUEST_CODE_VOICE, Permission.RECORD_AUDIO);
                }
                break;
            case R.id.ll_image_album:
                // 动态申请相册权限
                checkPermission(REQUEST_CODE_IMAGE_ALBUM, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
                break;
            case R.id.ll_image_camera:
                // 动态申请相机权限
                checkPermission(REQUEST_CODE_IMAGE_CAMERA, Permission.CAMERA, Permission.RECORD_AUDIO);
                break;
            case R.id.ll_chat_location:
                // 动态申请定位权限
                checkPermission(REQUEST_CODE_LOCATION, Permission.ACCESS_FINE_LOCATION);
                break;
            case R.id.iv_chat_video:
                XXPermissions.with(this)
                        .permission(Permission.RECORD_AUDIO)
                        .permission(Permission.CAMERA)
                        .request(new PermissionCallback() {
                            @Override
                            public void onGranted(List<String> permissions, boolean all) {
                                if (all) {
                                    if (isGroup && ll_group_call.getVisibility() == View.VISIBLE) {
                                        showJoinGroupCallDialog();
                                    } else {
                                        showSendCallDialog();
                                    }
                                }
                            }
                        });

                break;
            case R.id.tv_join:
                if (latestCallMsg == null) {
                    Trace.d("initListener: call id is null");
                    return;
                }
                // 得发一个join通知会话方我加入了
                break;
            case R.id.iv_chat_file:
                checkPermission(REQUEST_CODE_FILE, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
                break;
            case R.id.tv_unread_tips:
                // 跳转到上次阅读位置
                if (CheckUtil.isFastDoubleClick()) {
                    return;
                }
                if (!TextUtils.isEmpty(lastOriginId)) {
                    MessageDao.getInstance().queryMessagePosition(ChatActivity.this, conversationId, lastOriginId, new MessageDao.MessageDaoCallback() {
                        @Override
                        public void queryMessagePosition(Integer[] ints) {
                            int count = ints[0];
                            int position = ints[1];
                            // 页码 先判断消息在哪一页
                            int page = position / MessageDao.PAGE_SIZE;
                            // 计算行号在当前页
                            int startOfPage = page * MessageDao.PAGE_SIZE;
                            // 当前页上的消息数量
                            int messagesOnCurrentPage =
                                    Math.min(MessageDao.PAGE_SIZE, count - startOfPage);
                            // 倒序行号
                            int lineNumDesc =
                                    messagesOnCurrentPage - (position - startOfPage) - 1;
                            pageCount = page;
                            lineNum = lineNumDesc;
                            // 点击未读消息后
                            loadChatRecord(false);
                            mScrollToBottomButton.show();
                            tv_unread_tips.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                break;
            case R.id.scroll_to_bottom_button:
                needScrollToEnd = true;
                if (pageCount != 0) {
                    pageCount = 0;
                    lineNum = -1;
                    loadChatRecord(false);
                } else {
                    mScrollToBottomButton.hide();
                    scrollToEnd(null, false);
                }
                break;
            case R.id.iv_lock:
                if (OmemoHelper.getInstance().isEnableEncrypt(conversationId)) {
                    // 已开启加密 取消加密
                    ivLock.setImageResource(R.drawable.ic_lock_open_outline_24dp);
                    OmemoHelper.getInstance().enableEncrypt(conversationId, false);
                } else {
                    ivLock.setImageResource(R.drawable.ic_lock_24dp);
                    OmemoHelper.getInstance().enableEncrypt(conversationId, true);
                }
                break;
        }
    }

    /**
     * 检查权限
     *
     * @param permissionCode
     */
    private void checkPermission(int permissionCode, String... permission) {
        XXPermissions.with(this)
                // 申请单个权限
                .permission(permission)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            toast(R.string.permissions_obtained_some);
                            return;
                        }
                        switch (permissionCode) {
                            case REQUEST_CODE_IMAGE_ALBUM:
                                showAlbum();
                                break;
                            case REQUEST_CODE_IMAGE_CAMERA:
                                showCamera();
                                break;
                            case REQUEST_CODE_LOCATION:
                                showMapPicker();
                                break;
                            case REQUEST_CODE_VOICE:
                                switchVoiceBtn(true);
                                break;
                            case REQUEST_CODE_FILE:
                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("*/*"); // 或指定具体的 MIME 类型，比如 "image/*" 只显示图片文件
                                startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_file)), REQUEST_CODE_FILE);
                                break;
                        }
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            toast(R.string.permanently_denied);
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            handleRejectPermission(permissionCode);
                        } else {
                            toast(R.string.failed_permission);
                        }
                    }
                });
    }

    /**
     * 显示加入群组通话弹窗
     */
    private void showJoinGroupCallDialog() {
        new MessageDialog.Builder(this)
                // 内容必须要填写
                .setMessage(R.string.to_join_group_call)
                // 确定按钮文本
                .setConfirm(getString(R.string.join))
                // 设置 null 表示不显示取消按钮
                .setCancel(getString(R.string.common_cancel))
                // 设置点击按钮后不关闭对话框
                //.setAutoDismiss(false)
                .setListener(new MessageDialog.OnListener() {

                    @Override
                    public void onConfirm(BaseDialog dialog) {
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                    }
                })
                .show();
    }

    private void showSendCallDialog() {
        List<DialogItemBean> callItems = new ArrayList<>();
        callItems.add(new DialogItemBean(getString(R.string.voice_call)));
        callItems.add(new DialogItemBean(getString(R.string.video_call)));
        new DemoListDialogFragment.Builder(ChatActivity.this)
                .setData(callItems)
                .setCancelColorRes(R.color.black)
                .setWindowAnimations(R.style.animate_dialog)
                .setOnItemClickListener(new DemoListDialogFragment.OnDialogItemClickListener() {
                    @Override
                    public void OnItemClick(View view, int position) {
                        switch (position) {
                            case 0:
                                // voice消息
                                if (isSingle) {
                                } else {
                                }
                                break;
                            case 1:
                                // 发起一次通话
                                if (isSingle) {
                                } else {
                                }
                                break;
                        }
                    }
                })
                .show();
    }

    /**
     * xmpp的发送文本消息
     *
     * @param conversationType 会话类型----群聊
     * @param conversationId   目标id-----群聊id
     * @param messageType      消息类型
     * @param messageContent   消息体 文字消息就是文字 图片消息是json 音频消息是json
     * @param elements         扩展消息
     * @param originId         消息来源
     * @param currentIndex     当前消息列表的长度
     */
    private void sendSmackTextMsg(String conversationType, String conversationId, String messageType, String messageContent,
                                  List<IExtension> elements, String originId, int currentIndex) {
        // 获取当前消息列表的长度
        currentIndex = currentIndex == -1 ? mChatMessageList.size() - 1 : currentIndex;
        if (conversationType.equals(SmartConversationType.GROUP.name())) {
            Trace.d("sendSmackTextMsg: 发送群聊消息");
            ChatMessageManager.getInstance().sendGroupMessage(
                    conversationId,
                    messageType,
                    messageContent,
                    elements,
                    currentIndex,
                    originId);
        } else {
            Trace.d("sendSmackTextMsg: 发送单聊消息");
            ChatMessageManager.getInstance().sendSingleMessage(
                    conversationId,
                    conversationTitle,
                    messageContent,
                    messageType,
                    elements,
                    currentIndex,
                    originId);
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        EventBus.getDefault().post(new SelectTextEvent(SelectTextEvent.DISMISS_ALLPOP_DELAYED));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                mManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 显示软键盘
     */
    private void showKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
            if (getCurrentFocus() != null) {
                Trace.d("showKeyboard: -=-=-=-=-=-=");
                postDelayed(() -> scrollToEnd(null, false), 150);
                postDelayed(() -> {
                    mManager.showSoftInput(mTextMsgEt, 0);
                }, 250);
            }
        }
    }

    /**
     * 发送文字消息 需要判断是单聊还是群聊
     *
     * @param content 消息内容
     */
    private void prepareTextMsg(String content, String msgType) {
        // 去掉文字末尾的空格和换行
        content = content.replaceAll("[\\s\\n\\r]+$", "");
        ArrayList<IExtension> elements = new ArrayList<>();
        if (mAitManager != null) {
            Trace.d("prepareTextMsg: " + mAitManager.getAitContactsModel(),
                    "list > " + mAitManager.getAitContactsModel().getAtBlockList());
            //包装@消息
            List<String> pushList = mAitManager.getAitTeamMember();
            if (pushList != null && !pushList.isEmpty()) {
                AitExtension aitExtension = new AitExtension(mAitManager.getAitData().toString());
                elements.add(aitExtension);
            }
        }
        ChatMessage chatMessage = ChatMessage.createTextMsg(conversationId,
                conversationType,
                msgType,
                content);
        MessageDao.getInstance().saveAndSetLastTimeStamp(chatMessage, isSuccess -> {
                    needScrollToEnd = true;
                    scrollToEnd(chatMessage, true);
                    sendSmackTextMsg(conversationType,
                            conversationId,
                            chatMessage.getMessageType(),
                            chatMessage.getMessageContent(),
                            elements,
                            chatMessage.getOriginId(),
                            -1);
                    mTextMsgEt.setText("");
                }
        );
    }

    /**
     * 预处理文件消息
     *
     * @param localPath 本地路径
     * @param msgType
     * @param fileBean
     * @return
     */
    private String addFileMsg(String localPath, String msgType, ChatFileBean fileBean) {
        ChatMessage chatMessage = ChatMessageManager.getInstance().prepareFileMsg(conversationType, conversationId,
                msgType, localPath, fileBean);
        // 之前的最后一条
        smartMessageAdapter.addData(chatMessage);
        // 隐藏底部功能栏
        mHelper.hookSystemBackByPanelSwitcher();
        // 发送图片消息 延迟滚动到底部
        needScrollToEnd = true;
        postDelayed(() -> scrollToEnd(null, true), 250);
        return chatMessage.getOriginId();
    }

    /**
     * 发送位置消息
     *
     * @param latitude      纬度
     * @param longitude     经度
     * @param address       地址
     * @param addressDetail 详细地址
     * @param path          地图截图http地址
     */
    private void sendLocationMsg(double latitude, double longitude, String address, String addressDetail, String path) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatMessageManager.getInstance().removeMsgCallback(this);
        ChatRoomListener.getInstance().removeListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void startJoinGroup(String groupId) {
        if (!TextUtils.isEmpty(groupId)) {
            if (groupId.equals(conversationId)) {
                showDialog();
            }
        }
    }

    @Override
    public void joinRoomSuccess(String groupId) {
        if (!TextUtils.isEmpty(groupId)) {
            if (groupId.equals(conversationId)) {
                hideDialog();
            }
        }
    }

    @Override
    public void joinRoomFailed(int code, String groupId, String desc) {
        if (!TextUtils.isEmpty(groupId)) {
            if (groupId.equals(conversationId)) {
                hideDialog();
            }
        }
    }

    @Override
    public void voiceRevoked(String groupId, String memberAccount) {
        post(() -> {
            mHelper.hookSystemBackByPanelSwitcher();
            ll_conversation_unavailable.setVisibility(View.VISIBLE);
        });

    }

    @Override
    public void voiceGranted(String groupId, String memberAccount) {
        post(() -> ll_conversation_unavailable.setVisibility(View.GONE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOnPause) {
            isOnPause = false;
            if (isFromSearchRecord) {
                return;
            }
            // 需要判断是否是搜索跳转记录 如果是 那么不加载最新
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 清除一下当前会话的未读数
                    long timestamp = 0;
                    if (null != mChatMessageList && !mChatMessageList.isEmpty()) {
                        // 获取最后一条消息的时间戳
                        timestamp = mChatMessageList.get(mChatMessageList.size() - 1)
                                .getTimestamp();
                    }
                    Trace.d(">>>>>onResume: 查看是否有新消息>>>>>",
                            smartMessageAdapter.getData().size(),
                            timestamp);
                    MessageDao.getInstance()
                            .getMessagesAfterTime(ChatActivity.this, conversationId,
                                    timestamp, new MessageDao.MessageDaoCallback() {
                                        @Override
                                        public void getMessagesAfterTime(List<ChatMessage> chatMessages) {
                                            if (!chatMessages.isEmpty()) {
                                                hasNewMsg = true;
                                                smartMessageAdapter.addData(chatMessages);
                                                // 判断一下如果之前滚动到了底部,应该继续滚动到底部
                                                if (needScrollToEnd) {
                                                    scrollToEnd(null, false);
                                                }
                                            }
                                        }
                                    });
                    if (isGroup) {
                        // 获取最新的一条通话消息
                        MessageDao.getInstance().getLatestCallMsgById(ChatActivity.this, conversationId, new MessageDao.MessageDaoCallback() {
                            @Override
                            public void getLatestCallMsgById(ChatMessage chatMessage) {
                                latestCallMsg = chatMessage;
                                if (latestCallMsg != null && ChatMessage.isCallMsgType(latestCallMsg.getMessageType())) {
                                    // 判断latestCallMsg的时间戳timestamp距离现在小于1个小时 60 * 60 * 1000
                                    if (System.currentTimeMillis() - latestCallMsg.getTimestamp() < 60 * 60 * 1000) {
                                        // 判断latestCallMsg是否是通话结束
                                        // 如果不是通话结束
                                        if (!ChatMessage.isCloseCallType(latestCallMsg.getMessageType())) {
                                            //  只能用中文消息内容去判断 不够准确
                                            ll_group_call.setVisibility(View.VISIBLE);
                                            tv_group_call_tips.setText(getString(R.string.group_calling));
                                        } else {
                                            ll_group_call.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }, 300);
        }
        // 清除通知
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        // 假设senderUserId是发送者的userid
        notificationManager.cancel(conversationId.hashCode());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnPause = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 如果消息列表不为空 并且不是打开的搜索
        if (!mChatMessageList.isEmpty() && !isFromSearchRecord) {
            Trace.d("onStop: 记录阅读位置");
            ChatMessage chatMessage = mChatMessageList.get(mChatMessageList.size() - 1);
            MMKV.defaultMMKV().putString(conversationId + Constant.READ_POSITION, chatMessage.getOriginId());
            MMKV.defaultMMKV().putLong(conversationId + Constant.READ_TIMESTAMP, chatMessage.getTimestamp());
        }
        if (isGroup) {
            // 清除@消息
            ConversationHelper.updateAitInfo(conversationId, false);
        }
        EventBus.getDefault().post(new SelectTextEvent(SelectTextEvent.DISMISS_ALLPOP_DELAYED));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (null == data) {
                return;
            }
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            switch (requestCode) {
                case REQUEST_CODE_LOCATION:
                    // 获取经纬度，发送位置消息
                    double latitude = data.getDoubleExtra("latitude", 0);
                    double longitude = data.getDoubleExtra("longitude", 0);
                    String address = data.getStringExtra("address");
                    String addressDetail = data.getStringExtra("addressDetail");
                    String path = data.getStringExtra("path");
                    sendLocationMsg(latitude, longitude, address, addressDetail, path);
                    break;
                case REQUEST_CODE_FILE:
                    ChatFileBean fileMsg = ChatMessageManager.getInstance().createFileMsg(ChatActivity.this,
                            uri, conversationId, conversationTitle, conversationType);
                    String originId = addFileMsg(uri.toString(), SmartContentType.FILE, fileMsg);
                    fileMsg.setOriginId(originId);
                    Intent intent = new Intent(this, UploadService.class);
                    intent.putExtra(Constant.FILE_BEAN, fileMsg);
                    intent.putExtra(Constant.MESSAGE_FILE_LOCAL, fileMsg.fileLocalPath);
                    intent.putExtra(Constant.FILE_TYPE, SmartContentType.FILE);
                    startService(intent);
                    break;
            }
        }
    }

    /**
     * 发送图片
     */
    private void compressAndSendImg() {
        if (mFileSendQueue.isEmpty()) {
            return;
        }
        String filePath = mFileSendQueue.removeFirst();
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        // 检查文件名是否以 ".gif" 结尾 或者是原图
        if (filePath.toLowerCase().endsWith(".gif") || isOriginalImg) {
            String absolutePath = new File(filePath).getAbsolutePath();
            mFileBeanSendQueue.add(prepareImageFileMsg(absolutePath, ""));
            uploadImageFile();
            compressAndSendImg();
        } else {
            Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
            options.quality = 85;
            Tiny.getInstance()
                    .source(filePath)
                    .asFile()
                    .withOptions(options)
                    .compress(new FileCallback() {
                        @Override
                        public void callback(boolean isSuccess, String outfile, Throwable t) {
                            Trace.d("callback: 输出文件路径>>>" + outfile);
                            if (!isSuccess) {
                                // 压缩失败，发送原图
                                mFileBeanSendQueue.add(prepareImageFileMsg(filePath, ""));
                                uploadImageFile();
                                compressAndSendImg();
                                CrashReport.postCatchedException(t);  // bugly会将这个throwable上报
                            } else {
                                mFileBeanSendQueue.add(prepareImageFileMsg(outfile, ""));
                                uploadImageFile();
                                compressAndSendImg();
                            }
                        }
                    });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            EventBus.getDefault().post(new SelectTextEvent(SelectTextEvent.DISMISS_ALLPOP_DELAYED));
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 发送v
     *
     * @param filePath
     */
    private void compressAndSendVideo(String filePath) {
        ChatVideoBean chatVideoBean = FileUtil.buildVideoMessage(filePath);
        if (chatVideoBean != null) {
            chatVideoBean.conversationId = conversationId;
            chatVideoBean.contactTitle = isSingle ? conversationTitle : conversationId;
            chatVideoBean.conversationType = conversationType;
            final String originId = addFileMsg(filePath, SmartContentType.VIDEO, chatVideoBean);
            chatVideoBean.setOriginId(originId);
            // 视频用service上传
            Intent intent = new Intent(this, UploadService.class);
            intent.putExtra(Constant.FILE_BEAN, chatVideoBean);
            intent.putExtra(Constant.FILE_TYPE, Constant.FILE_TYPE_VIDEO);
            startService(intent);
        }
    }

    /**
     * 上传文件
     *
     * @param outfilePath
     * @param msgType
     * @param originId
     */
    private ChatImageBean prepareImageFileMsg(String outfilePath, String originId) {
        ChatImageBean imageFileBean = ChatMessageManager.getInstance().createImageFileBean(conversationId, conversationTitle, conversationType, outfilePath);
        if (TextUtils.isEmpty(originId)) {
            imageFileBean.originId = addFileMsg(outfilePath, SmartContentType.IMAGE, imageFileBean);
        } else {
            // 重发消息的情况 已经有originId
            imageFileBean.originId = originId;
        }
        return imageFileBean;
    }

    private void uploadImageFile() {
        if(isUploading || mFileBeanSendQueue.isEmpty()) {
            return;
        }
        isUploading = true;
        ChatFileBean chatFileBean = mFileBeanSendQueue.removeFirst();
        Intent intent = new Intent(ChatActivity.this, UploadService.class);
        intent.putExtra(Constant.FILE_BEAN, chatFileBean);
        intent.putExtra(Constant.MESSAGE_FILE_LOCAL, chatFileBean.fileLocalPath);
        intent.putExtra(Constant.FILE_TYPE, SmartContentType.IMAGE);
        startService(intent);
    }

    public void handleRejectPermission(int requestCode) {
        String content = "";
        // 非初次进入App且已授权
        switch (requestCode) {
            case REQUEST_CODE_LOCATION:
                content = getString(R.string.request_permission_location);
                break;
            case REQUEST_CODE_FILE:
            case REQUEST_CODE_IMAGE_ALBUM:
                content = getString(R.string.request_permission_storage);
                break;
            case REQUEST_CODE_IMAGE_CAMERA:
                content = getString(R.string.request_permission_camera);
                break;
            case REQUEST_CODE_VOICE:
                content = getString(R.string.request_permission_record_audio);
                break;
        }

        final ConfirmDialog mConfirmDialog = new ConfirmDialog(ChatActivity.this,
                getString(R.string.request_permission), content, getString(R.string.go_setting),
                getString(R.string.cancel), getColor(R.color.navy_blue));
        mConfirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
            @Override
            public void onOkClick() {
                mConfirmDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",
                        AppConfig.getPackageName(), null);
                intent.setData(uri);
                ChatActivity.this.startActivity(intent);
            }

            @Override
            public void onCancelClick() {
                mConfirmDialog.dismiss();
            }
        });
        // 点击空白处消失
        mConfirmDialog.setCancelable(false);
        mConfirmDialog.show();
    }

    /**
     * 进入地图选择页面
     */
    private void showMapPicker() {
    }

    /**
     * 跳转到相机
     */
    private void showCamera() {
        startActivityForResult(RecordVideoActivity.class, new OnActivityCallback() {
            @Override
            public void onActivityResult(int resultCode, @Nullable Intent data) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        String filePath = data.getStringExtra(Constant.MESSAGE_FILE_LOCAL);
                        String fileType = data.getStringExtra(Constant.FILE_TYPE);
                        Trace.d("onActivityResult: filePath>>>>" + filePath,
                                "fileType>>>" + fileType);
                        if (!TextUtils.isEmpty(filePath)) {
                            if (Constant.FILE_TYPE_IMAGE.equals(fileType)) {
                                // 仅拍照 发送图片
                                mFileSendQueue.add(filePath);
                                compressAndSendImg();
                            } else if (Constant.FILE_TYPE_VIDEO.equals(fileType)) {
                                // 发送视频消息
                                compressAndSendVideo(filePath);
                            }
                        }

                    }
                }
            }
        });
    }

    /**
     * 跳转到相册
     */
    private void showAlbum() {
        // 默认不发送原图
        isOriginalImg = false;
        ImageSelectActivity.start(ChatActivity.this, 9, true, new ImageSelectActivity.OnPhotoSelectListener() {
            @Override
            public void onSelected(List<String> data) {

            }

            @Override
            public void onSelected(List<String> data, boolean isOriginal) {
                // 添加至文件发送队列
                isOriginalImg = isOriginal;
                mFileSendQueue.addAll(data);
                compressAndSendImg();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Trace.d("onNewIntent: >>>> ");
        // 将新 Intent 设置为当前 Activity 的 Intent
        setIntent(intent);
        // 处理传递给该 Activity 的新 Intent
        if (intent.hasExtra(Constant.RECEIVE_NEW_MSG)) {
            SmartMessage msg = intent.getParcelableExtra(Constant.RECEIVE_NEW_MSG);
            // 刷新聊天记录
            if (msg != null) {
                // 如果新 Intent 中包含新消息的信息
                // 如果不是当前会话
                if (!isCurrentConversation(msg)) {
                    initOtherData();
                }
            }
        } else {
            // 不是通知过来的
            initOtherData();
        }

    }

    /**
     * 是否是当前会话
     *
     * @param msg
     * @return
     */
    private boolean isCurrentConversation(SmartMessage msg) {
        if (isSingle) {
            boolean sendToMe = conversationId.equals(msg.getFromUserId()) && myUserInfo.getUserId()
                    .equals(msg.getToUserId());
            boolean sendToThem = conversationId.equals(msg.getToUserId()) && myUserInfo.getUserId()
                    .equals(msg.getFromUserId());
            return sendToMe || sendToThem;
        } else if (!TextUtils.isEmpty(conversationId)) {
            return conversationId.equals(msg.getGroupId());
        }
        return false;
    }


    /**
     * 发送消息成功
     *
     * @param originId 用在通话消息
     * @param smartMsg
     */
    @Override
    public void sendSingleMsgSuccess(String originId, SmartMessage smartMsg, ChatMessage dbMsg, int messageIndex) {
        // 消息发送成功
        Trace.d("sendSingleMsgSuccess messageIndex>>> " + messageIndex,
                "Content " + smartMsg.getMessageContent(),
                "msgType " + smartMsg.getMessageType(),
                "callMessageIndex " + callMessageIndex,
                "originId " + originId);
        if (!isCurrentConversation(smartMsg)) {
            return;
        }
        if (-1 == messageIndex) {
            // 通话消息
            if (ChatMessage.isCallMsgType(smartMsg.getMessageType())) {
                if (smartMsg.isSingle()) {
                    // 只更新UI
                    if (callMessageIndex != -1) {
                        ChatMessage chatMessage = mChatMessageList.get(callMessageIndex);
                        chatMessage.setMessageType(dbMsg.getMessageType());
                        chatMessage.setMessageContent(dbMsg.getMessageContent());
                        chatMessage.setStatus(MessageStatus.SEND_SUCCESS.value());
                        smartMessageAdapter.notifyItemChanged(callMessageIndex);
                        Trace.d("sendSingleMsgSuccess: 更新通话消息内容 " + dbMsg.getMessageContent());
                    } else {
                        // 新增一条通话消息
                        Trace.d("sendSingleMsgSuccess: 页面新增一条通话消息 记录callMessageIndex");
                        scrollToEnd(dbMsg, false);
                        callMessageIndex = mChatMessageList.size() - 1;
                    }
                    if (ChatMessage.isCloseCallType(smartMsg.getMessageType())) {
                        // 本次通话结束
                        Trace.d("sendSingleMsgSuccess: 单聊本次通话结束");
                        callMessageIndex = -1;
                        scrollToEnd(null, false);
                    }
                }
            } else {
                // 先匹配当前聊天消息
                if (ChatMessage.isFileType(smartMsg.getMessageType())) {
                    refreshFileMsg(originId, "", smartMsg.getMessageContent());
//                    compressAndSendImg();
                } else {
                    // 转发到本聊天的情况
                    dbMsg.setStatus(MessageStatus.SEND_SUCCESS.value());
                    smartMessageAdapter.addData(dbMsg);
                    needScrollToEnd = true;
                    scrollToEnd(null, true);
                }
            }
            return;
        }
        ChatMessage chatMessage = mChatMessageList.get(messageIndex);
        chatMessage.setStatus(MessageStatus.SEND_SUCCESS.value());
        chatMessage.setMessageContent(smartMsg.getMessageContent());
        smartMessageAdapter.notifyItemChanged(messageIndex);
        if (ChatMessage.isFileType(smartMsg.getMessageType())) {
//            compressAndSendImg();
        }
    }


    @Override
    public void sendSingleMsgFailed(String originId, int code, String desc, int messageIndex) {
        if (-1 == messageIndex) {
            return;
        }
        // 发送消息失败的处理
        ChatMessage chatMessage = mChatMessageList.get(messageIndex);
        chatMessage.setStatus(MessageStatus.SEND_FAIL.value());
        smartMessageAdapter.notifyItemChanged(messageIndex);
        toast(desc);
    }

    @Override
    public void sendGroupMsgSuccess(String originId, SmartMessage simMessage, ChatMessage dbMsg, int messageIndex) {
        // 消息发送成功
        Trace.d("群消息发送成功index>>>" + messageIndex,
                ">>>>Content" + simMessage.getMessageContent(),
                simMessage.getMessageType() + ">>>>",
                "callMessageIndex>>>" + callMessageIndex,
                "originId>>>>" + originId);
        if (!isCurrentConversation(simMessage)) {
            return;
        }
        if (-1 == messageIndex) {
            if (ChatMessage.isCallMsgType(simMessage.getMessageType())) {
                if (callMessageIndex != -1) {
                    ChatMessage chatMessage = mChatMessageList.get(callMessageIndex);
                    chatMessage.setMessageType(dbMsg.getMessageType());
                    chatMessage.setMessageContent(dbMsg.getMessageContent());
                    chatMessage.setStatus(MessageStatus.SEND_SUCCESS.value());
                    smartMessageAdapter.notifyItemChanged(callMessageIndex);
                } else {
                    // 新增一条通话消息
                    Trace.d("getMessageByOriginId: 新增一条通话消息");
                    scrollToEnd(dbMsg, false);
                    callMessageIndex = mChatMessageList.size() - 1;
                }
                if (ChatMessage.isCloseCallType(simMessage.getMessageType())) {
                    // 本次通话结束
                    Trace.d("sendGroupMsgSuccess: 群聊本次通话结束");
                }
            } else {
                // 先匹配当前聊天消息
                if (ChatMessage.isFileType(simMessage.getMessageType())) {
                    refreshFileMsg(originId, "", simMessage.getMessageContent());
//                    compressAndSendImg();
                } else {
                    // 转发到本聊天的情况
                    dbMsg.setStatus(MessageStatus.SEND_SUCCESS.value());
                    smartMessageAdapter.addData(dbMsg);
                    needScrollToEnd = true;
                    scrollToEnd(null, true);
                }
            }
            return;
        }
        ChatMessage chatMessage = mChatMessageList.get(messageIndex);
        chatMessage.setStatus(MessageStatus.SEND_SUCCESS.value());
        chatMessage.setMessageContent(simMessage.getMessageContent());
        smartMessageAdapter.notifyItemChanged(messageIndex);
        if (ChatMessage.isFileType(simMessage.getMessageType())) {
//            compressAndSendImg();
        }
    }

    /**
     * 根据originId刷新消息 发送文件后 下载文件后
     *
     * @param originId
     */
    private void refreshFileMsg(String originId, String localPath, String messageContent) {
        List<ChatMessage> data = smartMessageAdapter.getData();
        for (int i = 0; i < data.size(); i++) {
            ChatMessage cMsg = data.get(i);
            if (originId.equals(cMsg.getOriginId())) {
                cMsg.setStatus(MessageStatus.SEND_SUCCESS.value());
                if (!TextUtils.isEmpty(localPath)) {
                    cMsg.setFileLocalPath(localPath);
                }
                if (!TextUtils.isEmpty(messageContent)) {
                    cMsg.setMessageContent(messageContent);
                }
                smartMessageAdapter.notifyItemChanged(i);
            }
        }
    }

    @Override
    public void sendGroupMsgFailed(String originId, int code, String desc, int messageIndex) {
        if (-1 == messageIndex) {
            MessageDao.getInstance().getMessageByOriginId(ChatActivity.this, originId, new MessageDao.MessageDaoCallback() {
                @Override
                public void getMessageByOriginId(ChatMessage dbMsg) {
                    if (conversationId.equals(dbMsg.getConversationId())) {
                        List<ChatMessage> data = smartMessageAdapter.getData();
                        for (int i = 0; i < data.size(); i++) {
                            ChatMessage cMsg = data.get(i);
                            if (originId.equals(cMsg.getOriginId())) {
                                cMsg.setStatus(MessageStatus.SEND_FAIL.value());
                                smartMessageAdapter.notifyItemChanged(i);
                            }
                        }
                    }
                }
            });
            toast(desc);
            return;
        }
        // 发送消息失败的处理
        ChatMessage chatMessage = mChatMessageList.get(messageIndex);
        chatMessage.setStatus(MessageStatus.SEND_FAIL.value());
        smartMessageAdapter.notifyItemChanged(messageIndex);
        toast(desc);
    }

    /**
     * 子线程收到消息
     *
     * @param msgEntity
     * @param chatMessage
     */
    @Override
    public void receivedMsg(SmartMessage msgEntity, ChatMessage chatMessage) {
        // 发送者 群聊的时候可能为空
        String fromFullUserId = msgEntity.getFromUserId();
        boolean isCurrentConversation = isCurrentConversation(msgEntity);
        // 不在当前会话 或者应用不在前台需要弹出通知 或者没有开启免打扰
        if (!isCurrentConversation || !ActivityManager.getInstance().isForeground()) {
            if (!msgEntity.isHistoryMsg()) {
                Trace.d("receivedMsg: 不在当前会话 发出通知");
                ChatMessageManager.getInstance().createMsgNotification(msgEntity);
            }
            return;
        }
        if (pageCount != 0) {
            return;
        }
        if (msgEntity.isHistoryMsg()) {
            // 拉取的历史消息
            post(() -> {
                smartMessageAdapter.addData(0, chatMessage);
            });
            return;
        }
        hasNewMsg = true;
        if (msgEntity.isSingle()) {
            // 标记为已读
            MessageDao.getInstance().markAsRead(chatMessage);
            // 更新当前聊天的未读数
            post(new Runnable() {
                @Override
                public void run() {
                    Trace.d("新消息 " + chatMessage.getMessageContent());
                    // 通话消息 对方拒绝-取消-结束需要更新UI
                    if (ChatMessage.isCallMsgType(msgEntity.getMessageType())) {
                        Trace.d("callMessageIndex" + callMessageIndex,
                                "通话消息 对方拒绝-取消-结束需要更新UI " + msgEntity.getMessageContent(),
                                "calltype " + msgEntity.getMessageType());
                        if (callMessageIndex != -1) {
                            ChatMessage callMsg = mChatMessageList.get(callMessageIndex);
                            callMsg.setMessageType(msgEntity.getMessageType());
                            // 单聊收到 更新通话消息
                            callMsg.setMessageContent(chatMessage.getMessageContent());
                            smartMessageAdapter.notifyItemChanged(callMessageIndex);
                        } else {
                            Trace.d("页面新增一条通话消息 记录callMessageIndex");
                            scrollToEnd(chatMessage, false);
                            // 接到一次通话消息 记录通话消息index
                            CallManager.CURRENT_CALL_ID = chatMessage.getCallId();
                            callMessageIndex = mChatMessageList.size() - 1;
                        }
                        if (ChatMessage.isCloseCallType(msgEntity.getMessageType())) {
                            // 本次通话结束
                            Trace.d("run: 本次通话结束更新通话消息");
                            if (callMessageIndex != -1) {
                                ChatMessage callMsg = mChatMessageList.get(callMessageIndex);
                                callMsg.setMessageContent(chatMessage.getMessageContent());
                                smartMessageAdapter.notifyItemChanged(callMessageIndex);
                            }
                            callMessageIndex = -1;

                        }
                    } else {
                        scrollToEnd(chatMessage, false);
                    }
                }
            });
        } else {
            // 群聊
            // 如果是当前会话
            if (conversationId.equals(msgEntity.getGroupId())) {
                if (fromFullUserId.equals(myUserInfo.getUserId())) {
                    Trace.d("receivedMsg: 群聊 自己发送的消息");
                    return;
                }
                mMessageRv.post(new Runnable() {
                    @Override
                    public void run() {
                        Trace.d(smartMessageAdapter.getData()
                                .size() + ">>>>run: 滚动到底部>>>>" + chatMessage.getMessageContent());
                        if (ChatMessage.isCallMsgType(msgEntity.getMessageType())) {
                            Trace.d(">>callMessageIndex>>" + callMessageIndex,
                                    "  onSuccess: 通话消息 对方拒绝-取消-结束需要更新UI>>>>" + msgEntity.getMessageContent(),
                                    "call type>>>>" + msgEntity.getMessageType());
                            latestCallMsg = chatMessage;
                            ll_group_call.setVisibility(View.VISIBLE);
                            tv_group_call_tips.setText(getString(R.string.group_calling));
                            if (callMessageIndex != -1) {
                                ChatMessage callMsg = mChatMessageList.get(callMessageIndex);
                                callMsg.setMessageType(msgEntity.getMessageType());
                                // 群聊收到 更新通话消息
                                callMsg.setMessageContent(chatMessage.getMessageContent());
                                smartMessageAdapter.notifyItemChanged(callMessageIndex);
                            } else {
                                Trace.d("run: 接到一次通话消息 记录通话消息index");
                                scrollToEnd(chatMessage, false);
                                // 接到一次通话消息 记录通话消息index
                                CallManager.CURRENT_CALL_ID = chatMessage.getCallId();
                                callMessageIndex = mChatMessageList.size() - 1;
                            }
                            if (chatMessage.isEndCallMsg()) {
                                // 本次通话结束
                                Trace.d("run: 群聊本次通话结束");
                                ll_group_call.setVisibility(View.GONE);
                                latestCallMsg = null;
                                callMessageIndex = -1;
                            }
                        } else {
                            scrollToEnd(chatMessage, false);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onLoadMore() {
        if (isLoadingMore) {
            return;
        }
        // 向下拉取消息
        pageCount--;
        Trace.d("onLoadMore: 加载第 " + pageCount + " 页");
        if (pageCount < 0) {
            return;
        }
        isLoadingMore = true;
        MessageDao.getInstance().getMessagesByConversationIdLoadMore(ChatActivity.this, conversationId, pageCount, new MessageDao.MessageDaoCallback() {
            @Override
            public void getMessagesByConversationId(List<ChatMessage> chatMessages) {
                for (ChatMessage chatMessage : chatMessages) {
                    smartMessageAdapter.addData(chatMessage);
                }
                if (chatMessages.size() < MessageDao.PAGE_SIZE || pageCount == 0) {
                    Trace.d("run: 没有更多了");
                    //如果不够一页,显示没有更多数据布局
                    smartMessageAdapter.getLoadMoreModule().loadMoreEnd(true);
                    smartMessageAdapter.getLoadMoreModule().setEnableLoadMore(false);
                } else {
                    Trace.d("run: 还有下一页");
                    smartMessageAdapter.getLoadMoreModule().setAutoLoadMore(true);
                    smartMessageAdapter.getLoadMoreModule().setEnableLoadMore(true);
                    smartMessageAdapter.getLoadMoreModule().loadMoreComplete();
                }
                isLoadingMore = false;
            }
        });
    }
}