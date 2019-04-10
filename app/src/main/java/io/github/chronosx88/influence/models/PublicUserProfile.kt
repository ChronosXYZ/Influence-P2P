package io.github.chronosx88.influence.models

import net.tomp2p.peers.PeerAddress

import java.io.Serializable

/**
 * Класс-модель публичного профиля для размещения в DHT-сети
 */
data class PublicUserProfile(var userName: String?, var peerAddress: PeerAddress?) : Serializable
