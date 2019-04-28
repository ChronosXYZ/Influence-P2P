package io.github.chronosx88.influence.helpers

import android.util.Log
import com.sleepycat.bind.EntryBinding
import com.sleepycat.je.DatabaseEntry
import io.netty.buffer.Unpooled
import net.tomp2p.connection.SignatureFactory
import net.tomp2p.storage.Data
import java.io.*
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.SignatureException


class DataSerializer(private val signatureFactory: SignatureFactory) : EntryBinding<Data>, Serializable {
    private val LOG_TAG = "DataSerializer"

    override fun entryToObject(databaseEntry: DatabaseEntry): Data? {
        if (databaseEntry.data == null) {
            return null
        }
        val dataInput = ByteArrayInputStream(databaseEntry.data)
        var buf = Unpooled.buffer()
        var data: Data? = null
        while (data == null) {
            buf.writeByte(dataInput.read())
            data = Data.decodeHeader(buf, signatureFactory)
        }
        val len = data.length()
        var me = ByteArray(len)
        try {
            dataInput.read(me)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        buf = Unpooled.wrappedBuffer(me)
        var retVal = data.decodeBuffer(buf)
        if (!retVal) {
            Log.e(LOG_TAG, "# ERROR: Data could not be deserialized!")
        }
        if (data.isSigned) {
            me = ByteArray(signatureFactory.signatureSize())
            dataInput.read(me)
            buf = Unpooled.wrappedBuffer(me)
        }
        retVal = data.decodeDone(buf, signatureFactory);
        if(!retVal) {
            throw IOException("Signature could not be read!")
        }
        return data
    }

    override fun objectToEntry(data: Data, databaseEntry: DatabaseEntry) {
        val out = ByteArrayOutputStream()
        val acb = Unpooled.buffer()
        // store data to disk
        // header first
        data.encodeHeader(acb, signatureFactory)
        writeData(out, acb.nioBuffers())
        acb.skipBytes(acb.writerIndex())
        // next data - no need to copy to another buffer, just take the data
        // from memory
        writeData(out, data.toByteBuffers())
        // rest
        try {
            data.encodeDone(acb, signatureFactory)
            writeData(out, acb.nioBuffers())
        } catch (e: InvalidKeyException) {
            throw IOException(e)
        } catch (e: SignatureException) {
            throw IOException(e)
        }
        out.flush()
        databaseEntry.data = out.toByteArray()
        out.close()
    }

    @Throws(IOException::class)
    private fun writeData(out: OutputStream, nioBuffers: Array<ByteBuffer>) {
        val length = nioBuffers.size
        for (i in 0 until length) {
            val remaining = nioBuffers[i].remaining()
            if (nioBuffers[i].hasArray()) {
                out.write(nioBuffers[i].array(), nioBuffers[i].arrayOffset(), remaining)
            } else {
                val me = ByteArray(remaining)
                nioBuffers[i].get(me)
                out.write(me)
            }
        }
    }

    companion object {
        private const val serialVersionUID = 1428836065493792295L
    }
}

