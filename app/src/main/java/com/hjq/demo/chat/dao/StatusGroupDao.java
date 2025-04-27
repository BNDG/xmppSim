package com.hjq.demo.chat.dao;

import com.hjq.demo.chat.entity.StatusGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 状态组
 *
 * @author zhou
 */
public class StatusGroupDao {
    private static volatile StatusGroupDao instance;

    private StatusGroupDao() {
    }

    public static StatusGroupDao getInstance() {
        if (instance == null) {
            synchronized (StatusGroupDao.class) {
                if (instance == null) {
                    instance = new StatusGroupDao();
                }
            }
        }
        return instance;
    }

    /**
     * 获取状态组列表
     *
     * @return 状态组列表
     */
    public List<StatusGroup> getStatusGroupList() {
        return new ArrayList<>();
    }

    /**
     * 保存状态组
     *
     * @param statusGroup 状态组
     */
    public void saveStatusGroup(StatusGroup statusGroup) {
    }

    /**
     * 清除状态组
     */
    public void clearStatusGroup() {
    }

}