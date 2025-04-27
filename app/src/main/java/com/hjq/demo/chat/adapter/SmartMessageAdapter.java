package com.hjq.demo.chat.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.module.UpFetchModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.chat.activity.SelectConversationActivity;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.entity.CardInfoBean;
import com.hjq.demo.chat.entity.ChatFileBean;
import com.hjq.demo.chat.entity.ChatImageBean;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ChatVideoBean;
import com.hjq.demo.chat.entity.enums.MessageStatus;
import com.hjq.demo.chat.listener.CustomClickableSpan;
import com.hjq.demo.chat.entity.ChatVoiceBean;
import com.hjq.demo.chat.manager.ChatManager;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.utils.BitmapLoaderUtil;
import com.hjq.demo.chat.utils.FileUtil;
import com.hjq.demo.chat.utils.PreferencesUtil;
import com.hjq.demo.chat.utils.TimestampUtil;
import com.hjq.demo.chat.widget.quote.QuoteHelper;
import com.hjq.demo.chat.widget.selecttext.CustomPop;
import com.hjq.demo.chat.widget.selecttext.SelectTextDialog;
import com.hjq.demo.chat.widget.selecttext.SelectTextEvent;
import com.hjq.demo.chat.widget.selecttext.SelectTextEventBus;
import com.hjq.demo.chat.widget.selecttext.SelectTextHelper;
import com.hjq.demo.http.glide.GlideApp;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.ui.activity.ImagePreviewMsgActivity;
import com.hjq.demo.ui.dialog.MessageDialog;
import com.hjq.demo.utils.CheckUtil;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.OpenFileUtils;
import com.hjq.demo.utils.Trace;
import com.hjq.toast.ToastUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.utils.XmppUri;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @author r
 * @date 2024/5/29
 * @description Brief description of the file content.
 */
public class SmartMessageAdapter extends BaseMultiItemQuickAdapter<ChatMessage, BaseViewHolder> implements UpFetchModule, LoadMoreModule {
    public static final int SENT_TEXT = 10;
    public static final int RECEIVED_TEXT = 11;
    public static final int SENT_IMAGE = 12;
    public static final int RECEIVED_IMAGE = 13;
    public static final int SENT_VOICE = 14;
    public static final int RECEIVED_VOICE = 15;
    public static final int SENT_LOCATION = 16;
    public static final int RECEIVED_LOCATION = 17;
    public static final int SYSTEM_MESSAGE = 1;
    public static final int SENT_SINGLE_VIDEO_CALL = 18;
    public static final int RECEIVED_SINGLE_VIDEO_CALL = 19;
    public static final int GROUP_VIDEO_CALL = 20;
    public static final int GROUP_VOICE_CALL = 21;
    public static final int SENT_SINGLE_VOICE_CALL = 22;
    public static final int RECEIVED_SINGLE_VOICE_CALL = 23;
    public static final int SENT_VIDEO = 24;
    public static final int RECEIVED_VIDEO = 25;
    public static final int SENT_FILE = 26;
    public static final int RECEIVED_FILE = 27;
    public static final int SENT_CARD_INFO = 28;
    public static final int RECEIVED_CARD_INFO = 29;
    public static final int RECEIVED_SPANNABLE_TEXT = 2;
    public static final int SENT_QUOTE_IMAGE = 30;
    public static final int RECEIVED_QUOTE_IMAGE = 31;
    private EditText mTextMsgEt;

    public SmartMessageAdapter(List<ChatMessage> data) {
        super(data);
        addItemType(SmartMessageAdapter.SYSTEM_MESSAGE, R.layout.item_system_msg);
        addItemType(SmartMessageAdapter.SENT_TEXT, R.layout.item_sent_text);
        addItemType(SmartMessageAdapter.RECEIVED_TEXT, R.layout.item_received_text);
        addItemType(SmartMessageAdapter.SENT_IMAGE, R.layout.item_sent_image);
        addItemType(SmartMessageAdapter.RECEIVED_IMAGE, R.layout.item_received_image);
        addItemType(SmartMessageAdapter.SENT_QUOTE_IMAGE, R.layout.item_sent_quote_image);
        addItemType(SmartMessageAdapter.RECEIVED_QUOTE_IMAGE, R.layout.item_received_quote_image);
        addItemType(SmartMessageAdapter.SENT_FILE, R.layout.item_sent_file);
        addItemType(SmartMessageAdapter.RECEIVED_FILE, R.layout.item_received_file);
        addItemType(SmartMessageAdapter.SENT_VOICE, R.layout.item_sent_voice);
        addItemType(SmartMessageAdapter.RECEIVED_VOICE, R.layout.item_received_voice);
        addItemType(SmartMessageAdapter.SENT_LOCATION, R.layout.item_sent_location);
        addItemType(SmartMessageAdapter.GROUP_VIDEO_CALL, R.layout.item_system_msg);
        addItemType(SmartMessageAdapter.GROUP_VOICE_CALL, R.layout.item_system_msg);
        addItemType(SmartMessageAdapter.SENT_SINGLE_VOICE_CALL, R.layout.item_sent_call);
        addItemType(SmartMessageAdapter.SENT_SINGLE_VIDEO_CALL, R.layout.item_sent_call);
        addItemType(SmartMessageAdapter.RECEIVED_SINGLE_VOICE_CALL, R.layout.item_received_call);
        addItemType(SmartMessageAdapter.RECEIVED_SINGLE_VIDEO_CALL, R.layout.item_received_call);
        addItemType(SmartMessageAdapter.SENT_VIDEO, R.layout.item_sent_video);
        addItemType(SmartMessageAdapter.RECEIVED_VIDEO, R.layout.item_received_video);
        addItemType(SmartMessageAdapter.RECEIVED_SPANNABLE_TEXT, R.layout.item_received_text);
        addItemType(SmartMessageAdapter.SENT_CARD_INFO, R.layout.item_sent_card_info);
        addItemType(SmartMessageAdapter.RECEIVED_CARD_INFO, R.layout.item_receive_card_info);

        addChildClickViewIds(R.id.iv_msg_status,
                R.id.cv_voice_content, R.id.iv_video_thumbnail,
                R.id.cv_file_content, R.id.sdv_image_content, R.id.sdv_avatar, R.id.cv_card_info);
        addChildLongClickViewIds(R.id.sdv_avatar);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ChatMessage chatMessage) {
        boolean canShowTime = !chatMessage.needShowTime();
        switch (helper.getItemViewType()) {
            case GROUP_VIDEO_CALL:
            case GROUP_VOICE_CALL:
            case SYSTEM_MESSAGE:
                helper.setText(R.id.tv_system_message, chatMessage.getMessageContent());
                break;
            case SENT_SINGLE_VOICE_CALL:
            case SENT_SINGLE_VIDEO_CALL: {
                helper.setGone(R.id.pb_sending, chatMessage.getStatus() != MessageStatus.SENDING.value())
                        .setGone(R.id.iv_msg_status, chatMessage.getStatus() != MessageStatus.SEND_FAIL.value())
                        .setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                        .setText(R.id.tv_chat_content, chatMessage.getMessageContent())
                        .setGone(R.id.tv_timestamp, canShowTime);
                TextView sentTv = helper.getView(R.id.tv_chat_content);
                // 获取drawable资源
                Drawable drawable = getContext().getResources().getDrawable(R.drawable.icon_sent_video_call);
                if (helper.getItemViewType() == SENT_SINGLE_VIDEO_CALL) {
                    drawable = getContext().getResources().getDrawable(R.drawable.icon_sent_video_call);
                } else if (helper.getItemViewType() == SENT_SINGLE_VOICE_CALL) {
                    drawable = getContext().getResources().getDrawable(R.drawable.icon_voice_call);
                }
                // 设置drawable的位置和大小，使用setBounds方法
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                // 将drawable设置到TextView的右边
                sentTv.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
            }
            break;
            case SENT_TEXT:
                TextView sentTv = helper.getView(R.id.tv_chat_content);
//                checkContentType(sentTv, chatMessage.getMessageContent());
                helper.setGone(R.id.pb_sending, chatMessage.getStatus() != MessageStatus.SENDING.value())
                        .setGone(R.id.iv_msg_status, chatMessage.getStatus() != MessageStatus.SEND_FAIL.value())
                        .setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                        .setText(R.id.tv_chat_content, chatMessage.getMessageContent())
                        .setGone(R.id.tv_timestamp, canShowTime);
                TextView tvChatContent = helper.getView(R.id.tv_chat_content);
                if (tvChatContent.getTag() == null) {
                    SelectTextHolder selectTextHolder = new SelectTextHolder(chatMessage, sentTv,
                            tvChatContent, helper.getLayoutPosition());
                    selectTextHolder.selectText();
                    tvChatContent.setTag(selectTextHolder);
                } else {
                    SelectTextHolder tagSelectTextHolder = (SelectTextHolder) tvChatContent.getTag();
                    tagSelectTextHolder.setChatmessage(chatMessage);
                    tagSelectTextHolder.setTextView(tvChatContent);
                    tagSelectTextHolder.setParentPosition(helper.getLayoutPosition());
                    tagSelectTextHolder.setPopTargetView(tvChatContent);
                    tagSelectTextHolder.selectText();
                }
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                break;
            case RECEIVED_SINGLE_VOICE_CALL:
            case RECEIVED_SINGLE_VIDEO_CALL: {
                helper.setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                        .setText(R.id.tv_group_nickname, chatMessage.getFromUserName())
                        .setText(R.id.tv_chat_content, chatMessage.getMessageContent())
                        .setGone(R.id.tv_timestamp, canShowTime)
                        .setGone(R.id.tv_group_nickname, !chatMessage.isGroupMsg());
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                TextView receivedTv = helper.getView(R.id.tv_chat_content);
                Drawable drawable = getContext().getResources().getDrawable(R.drawable.icon_received_video_call);
                if (helper.getItemViewType() == RECEIVED_SINGLE_VIDEO_CALL) {
                    drawable = getContext().getResources().getDrawable(R.drawable.icon_received_video_call);
                } else if (helper.getItemViewType() == RECEIVED_SINGLE_VOICE_CALL) {
                    drawable = getContext().getResources().getDrawable(R.drawable.icon_voice_call);
                }
                // 设置drawable的位置和大小，使用setBounds方法
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                // 将drawable设置到TextView的右边
                receivedTv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                break;
            }
            case RECEIVED_SPANNABLE_TEXT:
            case RECEIVED_TEXT:
                receivedText(helper, chatMessage, canShowTime);
                break;
            case SENT_IMAGE:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                loadImage(helper, chatMessage, chatMessage.getIsSent());
                break;
            case RECEIVED_IMAGE:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                loadImage(helper, chatMessage, chatMessage.getIsSent());
                helper.setText(R.id.tv_group_nickname, chatMessage.getFromUserName())
                        .setGone(R.id.tv_group_nickname, !chatMessage.isGroupMsg());
                break;
            case RECEIVED_QUOTE_IMAGE:
                receivedText(helper, chatMessage, canShowTime);
                loadQuoteImage(helper, chatMessage.getExtraData());
                helper.setText(R.id.tv_quote_nickname, chatMessage.getFromUserName())
                        .setGone(R.id.tv_quote_nickname, !chatMessage.isGroupMsg());
                break;
            case SENT_FILE:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                loadFile(helper, chatMessage, chatMessage.getIsSent());
                break;
            case RECEIVED_FILE:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                loadFile(helper, chatMessage, chatMessage.getIsSent());
                helper.setText(R.id.tv_group_nickname, chatMessage.getFromUserName())
                        .setGone(R.id.tv_group_nickname, !chatMessage.isGroupMsg());
                break;
            case SENT_VIDEO:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                loadVideo(helper, chatMessage, chatMessage.getIsSent());
                break;
            case RECEIVED_VIDEO:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                loadVideo(helper, chatMessage, chatMessage.getIsSent());
                helper.setText(R.id.tv_group_nickname, chatMessage.getFromUserName())
                        .setGone(R.id.tv_group_nickname, !chatMessage.isGroupMsg());
                break;
            case SENT_VOICE:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                helper.setGone(R.id.pb_sending, chatMessage.getStatus() != MessageStatus.SENDING.value())
                        .setGone(R.id.iv_msg_status, chatMessage.getStatus() != MessageStatus.SEND_FAIL.value())
                        .setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                        .setGone(R.id.tv_timestamp, canShowTime)
                        .setText(R.id.voiceTimeTv, ChatVoiceBean.getVoiceTime(chatMessage.getExtraData()));
                break;
            case RECEIVED_VOICE:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                helper.setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                        .setGone(R.id.tv_timestamp, canShowTime)
                        .setText(R.id.voiceTimeTv, ChatVoiceBean.getVoiceTime(chatMessage.getExtraData()))
                        .setText(R.id.tv_group_nickname, chatMessage.getFromUserName())
                        .setGone(R.id.tv_group_nickname, !chatMessage.isGroupMsg());
                break;
            case SENT_CARD_INFO:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                loadCardInfo(helper, chatMessage, chatMessage.getIsSent());
                break;
            case RECEIVED_CARD_INFO:
                loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
                loadCardInfo(helper, chatMessage, chatMessage.getIsSent());
                break;
            case SENT_LOCATION:
                break;
            case RECEIVED_LOCATION:
                break;
        }
    }

    private void loadQuoteImage(BaseViewHolder helper, String extraData) {
        ImageView imageView = helper.getView(R.id.aiv_quote);
        Uri uri = Uri.parse(extraData);
        // centerCrop 图片变形
        GlideApp.with(getContext())
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存策略
                .into(imageView);
    }

    private void receivedText(@NonNull BaseViewHolder helper, ChatMessage chatMessage, boolean canShowTime) {
        TextView receivedTv = helper.getView(R.id.tv_chat_content);
        helper.setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                .setGone(R.id.tv_timestamp, canShowTime)
                .setText(R.id.tv_group_nickname, chatMessage.getFromUserName())
                .setGone(R.id.tv_group_nickname, !chatMessage.isGroupMsg());
        if (chatMessage.getItemType() == RECEIVED_SPANNABLE_TEXT) {
            String str = chatMessage.getMessageContent();
            List<String> spannableStr = CheckUtil.findSpannableStr(str);
            if (!spannableStr.isEmpty()) {
                SpanUtils with = SpanUtils.with(receivedTv);
                int startIndex = 0;
                for (int i = 0; i < spannableStr.size(); i++) {
                    String actionStr = spannableStr.get(i);
                    int endIndex = str.indexOf(actionStr);
                    // endIndex - 1去掉第一个#
                    with.append(str.substring(startIndex, endIndex - 1));
                    String[] split = actionStr.split("action::");
                    with.append(split[0]).setForegroundColor(Color.BLUE).setClickSpan(new CustomClickableSpan(split[1]));
                    // startIndex + actionStr.length() + 1去掉最后一个#
                    startIndex = endIndex + actionStr.length() + 1;
                }
                with.append(str.substring(startIndex)).create();
            } else {
                receivedTv.setText(str);
            }
        } else {
            receivedTv.setText(chatMessage.getMessageContent());
            if (receivedTv.getTag() == null) {
                SelectTextHolder selectTextHolder = new SelectTextHolder(chatMessage, receivedTv,
                        receivedTv, helper.getLayoutPosition());
                selectTextHolder.selectText();
                receivedTv.setTag(selectTextHolder);
            } else {
                SelectTextHolder tagSelectTextHolder = (SelectTextHolder) receivedTv.getTag();
                tagSelectTextHolder.setChatmessage(chatMessage);
                tagSelectTextHolder.setTextView(receivedTv);
                tagSelectTextHolder.setParentPosition(helper.getLayoutPosition());
                tagSelectTextHolder.setPopTargetView(receivedTv);
                tagSelectTextHolder.selectText();
            }
        }
        loadAvatar(chatMessage, helper.getView(R.id.sdv_avatar));
    }

    private void loadCardInfo(BaseViewHolder helper, ChatMessage chatMessage, boolean isSent) {
        String extraData = chatMessage.getExtraData();
        if (TextUtils.isEmpty(extraData)) {
            return;
        }
        CardInfoBean cardInfoBean = JsonParser.deserializeByJson(extraData, CardInfoBean.class);
        if (cardInfoBean == null) {
            return;
        }
        if (isSent) {
            helper.setGone(R.id.pb_sending, chatMessage.getStatus() != MessageStatus.SENDING.value())
                    .setGone(R.id.iv_msg_status, chatMessage.getStatus() != MessageStatus.SEND_FAIL.value())
                    .setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                    .setGone(R.id.tv_timestamp, !chatMessage.needShowTime())
                    .setText(R.id.tv_contact_name, cardInfoBean.getNickName());
        } else {
            helper.setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                    .setText(R.id.tv_contact_name, cardInfoBean.getNickName());
        }
        AvatarGenerator.loadAvatar(getContext(), cardInfoBean.getUserId(), cardInfoBean.getNickName(), helper.getView(R.id.iv_contact), false);
    }

    private void loadVideo(BaseViewHolder helper, ChatMessage chatMessage, boolean isSent) {
        String extraData = chatMessage.getExtraData();
        if (TextUtils.isEmpty(extraData)) {
            return;
        }
        ChatVideoBean chatVideoBean = JsonParser.deserializeByJson(extraData, ChatVideoBean.class);
        if (chatVideoBean == null) {
            return;
        }
        if (isSent) {
            helper.setGone(R.id.pb_sending, chatMessage.getStatus() != MessageStatus.SENDING.value())
                    .setGone(R.id.iv_msg_status, chatMessage.getStatus() != MessageStatus.SEND_FAIL.value())
                    .setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                    .setGone(R.id.tv_timestamp, !chatMessage.needShowTime());
        } else {
            helper.setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true));
        }
        ImageView imageView = helper.getView(R.id.iv_video_thumbnail);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                showCustomPop(imageView, chatMessage, "");
                return true;
            }
        });
        Uri uri = null;
        String thumbnailLocalPath = chatVideoBean.getThumbnailLocalPath();
        if (!TextUtils.isEmpty(thumbnailLocalPath)) {
            File file = new File(thumbnailLocalPath);
            if (isSent && file.exists()) {
                // 本地读
                uri = Uri.fromFile(new File(thumbnailLocalPath));
            } else {
                // 网络获取
                uri = Uri.parse(chatVideoBean.getThumbnailUrl());
            }
        } else if (!TextUtils.isEmpty(chatVideoBean.getThumbnailUrl())) {
            // 网络获取
            uri = Uri.parse(chatVideoBean.getThumbnailUrl());
        }
        if (uri != null) {
            GlideApp.with(getContext())
                    .load(uri)
                    .centerInside()
                    .placeholder(R.drawable.image_loading_ic)
                    .into(imageView);
        }
    }

    /**
     * 注意回收SelectText对象
     *
     * @param holder
     */
    @Override
    public void onViewRecycled(@NonNull BaseViewHolder holder) {
        super.onViewRecycled(holder);
        switch (holder.getItemViewType()) {
            case SENT_TEXT:
            case RECEIVED_TEXT:
                View sentTv = holder.getView(R.id.tv_chat_content);
                if (null != sentTv) {
                    SelectTextHolder tag = (SelectTextHolder) sentTv.getTag();
                    if (tag != null) {
                        tag.reset();
                    }
                }
                break;
        }
    }

    /**
     * 加载头像
     *
     * @param chatMessage
     * @param imageView
     */
    private void loadAvatar(ChatMessage chatMessage, ImageView imageView) {
        String key = chatMessage.getFromUserId();
        if (TextUtils.isEmpty(key)) {
            // userid为空 是群聊
            key = chatMessage.getFromUserName();
        }
        AvatarGenerator.loadAvatar(getContext(), chatMessage.getFromUserId(), chatMessage.getFromUserName(), imageView, false);
    }

    private void loadFile(BaseViewHolder helper, ChatMessage chatMessage, boolean isSent) {
        if (isSent) {
            helper.setGone(R.id.pb_sending, chatMessage.getStatus() != MessageStatus.SENDING.value())
                    .setGone(R.id.iv_msg_status, chatMessage.getStatus() != MessageStatus.SEND_FAIL.value())
                    .setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                    .setGone(R.id.tv_timestamp, !chatMessage.needShowTime());
        } else {
            helper.setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true));
        }
        View view = helper.getView(R.id.cv_file_content);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showCustomPop(view, chatMessage, "", helper.getLayoutPosition());
                return true;
            }
        });
        String extraData = chatMessage.getExtraData();
        ChatFileBean chatFileBean = JsonParser.deserializeByJson(extraData, ChatFileBean.class);
        if (null == chatFileBean) {
            return;
        }
        helper.setText(R.id.tv_file_size, FileUtil.formatFileSize(Long.parseLong(chatFileBean.fileSize)))
                .setText(R.id.tv_file_name, chatFileBean.fileName)
                .setImageResource(R.id.iv_file_type, FileUtil.getFileIcon(FileUtil.getFileExtension(chatFileBean.fileName)));
    }

    private void loadImage(@NonNull BaseViewHolder helper, ChatMessage chatMessage, boolean isSent) {
        if (isSent) {
            helper.setGone(R.id.pb_sending, chatMessage.getStatus() != MessageStatus.SENDING.value())
                    .setGone(R.id.iv_msg_status, chatMessage.getStatus() != MessageStatus.SEND_FAIL.value())
                    .setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true))
                    .setGone(R.id.tv_timestamp, !chatMessage.needShowTime());

        } else {
            helper.setText(R.id.tv_timestamp, TimestampUtil.getTimeStringAutoShort2(chatMessage.getTimestamp(), true));
        }
        ImageView imageView = helper.getView(R.id.sdv_image_content);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showCustomPop(imageView, chatMessage, "", helper.getLayoutPosition());
                return true;
            }
        });
        Uri uri;
        if (!TextUtils.isEmpty(chatMessage.getFileLocalPath()) && isSent) {
            // 本地读
            uri = Uri.fromFile(new File(chatMessage.getFileLocalPath()));
        } else {
            // 网络获取
            uri = Uri.parse(chatMessage.getMessageContent());
        }
        int pic_width = 0;
        int pic_height = 0;
        if (!TextUtils.isEmpty(chatMessage.getExtraData())) {
            ChatImageBean imageBean = JsonParser.deserializeByJson(chatMessage.getExtraData(), ChatImageBean.class);
            if (imageBean != null) {
                pic_width = imageBean.getImageWidth();
                pic_height = imageBean.getImageHeight();
            }
        }
        int[] widthAndHeight = BitmapLoaderUtil.getImageWidthAndHeightToTalk(pic_width, pic_height);
        MultiTransformation<Bitmap> transformation = new MultiTransformation<>(new CenterInside(), new RoundedCorners((int) getContext().getResources().getDimension(R.dimen.dp_4)));
        if (pic_width > 0 && widthAndHeight[1] > SizeUtils.dp2px(180) * 1.5) {
            transformation = new MultiTransformation<>(new CenterCrop(), new RoundedCorners((int) getContext().getResources().getDimension(R.dimen.dp_4)));
        }
        // centerCrop 图片变形
        GlideApp.with(getContext())
                .load(uri)
                .transform(transformation)
                .override(widthAndHeight[0], widthAndHeight[1])
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存策略
                .into(imageView);
    }

    public void refreshDownloadProgress(String eventOriginId, String type, int progress) {
        List<ChatMessage> data = getData();
        for (int i = 0; i < data.size(); i++) {
            ChatMessage chatMessage = data.get(i);
            if (chatMessage.getOriginId().equals(eventOriginId)) {
                chatMessage.progress = progress;
                notifyItemChanged(i, type);
            }
        }
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ChatMessage item, @NonNull List<?> payloads) {
        switch (item.getItemType()) {
            case RECEIVED_FILE: {
                for (Object p : payloads) {
                    String payload = String.valueOf(p);
                    if (Constant.DOWNLOAD_PROGRESS.equals(payload)) {
                        helper.setVisible(R.id.file_progress_fl, item.progress != 100);
                        CircularProgressIndicator circularProgressIndicator = helper.getView(R.id.progress_bar);
                        circularProgressIndicator.setProgress(item.progress);
                    }
                }
            }
            break;
        }
    }

    public void setEditText(EditText mTextMsgEt) {
        this.mTextMsgEt = mTextMsgEt;
    }

    private class SelectTextHolder {
        private ChatMessage chatmessage;
        private TextView textView;
        private View popTargetView;
        private int parentPosition;
        private SelectTextHelper selectTextHelper;
        private String selectedText;

        public SelectTextHelper getSelectTextHelper() {
            return selectTextHelper;
        }

        public void setParentPosition(int parentPosition) {
            this.parentPosition = parentPosition;
        }

        public void setPopTargetView(View popTargetView) {
            this.popTargetView = popTargetView;
        }

        public void setTextView(TextView textView) {
            this.textView = textView;
        }

        public void setChatmessage(ChatMessage chatmessage) {
            this.chatmessage = chatmessage;
        }

        Runnable mShowCustomPopRunnable = new Runnable() {
            @Override
            public void run() {
                showCustomPop(popTargetView, chatmessage, selectedText, parentPosition);
            }
        };

        Runnable mShowSelectViewRunnable = new Runnable() {
            @Override
            public void run() {
                selectTextHelper.reset();
            }
        };
        private long downTime;

        public SelectTextHolder(ChatMessage chatMessage, TextView textView, View viewGroup, int parentPosition) {
            this.chatmessage = chatMessage;
            this.textView = textView;
            this.popTargetView = viewGroup;
            this.parentPosition = parentPosition;
        }

        /**
         * 自定义SelectTextEvent 隐藏 光标
         */
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEvent(SelectTextEvent event) {
            if (null == selectTextHelper) {
                return;
            }
            String type = event.getType();
            if (TextUtils.isEmpty(type)) {
                return;
            }
            switch (type) {
                case "dismissAllPop":
                    selectTextHelper.reset();
                    break;
                case "dismissAllPopDelayed":
                    postReset(100L);
                    break;
            }
        }

        /**
         * 演示消息列表选择文本
         */
        public void selectText() {
            selectTextHelper = new SelectTextHelper.Builder(textView) // 放你的textView到这里！！
                    .setCursorHandleColor(ContextCompat.getColor(getContext(), R.color.global_color)) // 游标颜色
                    .setCursorHandleSizeInDp(22f) // 游标大小 单位dp
                    .setSelectedColor(
                            ContextCompat.getColor(
                                    getContext(),
                                    R.color.colorAccentTransparent
                            )
                    ) // 选中文本的颜色
                    .setSelectAll(true) // 初次选中是否全选 default true
                    .setScrollShow(true) // 滚动时是否继续显示 default true
                    .setSelectedAllNoPop(true) // 已经全选无弹窗，设置了监听会回调 onSelectAllShowCustomPop 方法
                    .setMagnifierShow(true) // 放大镜 default true
                    .setSelectTextLength(2)// 首次选中文本的长度 default 2
                    .setPopDelay(100)// 弹窗延迟时间 default 100毫秒
                    .setPopAnimationStyle(R.style.Base_Animation_AppCompat_Dialog)// 弹窗动画 default 无动画
                    // 双击进入大文本的时候弹出的对话框
                    .addItem(R.drawable.ic_msg_copy,
                            R.string.copy,
                            () -> {
                                selectTextHelper.reset();
                                ClipboardUtils.copyText(selectedText);
                                ToastUtils.show(getContext().getString(R.string.already_copy));
                            }).addItem(R.drawable.ic_msg_select_all,
                            R.string.select_all,
                            () -> {
                                selectAll();
                            }).addItem(R.drawable.ic_msg_forward,
                            R.string.forward,
                            () -> {
                                forward(null, selectedText);
                            })
                    .addItem(R.drawable.ic_msg_share,
                            R.string.share, () -> shareSelectedText(null, selectedText))
                    .addItem(R.drawable.ic_msg_delete,
                            R.string.delete, () -> deleteMsg(chatmessage.getOriginId(), parentPosition))
                    .setPopSpanCount(5) // 设置操作弹窗每行个数 default 5
                    .setPopStyle(
                            R.drawable.shape_color_4c4c4c_radius_8 /*操作弹窗背*/,
                            R.drawable.ic_arrow /*箭头图片*/
                    ) // 设置操作弹窗背景色、箭头图片
                    .build();

            selectTextHelper.setSelectListener(new SelectTextHelper.OnSelectListener() {

                /**
                 * 点击回调
                 */
                @Override
                public void onClick(@Nullable View view, @Nullable CharSequence charSequence) {
                    // 拿原始文本方式
                    clickTextView(chatmessage.getMessageContent()); // 推荐
                    // clickTextView(originalContent!!) // 不推荐 富文本可能被修改值 导致gif动不了
                }


                /**
                 * 长按回调
                 */
                @Override
                public void onLongClick(@Nullable View view) {
                    postShowCustomPop(100L);
                }

                /**
                 * 选中文本回调
                 */
                @Override
                public void onTextSelected(@Nullable CharSequence content) {
                    selectedText = content.toString();
                }

                /**
                 * 弹窗关闭回调
                 */
                @Override
                public void onDismiss() {
                }

                /**
                 * 点击TextView里的url回调
                 *
                 * 已被下面重写
                 * textView.setMovementMethod(new LinkMovementMethodInterceptor());
                 */
                @Override
                public void onClickUrl(@Nullable String url) {
                    Trace.d("onClickUrl: >>>>" + url);
                    if (CheckUtil.isImgUrl(url)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constant.MESSAGE_FILE_LOCAL, chatmessage.getFileLocalPath());
                        bundle.putString(Constant.MESSAGE_ORIGIN_ID, chatmessage.getOriginId());
                        bundle.putString(Constant.MESSAGE_CONTENT, chatmessage.getMessageContent());
                        bundle.putString(Constant.CONVERSATION_ID, chatmessage.getConversationId());
                        ImagePreviewMsgActivity.start(getContext(), bundle);
                    } else if (CheckUtil.isXmppLink(url)) {
                        if (url.startsWith(XmppUri.XMPP_PRE) && url.endsWith(XmppUri.ACTION_JOIN)) {
                            String conversationId = CheckUtil.getRoomIdFromUrl(url);
                            Disposable subscribe = DBManager.Companion.getInstance(getContext())
                                    .getConversationByConversationId(PreferencesUtil.getInstance().getUserId(),
                                            conversationId)
                                    .subscribe(conversationInfoList -> {
                                        if (conversationInfoList.isEmpty()) {
                                            // 先查询群信息是否需要密码
                                            new MessageDialog.Builder(getContext())
                                                    .setTitle(getContext().getString(R.string.tips))
                                                    .setMessage(getContext().getString(R.string.join_group_tips, conversationId))
                                                    .setConfirm(getContext().getString(R.string.join))
                                                    .setCancel(getContext().getString(R.string.common_only_copy))
                                                    .setListener(new MessageDialog.OnListener() {

                                                        @Override
                                                        public void onConfirm(BaseDialog dialog) {
                                                            Activity topActivity = ActivityManager.getInstance().getTopActivity();
                                                            if (topActivity instanceof AppActivity) {
                                                                ChatManager.getInstance().joinGroup(conversationId, (AppActivity) topActivity);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancel(BaseDialog dialog) {
                                                            ClipboardUtils.copyText(url);
                                                            ToastUtils.show(getContext().getString(R.string.already_copy));
                                                        }
                                                    })
                                                    .show();
                                        } else {
                                            ClipboardUtils.copyText(url);
                                            ToastUtils.show(getContext().getString(R.string.already_copy));
                                        }
                                    });
                        } else {
                            ClipboardUtils.copyText(url);
                            ToastUtils.show(getContext().getString(R.string.already_copy));
                        }
                    } else {
                        new MessageDialog.Builder(getContext())
                                .setTitle(getContext().getString(R.string.open_url_by_browser))
                                .setMessage(url)
                                .setConfirm(getContext().getString(R.string.common_confirm))
                                .setCancel(getContext().getString(R.string.common_only_copy))
                                .setListener(new MessageDialog.OnListener() {

                                    @Override
                                    public void onConfirm(BaseDialog dialog) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                        getContext().startActivity(intent);
                                    }

                                    @Override
                                    public void onCancel(BaseDialog dialog) {
                                        ClipboardUtils.copyText(url);
                                        ToastUtils.show(getContext().getString(R.string.already_copy));
                                    }
                                })
                                .show();
                    }

                }

                /**
                 * 全选显示自定义弹窗回调
                 */
                @Override
                public void onSelectAllShowCustomPop() {
                    postShowCustomPop(100L);
                }

                /**
                 * 重置回调
                 */
                @Override
                public void onReset() {
                    SelectTextEventBus.Companion.getInstance().dispatch(new SelectTextEvent("dismissOperatePop"));
                }

                /**
                 * 解除自定义弹窗回调
                 */
                @Override
                public void onDismissCustomPop() {
                    SelectTextEventBus.Companion.getInstance().dispatch(new SelectTextEvent("dismissOperatePop"));
                }

                /**
                 * 是否正在滚动回调
                 */
                @Override
                public void onScrolling() {
                    removeShowSelectView();
                }
            });

            // 注册
            if (!SelectTextEventBus.Companion.getInstance().isRegistered(this)) {
                SelectTextEventBus.Companion.getInstance().register(this, SelectTextEvent.class);
            }
        }

        private void removeShowSelectView() {
            textView.removeCallbacks(mShowSelectViewRunnable);
        }

        /**
         * 双击进入查看内容
         *
         * @param content 内容
         */
        private void clickTextView(CharSequence content) {
            if (System.currentTimeMillis() - downTime < 500) {
                downTime = 0;
                SelectTextDialog dialog = new SelectTextDialog(getContext(), content);
                dialog.show();
            } else {
                downTime = System.currentTimeMillis();
            }
        }

        /**
         * 延迟重置
         * 为了支持滑动不重置
         */
        private void postReset(Long duration) {
            textView.removeCallbacks(mShowSelectViewRunnable);
            textView.postDelayed(mShowSelectViewRunnable, duration);
        }

        /**
         * 延迟显示CustomPop
         * 防抖
         */
        private void postShowCustomPop(Long duration) {
            textView.removeCallbacks(mShowCustomPopRunnable);
            textView.postDelayed(mShowCustomPopRunnable, duration);
        }

        /**
         * 全选
         */
        private void selectAll() {
            SelectTextEventBus.Companion.getInstance().dispatch(new SelectTextEvent("dismissAllPop"));
            if (null != selectTextHelper) {
                selectTextHelper.selectAll();
            }
        }


        public void reset() {
            if (selectTextHelper != null) {
                selectTextHelper.reset();
            }
        }
    }

    /**
     * 自定义弹窗
     *
     * @param targetView     目标View
     * @param message        实体
     * @param parentPosition
     */
    private void showCustomPop(View targetView, ChatMessage message, String selectedText, int parentPosition) {
        CustomPop msgPop = new CustomPop(getContext(), targetView, false);
        if (!TextUtils.isEmpty(selectedText)) {
            msgPop.addItem(
                    R.drawable.ic_msg_copy,
                    R.string.copy,
                    () -> {
                        ClipboardUtils.copyText(selectedText);
                        ToastUtils.show(getContext().getString(R.string.already_copy));
                    }
            );
        }
        msgPop.addItem(R.drawable.ic_msg_forward,
                R.string.forward,
                () -> {
                    forward(message, selectedText);
                });
        msgPop.addItem(R.drawable.ic_msg_delete,
                R.string.delete, () -> {
                    deleteMsg(message.getOriginId(), parentPosition);
                });
        if (!TextUtils.isEmpty(message.getFileLocalPath())) {
            if (SmartContentType.IMAGE.equals(message.getMessageType()) && FileUtils.isFileExists(message.getFileLocalPath())) {
                msgPop.addItem(R.drawable.ic_msg_share,
                        R.string.share,
                        () -> {
                            Intent imageIntent = Intent.createChooser(OpenFileUtils.getImageFileIntent(getContext(), new File(message.getFileLocalPath())), "分享到");
                            getContext().startActivity(imageIntent);
                        });
            }
        }
        if (!TextUtils.isEmpty(selectedText)) {

            msgPop.addItem(R.drawable.ic_msg_quote,
                    R.string.quote, () -> quoteText(selectedText));
            msgPop.addItem(R.drawable.ic_msg_share,
                    R.string.share,
                    () -> {
                        shareSelectedText(message, selectedText);
                    });
        }

            /*msgPop.addItem(R.drawable.ic_msg_rollback,
                    R.string.rollback,
                    () -> {
                    });*/
        // msgPop.setItemWrapContent(); // 自适应每个item
        msgPop.show();
    }

    private void deleteMsg(String originId, int position) {
        new MessageDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.tips))
                .setMessage(getContext().getString(R.string.confirm_delete))
                .setConfirm(getContext().getString(R.string.common_confirm))
                .setCancel(getContext().getString(R.string.common_cancel))
                .setListener(new MessageDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                        MessageDao.getInstance().disableMessageByOriginId(originId);
                        remove(position);
                    }

                    @Override
                    public void onCancel(BaseDialog dialog) {
                    }
                })
                .show();

    }

    /**
     * 转发
     */
    private void forward(ChatMessage message, String selectedText) {
        if (!TextUtils.isEmpty(selectedText)) {
            SelectTextEventBus.Companion.getInstance().dispatch(new SelectTextEvent("dismissAllPop"));
            SelectConversationActivity.startTextType(getContext(), selectedText, SmartContentType.TEXT, "");
        } else if (message != null && SmartContentType.IMAGE.equals(message.getMessageType())) {
            SelectConversationActivity.startExtensionType(getContext(), message.getMessageType(),
                    message.getMessageContent(), message.getFileLocalPath(), message.getExtraData());
        } else if (SmartContentType.FILE.equals(message.getMessageType())) {
            SelectConversationActivity.startExtensionType(getContext(), message.getMessageType(), message.getMessageContent(), message.getFileLocalPath(), message.getExtraData());
        }
        // 此时activity是chatactivity
        ActivityManager.getInstance().getTopActivity().overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.fake_anim);
    }

    /**
     * 引用
     *
     * @param text
     */
    private void quoteText(String text) {
        text = QuoteHelper.replaceAltQuoteCharsInText(text);
        text = text
                // first replace all '>' at the beginning of the line with nice and tidy '>>'
                // for nested quoting
                .replaceAll("(^|\n)(" + QuoteHelper.QUOTE_CHAR + ")", "$1$2$2")
                // then find all other lines and have them start with a '> '
                .replaceAll("(^|\n)(?!" + QuoteHelper.QUOTE_CHAR + ")(.*)", "$1> $2")
        ;
        Editable editable = mTextMsgEt.getEditableText();
        int position = mTextMsgEt.getSelectionEnd();
        if (position == -1) position = editable.length();
        if (position > 0 && editable.charAt(position - 1) != '\n') {
            editable.insert(position++, "\n");
        }
        editable.insert(position, text);
        position += text.length();
        editable.insert(position++, "\n");
        if (position < editable.length() && editable.charAt(position) != '\n') {
            editable.insert(position, "\n");
        }
        if (!mTextMsgEt.hasFocus()) {
            mTextMsgEt.requestFocus();
        }
        mTextMsgEt.setSelection(position);
    }

    /**
     * 分享
     */
    private void shareSelectedText(ChatMessage message, String selectedText) {
        if (!TextUtils.isEmpty(selectedText)) {
            SelectTextEventBus.Companion.getInstance().dispatch(new SelectTextEvent("dismissAllPop"));
            getContext().startActivity(Intent.createChooser(OpenFileUtils.getShareTextIntent(selectedText), "分享到"));
        }
    }
}
