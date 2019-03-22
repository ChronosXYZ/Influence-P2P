package io.github.chronosx88.influence.helpers;

import android.util.Log;

import net.tomp2p.connection.DSASignatureFactory;
import net.tomp2p.connection.SignatureFactory;
import net.tomp2p.dht.Storage;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number320;
import net.tomp2p.peers.Number480;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.AlternativeCompositeByteBuf;
import net.tomp2p.storage.Data;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static io.github.chronosx88.influence.helpers.Serializer.deserialize;
import static io.github.chronosx88.influence.helpers.Serializer.serialize;

public class StorageMVStore implements Storage {
    private final String LOG_TAG = "StorageMVStore";

    private MVStore db;

    // Core
    final private MVMap<byte[], byte[]> dataMap; // <Number 640, Data>

    // Maintenance
    final private MVMap<byte[], byte[]> timeoutMap; // <Number640, Long>
    final private MVMap<byte[], byte[]> timeoutMapRev; // <Long, Set<Number640>>

    // Protection
    final private MVMap<byte[], byte[]> protectedDomainMap; // <Number320, PublicKey>
    final private MVMap<byte[], byte[]> protectedEntryMap; // <Number480, PublicKey>

    // Responsibility
    final private MVMap<byte[], byte[]> responsibilityMap; // <Number160, Number160>
    final private MVMap<byte[], byte[]> responsibilityMapRev; // <Number160, Set<Number160>>

    final private int storageCheckIntervalMillis;

    final private SignatureFactory signatureFactory;
    final private KeyPairManager keyPairManager;

    public StorageMVStore(Number160 peerID, File path) {
        db = new MVStore.Builder()
                .fileName(path.getAbsolutePath() + "/coreDB.db")
                .open();
        dataMap = db.openMap("dataMap_" + peerID.toString());
        timeoutMap = db.openMap("timeoutMap_" + peerID.toString());
        timeoutMapRev = db.openMap("timeoutMapRev_" + peerID.toString());
        protectedDomainMap = db.openMap("protectedDomainMap_" + peerID.toString());
        protectedEntryMap = db.openMap("protectedEntryMap_" + peerID.toString());
        responsibilityMap = db.openMap("responsibilityMap_" + peerID.toString());
        responsibilityMapRev = db.openMap("responsibilityMapRev_ " + peerID.toString());
        storageCheckIntervalMillis = 60 * 1000;
        signatureFactory = new DSASignatureFactory();
        keyPairManager = new KeyPairManager();
        Runtime.getRuntime().addShutdownHook(new JVMShutdownHook(this));
    }

    @Override
    public Data put(Number640 key, Data value) {
        Data oldData = deserializeData(dataMap.put(serialize(key), serializeData(value)));
        db.commit();
        return oldData;
    }

    @Override
    public Data get(Number640 key) {
        return deserializeData(dataMap.get(serialize(key)));
    }

    @Override
    public boolean contains(Number640 key) {
        return dataMap.containsKey(serialize(key));
    }

    @Override
    public int contains(Number640 from, Number640 to) {
        TreeMap<Number640, Data> tmp = new TreeMap<>();
        for (Map.Entry<byte[], byte[]> entry: dataMap.entrySet()) {
            tmp.put((Number640) deserialize(entry.getKey()), deserializeData(entry.getValue()));
        }

        return tmp.subMap(from, true, to, true).size();
    }

    @Override
    public Data remove(Number640 key, boolean returnData) {
        Data retVal = deserializeData(dataMap.remove(serialize(key)));
        db.commit();
        return retVal;
    }

    @Override
    public NavigableMap<Number640, Data> remove(Number640 from, Number640 to) {
        TreeMap<Number640, Data> tmp = new TreeMap<>();
        for (Map.Entry<byte[], byte[]> entry: dataMap.entrySet()) {
            tmp.put((Number640) deserialize(entry.getKey()), deserializeData(entry.getValue()));
        }

        NavigableMap<Number640, Data> tmpSubMap = tmp.subMap(from, true, to, true);
        for (Map.Entry<Number640, Data> entry : tmpSubMap.entrySet()) {
            dataMap.remove(serialize(entry.getKey()));
        }
        db.commit();

        return tmpSubMap;
    }

    @Override
    public NavigableMap<Number640, Data> subMap(Number640 from, Number640 to, int limit, boolean ascending) {
        TreeMap<Number640, Data> tmpDataMap = new TreeMap<>();
        for (Map.Entry<byte[], byte[]> entry: dataMap.entrySet()) {
            tmpDataMap.put((Number640) deserialize(entry.getKey()), deserializeData(entry.getValue()));
        }

        NavigableMap<Number640, Data> tmp = tmpDataMap.subMap(from, true, to, true);
        final NavigableMap<Number640, Data> retVal = new TreeMap<>();

        if (limit < 0) {
            for(final Map.Entry<Number640, Data> entry:(ascending ? tmp : tmp.descendingMap()).entrySet()) {
                retVal.put(entry.getKey(), entry.getValue());
            }
        } else {
            limit = Math.min(limit, tmp.size());
            Iterator<Map.Entry<Number640, Data>> iterator = ascending ? tmp.entrySet().iterator() : tmp
                    .descendingMap().entrySet().iterator();
            for (int i = 0; iterator.hasNext() && i < limit; i++) {
                Map.Entry<Number640, Data> entry = iterator.next();
                retVal.put(entry.getKey(), entry.getValue());
            }
        }
        return retVal;
    }

    @Override
    public NavigableMap<Number640, Data> map() {
        final NavigableMap<Number640, Data> retVal = new TreeMap<>();
        for(final Map.Entry<byte[], byte[]> entry : dataMap.entrySet()) {
            retVal.put((Number640) deserialize(entry.getKey()), deserializeData(entry.getValue()));
        }

        return retVal;
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public void addTimeout(Number640 key, long expiration) {
        Long oldExpiration = (Long) deserialize(timeoutMap.put(serialize(key), serialize(expiration)));
        putIfAbsent2(expiration, key);
        if (oldExpiration == null) {
            return;
        }
        removeRevTimeout(key, oldExpiration);
        db.commit();
    }

    private void putIfAbsent2(long expiration, Number640 key) {
        Set<Number640> timeouts = (Set<Number640>) deserialize(timeoutMapRev.get(serialize(expiration)));
        if(timeouts == null) {
            timeouts = Collections.newSetFromMap(new ConcurrentHashMap<>());
        }
        timeouts.add(key);
        timeoutMapRev.put(serialize(expiration), serialize(timeouts));
    }

    private void removeRevTimeout(Number640 key, Long expiration) {
        Set<Number640> tmp = (Set<Number640>) deserialize(timeoutMapRev.get(serialize(expiration)));
        if (tmp != null) {
            tmp.remove(key);
            if (tmp.isEmpty()) {
                timeoutMapRev.remove(serialize(expiration));
            } else {
                timeoutMapRev.put(serialize(expiration), serialize(tmp));
            }
        }
    }

    @Override
    public void removeTimeout(Number640 key) {
        Long expiration = (Long) deserialize(timeoutMap.remove(serialize(key)));
        if (expiration == null) {
            return;
        }
        removeRevTimeout(key, expiration);
        db.commit();
    }

    @Override
    public Collection<Number640> subMapTimeout(long to) {
        TreeMap<Long, Set<Number640>> tmpTimeoutMapRev = new TreeMap<>();
        for (Map.Entry<byte[], byte[]> entry: timeoutMapRev.entrySet()) {
            tmpTimeoutMapRev.put( (Long) deserialize(entry.getKey()), (Set<Number640>) deserialize(entry.getValue()));
        }

        SortedMap<Long, Set<Number640>> tmp = tmpTimeoutMapRev.subMap(0L, to);
        Collection<Number640> toRemove = new ArrayList<>();
        for (Set<Number640> set : tmp.values()) {
            toRemove.addAll(set);
        }
        return toRemove;
    }

    @Override
    public int storageCheckIntervalMillis() {
        return this.storageCheckIntervalMillis;
    }

    @Override
    public boolean protectDomain(Number320 key, PublicKey publicKey) {
        protectedDomainMap.put(serialize(key), serialize(publicKey));
        return true;
    }

    @Override
    public boolean isDomainProtectedByOthers(Number320 key, PublicKey publicKey) {
        PublicKey other = (PublicKey) deserialize(protectedDomainMap.get(serialize(key)));
        if (other == null) {
            return false;
        }
        return !other.equals(publicKey);
    }

    @Override
    public boolean protectEntry(Number480 key, PublicKey publicKey) {
        protectedEntryMap.put(serialize(key), serialize(publicKey));
        return true;
    }

    @Override
    public boolean isEntryProtectedByOthers(Number480 key, PublicKey publicKey) {
        PublicKey other = (PublicKey) deserialize(protectedEntryMap.get(serialize(key)));
        if (other == null) {
            return false;
        }
        return !other.equals(publicKey);
    }

    @Override
    public Number160 findPeerIDsForResponsibleContent(Number160 locationKey) {
        return (Number160) deserialize(responsibilityMap.get(serialize(locationKey)));
    }

    @Override
    public Collection<Number160> findContentForResponsiblePeerID(Number160 peerID) {
        return (Collection<Number160>) deserialize(responsibilityMapRev.get(serialize(peerID)));
    }

    @Override
    public boolean updateResponsibilities(Number160 locationKey, Number160 peerId) {
        final Number160 oldPeerID = (Number160) deserialize(responsibilityMap.put(serialize(locationKey), serialize(peerId)));
        final boolean hasChanged;
        if(oldPeerID != null) {
            if(oldPeerID.equals(peerId)) {
                hasChanged = false;
            } else {
                removeRevResponsibility(oldPeerID, locationKey);
                hasChanged = true;
            }
        } else {
            hasChanged = true;
        }
        Set<Number160> contentIDs = (Set<Number160>) deserialize(responsibilityMapRev.get(serialize(peerId)));
        if(contentIDs == null) {
            contentIDs = new HashSet<>();
        }
        contentIDs.add(locationKey);
        responsibilityMapRev.put(serialize(peerId), serialize(contentIDs));
        db.commit();
        return hasChanged;
    }

    @Override
    public void removeResponsibility(Number160 locationKey) {
        final Number160 peerId = new Number160(responsibilityMap.remove(serialize(locationKey)));
        if(peerId != null) {
            removeRevResponsibility(peerId, locationKey);
        }
        db.commit();
    }

    private void removeRevResponsibility(Number160 peerId, Number160 locationKey) {
        Set<Number160> contentIDs = (Set<Number160>) deserialize(responsibilityMapRev.get(serialize(peerId)));
        if (contentIDs != null) {
            contentIDs.remove(locationKey);
            if (contentIDs.isEmpty()) {
                responsibilityMapRev.remove(serialize(peerId));
            } else {
                responsibilityMapRev.put(serialize(peerId), serialize(contentIDs));
            }
        }
    }


    private byte[] serializeData(Data data) {
        KeyPair mainKeyPair = keyPairManager.openMainKeyPair();
        KeyPair forSigningKP = keyPairManager.getKeyPair("mainSigningKeyPair");
        data.sign(forSigningKP).protectEntry(mainKeyPair);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AlternativeCompositeByteBuf acb = AlternativeCompositeByteBuf.compBuffer(AlternativeCompositeByteBuf.UNPOOLED_HEAP);
        try {
            // header first
            data.encodeHeader(acb, signatureFactory);
            writeData(out, acb.nioBuffers());
            acb.skipBytes(acb.writerIndex());
            // next data - no need to copy to another buffer, just take the data
            // from memory
            writeData(out, data.toByteBuffers());
            // rest
            data.encodeDone(acb, signatureFactory);
            writeData(out, acb.nioBuffers());
        } catch (SignatureException | InvalidKeyException | IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void writeData(OutputStream out, ByteBuffer[] nioBuffers) throws IOException {
        final int length = nioBuffers.length;
        for(int i=0;i < length; i++) {
            int remaining = nioBuffers[i].remaining();
            if(nioBuffers[i].hasArray()) {
                out.write(nioBuffers[i].array(), nioBuffers[i].arrayOffset(), remaining);
            } else {
                byte[] me = new byte[remaining];
                nioBuffers[i].get(me);
                out.write(me);
            }
        }
    }

    private Data deserializeData(byte[] serializedData) {
        if(serializedData == null) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(serializedData);
        ByteBuf buf = Unpooled.buffer();
        Data data = null;
        while(data == null) {
            buf.writeByte(in.read());
            data = Data.decodeHeader(buf, signatureFactory);
        }
        int len = data.length();
        byte me[] = new byte[len];
        try {
            in.read(me);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buf = Unpooled.wrappedBuffer(me);
        boolean retVal = data.decodeBuffer(buf);
        if(!retVal) {
            Log.e(LOG_TAG, "# ERROR: Data could not be deserialized!");
        }
        retVal = data.decodeDone(buf, signatureFactory);
        if(!retVal) {
            Log.e(LOG_TAG, "# ERROR: Signature could not be read!");
        }
        return data;
    }
}
