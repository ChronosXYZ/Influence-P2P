/*
 * Copyright (C) 2019 ChronosX88
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chronosx88.influence.models;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.List;

import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class GenericDialog implements IDialog {
    private String dialogID;
    private String dialogPhoto = "";
    private String dialogName;
    private List<GenericUser> users;
    private IMessage lastMessage;
    private int unreadMessagesCount;

    public GenericDialog(ChatEntity chatEntity) {
        dialogID = chatEntity.jid;
        dialogName = chatEntity.chatName;
        users = new ArrayList<>();
        unreadMessagesCount = chatEntity.unreadMessagesCount;
    }

    @Override
    public String getId() {
        return dialogID;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(IMessage message) {
        lastMessage = message;
    }

    @Override
    public int getUnreadCount() {
        return unreadMessagesCount;
    }
}
