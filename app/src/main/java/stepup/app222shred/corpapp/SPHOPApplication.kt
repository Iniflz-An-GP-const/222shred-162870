package stepup.app222shred.corpapp

import android.app.Application
import stepup.app222shred.corpapp.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class SPHOPApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SPHOPApplication)
            modules(appModules)
        }
    }
}