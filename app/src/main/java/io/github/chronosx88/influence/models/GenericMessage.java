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

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import io.github.chronosx88.influence.models.roomEntities.MessageEntity;

public class GenericMessage implements IMessage {
    private long messageID;
    private IUser author;
    private long timestamp;
    private String text;

    public GenericMessage(MessageEntity messageEntity) {
        this.messageID = messageEntity.messageID;
        this.author = new GenericUser(messageEntity.senderJid, messageEntity.senderJid, "");
        this.timestamp = messageEntity.timestamp;
        this.text = messageEntity.text;
    }

    @Override
    public String getId() {
        return String.valueOf(messageID);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return new Date(timestamp);
    }
}
