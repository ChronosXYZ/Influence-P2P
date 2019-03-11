package io.github.chronosx88.influence.helpers;

public class JVMShutdownHook extends Thread {
    StorageMVStore storage;

    public JVMShutdownHook(StorageMVStore storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        super.run();
        storage.close();
    }
}
