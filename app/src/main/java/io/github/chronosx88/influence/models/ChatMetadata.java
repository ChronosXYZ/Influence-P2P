package io.github.chronosx88.influence.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatMetadata implements Serializable {
    private String name;
    private ArrayList<String> admins;
    private ArrayList<String> banned;

    public ChatMetadata(String name, ArrayList<String> admins, ArrayList<String> banned) {
        this.name = name;
        this.admins = admins;
        this.banned = banned;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getAdmins() {
        return admins;
    }

    public ArrayList<String> getBanned() {
        return banned;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAdmins(ArrayList<String> admins) {
        this.admins = admins;
    }

    public void setBanned(ArrayList<String> banned) {
        this.banned = banned;
    }
}
