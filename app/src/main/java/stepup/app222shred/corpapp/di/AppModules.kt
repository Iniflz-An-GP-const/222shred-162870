package stepup.app222shred.corpapp.di

import androidx.room.Room
import stepup.app222shred.corpapp.data.database.SPHOPAppDatabase
import stepup.app222shred.corpapp.data.repository.HighScoreRepository
import stepup.app222shred.corpapp.data.model.Difficulty
import stepup.app222shred.corpapp.ui.viewmodel.GameViewModel
import stepup.app222shred.corpapp.ui.viewmodel.HighScoreViewModel
import stepup.app222shred.corpapp.ui.viewmodel.MenuViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private const val DB_NAME = "sphop_db"

val dataModule = module {
    single {
        Room.databaseBuilder(
            context = get(),
            klass = SPHOPAppDatabase::class.java,
            name = DB_NAME
        ).build()
    }

    single { get<SPHOPAppDatabase>().highScoreDao() }

    single {
        HighScoreRepository(
            highScoreDao = get(),
            coroutineDispatcher = get(DispatchersQualifiers.IO)
        )
    }
}

val viewModule = module {
    viewModel { (difficulty: Difficulty) ->
        GameViewModel(difficulty = difficulty, highScoreRepository = get())
    }

    viewModel { HighScoreViewModel(highScoreRepository = get()) }

    viewModel { MenuViewModel(highScoreRepository = get()) }
}

val appModules: List<Module> = listOf(dispatcherModule, dataModule, viewModule)
