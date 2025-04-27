// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.hjq.demo.chat.views.ait;


import com.hjq.demo.chat.entity.GroupMember;
import com.hjq.demo.chat.model.ait.AitUserInfo;
import com.hjq.demo.chat.utils.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @ 帮助类
 */
public class AitHelper {

    /**
     * 转换群成员为AitUserInfo
     *
     * @param userInfoWithTeams 群成员
     * @return 不包含AI聊用户
     */
    public static List<AitUserInfo> convertTeamMemberToAitUserInfo(
            List<GroupMember> userInfoWithTeams) {
        List<AitUserInfo> aitUsers = new ArrayList<>(userInfoWithTeams.size());
        for (GroupMember userInfoWithTeam : userInfoWithTeams) {
            if(PreferencesUtil.getInstance().getUserId().equals(userInfoWithTeam.getMemberOriginId())) {
                // 自己
                continue;
            }
            aitUsers.add(
                    new AitUserInfo(
                            userInfoWithTeam.getMemberOriginId(),
                            userInfoWithTeam.getMemberName(),
                            userInfoWithTeam.getMemberAccount()));
        }
        return aitUsers;
    }

}
