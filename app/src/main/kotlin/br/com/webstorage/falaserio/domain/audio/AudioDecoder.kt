package br.com.webstorage.falaserio.domain.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utilitário para decodificar arquivos de áudio do dispositivo (MP3, M4A, AAC, etc.)
 * para o formato WAV PCM mono de 16-bit a 44.1kHz exigido pelo VsaAnalyzer.
 */
object AudioDecoder {
    private const val TIMEOUT_US = 10000L

    suspend fun decodeToWav(context: Context, uri: Uri, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        val tempPcmFile = File(context.cacheDir, "decoded_temp_${System.currentTimeMillis()}.pcm")
        var fos: FileOutputStream? = null

        try {
            extractor.setDataSource(context, uri, null)
            val trackIndex = selectAudioTrack(extractor)
            if (trackIndex < 0) return@withContext false

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return@withContext false

            val inputSampleRate = if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            } else {
                44100
            }

            val inputChannelCount = if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            } else {
                1
            }

            codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            fos = FileOutputStream(tempPcmFile)

            val info = MediaCodec.BufferInfo()
            var isInputEOS = false
            var isOutputEOS = false

            while (!isOutputEOS) {
                if (!isInputEOS) {
                    val inputBufferIndex = codec.dequeueInputBuffer(TIMEOUT_US)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                        if (inputBuffer != null) {
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inputBufferIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                isInputEOS = true
                            } else {
                                codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, extractor.sampleTime, 0)
                                extractor.advance()
                            }
                        }
                    }
                }

                val outputBufferIndex = codec.dequeueOutputBuffer(info, TIMEOUT_US)
                if (outputBufferIndex >= 0) {
                    val outputBuffer = codec.getOutputBuffer(outputBufferIndex)
                    if (outputBuffer != null && info.size > 0) {
                        val chunk = ByteArray(info.size)
                        outputBuffer.position(info.offset)
                        outputBuffer.get(chunk)
                        fos.write(chunk)
                    }

                    codec.releaseOutputBuffer(outputBufferIndex, false)

                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        isOutputEOS = true
                    }
                }
            }

            fos.close()
            fos = null

            // Processar PCM e exportar WAV
            processPcmAndSaveWav(tempPcmFile, outputFile, inputSampleRate, inputChannelCount)
            tempPcmFile.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            tempPcmFile.delete()
            false
        } finally {
            try {
                extractor.release()
            } catch (e: Exception) {}
            try {
                codec?.stop()
                codec?.release()
            } catch (e: Exception) {}
            try {
                fos?.close()
            } catch (e: Exception) {}
        }
    }

    private fun selectAudioTrack(extractor: MediaExtractor): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("audio/")) {
                return i
            }
        }
        return -1
    }

    private fun processPcmAndSaveWav(
        pcmFile: File,
        wavFile: File,
        inputSampleRate: Int,
        inputChannelCount: Int
    ) {
        val targetSampleRate = 44100
        val tempProcessedPcm = File(pcmFile.parentFile, "processed_temp_${System.currentTimeMillis()}.pcm")

        val inputBytes = pcmFile.readBytes()
        if (inputBytes.isEmpty()) return

        val inputShorts = ShortArray(inputBytes.size / 2)
        ByteBuffer.wrap(inputBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputShorts)

        // Mixar para Mono
        val monoShorts = if (inputChannelCount > 1) {
            val outputSize = inputShorts.size / inputChannelCount
            val result = ShortArray(outputSize)
            for (i in 0 until outputSize) {
                var sum = 0
                for (c in 0 until inputChannelCount) {
                    sum += inputShorts[i * inputChannelCount + c]
                }
                result[i] = (sum / inputChannelCount).toShort()
            }
            result
        } else {
            inputShorts
        }

        // Resampar usando Interpolação Linear
        val resampledShorts = if (inputSampleRate != targetSampleRate && inputSampleRate > 0) {
            val ratio = inputSampleRate.toDouble() / targetSampleRate
            val outputSize = (monoShorts.size / ratio).toInt()
            val result = ShortArray(outputSize)
            for (i in 0 until outputSize) {
                val inputIndex = i * ratio
                val index1 = inputIndex.toInt()
                val index2 = (index1 + 1).coerceAtMost(monoShorts.size - 1)
                val weight = inputIndex - index1

                val sample1 = monoShorts[index1].toDouble()
                val sample2 = monoShorts[index2].toDouble()
                result[i] = (sample1 * (1.0 - weight) + sample2 * weight).toInt().toShort()
            }
            result
        } else {
            monoShorts
        }

        val outputBytes = ByteBuffer.allocate(resampledShorts.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in resampledShorts) {
            outputBytes.putShort(sample)
        }
        tempProcessedPcm.writeBytes(outputBytes.array())

        savePcmAsWav(tempProcessedPcm, wavFile, targetSampleRate)
        tempProcessedPcm.delete()
    }

    private fun savePcmAsWav(pcmFile: File, wavFile: File, sampleRate: Int) {
        val pcmDataSize = pcmFile.length()
        val totalDataLen = pcmDataSize + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2

        FileOutputStream(wavFile).use { fos ->
            fos.write("RIFF".toByteArray())
            fos.write(intToByteArray(totalDataLen.toInt()))
            fos.write("WAVE".toByteArray())
            fos.write("fmt ".toByteArray())
            fos.write(intToByteArray(16))
            fos.write(shortToByteArray(1)) // PCM
            fos.write(shortToByteArray(channels.toShort()))
            fos.write(intToByteArray(sampleRate))
            fos.write(intToByteArray(byteRate))
            fos.write(shortToByteArray((channels * 2).toShort()))
            fos.write(shortToByteArray(16)) // 16-bit
            fos.write("data".toByteArray())
            fos.write(intToByteArray(pcmDataSize.toInt()))

            FileInputStream(pcmFile).use { fis ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    fos.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    private fun intToByteArray(value: Int) = byteArrayOf(
        value.toByte(), (value shr 8).toByte(), (value shr 16).toByte(), (value shr 24).toByte()
    )

    private fun shortToByteArray(value: Short) = byteArrayOf(
        value.toByte(), (value.toInt() shr 8).toByte()
    )
}
