package com.heroesports.heroci

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.heroesports.heroci.ui.screens.*
import com.heroesports.heroci.ui.theme.HeroCITheme
import com.heroesports.heroci.ui.viewmodel.CheckInViewModel
import com.heroesports.heroci.ui.viewmodel.ProjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeroCITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            val viewModel = hiltViewModel<ProjectViewModel>()
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToManage = { projectId, isAdmin ->
                                    navController.navigate("manage/$projectId/$isAdmin")
                                },
                                onNavigateToCheckIn = { projectId ->
                                    navController.navigate("check_in/$projectId")
                                }
                            )
                        }
                        composable("manage/{projectId}/{isAdmin}") { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull() ?: return@composable
                            val isAdmin = backStackEntry.arguments?.getString("isAdmin")?.toBoolean() ?: false
                            val viewModel = hiltViewModel<CheckInViewModel>()
                            ManageScreen(
                                projectId = projectId,
                                viewModel = viewModel,
                                isAdmin = isAdmin,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToCheckInDetail = { checkInId, isAdmin ->
                                    navController.navigate("check_in_detail/$projectId/$checkInId/$isAdmin")
                                }
                            )
                        }
                        composable("check_in/{projectId}") { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull() ?: return@composable
                            val viewModel = hiltViewModel<CheckInViewModel>()
                            CheckInScreen(
                                projectId = projectId,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            "check_in_detail/{projectId}/{checkInId}/{isAdmin}"
                        ) { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId")?.toLongOrNull() ?: return@composable
                            val checkInId = backStackEntry.arguments?.getString("checkInId")?.toLongOrNull() ?: return@composable
                            val isAdmin = backStackEntry.arguments?.getString("isAdmin")?.toBoolean() ?: false
                            val viewModel = hiltViewModel<CheckInViewModel>()
                            CheckInDetailScreen(
                                projectId = projectId,
                                checkInId = checkInId,
                                isAdmin = isAdmin,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}