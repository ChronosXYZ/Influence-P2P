package io.github.chronosx88.influence;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import net.tomp2p.connection.RSASignatureFactory;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.Storage;
import net.tomp2p.dht.StorageLayer;
import net.tomp2p.dht.StorageMemory;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number320;
import net.tomp2p.peers.Number480;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageDisk;

import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private PeerDHT peerDHT;
    private Number160 peerID;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getPreferences(MODE_PRIVATE);

        if(checkFirstRun()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("peerID", UUID.randomUUID().toString());
            editor.apply();
        } else {
            peerID = Number160.createHash(preferences.getString("peerID", "0"));
        }

        try {
            peerDHT = new PeerBuilderDHT(
                    new PeerBuilder(peerID)
                            .ports(7243)
                            .behindFirewall(true)
                            .start()
                    )
                    .storage(new StorageDisk(peerID, getFilesDir(), new RSASignatureFactory()))
                    .start();
            InetAddress address = Inet4Address.getByName("192.168.0.82");
            FutureDiscover futureDiscover = peerDHT.peer().discover().inetAddress( address ).ports( 7243 ).start();
            futureDiscover.awaitUninterruptibly();
            FutureBootstrap futureBootstrap = peerDHT.peer().bootstrap().inetAddress( address ).ports( 7243 ).start();
            futureBootstrap.awaitUninterruptibly();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean checkFirstRun() {
        if (preferences.getBoolean("firstRun", true)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("firstRun", false);
            editor.apply();
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.wtf("MainActivity", "onStop");
        peerDHT.shutdown();
    }
}
