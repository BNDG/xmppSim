package com.hjq.demo.http.floatlog;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.demo.R;

import java.util.List;

/**
 * =============================
 * 作    者：r
 * 描    述：
 * 创建日期：2020/9/2 下午5:07
 * =============================
 */
public class HttpLogAdapter extends BaseQuickAdapter<HttpLogEvent, BaseViewHolder> {

    public HttpLogAdapter(@Nullable List<HttpLogEvent> data) {
        super(R.layout.float_view_logitem, data);
        addChildClickViewIds(R.id.tv_url,R.id.tv_params);
        addChildLongClickViewIds(R.id.tv_url,R.id.tv_params);
    }

    @Override
    protected void convert(BaseViewHolder helper, HttpLogEvent item) {
        helper.setText(R.id.tv_url, item.url)
                .setText(R.id.tv_params, item.params)
                .setText(R.id.tv_header, "{\"accessToken\":\"" + item.header + "\"}")
                .setGone(R.id.tv_header, TextUtils.isEmpty(item.header));
    }
}
