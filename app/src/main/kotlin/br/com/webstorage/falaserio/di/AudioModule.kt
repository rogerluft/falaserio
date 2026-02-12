package br.com.webstorage.falaserio.di

import android.content.Context
import br.com.webstorage.falaserio.domain.audio.AudioPreprocessor
import br.com.webstorage.falaserio.domain.audio.AudioRecorder
import br.com.webstorage.falaserio.domain.audio.AudioRecorderImpl
import br.com.webstorage.falaserio.domain.audio.VsaAnalyzer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt para injeção de dependências de áudio.
 *
 * Usa @Provides ao invés de @Binds para maior controle na criação.
 * Isso permite passar o ApplicationContext sem vazamento de memória.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioPreprocessor(): AudioPreprocessor {
        return AudioPreprocessor()
    }

    @Provides
    @Singleton
    fun provideVsaAnalyzer(preprocessor: AudioPreprocessor): VsaAnalyzer {
        return VsaAnalyzer(preprocessor)
    }

    @Provides
    @Singleton
    fun provideAudioRecorder(
        @ApplicationContext context: Context
    ): AudioRecorder {
        return AudioRecorderImpl(context)
    }
}
