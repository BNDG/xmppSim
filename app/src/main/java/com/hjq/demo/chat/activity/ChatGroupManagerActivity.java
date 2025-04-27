package com.hjq.demo.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.hjq.demo.R;
import com.hjq.demo.aop.Log;
import com.hjq.demo.aop.SingleClick;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.DBManager;
import com.hjq.demo.chat.entity.AvatarEntity;
import com.hjq.demo.chat.entity.ChatEvent;
import com.hjq.demo.chat.utils.AvatarGenerator;
import com.hjq.demo.chat.widget.ConfirmDialog;
import com.hjq.demo.http.glide.GlideApp;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.ui.activity.ImageCropActivity;
import com.hjq.demo.ui.activity.ImageSelectActivity;
import com.hjq.demo.utils.Trace;
import com.hjq.http.model.FileContentResolver;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import butterknife.BindView;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.callback.IChatRoomCallback;
import com.bndg.smack.callback.IUserInfoCallback2;
import com.bndg.smack.model.SmartUserInfo;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * @author r
 * @date 2024/9/10
 * @description 群管理-群主权限-解散群聊-群管理
 */

public class ChatGroupManagerActivity extends ChatBaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_title)
    TextView mTitleTv;
    @BindView(R.id.sdv_avatar)
    ImageView mAvatarSdv;
    private String groupId;

    @Log
    public static void start(Context context, String param) {
        Intent intent = new Intent(context, ChatGroupManagerActivity.class);
        intent.putExtra(Constant.GROUP_ID, param);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_group_manager;
    }

    @Override
    public void initView() {
        mTitleTv.setText(R.string.group_manager);
        setTitleStrokeWidth(mTitleTv);
        setOnClickListener(R.id.sb_administrators, R.id.rl_avatar);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
        groupId = getIntent().getStringExtra(Constant.GROUP_ID);
        findViewById(R.id.rl_destroy_group).setOnClickListener(v -> {
            final ConfirmDialog exitConfirmDialog = new ConfirmDialog(this, "",
                    getString(R.string.disband_group_chat), getString(R.string.confirm), "");
            exitConfirmDialog.setOnDialogClickListener(new ConfirmDialog.OnDialogClickListener() {
                @Override
                public void onOkClick() {
                    exitConfirmDialog.dismiss();
                    SmartIMClient.getInstance().getSmartCommChatRoomManager().destroyRoom(groupId, new IChatRoomCallback() {
                        @Override
                        public void deleteRoomSuccess() {
                            ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_REMOVED);
                            event.obj = groupId;
                            EventBus.getDefault().post(event);
                            DBManager.Companion.getInstance(ChatGroupManagerActivity.this)
                                    .deleteConversation(myUserInfo.getUserId(), groupId)
                                    .subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            ChatEvent event = new ChatEvent(ChatEvent.CONVERSATION_REMOVED);
                                            event.obj = groupId;
                                            EventBus.getDefault().post(event);
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {

                                        }
                                    });
                            DBManager.Companion.getInstance(ChatGroupManagerActivity.this)
                                    .deleteMemberByGroupId(groupId);
                            startActivity(MainActivity.class);
                            finish();
                            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                            ActivityManager.getInstance().finishAllActivities(MainActivity.class);
                        }

                        @Override
                        public void deleteRoomFailed(int code, String desc) {
                            toast(desc);
                        }
                    });
                }

                @Override
                public void onCancelClick() {
                    exitConfirmDialog.dismiss();
                }
            });
            // 点击空白处消失
            exitConfirmDialog.setCancelable(true);
            exitConfirmDialog.show();
        });

        Disposable subscribe = DBManager.Companion.getInstance(getContext())
                .getAvatarByConversationId(groupId)
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
                                        .into(mAvatarSdv);
                            }
                        }
                    }
                });
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sb_administrators:
                ChatGroupAdministratorsActivity.start(this, groupId);
                break;
            case R.id.rl_avatar:
                ImageSelectActivity.start(this, data -> {
                    // 裁剪头像
                    cropImageFile(new File(data.get(0)));
                });
                break;
            default:
                break;
        }
    }

    /**
     * 裁剪图片
     */
    private void cropImageFile(File sourceFile) {
        ImageCropActivity.start(this, sourceFile, 1, 1, new ImageCropActivity.OnCropListener() {

            @Override
            public void onSucceed(Uri fileUri, String fileName) {
                File outputFile;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    outputFile = new FileContentResolver(getActivity(), fileUri, fileName);
                } else {
                    try {
                        outputFile = new File(new URI(fileUri.toString()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        outputFile = new File(fileUri.toString());
                    }
                }
                updateCropImage(outputFile, true);
            }

            @Override
            public void onError(String details) {
                // 没有的话就不裁剪，直接上传原图片
                // 但是这种情况极其少见，可以忽略不计
                updateCropImage(sourceFile, false);
            }
        });
    }

    /**
     * 更新裁剪后的图片
     *
     * @param file
     * @param deleteFile
     */
    private void updateCropImage(File file, boolean deleteFile) {
        showDialog();
        Uri contentUri = null;
        if (file instanceof FileContentResolver) {
            contentUri = ((FileContentResolver) file).getContentUri();
        } else {
            contentUri = Uri.fromFile(file);
        }
        Trace.d("updateCropImage: " + file);
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        Tiny.getInstance()
                .source(contentUri)
                .asFile()
                .withOptions(options)
                .compress(new FileCallback() {
                    @Override
                    public void callback(boolean isSuccess, String outfile, Throwable t) {
                        Trace.d("callback: 输出文件路径" + outfile);
                        SmartIMClient.getInstance().getSmartCommChatRoomManager().changeGroupAvatar(groupId, new File(outfile), new IChatRoomCallback() {
                            @Override
                            public void changeGroupAvatarSuccess() {
                                hideDialog();
                                GlideApp.with(getContext())
                                        .load(new File(outfile))
                                        .transform(new RoundedCorners(Constant.MIDDLE_CORNER)) // 设置圆角
                                        .into(mAvatarSdv);
                            }

                            @Override
                            public void changeGroupAvatarFailed() {
                                hideDialog();
                            }
                        });
                    }
                });

    }

}