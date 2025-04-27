package com.hjq.demo.chat.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.widget.LoadingDialog;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;

/**
 * 修改群聊名称
 *
 * @author zhou
 */
public class UpdateGroupNameActivity extends ChatBaseActivity {
    String groupId;
    String oldGroupName;
    private TextView mTitleTv;
    private EditText mGroupNameEt;
    private TextView mSaveTv;

    private LoadingDialog mDialog;

    @Log
    public static void start(AppActivity context, String groupId, String groupName, UpdateGroupNameListener listener) {
        Intent intent = new Intent(context, UpdateGroupNameActivity.class);
        intent.putExtra(Constant.CONVERSATION_ID, groupId);
        intent.putExtra(Constant.CONVERSATION_TITLE, groupName);
        context.startActivityForResult(intent, new OnActivityCallback() {
            @Override
            public void onActivityResult(int resultCode, @Nullable Intent data) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        listener.updateGroupName(data.getStringExtra("groupName"));
                    }
                }
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_update_group_name;
    }

    public void initView() {
        mDialog = new LoadingDialog(this);
        mTitleTv = findViewById(R.id.tv_title);
        TextPaint paint = mTitleTv.getPaint();
        paint.setFakeBoldText(true);
        mTitleTv.setText(R.string.group_manager);
        mGroupNameEt = findViewById(R.id.et_group_name);
        mSaveTv = findViewById(R.id.tv_right);
        mSaveTv.setVisibility(View.VISIBLE);
        groupId = getIntent().getStringExtra(Constant.CONVERSATION_ID);
        oldGroupName = getIntent().getStringExtra(Constant.CONVERSATION_TITLE);
        if (TextUtils.isEmpty(oldGroupName)) {
            mGroupNameEt.setHint(getString(R.string.unnamed));
        } else {
            mGroupNameEt.setText(oldGroupName);
            // 光标移至最后
            CharSequence charSequence = mGroupNameEt.getText();
            if (charSequence != null) {
                Spannable spanText = (Spannable) charSequence;
                Selection.setSelection(spanText, charSequence.length());
            }
        }
        mGroupNameEt.addTextChangedListener(new TextChange());

        mSaveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.setMessage("正在保存");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();
                final String groupName = mGroupNameEt.getText().toString();
                updateGroupName(groupId, groupName);
            }
        });
    }

    @Override
    protected void initData() {

    }


    @Override
    public void initListener() {

    }

    public interface UpdateGroupNameListener {
        void updateGroupName(String groupName);
    }

    class TextChange implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String newGroupName = mGroupNameEt.getText().toString();
            // 是否填写
            boolean isGroupNameHasText = newGroupName.length() > 0;
            // 是否修改
            boolean isGroupNameChanged = !oldGroupName.equals(newGroupName);

            if (isGroupNameHasText && isGroupNameChanged) {
                mSaveTv.setEnabled(true);
            } else {
                mSaveTv.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private void updateGroupName(String groupId, final String groupName) {
        SmartIMClient.getInstance().getSmartCommChatRoomManager().changeRoomName(groupId, groupName, new IChatRoomCallback() {
            @Override
            public void changeNameSuccess() {
                mDialog.dismiss();
                Intent intent = new Intent();
                intent.putExtra(Constant.GROUP_NAME, groupName);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void changeNameFailed() {
                mDialog.dismiss();
                toast(getString(R.string.failed));
            }
        });

    }
}
