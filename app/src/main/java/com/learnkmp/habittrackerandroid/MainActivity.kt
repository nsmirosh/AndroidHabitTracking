package com.learnkmp.habittrackerandroid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
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

                val backStack = remember { mutableStateListOf(startRoute) }

                NavDisplay(
                    modifier = Modifier.fillMaxSize(),
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<AppRoute.Login> {
                            LoginScreen(
                                onLoginSuccess = {
                                    Log.d("Login", "navigating to the Habit list")
                                    backStack.clear()
                                    backStack.add(AppRoute.HabitList)
                                },
                            )
                        }

                        entry<AppRoute.HabitList> {
                            HabitListScreen(
                                onCreateHabit = { backStack.add(AppRoute.CreateHabit) },
                            )
                        }

                        entry<AppRoute.CreateHabit> {
                            CreateHabitScreen(
                                onSaved = { backStack.removeLastOrNull() },
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }
                    },
                )
            }
        }
    }
}
