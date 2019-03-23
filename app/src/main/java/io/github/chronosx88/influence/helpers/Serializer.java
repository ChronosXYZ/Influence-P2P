package io.github.chronosx88.influence.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
    public static byte[] serialize(Object object) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArray);
            objectOutputStream.writeObject(object);;
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray.toByteArray();
    }

    public static Object deserialize(String serializedObject) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedObject.getBytes());
        Object object = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            object = objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static Object deserialize(byte[] serializedObject) {
        if(serializedObject == null)
            return null;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(serializedObject);
        Object object = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            object = objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return object;
    }
}
