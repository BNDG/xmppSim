package com.bndg.smack.callback;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;

import java.util.Collection;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.model.SmartUserInfo;
import com.bndg.smack.utils.SmartTrace;

/**
 * @author r
 * @date 2024/11/22
 * @description sdk使用 处理好友状态
 */

public class ReceiverFriendStatusListener implements RosterListener {


    @Override
    public void entriesAdded(Collection<Jid> addresses) {
        // 当有新的联系人添加到 roster 中时触发 添加好友就会触发
        SmartTrace.d("新的联系人 " + addresses);
        for (Jid address : addresses) {
            RosterEntry entry = Roster.getInstanceFor(SmartIMClient.getInstance().getConnection()).getEntry(address.asBareJid());
            // 确保是新的请求且未经批准
            if (!entry.canSeeHisPresence()) {
                SmartTrace.d("新的好友请求");
            } else {
                SmartTrace.d("对方通过了我的请求");
                SmartUserInfo info = new SmartUserInfo();
                info.setUserId(entry.getJid().toString());
                info.setNickname(entry.getName());
                info.setSubscribeStatus(entry.getType().name());
                SmartIMClient.getInstance().getFriendshipManager().receivedFriendAdded(info);

            }
        }
    }

    @Override
    public void entriesUpdated(Collection<Jid> addresses) {
        // todo 更新联系人
        // 当 roster 中的联系人信息发生更新时触发 添加或者删除都会触发 对方注销账号是unsubscribed
        // 通常不是用于处理好友请求接受或拒绝的情况，但可能也需要根据需求处理
        for (Jid address : addresses) {
            RosterEntry entry = Roster.getInstanceFor(SmartIMClient.getInstance().getConnection()).getEntry(address.asBareJid());
            SmartTrace.d(address,
                    entry + " 联系人更新了 名字会发生变化> " + entry.getName());

        }

    }

    @Override
    public void entriesDeleted(Collection<Jid> addresses) {
        SmartTrace.d(addresses + "被删除");
        // 当联系人从 roster 中删除时触发，这可能意味着好友请求被拒绝
        for (Jid address : addresses) {
            SmartTrace.d(
                    address.asBareJid() + "好友请求被拒绝 or 删除好友: " + address);
        }
    }

    @Override
    public void presenceChanged(Presence presence) {
        // 当某个联系人的在线状态发生变化时触发 一定是联系人
        // 这不直接用于检测好友请求的接受或拒绝，但可以用来获取好友当前的在线状态
        // 收到了好友同意 -> 更新联系人
        //            Presence Stanza [to=m2032@tigase.bndg.cn,from=m1918@tigase.bndg.cn/1423222896-tigase-488,id=1LKHW-3,type=available,prio=0,]
        Jid from = presence.getFrom();
        BareJid bareJid = from.asBareJid();
        RosterEntry entry = Roster.getInstanceFor(SmartIMClient.getInstance().getConnection()).getEntry(bareJid);
        SmartIMClient.getInstance().getFriendshipManager().receivedFriendPresent(presence, entry);
    }
}

    