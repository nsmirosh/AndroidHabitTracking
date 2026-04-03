package com.learnkmp.habittrackerandroid.navigation

sealed interface AppRoute {
    data object Login : AppRoute
    data object HabitList : AppRoute
    data object CreateHabit : AppRoute
}
