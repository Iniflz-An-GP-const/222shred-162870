package stepup.app222shred.corpapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import stepup.app222shred.corpapp.data.model.Difficulty
import java.time.LocalDateTime

@Entity("high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val difficulty: Difficulty,
    val moves: Int,
    @ColumnInfo(name = "time_ms") val timeMs: Long,
    val timestamp: LocalDateTime,
)
