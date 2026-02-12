package br.com.webstorage.falaserio

import android.app.Application
import android.content.Context
import br.com.webstorage.falaserio.data.repository.CreditsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class do FalaSério.
 *
 * @HiltAndroidApp é OBRIGATÓRIO para o Hilt funcionar.
 * Sem isso, a injeção de dependência não funciona!
 */
@HiltAndroidApp
class FalaSerioApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val PREFS_NAME = "falaserio_prefs"
        private const val KEY_FIRST_RUN = "is_first_run"

        /**
         * Verifica se é a primeira execução do app.
         */
        fun isFirstRun(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_FIRST_RUN, true)
        }

        /**
         * Marca que o app já foi executado.
         */
        fun setFirstRunComplete(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CreditsEntryPoint {
        fun creditsRepository(): CreditsRepository
    }

    override fun onCreate() {
        super.onCreate()
        initializeCreditsForNewUser()
    }

    /**
     * Inicializa créditos para novos usuários (3 créditos grátis).
     * Usa EntryPoint porque injeção direta não funciona em Application.onCreate().
     */
    private fun initializeCreditsForNewUser() {
        applicationScope.launch {
            val entryPoint = EntryPointAccessors.fromApplication(
                this@FalaSerioApp,
                CreditsEntryPoint::class.java
            )
            entryPoint.creditsRepository().initializeForNewUser()
        }
    }
}
