package stepup.app222shred.corpapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import stepup.app222shred.corpapp.data.database.converter.Converters
import stepup.app222shred.corpapp.data.database.dao.HighScoreDao
import stepup.app222shred.corpapp.data.entity.HighScore

@Database(
    entities = [HighScore::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SPHOPAppDatabase : RoomDatabase() {

    abstract fun highScoreDao(): HighScoreDao
}
