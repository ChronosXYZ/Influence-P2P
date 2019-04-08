package io.github.chronosx88.influence.helpers.comparators;

import java.io.Serializable;
import java.util.Comparator;

import io.github.chronosx88.influence.helpers.Serializer;

public class CompareLong implements Comparator<byte[]>, Serializable {
    @Override
    public int compare(byte[] o1, byte[] o2) {
        Serializer<Long> serializer = new Serializer<>();
        Long num1 = serializer.deserialize(o1);
        Long num2 = serializer.deserialize(o2);
        return num1.compareTo(num2);
    }
}
