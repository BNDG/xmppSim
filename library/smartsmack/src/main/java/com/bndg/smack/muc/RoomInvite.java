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

import android.content.Intent;

import com.bndg.smack.entity.UserJid;


/**
 * Invite to join the room.
 *
 * @author alexander.ivanov
 */
public class RoomInvite {

    /**
     * JID of entity that sent an invitation.
     */
    private final UserJid inviter;

    /**
     * Text of invitation.
     */
    private final String reason;

    /**
     * Password to be used in connection.
     */
    private final String password;

    public RoomInvite(UserJid user, UserJid inviter, String reason, String password) {
        this.inviter = inviter;
        this.reason = reason == null ? "" : reason;
        this.password = password == null ? "" : password;
    }


    /**
     * @return Text for the confirmation.
     */
    public String getConfirmation() {
        return "";
    }

    public UserJid getInviter() {
        return inviter;
    }

    public String getReason() {
        return reason;
    }

    public String getPassword() {
        return password;
    }

}
