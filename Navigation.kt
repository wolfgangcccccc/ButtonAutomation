package com.buttonautomation.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.buttonautomation.presentation.screens.EditorScreen
import com.buttonautomation.presentation.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Editor : Screen("editor?buttonId={buttonId}") {
        fun createRoute(buttonId: String? = null): String =
            if (buttonId != null) "editor?buttonId=$buttonId" else "editor"
    }
}

@Composable
fun ButtonAutomationNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onCreateButton = { navController.navigate(Screen.Editor.createRoute()) },
                onEditButton = { id -> navController.navigate(Screen.Editor.createRoute(id)) }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("buttonId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStack ->
            val buttonId = backStack.arguments?.getString("buttonId")
            EditorScreen(
                buttonId = buttonId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
