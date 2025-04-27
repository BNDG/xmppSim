package com.hjq.demo.chat.dao;

import com.hjq.demo.chat.entity.SearchHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索历史
 *
 * @author zhou
 */
public class SearchHistoryDao {
    private static volatile SearchHistoryDao instance;

    private SearchHistoryDao() {
    }

    public static SearchHistoryDao getInstance() {
        if (instance == null) {
            synchronized (SearchHistoryDao.class) {
                if (instance == null) {
                    instance = new SearchHistoryDao();
                }
            }
        }
        return instance;
    }

    /**
     * 保存搜索历史
     *
     * @param searchHistory 搜索历史
     */
    public void saveSearchHistory(SearchHistory searchHistory) {


    }

    /**
     * 取前N条搜索历史记录
     *
     * @param pageSize N
     * @return 前N条搜索历史记录
     */
    public List<SearchHistory> getSearchHistoryList(int pageSize) {
//        select * from search_history order by id desc limit ?
        return new ArrayList<>();
    }

    /**
     * 根据关键字删除单条搜索
     *
     * @param keyword 关键字
     */
    public void deleteSearchHistoryByKeyword(String keyword) {
//        String sql = "delete from search_history where keyword = ?";
    }

    /**
     * 清除搜索历史
     */
    public void clearSearchHistory() {
    }
}
