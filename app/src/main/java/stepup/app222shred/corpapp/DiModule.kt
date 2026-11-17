package stepup.app222shred.corpapp

import stepup.app222shred.corpapp.api.ApiService
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val diModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("https://nunataktower.top")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    worker {
        VisitRequestWorker(
            appContext = androidContext(),
            workerParams = get(),
            service = get()
        )
    }
}