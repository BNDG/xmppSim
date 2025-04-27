/**
 * Copyright (c) 2013, Redsolution LTD. All rights reserved.
 * <p>
 * This file is part of Xabber project; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License, Version 3.
 * <p>
 * Xabber is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License,
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package com.bndg.smack.muc;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.packet.MUCItem;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.parts.Resourcepart;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.bndg.smack.entity.UserJid;
import com.bndg.smack.enums.StatusMode;


/**
 * Chat room.
 * <p/>
 * Warning: We are going to remove SMACK components.
 *
 * @author alexander.ivanov
 */
public class RoomChat {

    /**
     * Information about occupants for STRING-PREPed resource.
     */
    private  Map<Resourcepart, Occupant> occupants = new HashMap<>();
    /**
     * Invited user for the sent packet ID.
     */
    private  Map<String, UserJid> invites = new HashMap<>();
    private UserJid user;
    /**
     * Joining was requested from the UI.
     */
    private boolean requested;
    /**
     * Nickname used in the room.
     */
    private Resourcepart nickname;
    private String password;
    private RoomState state;
    private String subject;
    /**
     * SMACK MUC implementation.
     */
    private MultiUserChat multiUserChat;

    public static RoomChat create(EntityBareJid user, Resourcepart nickname, String password) throws UserJid.UserJidCreateException {
        return new RoomChat(UserJid.from(user), nickname, password);
    }

    private RoomChat(UserJid user, Resourcepart nickname, String password) {
        this.user = user;
        this.nickname = nickname;
        this.password = password;
        requested = false;
        state = RoomState.unavailable;
        subject = "";
        multiUserChat = null;
        occupants = new HashMap<>();
        invites = new HashMap<>();
    }

    public RoomChat() {
        state = RoomState.available;
    }

    @NonNull
    public EntityBareJid getTo() {
        return getRoom();
    }

    public Type getType() {
        return Type.groupchat;
    }

    EntityBareJid getRoom() {
        return user.getJid().asEntityBareJidIfPossible();
    }

    public Resourcepart getNickname() {
        return nickname;
    }

    public void setNickname(Resourcepart nickname) {
        this.nickname = nickname;
    }

    String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    boolean isRequested() {
        return requested;
    }

    void setRequested(boolean requested) {
        this.requested = requested;
    }

    public RoomState getState() {
        return state;
    }

    public  void setState(RoomState state) {
        this.state = state;
        if (!state.inUse()) {
            multiUserChat = null;
            occupants.clear();
            invites.clear();
        }
        if (state == RoomState.available) {
        }
    }

    Collection<Occupant> getOccupants() {
        return Collections.unmodifiableCollection(occupants.values());
    }

    String getSubject() {
        return subject;
    }

    public MultiUserChat getMultiUserChat() {
        return multiUserChat;
    }

    public void setMultiUserChat(MultiUserChat multiUserChat) {
        this.multiUserChat = multiUserChat;
    }

    void putInvite(String packetID, UserJid user) {
        invites.put(packetID, user);
    }


    protected boolean onPacket(UserJid bareAddress, Stanza stanza, boolean isCarbons) {
        if (user.equals(bareAddress)) {
            return false;
        }

        final org.jxmpp.jid.Jid from = stanza.getFrom();
        final Resourcepart resource = from.getResourceOrNull();
        if (stanza instanceof Message) {
            final Message message = (Message) stanza;

            if (message.getType() == Type.error) {
                UserJid invite = invites.remove(message.getStanzaId());
                if (invite != null) {
                }
                return true;
            }
            MUCUser mucUser = MUCUser.from(stanza);
            if (mucUser != null && mucUser.getDecline() != null) {
                onInvitationDeclined(mucUser.getDecline().getFrom(), mucUser.getDecline().getReason());
                return true;
            }
            if (mucUser != null && mucUser.getStatus() != null && mucUser.getStatus().contains(MUCUser.Status.create("100"))
            ) {
                // 'This room is not anonymous'
                return true;
            }
            String text = message.getBody();
            final String subject = message.getSubject();
            if (text == null && subject == null) {
                return true;
            }
            if (subject != null) {
                if (this.subject.equals(subject)) {
                    return true;
                }
                this.subject = subject;
            } else {
            }
        } else if (stanza instanceof Presence) {
            Presence presence = (Presence) stanza;
            if (presence.getType() == Presence.Type.available) {
                Occupant oldOccupant = occupants.get(resource);
                Occupant newOccupant = createOccupant(resource, presence);
                newOccupant.setJid(from);
                occupants.put(resource, newOccupant);
                if (oldOccupant == null) {
                    onAvailable(resource);
                } else {
                    boolean changed = false;
                    if (oldOccupant.getAffiliation() != newOccupant.getAffiliation()) {
                        changed = true;
                        onAffiliationChanged(resource, newOccupant.getAffiliation());
                    }
                    if (oldOccupant.getRole() != newOccupant.getRole()) {
                        changed = true;
                        onRoleChanged(resource, newOccupant.getRole());
                    }
                    if (oldOccupant.getStatusMode() != newOccupant.getStatusMode()
                            || !oldOccupant.getStatusText().equals(newOccupant.getStatusText())) {
                        changed = true;
                        onStatusChanged(resource, newOccupant.getStatusMode(), newOccupant.getStatusText());
                    }
                    if (changed) {
                    }
                }
            } else if (presence.getType() == Presence.Type.unavailable && state == RoomState.available) {
                occupants.remove(resource);
                MUCUser mucUser = MUCUser.from(presence);
                if (mucUser != null && mucUser.getStatus() != null) {
                    if (mucUser.getStatus().contains(MUCUser.Status.KICKED_307)) {
                        onKick(resource, mucUser.getItem().getActor());
                    } else if (mucUser.getStatus().contains(MUCUser.Status.BANNED_301)) {
                        onBan(resource, mucUser.getItem().getActor());
                    } else if (mucUser.getStatus().contains(MUCUser.Status.NEW_NICKNAME_303)) {
                        Resourcepart newNick = mucUser.getItem().getNick();
                        if (newNick == null) {
                            return true;
                        }
                        onRename(resource, newNick);
                        Occupant occupant = createOccupant(newNick, presence);
                        occupants.put(newNick, occupant);
                    } else if (mucUser.getStatus().contains(MUCUser.Status.REMOVED_AFFIL_CHANGE_321)) {
                        onRevoke(resource, mucUser.getItem().getActor());
                    }
                } else {
                    onLeave(resource);
                }
            }
        }
        return true;
    }

    protected String parseInnerMessage(boolean ui, Message message, Date timestamp, String parentMessageId) {
        if (message.getType() == Type.error) return null;

        final org.jxmpp.jid.Jid from = message.getFrom();
        final Resourcepart resource = from.getResourceOrNull();
        String text = message.getBody();
        final String subject = message.getSubject();

        if (text == null) return null;
        if (subject != null) return null;

        String originalStanza = message.toXML().toString();
        String originalFrom = message.getFrom().toString();
        boolean fromMUC = message.getType().equals(Type.groupchat);

        // forward comment (to support previous forwarded xep)
        // modify body with references
        return "";
    }

    /**
     * @return Whether resource is own nickname.
     * @param resource
     */
    private boolean isSelf(Resourcepart resource) {
        return nickname != null && resource != null && nickname.equals(resource);
    }

    /**
     * Informs that the invitee has declined the invitation.
     */
    private void onInvitationDeclined(EntityBareJid from, String reason) {
        // TODO
    }

    /**
     * A occupant becomes available.
     * @param resource
     */
    private void onAvailable(Resourcepart resource) {
        if (isSelf(resource)) {
            setState(RoomState.available);
            if (isRequested()) {
                setRequested(false);
            } else {
            }
        } else {
            if (state == RoomState.available) {
            }
        }
    }

    /**
     * Warning: this method should be placed with packet provider.
     *
     * @return New occupant based on presence information.
     */
    private Occupant createOccupant(Resourcepart resource, Presence presence) {
        Occupant occupant = new Occupant(resource);
        org.jxmpp.jid.Jid jid = null;
        MUCAffiliation affiliation = MUCAffiliation.none;
        MUCRole role = MUCRole.none;


        StatusMode statusMode = StatusMode.unavailable;
        String statusText = null;
        MUCUser mucUser = MUCUser.from(presence);
        if (mucUser != null) {
            MUCItem item = mucUser.getItem();
            if (item != null) {
                jid = item.getJid();
                try {
                    affiliation = item.getAffiliation();
                } catch (NoSuchElementException e) {
                }
                try {
                    role = item.getRole();
                } catch (NoSuchElementException e) {
                }
                statusMode = StatusMode.createStatusMode(presence);
                statusText = presence.getStatus();
            }
        }
        if (statusText == null) {
            statusText = "";
        }
        occupant.setJid(jid);
        occupant.setAffiliation(affiliation);
        occupant.setRole(role);
        occupant.setStatusMode(statusMode);
        occupant.setStatusText(statusText);
        return occupant;
    }

    private void onAffiliationChanged(Resourcepart resource, MUCAffiliation affiliation) {
    }

    private void onRoleChanged(Resourcepart resource, MUCRole role) {
    }

    private void onStatusChanged(Resourcepart resource, StatusMode statusMode, String statusText) {
    }

    /**
     * A occupant leaves room.
     * @param resource
     */
    private void onLeave(Resourcepart resource) {
    }

    /**
     * A occupant was kicked.
     *
     * @param resource
     * @param actor
     */
    private void onKick(Resourcepart resource, org.jxmpp.jid.Jid actor) {
        if (isSelf(resource)) {
        }
    }

    /**
     * A occupant was banned.
     *
     * @param resource
     * @param actor
     */
    private void onBan(Resourcepart resource, org.jxmpp.jid.Jid actor) {
        if (isSelf(resource)) {
//            MUCManager.getInstance().leaveRoom(account, getRoom());
        }
    }

    /**
     * A occupant has changed his nickname in the room.
     *
     * @param resource
     * @param newNick
     */
    private void onRename(Resourcepart resource, Resourcepart newNick) {
    }

    /**
     * A user's membership was revoked from the room
     *
     * @param resource
     * @param actor
     */
    private void onRevoke(Resourcepart resource, org.jxmpp.jid.Jid actor) {
        if (isSelf(resource)) {
//            MUCManager.getInstance().leaveRoom(account, getRoom());
        }
    }

    protected void onComplete() {
        if (getState() == RoomState.waiting) {
//            MUCManager.getInstance().joinRoom(account, getRoom(), false);
        }
    }

    protected void onDisconnect() {
        if (state != RoomState.unavailable) {
            setState(RoomState.waiting);
        }
    }

    public boolean canJoin() {
        return state != RoomState.error && state != RoomState.destroyed && state != RoomState.forrbidden;
    }
}