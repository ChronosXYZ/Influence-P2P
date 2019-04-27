/*
 * Copyright (C) 2019 ChronosX88
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.chronosx88.influence.helpers

import com.esotericsoftware.kryo.serializers.JavaSerializer
import net.tomp2p.connection.SignatureFactory
import net.tomp2p.dht.Storage
import net.tomp2p.peers.Number160
import net.tomp2p.peers.Number320
import net.tomp2p.peers.Number480
import net.tomp2p.peers.Number640
import net.tomp2p.storage.Data
import org.mapdb.BTreeMap
import org.mapdb.DB
import org.mapdb.DBMaker
import org.mapdb.serializer.SerializerJava
import java.io.File
import java.security.PublicKey
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList


class StorageMapDB(peerId: Number160, path : File, signatureFactory: SignatureFactory) : Storage {
    // Core
    private val dataMap: BTreeMap<Number640, Data>
    // Maintenance
    private val timeoutMap: BTreeMap<Number640, Long>
    private val timeoutMapRev: BTreeMap<Long, Set<Number640>>
    // Protection
    private val protectedDomainMap: BTreeMap<Number320, PublicKey>
    private val protectedEntryMap: BTreeMap<Number480, PublicKey>
    // Responsibility
    private val responsibilityMap: BTreeMap<Number160, Number160>
    private val responsibilityMapRev: BTreeMap<Number160, Set<Number160>>

    private val db: DB = DBMaker.fileDB(path).closeOnJvmShutdown().make()
    private val storageCheckIntervalMillis: Int = 60 * 1000

    init {
        val dataSerializer = DataSerializerMapDB(signatureFactory)
        //val kryoSerializer = KryoSerializer() // We use this because Elsa serializer works very poor
        val javaSerializer = SerializerJava()
        dataMap = db
                .treeMap("dataMap_$peerId")
                .valueSerializer(dataSerializer)
                .createOrOpen() as BTreeMap<Number640, Data>
        timeoutMap = db
                .treeMap("timeoutMap_$peerId")
                .createOrOpen() as BTreeMap<Number640, Long>
        timeoutMapRev = db
                .treeMap("timeoutMapRev_$peerId")
                .createOrOpen() as BTreeMap<Long, Set<Number640>>
        protectedDomainMap = db
                .treeMap("protectedDomainMap_$peerId")
                .createOrOpen() as BTreeMap<Number320, PublicKey>
        protectedEntryMap = db
                .treeMap("protectedEntryMap_$peerId")
                .createOrOpen() as BTreeMap<Number480, PublicKey>
        responsibilityMap = db
                .treeMap("responsibilityMap_$peerId")
                .createOrOpen() as BTreeMap<Number160, Number160>
        responsibilityMapRev = db
                .treeMap("responsibilityMapRev_$peerId")
                .createOrOpen() as BTreeMap<Number160, Set<Number160>>
    }


    override fun contains(key: Number640?): Boolean {
        return dataMap.containsKey(key)
    }

    override fun contains(from: Number640?, to: Number640?): Int {
        return dataMap.subMap(from, true, to, true).size
    }

    override fun findContentForResponsiblePeerID(peerID: Number160?): MutableSet<Number160>? {
        return responsibilityMapRev[peerID] as MutableSet<Number160>?
    }

    override fun findPeerIDsForResponsibleContent(locationKey: Number160?): Number160? {
        return responsibilityMap[locationKey]
    }

    override fun put(key: Number640?, value: Data?): Data? {
        val oldData = dataMap.put(key, value)
        db.commit()
        return oldData
    }

    override fun get(key: Number640?): Data? {
        return dataMap[key]
    }

    override fun remove(key: Number640?, returnData: Boolean): Data? {
        val retVal = dataMap.remove(key)
        db.commit()
        return retVal
    }

    override fun remove(from: Number640?, to: Number640?): NavigableMap<Number640, Data> {
        val tmp = dataMap.subMap(from, true, to, true)
        val retVal = TreeMap<Number640, Data>()
        for(entry : Map.Entry<Number640, Data> in tmp.entries) {
            retVal[entry.key] = entry.value
        }
        tmp.clear()
        db.commit()
        return retVal
    }

    override fun addTimeout(key: Number640, expiration: Long) {
        val oldExpiration = timeoutMap.put(key, expiration)
        putIfAbsent2(expiration, key)
        if (oldExpiration == null) {
            return
        }
        removeRevTimeout(key, oldExpiration)
        db.commit()
    }

    private fun putIfAbsent2(expiration: Long, key: Number640) {
        var timeouts = timeoutMapRev[expiration]
        //var timeouts : MutableSet<Number640> = timeoutMapRev[expiration] as MutableSet<Number640>
        if (timeouts == null) {
            timeouts = Collections.newSetFromMap(ConcurrentHashMap())
        }
        (timeouts as MutableSet).add(key)
        timeoutMapRev[expiration] = timeouts
    }

    private fun removeRevTimeout(key: Number640, expiration: Long?) {
        val tmp = timeoutMapRev[expiration] as MutableSet<Number640>?
        if (tmp != null) {
            tmp.remove(key)
            if (tmp.isEmpty()) {
                timeoutMapRev.remove(expiration)
            } else {
                timeoutMapRev[expiration!!] = tmp
            }
        }
    }

    override fun updateResponsibilities(locationKey: Number160, peerId: Number160?): Boolean {
        val oldPeerID = responsibilityMap.put(locationKey, peerId)
        val hasChanged: Boolean
        if (oldPeerID != null) {
            if (oldPeerID == peerId) {
                hasChanged = false
            } else {
                removeRevResponsibility(oldPeerID, locationKey)
                hasChanged = true
            }
        } else {
            hasChanged = true
        }
        var contentIDs: MutableSet<Number160>? = responsibilityMapRev[peerId] as MutableSet
        if (contentIDs == null) {
            contentIDs = HashSet()
        }
        contentIDs.add(locationKey)
        responsibilityMapRev[peerId] = contentIDs
        db.commit()
        return hasChanged
    }

    private fun removeRevResponsibility(peerId: Number160, locationKey: Number160) {
        val contentIDs = responsibilityMapRev[peerId] as MutableSet?
        if (contentIDs != null) {
            contentIDs.remove(locationKey)
            if (contentIDs.isEmpty()) {
                responsibilityMapRev.remove(peerId)
            } else {
                responsibilityMapRev[peerId] = contentIDs
            }
        }
    }

    override fun protectDomain(key: Number320?, publicKey: PublicKey?): Boolean {
        protectedDomainMap[key] = publicKey
        db.commit()
        return true
    }

    override fun storageCheckIntervalMillis(): Int {
        return storageCheckIntervalMillis
    }

    override fun isDomainProtectedByOthers(key: Number320?, publicKey: PublicKey?): Boolean {
        val other = protectedDomainMap[key] ?: return false
        return other != publicKey
    }

    override fun removeTimeout(key: Number640) {
        val expiration = timeoutMap.remove(key) ?: return
        removeRevTimeout(key, expiration)
        db.commit()
    }

    override fun removeResponsibility(locationKey: Number160) {
        val peerId = responsibilityMap.remove(locationKey)
        if (peerId != null) {
            removeRevResponsibility(peerId, locationKey)
        }
        db.commit()
    }

    override fun protectEntry(key: Number480?, publicKey: PublicKey?): Boolean {
        publicKey ?: return true
        protectedEntryMap[key] = publicKey
        return true
    }

    override fun map(): NavigableMap<Number640, Data> {
        val retVal = TreeMap<Number640, Data>()
        for ((key, value) in dataMap) {
            retVal[key] = value
        }

        return retVal
    }

    override fun isEntryProtectedByOthers(key: Number480?, publicKey: PublicKey?): Boolean {
        val other = protectedEntryMap[key] ?: return false
        return other != publicKey
    }

    override fun subMap(from: Number640?, to: Number640?, limit: Int, ascending: Boolean): NavigableMap<Number640, Data> {
        val tmp = dataMap.subMap(from, true, to, true)
        val retVal = TreeMap<Number640, Data>()
        if (limit < 0) {
            for ((key, value) in if (ascending) tmp else tmp.descendingMap()) {
                retVal[key] = value
            }
        } else {
            val limit1 = Math.min(limit, tmp.size)
            val iterator = if (ascending)
                tmp.entries.iterator()
            else
                tmp.descendingMap().entries.iterator()
            var i = 0
            while (iterator.hasNext() && i < limit1) {
                val entry = iterator.next()
                retVal[entry.key] = entry.value
                i++
            }
        }
        return retVal
    }

    override fun subMapTimeout(to: Long): MutableCollection<Number640> {
        val tmp = timeoutMapRev.subMap(0L, to)
        val toRemove = ArrayList<Number640>()
        for (set in tmp.values) {
            toRemove.addAll(set)
        }
        return toRemove
    }

    override fun close() {
        db.close()
    }
}