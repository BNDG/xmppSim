package com.bndg.smack.callback;

import org.jivesoftware.smackx.bookmarks.BookmarkedConference;

import java.util.List;

/**
 * @author r
 * @date 2024/6/13
 * @description 保存的群书签列表
 */
public interface IBookmarkedConferenceCallback {
    void onSuccess(List<BookmarkedConference> conferences);
}
