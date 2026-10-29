package stepup.app222shred.corpapp.ui.composable.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import stepup.app222shred.corpapp.ui.composable.screen.about.SPHOPAboutScreen
import stepup.app222shred.corpapp.ui.composable.screen.game.SPHOPGameScreen
import stepup.app222shred.corpapp.ui.composable.screen.high_scores.SPHOPHighScoresScreen
import stepup.app222shred.corpapp.ui.composable.screen.main_menu.SPHOPMainMenuScreen
import stepup.app222shred.corpapp.ui.composable.screen.splash.SPHOPSplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current

    NavHost(
        navController = navController,
        startDestination = NavRoute.Splash,
        modifier = modifier,
    ) {
        composable<NavRoute.Splash> {
            SPHOPSplashScreen(
                onMainMenuScreenNavigation = {
                    navController.navigate(route = NavRoute.MainMenu) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<NavRoute.MainMenu> {
            SPHOPMainMenuScreen(
                onPlayClick = { difficulty ->
                    navController.navigate(
                        route = NavRoute.Game(
                            difficulty
                        )
                    )
                },
                onHighScoresScreenNavigation = { navController.navigate(route = NavRoute.HighScores) },
                onAboutScreenNavigation = { navController.navigate(route = NavRoute.About) },
                onExit = { activity?.finishAffinity() }
            )
        }

        composable<NavRoute.Game> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.Game>()
            SPHOPGameScreen(
                difficulty = route.difficulty,
                onMainMenu = {
                    navController.navigate(route = NavRoute.MainMenu) {
                        popUpTo(NavRoute.MainMenu) {
                            inclusive = true
                        }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<NavRoute.About> {
            SPHOPAboutScreen(onBackClick = { navController.popBackStack() })
        }

        composable<NavRoute.HighScores> {
            SPHOPHighScoresScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
