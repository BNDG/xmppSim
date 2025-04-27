package com.hjq.demo.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.adapter.SmartConversationAdapter;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.ConversationInfo;
import com.hjq.demo.utils.Trace;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;

import butterknife.OnClick;
import com.bndg.smack.enums.SmartConversationType;

/**
 * @author r
 * @date 2024/9/10
 * @description 所有群聊列表
 */

public class ConversationListActivity extends ChatBaseActivity {

    SmartConversationAdapter smartConversationAdapter;
    private RecyclerView rvList;

    @Log
    public static void start(Context context) {
        Intent intent = new Intent(context, ConversationListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.data_list_activity;
    }

    @Override
    public void initView() {
        rvList = findViewById(R.id.rv_list);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.group_chats));
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

        smartConversationAdapter = new SmartConversationAdapter(new ArrayList<>());
        smartConversationAdapter.setActivity(this);
        rvList.setAdapter(smartConversationAdapter);
        smartConversationAdapter.setOnItemClickListener((baseQuickAdapter, view, position) -> {
            ConversationInfo conversation = (ConversationInfo) baseQuickAdapter.getItem(position);
            if (null != conversation) {
                Trace.d("ConversationListActivity initData: ");
                conversation.setUnReadNum(0);
                ChatActivity.start(getContext(), SmartConversationType.GROUP.name(),
                        conversation.getConversationId(), conversation.getConversationTitle());
            }
        });

        DBManager.Companion.getInstance(this)
                .getConversationListByUserIdAndType(myUserInfo.getUserId(),
                        SmartConversationType.GROUP.name())
                .to(RxLife.to(this))
                .subscribe(list -> {
                    if (list != null) {
                        smartConversationAdapter.setList(list);
                    }
                });
    }

    @SingleClick
    @OnClick({R.id.iv_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }

}