package com.hjq.demo.chat.fragment;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.demo.R;
import com.hjq.demo.app.AppFragment;
import com.hjq.demo.chat.adapter.FileListAdapter;
import com.hjq.demo.chat.cons.Constant;
import com.hjq.demo.chat.dao.MessageDao;
import com.hjq.demo.chat.entity.ChatFileBean;
import com.hjq.demo.chat.entity.ChatMessage;
import com.hjq.demo.chat.entity.FileModel;
import com.hjq.demo.chat.utils.TimestampUtil;
import com.hjq.demo.ui.activity.CopyActivity;
import com.hjq.demo.utils.JsonParser;
import com.hjq.demo.utils.OpenFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 可进行拷贝的副本
 */
public final class FilesListFragment extends AppFragment<CopyActivity> {

    private RecyclerView rvList;
    private FileListAdapter fileListAdapter;

    public static FilesListFragment newInstance(boolean isReceived) {
        Bundle args = new Bundle();
        args.putBoolean(Constant.FILE_RECEIVED, isReceived);
        FilesListFragment fragment = new FilesListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.files_list_fragment;
    }

    @Override
    protected void initView() {
        rvList = findViewById(R.id.rv_list);
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        fileListAdapter = new FileListAdapter(new ArrayList<>());
        rvList.setAdapter(fileListAdapter);
        fileListAdapter.setOnItemClickListener((baseQuickAdapter, view, position) -> {
            FileModel fileModel = (FileModel) baseQuickAdapter.getItem(position);
            if (fileModel != null) {
                OpenFileUtils.chooseOpenFile(getContext(), fileModel.getFileName(), fileModel.getFilePath());
            }
        });
    }

    @Override
    protected void initData() {
        boolean received = getBundle().getBoolean(Constant.FILE_RECEIVED);

        MessageDao.getInstance().getFileMsg(getAttachActivity(), received, new MessageDao.MessageDaoCallback() {
            @Override
            public void getFileMsgs(List<ChatMessage> chatMessages) {
                List<FileModel> fileModels = new ArrayList<>();
                for (ChatMessage chatMessage : chatMessages) {
                    ChatFileBean chatFileBean = JsonParser.deserializeByJson(chatMessage.getExtraData(), ChatFileBean.class);
                    if (chatFileBean == null) {
                        continue;
                    }
                    FileModel fileModel = new FileModel(chatFileBean.fileName,
                            chatMessage.getFileLocalPath(), chatFileBean.contactTitle, TimestampUtil.getTimePoint(chatMessage.getTimestamp()));
                    fileModels.add(fileModel);
                }
                fileListAdapter.setList(fileModels);
            }
        });
    }
}