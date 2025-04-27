package com.bndg.smack.contract;

import java.util.List;

import com.bndg.smack.callback.ISmartCallback;

public interface ISmartComm {
    /**
     * 获取所有组
     *
     * @return 所有组集合
     */
    <T> List<T> getGroups();

    /**
     * 添加一个分组
     *
     * @param groupName groupName
     * @return boolean
     */
    boolean addGroup(String groupName);

    /**
     * 获取某个组里面的所有好友
     *
     * @param groupName 组名
     * @return List<RosterEntry>
     */
    <T> List<T> getEntriesByGroup(String groupName);

}
