package io.github.chronosx88.influence.helpers;

import net.tomp2p.dht.Storage;

public class JVMShutdownHook extends Thread {
    Storage storage;

    public JVMShutdownHook(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        super.run();
        storage.close();
    }
}
