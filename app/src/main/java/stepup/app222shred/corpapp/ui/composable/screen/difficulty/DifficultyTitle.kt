package stepup.app222shred.corpapp.ui.composable.screen.difficulty

import stepup.app222shred.corpapp.R
import stepup.app222shred.corpapp.data.model.Difficulty

fun Difficulty.titleRes(): Int = when (this) {
    Difficulty.Easy -> R.string.difficulty_easy
    Difficulty.Medium -> R.string.difficulty_medium
    Difficulty.Hard -> R.string.difficulty_hard
}
