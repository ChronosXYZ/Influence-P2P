package io.github.chronosx88.influence.models;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;

import io.github.chronosx88.influence.helpers.DSAKey;

/**
 * Класс-модель публичного профиля для размещения в DHT-сети
 */
public class PublicUserProfile implements Serializable {
    private String userName;
    private PeerAddress peerAddress;
    private DSAKey publicKey;

    public PublicUserProfile(String userName, PeerAddress peerAddress, DSAKey publicKey) {
        this.userName = userName;
        this.peerAddress = peerAddress;
        this.publicKey = publicKey;
    }

    public String getUserName() {
        return userName;
    }

    public PeerAddress getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(PeerAddress peerAddress) {
        this.peerAddress = peerAddress;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public DSAKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(DSAKey publicKey) {
        this.publicKey = publicKey;
    }
}
