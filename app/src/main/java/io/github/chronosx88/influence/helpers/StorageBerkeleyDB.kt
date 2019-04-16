package io.github.chronosx88.influence.helpers

import com.sleepycat.collections.StoredSortedMap
import com.sleepycat.je.Database
import com.sleepycat.je.DatabaseConfig
import com.sleepycat.je.Environment
import com.sleepycat.je.EnvironmentConfig
import io.github.chronosx88.influence.helpers.comparators.CompareLong
import io.github.chronosx88.influence.helpers.comparators.CompareNumber640
import net.tomp2p.connection.SignatureFactory
import net.tomp2p.dht.Storage
import net.tomp2p.peers.Number160
import net.tomp2p.peers.Number320
import net.tomp2p.peers.Number480
import net.tomp2p.peers.Number640
import net.tomp2p.storage.Data
import java.io.File
import java.security.PublicKey
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class StorageBerkeleyDB(peerId: Number160, path : File, signatureFactory: SignatureFactory) : Storage {
    // Core
    private val dataMap: StoredSortedMap<Number640, Data>
    // Maintenance
    private val timeoutMap: StoredSortedMap<Number640, Long>
    private val timeoutMapRev: StoredSortedMap<Long, Set<Number640>>
    // Protection
    private val protectedDomainMap: StoredSortedMap<Number320, PublicKey>
    private val protectedEntryMap: StoredSortedMap<Number480, PublicKey>
    // Responsibility
    private val responsibilityMap: StoredSortedMap<Number160, Number160>
    private val responsibilityMapRev: StoredSortedMap<Number160, Set<Number160>>

    private val dataMapDB: Database
    private val timeoutMapDB: Database
    private val timeoutMapRevDB: Database
    private val protectedDomainMapDB: Database
    private val protectedEntryMapDB: Database
    private val responsibilityMapDB: Database
    private val responsibilityMapRevDB: Database


    private val storageCheckIntervalMillis: Int
    private val dbEnvironment: Environment

    init {
        val envConfig = EnvironmentConfig()
        envConfig.allowCreate = true
        dbEnvironment = Environment(path, envConfig)

        val configMap : HashMap<String, com.sleepycat.je.DatabaseConfig> = HashMap()

        val compareNumber640 = CompareNumber640()
        val compareLong = CompareLong()
        configMap["dataMapConfig"] = DatabaseConfig().setBtreeComparator(compareNumber640)
        configMap["dataMapConfig"]!!.allowCreate = true
        configMap["timeoutMapRevConfig"] = DatabaseConfig().setBtreeComparator(compareLong)
        configMap["timeoutMapRevConfig"]!!.allowCreate = true
        configMap["other"] = DatabaseConfig()
        configMap["other"]!!.allowCreate = true

        dataMapDB = dbEnvironment.openDatabase(null, "dataMap_$peerId", configMap["dataMapConfig"])
        timeoutMapDB = dbEnvironment.openDatabase(null, "timeoutMap_$peerId", configMap["other"])
        timeoutMapRevDB = dbEnvironment.openDatabase(null, "timeoutMapRev_$peerId", configMap["timeoutMapRevConfig"])
        protectedDomainMapDB = dbEnvironment.openDatabase(null, "protectedDomainMap_$peerId", configMap["other"])
        protectedEntryMapDB = dbEnvironment.openDatabase(null, "protectedEntryMap_$peerId", configMap["other"])
        responsibilityMapDB = dbEnvironment.openDatabase(null, "responsibilityMap_$peerId", configMap["other"])
        responsibilityMapRevDB = dbEnvironment.openDatabase(null, "responsibilityMapRev_$peerId", configMap["other"])

        storageCheckIntervalMillis = 60 * 1000

        dataMap = StoredSortedMap(dataMapDB, Serializer<Number640>(), DataSerializer(signatureFactory), true)
        timeoutMap = StoredSortedMap(timeoutMapDB, Serializer<Number640>(), Serializer<Long>(), true)
        timeoutMapRev = StoredSortedMap(timeoutMapRevDB, Serializer<Long>(), Serializer<Set<Number640>>(), true)
        protectedDomainMap = StoredSortedMap(protectedDomainMapDB, Serializer<Number320>(), Serializer<PublicKey>(), true)
        protectedEntryMap = StoredSortedMap(protectedEntryMapDB, Serializer<Number480>(), Serializer<PublicKey>(), true)
        responsibilityMap = StoredSortedMap(responsibilityMapDB, Serializer<Number160>(), Serializer<Number160>(), true)
        responsibilityMapRev = StoredSortedMap(responsibilityMapRevDB, Serializer<Number160>(), Serializer<Set<Number160>>(), true)
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
        return dataMap.put(key, value)
    }

    override fun get(key: Number640?): Data? {
        return dataMap[key]
    }

    override fun remove(key: Number640?, returnData: Boolean): Data? {
        return dataMap.remove(key)
    }

    override fun remove(from: Number640?, to: Number640?): NavigableMap<Number640, Data> {
        val tmp = dataMap.subMap(from, true, to, true)
        val retVal = TreeMap<Number640, Data>()
        for(entry : Map.Entry<Number640, Data> in tmp.entries) {
            retVal[entry.key] = entry.value
        }
        tmp.clear()
        return retVal
    }

    override fun addTimeout(key: Number640, expiration: Long) {
        val oldExpiration = timeoutMap.put(key, expiration)
        putIfAbsent2(expiration, key)
        if (oldExpiration == null) {
            return
        }
        removeRevTimeout(key, oldExpiration)
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
        val tmp = timeoutMapRev[expiration] as MutableSet<Number640>
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
        return hasChanged
    }

    private fun removeRevResponsibility(peerId: Number160, locationKey: Number160) {
        val contentIDs = responsibilityMapRev[peerId] as MutableSet
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
    }

    override fun removeResponsibility(locationKey: Number160) {
        val peerId = responsibilityMap.remove(locationKey)
        if (peerId != null) {
            removeRevResponsibility(peerId, locationKey)
        }
    }

    override fun protectEntry(key: Number480?, publicKey: PublicKey?): Boolean {
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
        val descendingMap = TreeMap<Number640, Data>(tmp).descendingMap()
        val retVal = TreeMap<Number640, Data>()
        if (limit < 0) {
            for ((key, value) in if (ascending) tmp else descendingMap) {
                retVal[key] = value
            }
        } else {
            val limit1 = Math.min(limit, tmp.size)
            val iterator = if (ascending)
                tmp.entries.iterator()
            else
                descendingMap.entries.iterator()
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
        dataMapDB.close()
        timeoutMapDB.close()
        timeoutMapRevDB.close()
        protectedDomainMapDB.close()
        protectedEntryMapDB.close()
        responsibilityMapDB.close()
        responsibilityMapRevDB.close()
    }
}
