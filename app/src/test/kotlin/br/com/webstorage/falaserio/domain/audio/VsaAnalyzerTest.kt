package br.com.webstorage.falaserio.domain.audio

import br.com.webstorage.falaserio.domain.model.VsaMetrics
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Testes unitários para VsaAnalyzer.
 * 
 * Valida cálculos de métricas DSP e tratamento de casos especiais.
 */
class VsaAnalyzerTest {

    private lateinit var analyzer: VsaAnalyzer
    private lateinit var preprocessor: AudioPreprocessor

    @Before
    fun setup() {
        preprocessor = mockk(relaxed = true)
        analyzer = VsaAnalyzer(preprocessor)
    }

    @Test
    fun `analyze returns empty metrics for non-existent file`() = runTest {
        val fakeFile = File("non_existent.wav")
        val result = analyzer.analyze(fakeFile)

        assertEquals(VsaMetrics.empty(), result)
    }

    @Test
    fun `analyze returns empty metrics for empty file`() = runTest {
        val emptyFile = createTempFile("empty", ".wav")
        emptyFile.deleteOnExit()

        val result = analyzer.analyze(emptyFile)

        assertEquals(VsaMetrics.empty(), result)
    }

    @Test
    fun `metrics are within expected ranges for valid audio`() = runTest {
        // Criar arquivo WAV válido mínimo (44 bytes header + alguns samples)
        val tempFile = createTempFile("test", ".wav").apply {
            deleteOnExit()
            writeBytes(createMinimalWavFile())
        }

        val result = analyzer.analyze(tempFile)

        // Verificar ranges das métricas
        assertTrue("Micro-tremor deve estar entre 8-12 Hz", 
            result.microTremor in 8f..12f)
        assertTrue("Pitch variation deve ser >= 0", 
            result.pitchVariation >= 0f)
        assertTrue("Jitter deve ser >= 0", 
            result.jitter >= 0f)
        assertTrue("Shimmer deve ser >= 0", 
            result.shimmer >= 0f)
        assertTrue("HNR deve ser >= 0", 
            result.hnr >= 0f)
        assertTrue("Overall stress deve estar entre 0-100", 
            result.overallStressScore in 0f..100f)
    }

    @Test
    fun `empty metrics has all values at zero or neutral`() {
        val empty = VsaMetrics.empty()

        assertEquals(0f, empty.microTremor, 0.01f)
        assertEquals(0f, empty.pitchVariation, 0.01f)
        assertEquals(0f, empty.jitter, 0.01f)
        assertEquals(0f, empty.shimmer, 0.01f)
        assertEquals(0f, empty.hnr, 0.01f)
        assertEquals(0f, empty.overallStressScore, 0.01f)
    }

    @Test
    fun `stress level returns correct category`() {
        val veryLow = VsaMetrics(overallStressScore = 10f)
        val low = VsaMetrics(overallStressScore = 30f)
        val medium = VsaMetrics(overallStressScore = 50f)
        val high = VsaMetrics(overallStressScore = 70f)
        val veryHigh = VsaMetrics(overallStressScore = 90f)

        assertEquals("Stress Muito Baixo", veryLow.getStressLevel())
        assertEquals("Stress Baixo", low.getStressLevel())
        assertEquals("Stress Médio", medium.getStressLevel())
        assertEquals("Stress Alto", high.getStressLevel())
        assertEquals("Stress Muito Alto", veryHigh.getStressLevel())
    }

    /**
     * Cria um arquivo WAV mínimo válido para testes.
     * Header WAV (44 bytes) + alguns samples de silêncio.
     */
    private fun createMinimalWavFile(): ByteArray {
        val sampleRate = 44100
        val numSamples = 4096 * 3 // 3 frames mínimos
        val dataSize = numSamples * 2 // 16-bit = 2 bytes por sample

        return ByteArray(44 + dataSize).apply {
            // RIFF header
            "RIFF".toByteArray().copyInto(this, 0)
            writeInt32LE(36 + dataSize, 4)
            "WAVE".toByteArray().copyInto(this, 8)

            // fmt chunk
            "fmt ".toByteArray().copyInto(this, 12)
            writeInt32LE(16, 16) // fmt chunk size
            writeInt16LE(1, 20)  // audio format (PCM)
            writeInt16LE(1, 22)  // channels (mono)
            writeInt32LE(sampleRate, 24)
            writeInt32LE(sampleRate * 2, 28) // byte rate
            writeInt16LE(2, 32)  // block align
            writeInt16LE(16, 34) // bits per sample

            // data chunk
            "data".toByteArray().copyInto(this, 36)
            writeInt32LE(dataSize, 40)
            // Samples ficam zerados (silêncio) - já inicializados pelo ByteArray
        }
    }

    private fun ByteArray.writeInt32LE(value: Int, offset: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
        this[offset + 2] = ((value shr 16) and 0xFF).toByte()
        this[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun ByteArray.writeInt16LE(value: Int, offset: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}
