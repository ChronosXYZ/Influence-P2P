package io.github.chronosx88.influence.helpers;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class PrepareData {
    public static <T> String prepareToStore(T object) {
        String serializedObject = Serializer.serializeObject(object);
        return Base64.encodeToString(serializedObject.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE);
    }

    public static <T> T prepareFromStore(String object) {
        String decodedString = new String(Base64.decode(object, Base64.URL_SAFE), StandardCharsets.UTF_8);
        return Serializer.deserializeObject(decodedString);
    }
}
