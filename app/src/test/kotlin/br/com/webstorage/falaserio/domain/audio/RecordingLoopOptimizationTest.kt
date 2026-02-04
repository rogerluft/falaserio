package br.com.webstorage.falaserio.domain.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RecordingLoopOptimizationTest {

    @Test
    fun `verify byte buffer logic`() {
        val buffer = shortArrayOf(1, 2, 3, 4, 5)
        val readCount = 5

        // Old way
        val oldByteBuffer = ByteBuffer.allocate(readCount * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until readCount) {
            oldByteBuffer.putShort(buffer[i])
        }
        val oldArray = oldByteBuffer.array()

        // New way (reusable)
        val reusableByteBuffer = ByteBuffer.allocate(100).order(ByteOrder.LITTLE_ENDIAN)
        reusableByteBuffer.clear()
        for (i in 0 until readCount) {
            reusableByteBuffer.putShort(buffer[i])
        }
        val newArray = reusableByteBuffer.array().copyOfRange(0, readCount * 2)

        assertArrayEquals(oldArray, newArray)
    }

    @Test
    fun `verify float array logic`() {
        val buffer = shortArrayOf(16384, 32767, -32768)
        val readCount = 3

        // Old way
        val oldFloatSamples = FloatArray(readCount) { i -> buffer[i].toFloat() / Short.MAX_VALUE }

        // New way
        val reusableFloatSamples = FloatArray(100)
        for (i in 0 until readCount) {
            reusableFloatSamples[i] = buffer[i].toFloat() / Short.MAX_VALUE
        }
        val newFloatSamples = reusableFloatSamples.copyOf(readCount)

        assertArrayEquals(oldFloatSamples, newFloatSamples, 0.0001f)
    }
}
