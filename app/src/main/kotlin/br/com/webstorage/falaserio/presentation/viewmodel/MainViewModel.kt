package br.com.webstorage.falaserio.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.webstorage.falaserio.data.repository.CreditsRepository
import br.com.webstorage.falaserio.data.repository.HistoryRepository
import br.com.webstorage.falaserio.domain.audio.AudioRecorder
import br.com.webstorage.falaserio.domain.audio.VsaAnalyzer
import br.com.webstorage.falaserio.domain.audio.AudioDecoder
import br.com.webstorage.falaserio.domain.model.VsaMetrics
import br.com.webstorage.falaserio.domain.usecase.AnalyzeAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel principal do FalaSério.
 *
 * Suporta gravação, calibração de voz, seleção de arquivos e reanálise.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val analyzeAudioUseCase: AnalyzeAudioUseCase,
    private val creditsRepository: CreditsRepository,
    private val historyRepository: HistoryRepository,
    private val vsaAnalyzer: VsaAnalyzer
) : ViewModel() {

    // ========== UI STATE ==========
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // ========== RECORDING STATE ==========
    val isRecording: StateFlow<Boolean> = audioRecorder.isRecording
    val recordingDuration: StateFlow<Long> = audioRecorder.recordingDuration
    val currentAmplitude: StateFlow<Float> = audioRecorder.currentAmplitude

    // ========== CREDITS ==========
    private val _credits = MutableStateFlow(0)
    val credits: StateFlow<Int> = _credits.asStateFlow()

    // ========== CALIBRATION ==========
    private val _hasCalibration = MutableStateFlow(false)
    val hasCalibration: StateFlow<Boolean> = _hasCalibration.asStateFlow()

    private val _isCalibrating = MutableStateFlow(false)
    val isCalibrating: StateFlow<Boolean> = _isCalibrating.asStateFlow()

    private var isCalibratingRecording = false

    init {
        loadCredits()
        checkCalibration()
    }

    private fun loadCredits() {
        viewModelScope.launch {
            creditsRepository.getCredits().collect { entity ->
                entity?.let {
                    _credits.value = if (it.isUnlimited) Int.MAX_VALUE else it.available
                }
            }
        }
    }

    private fun checkCalibration() {
        _hasCalibration.value = vsaAnalyzer.hasCalibration()
    }

    // ========== RECORDING ACTIONS ==========

    fun startRecording() {
        viewModelScope.launch {
            // VERIFICAÇÃO PREVENTIVA: Se não tem crédito, nem começa.
            if (_credits.value <= 0) {
                _uiState.update { it.copy(error = "Você não possui créditos suficientes.") }
                return@launch
            }

            _uiState.update { it.copy(isAnalyzing = false, error = null, metrics = null) }
            isCalibratingRecording = false
            _isCalibrating.value = false
            audioRecorder.start()
        }
    }

    fun startCalibration() {
        viewModelScope.launch {
            // Calibração é gratuita (não gasta créditos)
            _uiState.update { it.copy(isAnalyzing = false, error = null, metrics = null) }
            isCalibratingRecording = true
            _isCalibrating.value = true
            audioRecorder.start()
        }
    }

    fun clearCalibration() {
        vsaAnalyzer.clearCalibration()
        _hasCalibration.value = false
        _uiState.update { it.copy(error = "Calibração removida com sucesso!") }
    }

    fun stopRecording() {
        viewModelScope.launch {
            val file = audioRecorder.stop()
            file?.let {
                if (isCalibratingRecording) {
                    analyzeCalibration(it)
                } else {
                    analyzeRecording(it)
                }
            }
            _isCalibrating.value = false
        }
    }

    fun cancelRecording() {
        viewModelScope.launch {
            audioRecorder.cancel()
            isCalibratingRecording = false
            _isCalibrating.value = false
            _uiState.update { it.copy(isAnalyzing = false, error = null) }
        }
    }

    // ========== ANALYSIS ==========

    private fun analyzeRecording(file: File) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }

            // Tenta usar o crédito de fato agora
            val success = creditsRepository.useCredit()
            if (!success) {
                _uiState.update {
                    it.copy(isAnalyzing = false, error = "Erro ao processar crédito.")
                }
                return@launch
            }

            try {
                val metrics = analyzeAudioUseCase(file)
                historyRepository.saveAnalysis(file, metrics)

                _uiState.update {
                    it.copy(isAnalyzing = false, metrics = metrics, error = null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isAnalyzing = false, error = "Erro na análise: ${e.message}")
                }
            }
        }
    }

    private fun analyzeCalibration(file: File) {
        viewModelScope.launch(Dispatchers.Default) {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }
            try {
                val metrics = analyzeAudioUseCase(file)
                vsaAnalyzer.saveCalibration(metrics)
                _hasCalibration.value = true
                isCalibratingRecording = false
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        metrics = metrics,
                        error = "Calibração concluída com sucesso! Sua voz foi registrada como base."
                    )
                }
            } catch (e: Exception) {
                isCalibratingRecording = false
                _uiState.update {
                    it.copy(isAnalyzing = false, error = "Erro na calibração: ${e.message}")
                }
            }
        }
    }

    fun selectAudio(uri: Uri, context: Context) {
        viewModelScope.launch {
            if (_credits.value <= 0) {
                _uiState.update { it.copy(error = "Você não possui créditos suficientes.") }
                return@launch
            }

            _uiState.update { it.copy(isAnalyzing = true, error = null, metrics = null) }

            // Usa crédito
            val success = creditsRepository.useCredit()
            if (!success) {
                _uiState.update { it.copy(isAnalyzing = false, error = "Erro ao processar crédito.") }
                return@launch
            }

            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val cacheFile = File(context.cacheDir, "selected_${System.currentTimeMillis()}.wav")
                    val decoded = AudioDecoder.decodeToWav(context, uri, cacheFile)
                    
                    if (decoded && cacheFile.exists()) {
                        val metrics = analyzeAudioUseCase(cacheFile)
                        historyRepository.saveAnalysis(cacheFile, metrics)
                        _uiState.update {
                            it.copy(isAnalyzing = false, metrics = metrics, error = null)
                        }
                    } else {
                        _uiState.update {
                            it.copy(isAnalyzing = false, error = "Falha ao decodificar o arquivo de áudio.")
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(isAnalyzing = false, error = "Erro na análise: ${e.message}")
                    }
                }
            }
        }
    }

    fun reanalyzeRecording(historyId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null, metrics = null) }
            
            val history = historyRepository.getById(historyId)
            if (history == null) {
                _uiState.update { it.copy(isAnalyzing = false, error = "Análise não encontrada no histórico.") }
                return@launch
            }

            val file = File(history.filePath)
            if (!file.exists()) {
                _uiState.update { it.copy(isAnalyzing = false, error = "Arquivo de áudio original não existe mais.") }
                return@launch
            }

            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val metrics = analyzeAudioUseCase(file)
                    // Salva a reanálise como uma nova entrada com as calibrações atuais aplicadas
                    historyRepository.saveAnalysis(file, metrics)
                    
                    _uiState.update {
                        it.copy(isAnalyzing = false, metrics = metrics, error = "Reanálise concluída!")
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(isAnalyzing = false, error = "Erro na reanálise: ${e.message}")
                    }
                }
            }
        }
    }

    fun onAdWatched() {
        viewModelScope.launch {
            creditsRepository.addCredits(1)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.release()
    }
}

data class UiState(
    val isAnalyzing: Boolean = false,
    val metrics: VsaMetrics? = null,
    val error: String? = null
)
