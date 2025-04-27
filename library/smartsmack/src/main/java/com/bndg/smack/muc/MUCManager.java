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

import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.MUCRole;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.parts.Resourcepart;

import java.util.Collection;
import java.util.Collections;

import com.bndg.smack.SmartIMClient;
import com.bndg.smack.entity.UserJid;

/**
 * Manage multi user chats.
 * <p/>
 * Warning: We are going to remove SMACK components.
 *
 * @author alexander.ivanov
 */
public class MUCManager {

    private static MUCManager instance;

    public static MUCManager getInstance() {
        if (instance == null) {
            instance = new MUCManager();
        }

        return instance;
    }

    private MUCManager() {
    }

    public void onLoad() {
    }

    private void onLoaded(Collection<RoomChat> roomChats, Collection<RoomChat> needJoins) {
        for (RoomChat roomChat : roomChats) {
        }
    }

    /**
     * @return <code>null</code> if does not exists.
     */
    public RoomChat getRoomChat(EntityBareJid room) {
        return null;
    }

    public boolean hasRoom(UserJid room) {
        EntityBareJid entityBareJid = room.getJid().asEntityBareJidIfPossible();
        if (entityBareJid == null) {
            return false;
        }

        return hasRoom(room.getJid().asEntityBareJidIfPossible());
    }

    /**
     * @return Whether there is such room.
     */
    public boolean hasRoom(EntityBareJid room) {
        return getRoomChat(room) != null;
    }

    public boolean isMucPrivateChat(UserJid user) {
        if (user == null) return false;
        EntityBareJid entityBareJid = user.getJid().asEntityBareJidIfPossible();
        if (entityBareJid == null) {
            return false;
        }

        return hasRoom(entityBareJid) && user.getJid().getResourceOrNull() != null;
    }

    public Resourcepart getNickname(EntityBareJid room) {
        RoomChat roomChat = getRoomChat(room);
        if (roomChat == null) {
            return Resourcepart.EMPTY;
        }
        return roomChat.getNickname();
    }

    /**
     * @param room
     * @return password or empty string if room does not exists.
     */
    public String getPassword(EntityBareJid room) {
        RoomChat roomChat = getRoomChat(room);
        if (roomChat == null) {
            return "";
        }
        return roomChat.getPassword();
    }

    /**
     * @return list of occupants or empty list.
     */
    public Collection<Occupant> getOccupants(EntityBareJid room) {
        RoomChat roomChat = getRoomChat(room);
        if (roomChat == null) {
            return Collections.emptyList();
        }
        return roomChat.getOccupants();
    }


    /**
     * Creates or updates existed room.
     */
    public void createRoom(EntityBareJid room, Resourcepart nickname,
                           String password, boolean join) {
    }


    /**
     * @return Whether room is disabled.
     */
    public boolean isDisabled(final EntityBareJid room) {
        RoomChat roomChat = getRoomChat(room);
        return roomChat == null || roomChat.getState() == RoomState.unavailable;
    }

    /**
     * @return Whether connected is establish or connection is in progress.
     */
    public boolean inUse(final SmartIMClient account, final EntityBareJid room) {
        RoomChat roomChat = getRoomChat(room);
        return roomChat != null && roomChat.getState().inUse();
    }

    public boolean isOwner(String affiliation) {
        return MUCAffiliation.owner.name().equals(affiliation);
    }

    public boolean isAdmin(String affiliation) {
        return MUCAffiliation.admin.name().equals(affiliation);
    }

    public boolean isVisitor(String role) {
        return MUCRole.visitor.name().equals(role);
    }

    public boolean isParticipant(String role) {
        return MUCRole.participant.name().equals(role);
    }
}
