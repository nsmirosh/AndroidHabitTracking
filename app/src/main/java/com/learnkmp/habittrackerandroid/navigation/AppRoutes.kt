package com.learnkmp.habittrackerandroid.navigation

import androidx.navigation3.runtime.NavKey

sealed interface AppRoute : NavKey {
    data object Login : AppRoute
    data object HabitList : AppRoute
    data class CreateHabit(val forDate: String) : AppRoute
    data class EditHabit(val habitId: String) : AppRoute
}
