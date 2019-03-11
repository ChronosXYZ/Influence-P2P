package io.github.chronosx88.influence.helpers;

import org.jboss.serial.io.JBossObjectInputStream;
import org.jboss.serial.io.JBossObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Serializer {
    public static <T> String serializeObject(T t) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JBossObjectOutputStream out = new JBossObjectOutputStream(outputStream);
            out.writeObject(t);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return outputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T deserializeObject(String str) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        T obj = null;
        try {
            JBossObjectInputStream in = new JBossObjectInputStream(inputStream);
            obj = (T) in.readObject();
            in.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
