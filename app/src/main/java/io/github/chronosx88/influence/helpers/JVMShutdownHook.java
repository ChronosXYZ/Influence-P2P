package io.github.chronosx88.influence.helpers;

import android.util.Log;

import net.tomp2p.dht.Storage;

public class JVMShutdownHook extends Thread {

    Storage storage;

    public JVMShutdownHook(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        super.run();
        Log.d("JVMShutdownHook", "# Closing storage...");
        storage.close();
        Log.d("JVMShutdownHook", "# Storage is closed");
    }

}
