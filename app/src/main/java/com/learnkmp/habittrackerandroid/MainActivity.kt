package com.learnkmp.habittrackerandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.google.firebase.auth.FirebaseAuth
import com.learnkmp.habittrackerandroid.navigation.AppRoute
import com.learnkmp.habittrackerandroid.ui.auth.LoginScreen
import com.learnkmp.habittrackerandroid.ui.createhabit.CreateHabitScreen
import com.learnkmp.habittrackerandroid.ui.habitlist.HabitListScreen
import com.learnkmp.habittrackerandroid.ui.theme.HabitTrackerAndroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitTrackerAndroidTheme {
                val startRoute: AppRoute =
                    if (FirebaseAuth.getInstance().currentUser != null) AppRoute.HabitList
                    else AppRoute.Login

                val backStack = rememberNavBackStack(startRoute)

                NavDisplay(
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                ) { route ->
                    when (route) {
                        AppRoute.Login -> NavEntry(key = route) {
                            LoginScreen(
                                onLoginSuccess = {
                                    backStack.clear()
                                    backStack.add(AppRoute.HabitList)
                                },
                            )
                        }

                        AppRoute.HabitList -> NavEntry(key = route) {
                            HabitListScreen(
                                onCreateHabit = { backStack.add(AppRoute.CreateHabit) },
                            )
                        }

                        AppRoute.CreateHabit -> NavEntry(key = route) {
                            CreateHabitScreen(
                                onSaved = { backStack.removeLastOrNull() },
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }

                        else -> NavEntry(key = route) {}
                    }
                }
            }
        }
    }
}
