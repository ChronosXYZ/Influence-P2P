package io.github.chronosx88.influence.helpers.comparators;

import net.tomp2p.peers.Number640;

import java.io.Serializable;
import java.util.Comparator;

import io.github.chronosx88.influence.helpers.Serializer;

public class CompareNumber640 implements Comparator<byte[]>, Serializable {
    @Override
    public int compare(byte[] o1, byte[] o2) {
        Serializer<Number640> serializer = new Serializer<>();
        Number640 num1 = serializer.deserialize(o1);
        Number640 num2 = serializer.deserialize(o2);
        return num1.compareTo(num2);
    }
}


