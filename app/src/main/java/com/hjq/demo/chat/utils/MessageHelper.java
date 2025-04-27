// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.hjq.demo.chat.utils;


import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.hjq.demo.R;
import com.hjq.demo.chat.extensions.AitExtension;
import com.hjq.demo.chat.model.ait.AitBlock;
import com.hjq.demo.chat.model.ait.AtContactsModel;
import com.hjq.demo.manager.ActivityManager;
import com.hjq.demo.utils.JsonParser;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import com.bndg.smack.entity.SmartMessage;
import com.bndg.smack.utils.SIMXmlParser;

/**
 * 消息相关工具类，主要用于创建消息，消息内容解析等
 */
public class MessageHelper {

    public static final int REVOKE_TIME_INTERVAL = 2 * 60 * 1000;

    public static final float DEF_SCALE = 0.6f;
    public static final float SMALL_SCALE = 0.4F;
    private static final String TAG = "MessageUtil";

    // @信息高亮颜色值
    private static final int AT_HIGHLIGHT = R.color.text_link_color;

    /**
     * 识别文本消息内容中的@信息并高亮
     *
     * @param context         上下文
     * @param spannableString 设置高亮的文本内容
     * @param color           高亮部分颜色值
     * @param content         消息内容
     * @param message         消息体
     */
    public static void identifyAtExpression(
            Context context,
            SpannableString spannableString,
            int color,
            String content,
            SmartMessage message) {
        AtContactsModel atContactsModel = getAitBlockFromMsg("");
        if (atContactsModel != null && !TextUtils.isEmpty(content)) {
            List<AitBlock> blockList = atContactsModel.getAtBlockList();
            for (AitBlock block : blockList) {
                for (AitBlock.AitSegment segment : block.segments) {
                    if (segment.start >= 0 && segment.end > segment.start && segment.end < content.length()) {
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
                        spannableString.setSpan(
                                colorSpan, segment.start, segment.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
    }


    /**
     * 将文本消息和@信息进行解析，并设置高亮色到SpannableString中
     *
     * @param context         上下文
     * @param spannableString 设置高亮的文本内容
     * @param color           高亮部分颜色值
     * @param content         消息内容
     * @param atContactsModel @信息
     */
    public static void identifyAtExpression(
            Context context,
            SpannableString spannableString,
            int color,
            String content,
            AtContactsModel atContactsModel) {
        if (atContactsModel != null && !TextUtils.isEmpty(content)) {
            List<AitBlock> blockList = atContactsModel.getAtBlockList();
            for (AitBlock block : blockList) {
                for (AitBlock.AitSegment segment : block.segments) {
                    if (segment.start >= 0 && segment.end > segment.start && segment.end < content.length()) {
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
                        spannableString.setSpan(
                                colorSpan, segment.start, segment.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
    }

    /**
     * 生成@信息高亮的SpannableString
     *
     * @param content 消息内容
     * @return 高亮的SpannableString
     */
    public static SpannableString generateAtSpanString(String content) {
        SpannableString spannableString = new SpannableString(content);
        int color = ActivityManager.getInstance().getApplication().getResources().getColor(AT_HIGHLIGHT);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        spannableString.setSpan(colorSpan, 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    /**
     * 获取消息内容中的@信息
     *
     * @return @信息
     */
    public static AtContactsModel getAitBlockFromMsg(String xmlRepresentation) {
        AitExtension aitExtension = (AitExtension) SIMXmlParser.getInstance().parseXml(xmlRepresentation, new AitExtension.Provider());
        if (aitExtension != null) {
            String jsonData = aitExtension.getJsonData();
            if (!TextUtils.isEmpty(jsonData)) {
                // 处理@消息 如果是发现@我 那么会话显示？
                Map mapByJson = JsonParser.getMapByJson(jsonData);
                if(mapByJson != null) {
                    JSONObject aitJson = new JSONObject( mapByJson);
                    return AtContactsModel.parseFromJson(aitJson);
                }
            }
        }
        return null;
    }

    // 设置TextView的内容
    private static void viewSetText(View textView, SpannableString mSpannableString) {
        if (textView instanceof TextView) {
            ((TextView) textView).setText(mSpannableString);
        }
    }


}
