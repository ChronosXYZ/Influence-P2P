package io.github.chronosx88.influence.helpers;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer<T> implements EntryBinding<T> {
    public byte[] serialize(T object) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArray);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray.toByteArray();
    }

    public T deserialize(byte[] serializedObject) {
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
        return (T) object;
    }

    @Override
    public T entryToObject(DatabaseEntry databaseEntry) {
        return deserialize(databaseEntry.getData());
    }

    @Override
    public void objectToEntry(T object, DatabaseEntry databaseEntry) {
        databaseEntry.setData(serialize(object));
    }
}
