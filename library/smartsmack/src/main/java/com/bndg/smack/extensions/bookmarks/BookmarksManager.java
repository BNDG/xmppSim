package com.bndg.smack.extensions.bookmarks;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bookmarks.BookmarkManager;
import org.jivesoftware.smackx.bookmarks.BookmarkedConference;
import org.jivesoftware.smackx.bookmarks.BookmarkedURL;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.bndg.smack.SmartCommHelper;
import com.bndg.smack.SmartIMClient;
import com.bndg.smack.entity.UserJid;
import com.bndg.smack.muc.MUCManager;
import com.bndg.smack.utils.SmartTrace;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Manage bookmarks and there requests.
 * <p>
 * Created by valery.miller on 02.06.17.
 */

public class BookmarksManager {

    public static final String XABBER_NAME = "Xabber bookmark";
    public static final String XABBER_URL = "Required to correctly sync bookmarks";

    private static BookmarksManager instance;
    private List<BookmarkedConference> conferencesFromBookmarks;

    public static BookmarksManager getInstance() {
        if (instance == null)
            instance = new BookmarksManager();
        return instance;
    }

    /**
     * 是否支持书签
     *
     * @return
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException.NotConnectedException
     * @throws InterruptedException
     * @throws SmackException.NoResponseException
     */
    public boolean isSupported() throws XMPPException.XMPPErrorException,
            SmackException.NotConnectedException, InterruptedException,
            SmackException.NoResponseException {
        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
        return bookmarkManager.isSupported();
    }

    public List<BookmarkedURL> getUrlFromBookmarks() {
        List<BookmarkedURL> urls = Collections.emptyList();
        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
        try {
            urls = bookmarkManager.getBookmarkedURLs();
        } catch (SmackException.NoResponseException | InterruptedException |
                 SmackException.NotConnectedException | XMPPException.XMPPErrorException e) {
            SmartTrace.e(e);
        }
        return urls;
    }

    public void addUrlToBookmarks(String url, String name, boolean isRSS) {
        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
        try {
            bookmarkManager.addBookmarkedURL(url, name, isRSS);
        } catch (SmackException.NoResponseException | InterruptedException |
                 SmackException.NotConnectedException | XMPPException.XMPPErrorException e) {
            SmartTrace.e(e);
        }
    }

    public void removeUrlFromBookmarks(String url) {
        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
        try {
            bookmarkManager.removeBookmarkedURL(url);
        } catch (SmackException.NoResponseException | InterruptedException |
                 SmackException.NotConnectedException | XMPPException.XMPPErrorException e) {
            SmartTrace.e(e);
        }
    }

    /**
     * 添加到书签
     *
     * @param conferenceName 群聊名称
     * @param conferenceJid
     * @param userNick
     */
    public void addConferenceToBookmarks(String conferenceName,
                                         EntityBareJid conferenceJid, Resourcepart userNick) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (conferencesFromBookmarks == null) {
                            conferencesFromBookmarks = BookmarksManager.getInstance().getConferencesFromBookmarks();
                        }
                        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(
                                SmartIMClient.getInstance().getConnection());
                        // 旧版书签一定特别注意name不能为null 否则smack会抛异常！！
                        bookmarkManager.addBookmarkedConference(TextUtils.isEmpty(conferenceName) ? conferenceJid.toString() : conferenceName, conferenceJid, true,
                                userNick, "");
                        /*if (!hasConference(conferencesFromBookmarks, conferenceJid)) {
                            SmartTrace.w("加入新书签");

                        } else {
                            SmartTrace.w("书签已经存在");
                        }*/
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                }, onError -> {
                    SmartTrace.w("加入书签出错",
                            onError);
                });

    }

    @NonNull
    public List<BookmarkedConference> getConferencesFromBookmarks()
            throws SmackException.NoResponseException, SmackException.NotConnectedException,
            InterruptedException, XMPPException.XMPPErrorException {

        List<BookmarkedConference> conferences = Collections.emptyList();
        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
        conferences = bookmarkManager.getBookmarkedConferences();
        return conferences;
    }

    /**
     * 从书签中删除群聊
     *
     * @param conferenceJid
     */
    public void removeConferenceFromBookmarks(EntityBareJid conferenceJid) {
        Disposable subscribe = Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
                        try {
                            SmartTrace.d(conferenceJid, "从书签中删除 start ");
                            bookmarkManager.removeBookmarkedConference(conferenceJid);
                            SmartTrace.d(conferenceJid, "从书签中删除");
                        } catch (Exception e) {
                            SmartTrace.e(e);
                        }
                        return true;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess -> {
                    if (onSuccess) {

                    } else {

                    }
                }, onError -> {

                });
    }

    public void removeBookmarks(ArrayList<BookmarkVO> bookmarks) {
        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
        try {
            for (BookmarkVO bookmark : bookmarks) {
                // remove conferences
                if (bookmark.getType() == BookmarkVO.TYPE_CONFERENCE) {
                    bookmarkManager.removeBookmarkedConference(
                            JidCreate.from(bookmark.getJid()).asEntityBareJidIfPossible());
                }
                // remove url
                if (bookmark.getType() == BookmarkVO.TYPE_URL)
                    bookmarkManager.removeBookmarkedURL(bookmark.getUrl());
            }
        } catch (SmackException.NoResponseException | InterruptedException |
                 SmackException.NotConnectedException | XMPPException.XMPPErrorException
                 | XmppStringprepException e) {
            SmartTrace.e(e);
        }
    }

    public void cleanCache(EntityBareJid entityBareJid) {
        BookmarkManager bookmarkManager = BookmarkManager.getBookmarkManager(SmartIMClient.getInstance().getConnection());
        try {
            bookmarkManager.removeBookmarkedConference(entityBareJid);
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException |
                 SmackException.NotConnectedException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void onAuthorized() {
//        cleanCache(account);
        List<BookmarkedConference> conferences;
        try {
            conferences = getConferencesFromBookmarks();
        } catch (SmackException.NoResponseException | InterruptedException |
                 SmackException.NotConnectedException | XMPPException.XMPPErrorException e) {
            SmartTrace.e(e);
            return;
        }

        if (!conferences.isEmpty()) {
            for (BookmarkedConference conference : conferences) {
                if (!MUCManager.getInstance().hasRoom(conference.getJid())) {
                    SmartTrace.d(this, " Conference " + conference.getJid() + "was added to roster from bookmarks");
                }
            }
        }

        // Check bookmarks on first run new Xabber. Adding all conferences to bookmarks.
        if (!isBookmarkCheckedByXabber()) {
            // add conferences from phone to bookmarks
            // add url about check to bookmarks
            addUrlToBookmarks(XABBER_URL, XABBER_NAME, false);
        }

    }

    private boolean isBookmarkCheckedByXabber() {
        List<BookmarkedURL> urls = getUrlFromBookmarks();
        if (!urls.isEmpty()) {
            for (BookmarkedURL url : urls) {
                if (url.getURL().equals(XABBER_URL)) return true;
            }
        }
        return false;
    }

    private boolean hasConference(List<BookmarkedConference> conferences, EntityBareJid jid) {
        for (int i = 0; i < conferences.size(); i++) {
            BookmarkedConference conference = conferences.get(i);
            if (conference.getJid().toString().equals(jid.toString())) return true;
        }
        return false;
    }

    private void removeMUC(final UserJid user) {
        // 移除通知
        BookmarksManager.getInstance().removeConferenceFromBookmarks(user.getJid().asEntityBareJidIfPossible());
    }

    public boolean hasRoom(String roomId) {
        try {
            if (conferencesFromBookmarks == null) {
                conferencesFromBookmarks = BookmarksManager.getInstance().getConferencesFromBookmarks();
            }
            return hasConference(conferencesFromBookmarks, JidCreate.entityBareFrom(roomId));
        } catch (Exception e) {
        }
        return false;
    }
}
