package io.github.chronosx88.influence.helpers;

import com.google.gson.Gson;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FuturePing;
import net.tomp2p.peers.PeerAddress;

public class P2PUtils {
    private static Gson gson = new Gson();
    private static PeerDHT peerDHT = AppHelper.getPeerDHT();

    public static boolean ping(PeerAddress recipientPeerAddress) {
        // For connection opening
        for (int i = 0; i < 2; i++) {
            peerDHT
                    .peer()
                    .ping()
                    .tcpPing(true)
                    .peerAddress(recipientPeerAddress)
                    .start()
                    .awaitUninterruptibly();
        }

        FuturePing ping = peerDHT
                .peer()
                .ping()
                .tcpPing(true)
                .peerAddress(recipientPeerAddress)
                .start()
                .awaitUninterruptibly();
        return ping.isSuccess();
    }
}
