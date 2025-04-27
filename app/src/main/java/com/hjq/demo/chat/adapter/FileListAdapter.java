package com.hjq.demo.chat.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.demo.R;
import com.hjq.demo.chat.entity.FileModel;
import com.hjq.demo.chat.utils.FileUtil;

import java.util.List;

/**
 * @author r
 * @date 2024/8/12
 * @description Brief description of the file content.
 */
public class FileListAdapter extends BaseQuickAdapter<FileModel, BaseViewHolder> {
    public FileListAdapter(@Nullable List<FileModel> data) {
        super(R.layout.item_file, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, FileModel fileModel) {
        helper.setText(R.id.tv_files_title, fileModel.getFileName())
                .setText(R.id.tv_files_date, fileModel.getFileDate())
                .setImageResource(R.id.iv_file_type, FileUtil.getFileIcon(FileUtil.getFileExtension(fileModel.getFileName())));
    }

}
