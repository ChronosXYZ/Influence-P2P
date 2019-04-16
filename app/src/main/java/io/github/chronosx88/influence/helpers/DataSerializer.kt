package io.github.chronosx88.influence.helpers

import android.util.Log
import com.sleepycat.bind.EntryBinding
import com.sleepycat.je.DatabaseEntry
import io.netty.buffer.Unpooled
import net.tomp2p.connection.SignatureFactory
import net.tomp2p.storage.AlternativeCompositeByteBuf
import net.tomp2p.storage.Data
import org.mapdb.DataInput2
import java.io.*
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.SignatureException

class DataSerializerEx(private val signatureFactory: SignatureFactory) : EntryBinding<Data>, Serializable {
    private val LOG_TAG = "DataSerializerEx"

    override fun entryToObject(databaseEntry: DatabaseEntry): Data? {
        if (databaseEntry.data == null) {
            return null
        }
        val inputStream = ByteArrayInputStream(databaseEntry.data)
        var buf = Unpooled.buffer()
        var data: Data? = null
        while (data == null) {
            buf.writeByte(inputStream.read())
            data = Data.decodeHeader(buf, signatureFactory)
        }
        val len = data.length()
        var me = ByteArray(len)
        try {
            inputStream.read(me)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        buf = Unpooled.wrappedBuffer(me)
        var retVal = data.decodeBuffer(buf)
        if (!retVal) {
            Log.e(LOG_TAG, "# ERROR: Data could not be deserialized!")
        }
        val dataInput = DataInputStream(inputStream)
        me = ByteArray(signatureFactory.signatureSize())
        dataInput.readFully(me)
        buf = Unpooled.wrappedBuffer(me)
        retVal = data.decodeDone(buf, signatureFactory);
        if(!retVal) {
            throw IOException("signature could not be read")
        }
        return data
    }

    override fun objectToEntry(data: Data, databaseEntry: DatabaseEntry) {
        val out = ByteArrayOutputStream()
        val acb = AlternativeCompositeByteBuf.compBuffer(AlternativeCompositeByteBuf.UNPOOLED_HEAP)
        try {
            // header first
            data.encodeHeader(acb, signatureFactory)
            writeData(out, acb.nioBuffers())
            acb.skipBytes(acb.writerIndex())
            // next data - no need to copy to another buffer, just take the data
            // from memory
            writeData(out, data.toByteBuffers())
        } catch (e: SignatureException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        databaseEntry.data = out.toByteArray()
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

