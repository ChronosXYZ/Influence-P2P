package io.github.chronosx88.influence.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyPairManager {
    private File keyPairDir;

    public KeyPairManager() {
        this.keyPairDir = new File(AppHelper.getContext().getFilesDir().getAbsoluteFile(), "keyPairs");
    }

    public KeyPair openMainKeyPair() {
        return getKeyPair("mainKeyPair");
    }

    public KeyPair getKeyPair(String keyPairName) {
        KeyPair keyPair = null;
        keyPairName = keyPairName + ".kp";
        File keyPairFile = new File(keyPairDir, keyPairName);
        if (!keyPairFile.exists()) {
            return createKeyPairFile(keyPairFile);
        }
        return openKeyPairFile(keyPairFile);
    }

    private synchronized KeyPair openKeyPairFile(File keyPairFile) {
        KeyPair keyPair = null;
        try {
            FileInputStream inputStream = new FileInputStream(keyPairFile);
            byte[] serializedKeyPair = new byte[(int) keyPairFile.length()];
            inputStream.read(serializedKeyPair);
            inputStream.close();
            keyPair = Serializer.deserializeObject(new String(serializedKeyPair, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    private synchronized KeyPair createKeyPairFile(File keyPairFile) {
        KeyPair keyPair = null;
        try {
            keyPairFile.createNewFile();
            keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            FileOutputStream outputStream = new FileOutputStream(keyPairFile);
            outputStream.write(Serializer.serializeObject(keyPair).getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    public synchronized void saveKeyPair(String keyPairID, KeyPair keyPair) {
        File keyPairFile = new File(keyPairDir, keyPairID + ".kp");
        if(!keyPairFile.exists()) {
            try {
                FileOutputStream outputStream = new FileOutputStream(keyPairFile);
                outputStream.write(Serializer.serializeObject(keyPair).getBytes(StandardCharsets.UTF_8));
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
