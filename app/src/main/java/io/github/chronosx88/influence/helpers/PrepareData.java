package io.github.chronosx88.influence.helpers;

import org.springframework.security.crypto.codec.Base64;

import java.io.Serializable;

public class PrepareData {
    public static <T> String prepareToStore(T object) {
        if(object instanceof Serializable) {
            byte[] serializedObject = Serializer.serialize(object);
            return new String(Base64.encode(serializedObject));
        }
        return null;
    }

    public static Object prepareFromStore(String object) {
        return Serializer.deserialize(Base64.decode(object.getBytes()));
    }
}
