package com.hjq.demo.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.base.BaseDialog;
import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.adapter.SmartConversationAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.entity.CardInfoBean;
import com.hjq.demo.chat.entity.ChatFileBean;
import com.hjq.demo.chat.entity.ChatImageBean;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.chat.extensions.CardInfoExtension;
import com.hjq.demo.chat.extensions.FileInfoExtension;
import com.hjq.demo.chat.extensions.ImageSizeExtension;
import com.hjq.demo.chat.manager.ChatMessageManager;
import com.hjq.demo.ui.dialog.ForwardMsgDialog;
import com.hjq.demo.utils.JsonParser;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.OnClick;
import com.bndg.smack.enums.SmartContentType;
import com.bndg.smack.enums.SmartConversationType;
import com.bndg.smack.extensions.base.IExtension;

/**
 * 选择最近聊天 转发消息
 *
 * @author zhou
 */
public class SelectConversationActivity extends ChatBaseActivity {

    SmartConversationAdapter smartConversationAdapter;
    private RecyclerView rvList;
    private String messageContent;
    private String messageType;
    private String originId;
    private String fileLocalPath;
    private String extraData;

    @Log
    public static void startTextType(Context context,
                                     String content,
                                     String msgType,
                                     String msgOriginId) {
        Intent intent = new Intent(context, SelectConversationActivity.class);
        intent.putExtra(Constant.MESSAGE_CONTENT, content);
        intent.putExtra(Constant.MESSAGE_TYPE, msgType);
        intent.putExtra(Constant.MESSAGE_ORIGIN_ID, msgOriginId);
        context.startActivity(intent);
    }

    public static void startExtensionType(Context context,
                                          String msgType,
                                          String content,
                                          String localPath,
                                          String extraData) {
        Intent intent = new Intent(context, SelectConversationActivity.class);
        intent.putExtra(Constant.MESSAGE_TYPE, msgType);
        intent.putExtra(Constant.MESSAGE_CONTENT, content);
        intent.putExtra(Constant.MESSAGE_FILE_LOCAL, localPath);
        intent.putExtra(Constant.MESSAGE_EXTRA_DATA, extraData);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.select_conversation_activity;
    }

    @Override
    public void initView() {
        rvList = findViewById(R.id.rv_list);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.select_chat));
    }

    @Override
    public void initListener() {
    }

    @Override
    public void initData() {
        fileLocalPath = getIntent().getStringExtra(Constant.MESSAGE_FILE_LOCAL);
        extraData = getIntent().getStringExtra(Constant.MESSAGE_EXTRA_DATA);
        messageContent = getIntent().getStringExtra(Constant.MESSAGE_CONTENT);
        messageType = getIntent().getStringExtra(Constant.MESSAGE_TYPE);
        originId = getIntent().getStringExtra(Constant.MESSAGE_ORIGIN_ID);

        smartConversationAdapter = new SmartConversationAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvList.setLayoutManager(layoutManager);
        rvList.setAdapter(smartConversationAdapter);
        smartConversationAdapter.setOnItemClickListener((baseQuickAdapter, view, position) -> {
            ConversationInfo conversation = (ConversationInfo) baseQuickAdapter.getItem(position);
            if (null != conversation) {
                String conversationId = conversation.getConversationId();
                String conversationTitle = conversation.getConversationTitle();
                String conversationType = conversation.getConversationType();
                HashMap<String, String> conversations = new HashMap<>();
                conversations.put(conversationId, conversationTitle);
                showSendDialog(conversations, conversationType);
            }
        });
        DBManager.Companion.getInstance(this)
                .getConversationListByUserId(myUserInfo.getUserId())
                .to(RxLife.to(this))
                .subscribe(newConversationList -> {
                    smartConversationAdapter.setList(newConversationList);
                });
    }

    /**
     * 显示转发对话框
     */
    private void showSendDialog(HashMap<String, String> conversations, String conversationType) {
        StringBuilder conversationId = new StringBuilder();
        StringBuilder conversationTitle = new StringBuilder();
        for (Map.Entry<String, String> entry : conversations.entrySet()) {
            conversationId.append(entry.getKey()).append(",");
            conversationTitle.append(entry.getValue()).append(",");
        }
        String[] ids = conversationId.toString().split(",");
        // 去掉title最后一个,
        String title = conversationTitle.substring(0, conversationTitle.length() - 1);
        new ForwardMsgDialog.Builder(this)
                .setMessage(messageContent)
                .setConversationId(ids[0])
                .setConversationTitle(title)
                .setListener(new ForwardMsgDialog.OnListener() {
                    @Override
                    public void onConfirm(BaseDialog dialog) {
                        for (String conversationId : ids) {
                            String originId = "";
                            ArrayList<IExtension> elements = new ArrayList<>();
                            if (SmartContentType.TEXT.equals(messageType)) {
                                ChatMessage textMsg = ChatMessage.createTextMsg(conversationId,
                                        conversationType,
                                        messageType,
                                        messageContent);
                                MessageDao.getInstance().saveAndSetLastTimeStamp(textMsg, isSuccess -> {
                                });
                                originId = textMsg.getOriginId();
                            } else if (SmartContentType.IMAGE.equals(messageType)) {
                                ChatMessage imageMsg = ChatMessage.createForwardFileMsg(conversationId, conversationType, messageType, fileLocalPath, messageContent, extraData);
                                originId = imageMsg.getOriginId();
                                ChatImageBean chatImageBean = JsonParser.deserializeByJson(extraData, ChatImageBean.class);
                                if (null == chatImageBean) {
                                    return;
                                } else {
                                    elements.add(new ImageSizeExtension(chatImageBean.getImageWidth(), chatImageBean.getImageHeight()));
                                }
                            } else if (SmartContentType.FILE.equals(messageType)) {
                                ChatMessage fileMsg = ChatMessage.createForwardFileMsg(conversationId, conversationType, messageType,
                                        fileLocalPath, messageContent, extraData);
                                originId = fileMsg.getOriginId();
                                ChatFileBean chatFileBean = JsonParser.deserializeByJson(extraData, ChatFileBean.class);
                                if (null == chatFileBean) {
                                    return;
                                } else {
                                    elements.add(new FileInfoExtension(chatFileBean.fileName, chatFileBean.fileSize));
                                }
                            } else if (Constant.MSG_TYPE_CARD_INFO.equals(messageType)) {
                                ChatMessage cardMsg = ChatMessage.createCardInfoMsg(conversationId, conversationType, messageType, messageContent, extraData);
                                originId = cardMsg.getOriginId();
                                CardInfoBean cardInfoBean = JsonParser.deserializeByJson(extraData, CardInfoBean.class);
                                if (null == cardInfoBean) {
                                    return;
                                }
                                elements.add(new CardInfoExtension(cardInfoBean.getUserId(), cardInfoBean.getNickName()));
                            }

                            if (SmartConversationType.SINGLE.name().equals(conversationType)) {
                                ChatMessageManager.getInstance().sendSingleMessage(
                                        conversationId,
                                        conversations.get(conversationId),
                                        messageContent,
                                        messageType,
                                        elements,
                                        -1,
                                        originId);
                            } else {
                                ChatMessageManager.getInstance().sendGroupMessage(
                                        conversationId,
                                        messageType,
                                        messageContent,
                                        elements,
                                        -1,
                                        originId);
                            }
                        }
                        finish();
                    }
                })
                .show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fake_anim, R.anim.slide_in_out_bottom);
    }

    @SingleClick
    @OnClick({R.id.iv_back, R.id.tv_new_chat})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                overridePendingTransition(R.anim.fake_anim, R.anim.slide_in_out_bottom);
                break;
            case R.id.tv_new_chat:
                CreateChatActivity.start(SelectConversationActivity.this, new CreateChatActivity.CreateChatsListener() {

                    @Override
                    public void getCheckedConversations(HashMap<String, String> convresations) {
                        showSendDialog(convresations, SmartConversationType.SINGLE.name());
                    }
                });
                break;
        }
    }


}