package io.github.chronosx88.influence;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import io.github.chronosx88.influence.helpers.StorageMVStore;

public class MainActivity extends AppCompatActivity {

    private PeerDHT peerDHT;
    private Number160 peerID;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        org.apache.log4j.BasicConfigurator.configure();
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("main_config", MODE_PRIVATE);

        if(checkFirstRun()) {
            SharedPreferences.Editor editor = preferences.edit();
            String uuid = UUID.randomUUID().toString();
            editor.putString("peerID", uuid);
            editor.apply();
        }

        peerID = Number160.createHash(preferences.getString("peerID", "0"));

        try {
            peerDHT = new PeerBuilderDHT(
                    new PeerBuilder(peerID)
                            .ports(7243)
                            .behindFirewall(true)
                            .start()
                    )
                    .storage(new StorageMVStore(peerID, getFilesDir()))
                    .start();
            InetAddress address = Inet4Address.getByName("*IP*");
            FutureDiscover futureDiscover = peerDHT.peer().discover().inetAddress( address ).ports( 7243 ).start();
            futureDiscover.awaitUninterruptibly();
            FutureBootstrap futureBootstrap = peerDHT.peer().bootstrap().inetAddress( address ).ports( 7243 ).start();
            futureBootstrap.awaitUninterruptibly();
            Log.d("", futureBootstrap.failedReason());
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
    protected void onDestroy() {
        super.onDestroy();
        peerDHT.shutdown();
    }
}
