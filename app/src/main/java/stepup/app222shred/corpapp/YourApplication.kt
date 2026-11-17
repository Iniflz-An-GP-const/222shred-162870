package stepup.app222shred.corpapp

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import stepup.app222shred.corpapp.integration.IntegrationRepository
import stepup.app222shred.corpapp.integration.Integrations
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext
import stepup.app222shred.corpapp.di.appModules

class YourApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val repository = IntegrationRepository(this)
        Integrations.repository = repository

        GlobalContext.startKoin {
            androidContext(this@YourApplication)
            workManagerFactory()
            modules(diModule + appModules)
        }

        initFcm(repository)
    }

    private fun initFcm(repository: IntegrationRepository) {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    repository.onFcmToken(task.result.orEmpty())
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    /*
     private fun initFcm(repository: IntegrationRepository) {
        // ТИМЧАСОВИЙ КОД ДЛЯ ТЕСТУ РЕФРЕШУ
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Примусово видаляємо старий токен
                FirebaseMessaging.getInstance().deleteToken()

                // 2. Одразу просимо новий
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val newToken = task.result.orEmpty()
                        android.util.Log.d("FCM_TEST", "Згенеровано НОВИЙ токен: $newToken")
                        repository.onFcmToken(newToken)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    */
}