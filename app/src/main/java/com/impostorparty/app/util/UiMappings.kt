package com.impostorparty.app.util

import androidx.annotation.StringRes
import com.impostorparty.app.R
import com.impostorparty.domain.model.Category
import com.impostorparty.domain.model.WinnerSide

@StringRes
fun Category.titleRes(): Int = when (this) {
    Category.FOOD -> R.string.category_food
    Category.ANIMALS -> R.string.category_animals
    Category.PROFESSIONS -> R.string.category_professions
    Category.OBJECTS -> R.string.category_objects
    Category.PLACES -> R.string.category_places
    Category.MOVIES_SERIES -> R.string.category_movies_series
    Category.SPORTS -> R.string.category_sports
    Category.COUNTRIES_CITIES -> R.string.category_countries_cities
    Category.NATURE -> R.string.category_nature
    Category.GENERAL_KNOWLEDGE -> R.string.category_general_knowledge
    Category.PLAYERS -> R.string.category_players
}

@StringRes
fun WinnerSide.labelRes(): Int = when (this) {
    WinnerSide.CIVILIANS -> R.string.winner_civilians
    WinnerSide.IMPOSTORS -> R.string.winner_impostors
    WinnerSide.UNDECIDED -> R.string.winner_undecided
}
