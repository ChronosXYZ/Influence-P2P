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

import io.netty.buffer.Unpooled
import net.tomp2p.connection.SignatureFactory
import net.tomp2p.p2p.PeerBuilder
import net.tomp2p.storage.Data
import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.serializer.GroupSerializerObjectArray
import java.io.*
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.SignatureException


class DataSerializerMapDB(private val signatureFactory: SignatureFactory) : GroupSerializerObjectArray<Data>() {
    private val LOG_TAG = "DataSerializerMapDB"

    override fun serialize(out: DataOutput2, value: Data) {
        val dataOut = DataOutputStream(out)

        val acb = Unpooled.buffer()
        // store data to disk
        // header first
        if(value.publicKey()!= null && value.publicKey().equals(PeerBuilder.EMPTY_PUBLIC_KEY)) {
            value.publicKey(null)
        }
        value.encodeHeader(acb, signatureFactory)
        writeData(dataOut, acb.nioBuffers())
        acb.skipBytes(acb.writerIndex())
        // next data - no need to copy to another buffer, just take the data
        // from memory
        writeData(dataOut, value.toByteBuffers())
        // rest
        try {
            value.encodeDone(acb, signatureFactory)
            writeData(dataOut, acb.nioBuffers())
        } catch (e: InvalidKeyException) {
            throw IOException(e)
        } catch (e: SignatureException) {
            throw IOException(e)
        }
    }

    override fun deserialize(input: DataInput2, available: Int): Data {
        val dataInput = DataInputStream(DataInput2.DataInputToStream(input))

        var buf = Unpooled.buffer()
        var data: Data? = null
        while (data == null) {
            buf.writeByte(dataInput.readByte().toInt())
            data = Data.decodeHeader(buf, signatureFactory)
        }
        val len = data.length()
        var me = ByteArray(len)
        dataInput.readFully(me)
        buf = Unpooled.wrappedBuffer(me)
        var retVal = data.decodeBuffer(buf)
        if (!retVal) {
            throw IOException("data could not be read")
        }
        if (data.isSigned) {
            me = ByteArray(signatureFactory.signatureSize())
            dataInput.readFully(me)
            buf = Unpooled.wrappedBuffer(me)
        }
        retVal = data.decodeDone(buf, signatureFactory)
        if (!retVal) {
            throw IOException("signature could not be read")
        }
        return data
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