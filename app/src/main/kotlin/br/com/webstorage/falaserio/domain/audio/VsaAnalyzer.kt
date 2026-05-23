package br.com.webstorage.falaserio.domain.audio

import br.com.webstorage.falaserio.domain.model.VsaMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Analisador VSA (Voice Stress Analysis).
 *
 * Calcula 5 métricas de stress vocal:
 * 1. Micro-Tremor (8-12Hz)
 * 2. Pitch Variation (F0)
 * 3. Jitter
 * 4. Shimmer
 * 5. HNR (Harmonic-to-Noise Ratio)
 *
 * (Tarefa 1.6: Integrado com AudioPreprocessor para filtros de ruído)
 */
@Singleton
class VsaAnalyzer @Inject constructor(
    private val preprocessor: AudioPreprocessor,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) {

    companion object {
        const val SAMPLE_RATE = 44100
        const val FRAME_SIZE = 4096
        const val HOP_SIZE = 2048
        const val MIN_FREQ = 80f  // Hz - mínimo para voz humana
        const val MAX_FREQ = 400f // Hz - máximo para voz humana
    }

    /**
     * Analisa um arquivo WAV e retorna métricas VSA.
     */
    suspend fun analyze(file: File): VsaMetrics = withContext(Dispatchers.Default) {
        val samples = readWavFile(file)
        if (samples.isEmpty()) return@withContext VsaMetrics.empty()

        // Pré-processamento: Filtros e Noise Gate (Tarefa 1.6)
        val processedSamples = preprocessor.preprocess(samples, SAMPLE_RATE)

        // Dividir em frames com overlap usando samples processados
        val frames = extractFrames(processedSamples)
        if (frames.isEmpty()) return@withContext VsaMetrics.empty()

        // Pré-calcular pitches (F0) para evitar redundância
        val pitches = frames.map { detectPitch(it) }

        // Calcular cada métrica
        val microTremor = calculateMicroTremor(frames)
        val pitchVariation = calculatePitchVariation(pitches)
        val jitter = calculateJitter(pitches)
        val shimmer = calculateShimmer(frames)
        val hnr = calculateHNR(frames, pitches)

        // Calcular score geral de stress (0-100)
        val overallStress = calculateOverallStress(
            microTremor, pitchVariation, jitter, shimmer, hnr
        )

        VsaMetrics(
            microTremor = microTremor,
            pitchVariation = pitchVariation,
            jitter = jitter,
            shimmer = shimmer,
            hnr = hnr,
            overallStressScore = overallStress
        )
    }

    /**
     * Lê arquivo WAV e retorna samples normalizados.
     */
    private fun readWavFile(file: File): FloatArray {
        return try {
            RandomAccessFile(file, "r").use { raf ->
                // Pular header WAV (44 bytes)
                raf.seek(44)

                val dataSize = (file.length() - 44).toInt()
                val buffer = ByteArray(dataSize)
                raf.readFully(buffer)

                // Converter bytes para shorts (16-bit PCM)
                val shortBuffer = ByteBuffer.wrap(buffer)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asShortBuffer()

                val samples = FloatArray(shortBuffer.remaining())
                for (i in samples.indices) {
                    samples[i] = shortBuffer.get() / 32768f // Normalizar para -1..1
                }
                samples
            }
        } catch (e: Exception) {
            floatArrayOf()
        }
    }

    /**
     * Extrai frames com overlap do sinal.
     */
    private fun extractFrames(samples: FloatArray): List<FloatArray> {
        val frames = mutableListOf<FloatArray>()
        var offset = 0

        while (offset + FRAME_SIZE <= samples.size) {
            val frame = samples.copyOfRange(offset, offset + FRAME_SIZE)
            // Aplicar janela de Hamming
            applyHammingWindow(frame)
            frames.add(frame)
            offset += HOP_SIZE
        }

        return frames
    }

    /**
     * Aplica janela de Hamming ao frame.
     */
    private fun applyHammingWindow(frame: FloatArray) {
        for (i in frame.indices) {
            val multiplier = 0.54f - 0.46f * cos(2.0 * PI * i / (frame.size - 1)).toFloat()
            frame[i] *= multiplier
        }
    }

    /**
     * Calcula micro-tremor (8-12Hz) via análise de modulação de amplitude.
     */
    private fun calculateMicroTremor(frames: List<FloatArray>): Float {
        // Calcular envelope de amplitude para cada frame
        val envelope = frames.map { frame ->
            sqrt(frame.map { it * it }.average().toFloat())
        }

        if (envelope.size < 10) return 9f // Valor neutro

        // FFT simplificada para detectar frequência dominante no envelope
        val fftSize = 256
        val paddedEnvelope = FloatArray(fftSize)
        for (i in 0 until minOf(envelope.size, fftSize)) {
            paddedEnvelope[i] = envelope[i]
        }

        val spectrum = fft(paddedEnvelope)

        // Taxa de frames por segundo
        val frameRate = SAMPLE_RATE.toFloat() / HOP_SIZE

        // Procurar pico na faixa 8-12Hz
        val minBin = (8f / frameRate * fftSize).toInt().coerceIn(1, fftSize / 2)
        val maxBin = (12f / frameRate * fftSize).toInt().coerceIn(1, fftSize / 2)

        var maxMagnitude = 0f
        var peakBin = minBin
        for (bin in minBin until maxBin) {
            if (spectrum[bin] > maxMagnitude) {
                maxMagnitude = spectrum[bin]
                peakBin = bin
            }
        }

        // Converter bin para frequência
        val frequency = peakBin * frameRate / fftSize
        return frequency.coerceIn(8f, 12f)
    }

    /**
     * Calcula variação de pitch (F0) usando pitches pré-calculados.
     */
    private fun calculatePitchVariation(allPitches: List<Float>): Float {
        val pitches = allPitches.filter { it > 0 }

        if (pitches.size < 2) return 12f // Valor neutro

        // Calcular coeficiente de variação (CV = std / mean * 100)
        val mean = pitches.average().toFloat()
        val variance = pitches.map { (it - mean) * (it - mean) }.average().toFloat()
        val std = sqrt(variance)

        return (std / mean * 100f).coerceIn(0f, 50f)
    }

    /**
     * Detecta pitch via autocorrelação.
     */
    private fun detectPitch(frame: FloatArray): Float {
        val minLag = (SAMPLE_RATE / MAX_FREQ).toInt()
        val maxLag = (SAMPLE_RATE / MIN_FREQ).toInt().coerceAtMost(frame.size / 2)

        var maxCorr = 0f
        var bestLag = minLag

        for (lag in minLag until maxLag) {
            var corr = 0f
            for (i in 0 until frame.size - lag) {
                corr += frame[i] * frame[i + lag]
            }
            if (corr > maxCorr) {
                maxCorr = corr
                bestLag = lag
            }
        }

        return if (maxCorr > 0) SAMPLE_RATE.toFloat() / bestLag else 0f
    }

    /**
     * Calcula Jitter (variação ciclo-a-ciclo do período) usando pitches pré-calculados.
     */
    private fun calculateJitter(allPitches: List<Float>): Float {
        val periods = allPitches.filter { it > 0 }.map { 1000f / it }

        if (periods.size < 3) return 0.8f // Valor neutro

        // Calcular jitter como variação média entre períodos consecutivos
        var jitterSum = 0f
        for (i in 1 until periods.size) {
            jitterSum += abs(periods[i] - periods[i - 1])
        }

        val meanPeriod = periods.average().toFloat()
        val jitter = (jitterSum / (periods.size - 1)) / meanPeriod * 100f

        return jitter.coerceIn(0f, 10f)
    }

    /**
     * Calcula Shimmer (variação ciclo-a-ciclo da amplitude).
     */
    private fun calculateShimmer(frames: List<FloatArray>): Float {
        val amplitudes = frames.map { frame ->
            frame.maxOrNull()?.let { abs(it) } ?: 0f
        }

        if (amplitudes.size < 3) return 2f // Valor neutro

        // Calcular shimmer como variação média entre amplitudes consecutivas
        var shimmerSum = 0f
        for (i in 1 until amplitudes.size) {
            shimmerSum += abs(amplitudes[i] - amplitudes[i - 1])
        }

        val meanAmplitude = amplitudes.average().toFloat()
        if (meanAmplitude == 0f) return 2f

        val shimmer = (shimmerSum / (amplitudes.size - 1)) / meanAmplitude * 100f

        return shimmer.coerceIn(0f, 20f)
    }

    /**
     * Calcula HNR (Harmonic-to-Noise Ratio) em dB usando pitches pré-calculados.
     */
    private fun calculateHNR(frames: List<FloatArray>, allPitches: List<Float>): Float {
        val hnrValues = mutableListOf<Float>()

        for (i in frames.indices) {
            val pitch = allPitches[i]
            if (pitch > 0) {
                val period = (SAMPLE_RATE / pitch).toInt()
                val hnr = calculateFrameHNR(frames[i], period)
                if (hnr.isFinite()) hnrValues.add(hnr)
            }
        }

        if (hnrValues.isEmpty()) return 18f // Valor neutro

        return hnrValues.average().toFloat().coerceIn(0f, 40f)
    }

    /**
     * Calcula HNR para um frame específico.
     */
    private fun calculateFrameHNR(frame: FloatArray, period: Int): Float {
        if (period <= 0 || period >= frame.size / 2) return 18f

        var harmonicEnergy = 0f
        var totalEnergy = 0f

        for (i in 0 until frame.size - period) {
            val harmonic = (frame[i] + frame[i + period]) / 2f
            harmonicEnergy += harmonic * harmonic
            totalEnergy += frame[i] * frame[i]
        }

        if (totalEnergy == 0f) return 18f

        val noiseEnergy = totalEnergy - harmonicEnergy
        if (noiseEnergy <= 0) return 30f

        return 10f * log10(harmonicEnergy / noiseEnergy)
    }

    /**
     * FFT implementation (Cooley-Tukey algorithm).
     * Input signal size must be a power of 2.
     */
    private fun fft(signal: FloatArray): FloatArray {
        val n = signal.size
        // Verifica se n é potência de 2. Se não for, usa DFT (fallback)
        if (n == 0 || (n and (n - 1)) != 0) {
            return dft(signal)
        }

        val real = signal.copyOf()
        val imag = FloatArray(n)

        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n) {
            if (i < j) {
                val tempReal = real[i]
                real[i] = real[j]
                real[j] = tempReal
            }
            var m = n shr 1
            while (m >= 1 && j >= m) {
                j -= m
                m = m shr 1
            }
            j += m
        }

        // Cooley-Tukey iterative FFT
        var len = 2
        while (len <= n) {
            val ang = 2.0 * PI / len
            val wlenReal = cos(ang).toFloat()
            val wlenImag = (-sin(ang)).toFloat()
            for (i in 0 until n step len) {
                var wReal = 1f
                var wImag = 0f
                for (k in 0 until len / 2) {
                    val uReal = real[i + k]
                    val uImag = imag[i + k]
                    val vReal = real[i + k + len / 2] * wReal - imag[i + k + len / 2] * wImag
                    val vImag = real[i + k + len / 2] * wImag + imag[i + k + len / 2] * wReal
                    real[i + k] = uReal + vReal
                    imag[i + k] = uImag + vImag
                    real[i + k + len / 2] = uReal - vReal
                    imag[i + k + len / 2] = uImag - vImag
                    val nextWReal = wReal * wlenReal - wImag * wlenImag
                    wImag = wReal * wlenImag + wImag * wlenReal
                    wReal = nextWReal
                }
            }
            len = len shl 1
        }

        // Calculate magnitude for the first n/2 bins
        val magnitude = FloatArray(n / 2)
        for (i in 0 until n / 2) {
            magnitude[i] = sqrt(real[i] * real[i] + imag[i] * imag[i])
        }
        return magnitude
    }

    /**
     * Fallback DFT implementation for non-power-of-2 arrays.
     */
    private fun dft(signal: FloatArray): FloatArray {
        val n = signal.size
        val magnitude = FloatArray(n / 2)
        for (k in 0 until n / 2) {
            var real = 0f
            var imag = 0f
            for (t in signal.indices) {
                val angle = 2.0 * PI * k * t / n
                real += signal[t] * cos(angle).toFloat()
                imag -= signal[t] * sin(angle).toFloat()
            }
            magnitude[k] = sqrt(real * real + imag * imag)
        }
        return magnitude
    }

    /**
     * Salva as métricas fornecidas como calibração base nas preferências.
     */
    fun saveCalibration(metrics: VsaMetrics) {
        val prefs = context.getSharedPreferences("vsa_calibration", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("has_calibration", true)
            putFloat("baseline_micro_tremor", metrics.microTremor)
            putFloat("baseline_pitch_variation", metrics.pitchVariation)
            putFloat("baseline_jitter", metrics.jitter)
            putFloat("baseline_shimmer", metrics.shimmer)
            putFloat("baseline_hnr", metrics.hnr)
            apply()
        }
    }

    /**
     * Limpa a calibração de voz existente.
     */
    fun clearCalibration() {
        val prefs = context.getSharedPreferences("vsa_calibration", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Verifica se existe calibração salva.
     */
    fun hasCalibration(): Boolean {
        val prefs = context.getSharedPreferences("vsa_calibration", android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean("has_calibration", false)
    }

    /**
     * Obtém os limites (thresholds) de comparação com base na calibração se ela existir.
     */
    private fun getBaselineThresholds(): BaselineThresholds {
        val prefs = context.getSharedPreferences("vsa_calibration", android.content.Context.MODE_PRIVATE)
        if (!prefs.getBoolean("has_calibration", false)) {
            return BaselineThresholds(
                microTremorThreshold = 11f,
                pitchVariationThreshold = 20f,
                jitterThreshold = 2f,
                shimmerThreshold = 6f,
                hnrThreshold = 15f
            )
        }
        val mt = prefs.getFloat("baseline_micro_tremor", 9.5f)
        val pv = prefs.getFloat("baseline_pitch_variation", 12f)
        val jt = prefs.getFloat("baseline_jitter", 0.8f)
        val sh = prefs.getFloat("baseline_shimmer", 2f)
        val hnr = prefs.getFloat("baseline_hnr", 20f)

        return BaselineThresholds(
            microTremorThreshold = mt + 0.8f,
            pitchVariationThreshold = pv * 1.3f,
            jitterThreshold = jt * 1.5f,
            shimmerThreshold = sh * 1.5f,
            hnrThreshold = (hnr - 4f).coerceAtLeast(10f)
        )
    }

    private data class BaselineThresholds(
        val microTremorThreshold: Float,
        val pitchVariationThreshold: Float,
        val jitterThreshold: Float,
        val shimmerThreshold: Float,
        val hnrThreshold: Float
    )

    /**
     * Calcula o score geral de Verdade (0-100%) baseado nas 5 métricas.
     * Inicia em 50 e soma ou subtrai conforme cada item indica stress ou calmaria.
     */
    private fun calculateOverallStress(
        microTremor: Float,
        pitchVariation: Float,
        jitter: Float,
        shimmer: Float,
        hnr: Float
    ): Float {
        val thresholds = getBaselineThresholds()
        var score = 50f

        // Micro-Tremor (peso 15): menor frequência no envelope = menos stress (verdade)
        if (microTremor < thresholds.microTremorThreshold) score += 15f else score -= 15f

        // Pitch Variation (peso 10): menor variação = voz firme (verdade)
        if (pitchVariation < thresholds.pitchVariationThreshold) score += 10f else score -= 10f

        // Jitter (peso 10): menor jitter = voz estável (verdade)
        if (jitter < thresholds.jitterThreshold) score += 10f else score -= 10f

        // Shimmer (peso 7.5): menor shimmer = amplitude estável (verdade)
        if (shimmer < thresholds.shimmerThreshold) score += 7.5f else score -= 7.5f

        // HNR (peso 7.5): maior HNR = voz mais clara, sem ruídos e tremor de mentira
        if (hnr > thresholds.hnrThreshold) score += 7.5f else score -= 7.5f

        return score.coerceIn(0f, 100f)
    }
}
