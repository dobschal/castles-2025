package eu.dobschal.model.enum

enum class UnitType {
    WORKER,
    SWORDSMAN, // wins against spearman, loses against horseman
    HORSEMAN, // wins against swordsman, loses against spearman
    SPEARMAN, // wins against horseman, loses against swordsman
    DRAGON,
    ARCHER
}
