package io.github.chronosx88.influence.helpers;

import net.tomp2p.dht.Storage;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number320;
import net.tomp2p.peers.Number480;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.File;
import java.security.PublicKey;
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

public class StorageMVStore implements Storage {
    private MVStore db;

    // Core
    final private MVMap<String, String> dataMap; // <Number 640, Data>

    // Maintenance
    final private MVMap<String, String> timeoutMap; // <Number640, Long>
    final private MVMap<String, String> timeoutMapRev; // <Long, Set<Number640>>

    // Protection
    final private MVMap<String, String> protectedDomainMap; // <Number320, PublicKey>
    final private MVMap<String, String> protectedEntryMap; // <Number480, PublicKey>

    // Responsibility
    final private MVMap<String, String> responsibilityMap; // <Number160, Number160>
    final private MVMap<String, String> responsibilityMapRev; // <Number160, Set<Number160>>

    final private int storageCheckIntervalMillis;

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
        Runtime.getRuntime().addShutdownHook(new JVMShutdownHook(this));
    }

    @Override
    public Data put(Number640 key, Data value) {
        Data oldData = Serializer.deserializeObject(dataMap.put(Serializer.serializeObject(key), Serializer.serializeObject(value)));
        db.commit();
        return oldData;
    }

    @Override
    public Data get(Number640 key) {
        return Serializer.deserializeObject(dataMap.get(Serializer.serializeObject(key)));
    }

    @Override
    public boolean contains(Number640 key) {
        return dataMap.containsKey(Serializer.serializeObject(key));
    }

    @Override
    public int contains(Number640 from, Number640 to) {
        TreeMap<Number640, Data> tmp = new TreeMap<>();
        for (Map.Entry<String, String> entry: dataMap.entrySet()) {
            tmp.put(Serializer.deserializeObject(entry.getKey()), Serializer.deserializeObject(entry.getValue()));
        }

        return tmp.subMap(from, true, to, true).size();
    }

    @Override
    public Data remove(Number640 key, boolean returnData) {
        Data retVal = Serializer.deserializeObject(dataMap.remove(Serializer.serializeObject(key)));
        db.commit();
        return retVal;
    }

    @Override
    public NavigableMap<Number640, Data> remove(Number640 from, Number640 to) {
        TreeMap<Number640, Data> tmp = new TreeMap<>();
        for (Map.Entry<String, String> entry: dataMap.entrySet()) {
            tmp.put(Serializer.deserializeObject(entry.getKey()), Serializer.deserializeObject(entry.getValue()));
        }

        NavigableMap<Number640, Data> tmpSubMap = tmp.subMap(from, true, to, true);
        for (Map.Entry<Number640, Data> entry : tmpSubMap.entrySet()) {
            dataMap.remove(Serializer.serializeObject(entry.getKey()));
        }
        db.commit();

        return tmpSubMap;
    }

    @Override
    public NavigableMap<Number640, Data> subMap(Number640 from, Number640 to, int limit, boolean ascending) {
        TreeMap<Number640, Data> tmpDataMap = new TreeMap<>();
        for (Map.Entry<String, String> entry: dataMap.entrySet()) {
            tmpDataMap.put(Serializer.deserializeObject(entry.getKey()), Serializer.deserializeObject(entry.getValue()));
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
        for(final Map.Entry<String, String> entry:dataMap.entrySet()) {
            retVal.put(Serializer.deserializeObject(entry.getKey()), Serializer.deserializeObject(entry.getValue()));
        }

        return retVal;
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public void addTimeout(Number640 key, long expiration) {
        Long oldExpiration = Serializer.deserializeObject(timeoutMap.put(Serializer.serializeObject(key), Serializer.serializeObject(expiration)));
        putIfAbsent2(expiration, key);
        if (oldExpiration == null) {
            return;
        }
        removeRevTimeout(key, oldExpiration);
        db.commit();
    }

    private void putIfAbsent2(long expiration, Number640 key) {
        Set<Number640> timeouts = Serializer.deserializeObject(timeoutMapRev.get(Serializer.serializeObject(expiration)));
        if(timeouts == null) {
            timeouts = Collections.newSetFromMap(new ConcurrentHashMap<Number640, Boolean>());
        }
        timeouts.add(key);
        timeoutMapRev.put(Serializer.serializeObject(expiration), Serializer.serializeObject(timeouts));
    }

    private void removeRevTimeout(Number640 key, Long expiration) {
        Set<Number640> tmp = Serializer.deserializeObject(timeoutMapRev.get(Serializer.serializeObject(expiration)));
        if (tmp != null) {
            tmp.remove(key);
            if (tmp.isEmpty()) {
                timeoutMapRev.remove(expiration);
            } else {
                timeoutMapRev.put(Serializer.serializeObject(expiration), Serializer.serializeObject(tmp));
            }
        }
    }

    @Override
    public void removeTimeout(Number640 key) {
        Long expiration = Serializer.deserializeObject(timeoutMap.remove(Serializer.serializeObject(key)));
        if (expiration == null) {
            return;
        }
        removeRevTimeout(key, expiration);
        db.commit();
    }

    @Override
    public Collection<Number640> subMapTimeout(long to) {
        TreeMap<Long, Set<Number640>> tmpTimeoutMapRev = new TreeMap<>();
        for (Map.Entry<String, String> entry: timeoutMapRev.entrySet()) {
            tmpTimeoutMapRev.put(Serializer.deserializeObject(entry.getKey()), Serializer.deserializeObject(entry.getValue()));
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
        protectedDomainMap.put(Serializer.serializeObject(key), Serializer.serializeObject(publicKey));
        return true;
    }

    @Override
    public boolean isDomainProtectedByOthers(Number320 key, PublicKey publicKey) {
        PublicKey other = Serializer.deserializeObject(protectedDomainMap.get(Serializer.serializeObject(key)));
        if (other == null) {
            return false;
        }
        return !other.equals(publicKey);
    }

    @Override
    public boolean protectEntry(Number480 key, PublicKey publicKey) {
        protectedEntryMap.put(Serializer.serializeObject(key), Serializer.serializeObject(publicKey));
        return true;
    }

    @Override
    public boolean isEntryProtectedByOthers(Number480 key, PublicKey publicKey) {
        PublicKey other = Serializer.deserializeObject(protectedEntryMap.get(Serializer.serializeObject(key)));
        if (other == null) {
            return false;
        }
        return !other.equals(publicKey);
    }

    @Override
    public Number160 findPeerIDsForResponsibleContent(Number160 locationKey) {
        return Serializer.deserializeObject(responsibilityMap.get(Serializer.serializeObject(locationKey)));
    }

    @Override
    public Collection<Number160> findContentForResponsiblePeerID(Number160 peerID) {
        return Serializer.deserializeObject(responsibilityMapRev.get(Serializer.serializeObject(peerID)));
    }

    @Override
    public boolean updateResponsibilities(Number160 locationKey, Number160 peerId) {
        final Number160 oldPeerID = Serializer.deserializeObject(responsibilityMap.put(Serializer.serializeObject(locationKey), Serializer.serializeObject(peerId)));
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
        Set<Number160> contentIDs = Serializer.deserializeObject(responsibilityMapRev.get(Serializer.serializeObject(peerId)));
        if(contentIDs == null) {
            contentIDs = new HashSet<Number160>();
        }
        contentIDs.add(locationKey);
        responsibilityMapRev.put(Serializer.serializeObject(peerId), Serializer.serializeObject(contentIDs));
        db.commit();
        return hasChanged;
    }

    @Override
    public void removeResponsibility(Number160 locationKey) {

    }

    private void removeRevResponsibility(Number160 peerId, Number160 locationKey) {
        Set<Number160> contentIDs = Serializer.deserializeObject(responsibilityMapRev.get(Serializer.serializeObject(peerId)));
        if (contentIDs != null) {
            contentIDs.remove(locationKey);
            if (contentIDs.isEmpty()) {
                responsibilityMapRev.remove(peerId);
            } else {
                responsibilityMapRev.put(Serializer.serializeObject(peerId), Serializer.serializeObject(contentIDs));
            }
        }
    }
}
