package io.github.chronosx88.influence.helpers;
/*
import net.tomp2p.connection.SignatureFactory;
import net.tomp2p.dht.Storage;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number320;
import net.tomp2p.peers.Number480;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;

import org.mapdb.DB;
import org.mapdb.DBMaker;

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
import java.util.concurrent.ConcurrentNavigableMap;

public class StorageMapDB implements Storage {
    // Core
    final private NavigableMap<Number640, Data> dataMap;
    // Maintenance
    final private Map<Number640, Long> timeoutMap;
    final private ConcurrentNavigableMap<Long, Set<Number640>> timeoutMapRev;
    // Protection
    final private Map<Number320, PublicKey> protectedDomainMap;
    final private Map<Number480, PublicKey> protectedEntryMap;
    // Responsibility
    final private Map<Number160, Number160> responsibilityMap;
    final private Map<Number160, Set<Number160>> responsibilityMapRev;

    final private DB db;

    final private int storageCheckIntervalMillis;

    //for full control
    public StorageMapDB(DB db, Number160 peerId, File path, SignatureFactory signatureFactory, int storageCheckIntervalMillis) {
        this.db = db;
        DataSerializerEx dataSerializer = new DataSerializerEx(path, signatureFactory);
        this.dataMap = db.createTreeMap("dataMap_" + peerId.toString()).valueSerializer(dataSerializer).makeOrGet();
        this.timeoutMap = db.createTreeMap("timeoutMap_" + peerId.toString()).makeOrGet();
        this.timeoutMapRev = db.createTreeMap("timeoutMapRev_" + peerId.toString()).makeOrGet();
        this.protectedDomainMap = db.createTreeMap("protectedDomainMap_" + peerId.toString()).makeOrGet();
        this.protectedEntryMap = db.createTreeMap("protectedEntryMap_" + peerId.toString()).makeOrGet();
        this.responsibilityMap = db.createTreeMap("responsibilityMap_" + peerId.toString()).makeOrGet();
        this.responsibilityMapRev = db.createTreeMap("responsibilityMapRev_" + peerId.toString()).makeOrGet();
        this.storageCheckIntervalMillis = storageCheckIntervalMillis;
    }

    //set parameter to a reasonable default
    public StorageMapDB(Number160 peerId, File path, SignatureFactory signatureFactory) {
        this(DBMaker.newFileDB(new File(path, "coreDB")).closeOnJvmShutdown().make(),
                peerId, path, signatureFactory, 60 * 1000);
    }

    @Override
    public Data put(Number640 key, Data value) {
        Data oldData = dataMap.put(key, value);
        db.commit();
        return oldData;
    }

    @Override
    public Data get(Number640 key) {
        return dataMap.get(key);
    }

    @Override
    public boolean contains(Number640 key) {
        return dataMap.containsKey(key);
    }

    @Override
    public int contains(Number640 from, Number640 to) {
        NavigableMap<Number640, Data> tmp = dataMap.subMap(from, true, to, true);
        return tmp.size();
    }

    @Override
    public Data remove(Number640 key, boolean returnData) {
        Data retVal = dataMap.remove(key);
        db.commit();
        return retVal;
    }

    @Override
    public NavigableMap<Number640, Data> remove(Number640 from, Number640 to) {
        NavigableMap<Number640, Data> tmp = dataMap.subMap(from, true, to, true);
        final NavigableMap<Number640, Data> retVal = new TreeMap<Number640, Data>();
        for(final Map.Entry<Number640, Data> entry:tmp.entrySet()) {
            retVal.put(entry.getKey(), entry.getValue());
        }

        tmp.clear();
        db.commit();
        return retVal;
    }

    @Override
    public NavigableMap<Number640, Data> subMap(Number640 from, Number640 to, int limit, boolean ascending) {
        NavigableMap<Number640, Data> tmp = dataMap.subMap(from, true, to, true);
        final NavigableMap<Number640, Data> retVal = new TreeMap<Number640, Data>();
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
        final NavigableMap<Number640, Data> retVal = new TreeMap<Number640, Data>();
        for(final Map.Entry<Number640, Data> entry:dataMap.entrySet()) {
            retVal.put(entry.getKey(), entry.getValue());
        }

        return retVal;
    }

    // Maintenance
    @Override
    public void addTimeout(Number640 key, long expiration) {
        Long oldExpiration = timeoutMap.put(key, expiration);
        putIfAbsent2(expiration, key);
        if (oldExpiration == null) {
            return;
        }
        removeRevTimeout(key, oldExpiration);
        db.commit();
    }

    private void putIfAbsent2(long expiration, Number640 key) {
        Set<Number640> timeouts = timeoutMapRev.get(expiration);
        if(timeouts == null) {
            timeouts = Collections.newSetFromMap(new ConcurrentHashMap<Number640, Boolean>());
        }
        timeouts.add(key);
        timeoutMapRev.put(expiration, timeouts);
    }

    @Override
    public void removeTimeout(Number640 key) {
        Long expiration = timeoutMap.remove(key);
        if (expiration == null) {
            return;
        }
        removeRevTimeout(key, expiration);
        db.commit();
    }

    private void removeRevTimeout(Number640 key, Long expiration) {
        Set<Number640> tmp = timeoutMapRev.get(expiration);
        if (tmp != null) {
            tmp.remove(key);
            if (tmp.isEmpty()) {
                timeoutMapRev.remove(expiration);
            } else {
                timeoutMapRev.put(expiration, tmp);
            }
        }
    }

    @Override
    public Collection<Number640> subMapTimeout(long to) {
        SortedMap<Long, Set<Number640>> tmp = timeoutMapRev.subMap(0L, to);
        Collection<Number640> toRemove = new ArrayList<Number640>();
        for (Set<Number640> set : tmp.values()) {
            toRemove.addAll(set);
        }
        return toRemove;
    }



    // Responsibility
    @Override
    public Number160 findPeerIDsForResponsibleContent(Number160 locationKey) {
        return responsibilityMap.get(locationKey);
    }

    @Override
    public Collection<Number160> findContentForResponsiblePeerID(Number160 peerID) {
        return responsibilityMapRev.get(peerID);
    }

    @Override
    public boolean updateResponsibilities(Number160 locationKey, Number160 peerId) {
        final Number160 oldPeerID = responsibilityMap.put(locationKey, peerId);
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
        Set<Number160> contentIDs = responsibilityMapRev.get(peerId);
        if(contentIDs == null) {
            contentIDs = new HashSet<Number160>();
        }
        contentIDs.add(locationKey);
        responsibilityMapRev.put(peerId, contentIDs);
        db.commit();
        return hasChanged;
    }

    @Override
    public void removeResponsibility(Number160 locationKey) {
        final Number160 peerId = responsibilityMap.remove(locationKey);
        if(peerId != null) {
            removeRevResponsibility(peerId, locationKey);
        }
        db.commit();
    }

    private void removeRevResponsibility(Number160 peerId, Number160 locationKey) {
        Set<Number160> contentIDs = responsibilityMapRev.get(peerId);
        if (contentIDs != null) {
            contentIDs.remove(locationKey);
            if (contentIDs.isEmpty()) {
                responsibilityMapRev.remove(peerId);
            } else {
                responsibilityMapRev.put(peerId, contentIDs);
            }
        }
    }

    // Misc
    @Override
    public void close() {
        db.close();
    }

    // Protection Domain
    @Override
    public boolean protectDomain(Number320 key, PublicKey publicKey) {
        protectedDomainMap.put(key, publicKey);
        return true;
    }

    @Override
    public boolean isDomainProtectedByOthers(Number320 key, PublicKey publicKey) {
        PublicKey other = protectedDomainMap.get(key);
        if (other == null) {
            return false;
        }
        return !other.equals(publicKey);
    }

    // Protection Entry
    @Override
    public boolean protectEntry(Number480 key, PublicKey publicKey) {
        protectedEntryMap.put(key, publicKey);
        return true;
    }

    @Override
    public boolean isEntryProtectedByOthers(Number480 key, PublicKey publicKey) {
        PublicKey other = protectedEntryMap.get(key);
        if (other == null) {
            return false;
        }
        return !other.equals(publicKey);
    }

    @Override
    public int storageCheckIntervalMillis() {
        return storageCheckIntervalMillis;
    }
}
*/
