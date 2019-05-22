package io.github.chronosx88.influence.logic;

import org.jivesoftware.smack.roster.RosterEntry;

import java.util.List;
import java.util.Set;

import io.github.chronosx88.influence.contracts.CoreContracts;
import io.github.chronosx88.influence.helpers.AppHelper;
import io.github.chronosx88.influence.models.roomEntities.ChatEntity;

public class DialogListLogic implements CoreContracts.IDialogListLogicContract {

    @Override
    public List<ChatEntity> loadLocalChats() {
        return AppHelper.getChatDB().chatDao().getAllChats();
    }

    @Override
    public Set<RosterEntry> getRemoteContacts() {
        if(AppHelper.getXmppConnection() != null) {
            return AppHelper.getXmppConnection().getContactList();
        }
        return null;
    }
}
