package br.com.webstorage.falaserio.domain.audio

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

/**
 * Pré-processador de áudio para melhorar qualidade da análise VSA.
 * Aplica filtros para remover ruído e focar na faixa de voz humana.
 */
@Singleton
class AudioPreprocessor @Inject constructor() {

    companion object {
        const val MIN_FREQ_HZ = 80f
        const val MAX_FREQ_HZ = 400f
        const val NOISE_GATE_THRESHOLD_DB = -40f
    }

    /**
     * Pré-processa samples de áudio aplicando filtros.
     * @param samples Array de samples normalizados (-1 a 1)
     * @param sampleRate Taxa de amostragem (ex: 44100)
     * @return Samples filtrados
     */
    fun preprocess(samples: FloatArray, sampleRate: Int): FloatArray {
        if (samples.isEmpty()) return samples

        // 1. Aplicar noise gate (silenciar partes muito baixas)
        val gatedSamples = applyNoiseGate(samples)

        // 2. Aplicar filtro passa-banda (80-400Hz para voz humana)
        val filteredSamples = applyBandPassFilter(gatedSamples, sampleRate)

        return filteredSamples
    }

    /**
     * Aplica noise gate - silencia samples abaixo do threshold.
     */
    private fun applyNoiseGate(samples: FloatArray): FloatArray {
        val thresholdLinear = 10f.pow(NOISE_GATE_THRESHOLD_DB / 20f)
        return samples.map { sample ->
            if (abs(sample) < thresholdLinear) 0f else sample
        }.toFloatArray()
    }

    /**
     * Aplica filtro passa-banda usando coeficientes Butterworth simplificado.
     * Mantém apenas frequências entre MIN_FREQ_HZ e MAX_FREQ_HZ.
     */
    private fun applyBandPassFilter(samples: FloatArray, sampleRate: Int): FloatArray {
        if (samples.size < 3) return samples

        val centerFreq = (MIN_FREQ_HZ + MAX_FREQ_HZ) / 2f
        val bandwidth = MAX_FREQ_HZ - MIN_FREQ_HZ

        // Coeficientes simplificados do filtro passa-banda
        val omega = 2f * Math.PI.toFloat() * centerFreq / sampleRate
        val alpha = Math.sin(omega.toDouble()).toFloat() *
                    Math.sinh((Math.log(2.0) / 2 * (bandwidth / centerFreq) *
                    omega / Math.sin(omega.toDouble()))).toFloat()

        val b0 = alpha
        val b1 = 0f
        val b2 = -alpha
        val a0 = 1f + alpha
        val a1 = -2f * Math.cos(omega.toDouble()).toFloat()
        val a2 = 1f - alpha

        // Normalizar coeficientes
        val b0n = b0 / a0
        val b1n = b1 / a0
        val b2n = b2 / a0
        val a1n = a1 / a0
        val a2n = a2 / a0

        // Aplicar filtro IIR
        val output = FloatArray(samples.size)
        var x1 = 0f
        var x2 = 0f
        var y1 = 0f
        var y2 = 0f

        for (i in samples.indices) {
            val x0 = samples[i]
            output[i] = b0n * x0 + b1n * x1 + b2n * x2 - a1n * y1 - a2n * y2

            x2 = x1
            x1 = x0
            y2 = y1
            y1 = output[i]
        }

        return output
    }

    /**
     * Calcula RMS (Root Mean Square) do sinal em dB.
     */
    fun calculateRmsDb(samples: FloatArray): Float {
        if (samples.isEmpty()) return -100f
        val rms = Math.sqrt(samples.map { it * it }.average()).toFloat()
        return if (rms > 0) 20f * log10(rms) else -100f
    }
}
