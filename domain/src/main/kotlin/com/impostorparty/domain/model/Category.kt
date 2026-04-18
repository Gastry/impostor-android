package com.impostorparty.domain.model

enum class Category(val code: String) {
    FOOD("food"),
    ANIMALS("animals"),
    PROFESSIONS("professions"),
    OBJECTS("objects"),
    PLACES("places"),
    MOVIES_SERIES("movies_series"),
    SPORTS("sports"),
    COUNTRIES_CITIES("countries_cities"),
    NATURE("nature"),
    GENERAL_KNOWLEDGE("general_knowledge"),
    PLAYERS("players");

    companion object {
        val defaultSelection: Set<Category> = setOf(FOOD, ANIMALS, OBJECTS, PLACES)
        val wordDatasetCategories: List<Category> = entries.filterNot { it == PLAYERS }

        fun fromCode(code: String): Category? = entries.firstOrNull { it.code == code }
    }
}
