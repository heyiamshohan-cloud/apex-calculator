package com.shohankhan.apexcalculator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shohankhan.apexcalculator.core.domain.model.ApexTab
import com.shohankhan.apexcalculator.core.domain.model.CalculationSource
import com.shohankhan.apexcalculator.feature.about.AboutScreen
import com.shohankhan.apexcalculator.feature.about.PrivacyScreen
import com.shohankhan.apexcalculator.feature.favorites.FavoritesScreen
import com.shohankhan.apexcalculator.feature.formula.FormulaDetailScreen
import com.shohankhan.apexcalculator.feature.formula.FormulaLabScreen
import com.shohankhan.apexcalculator.feature.history.HistoryScreen
import com.shohankhan.apexcalculator.feature.programmer.ProgrammerScreen
import com.shohankhan.apexcalculator.feature.quicksolve.QuickSolveCatalogScreen
import com.shohankhan.apexcalculator.feature.quicksolve.SolverScreen
import com.shohankhan.apexcalculator.feature.scientific.ScientificScreen
import com.shohankhan.apexcalculator.feature.settings.SettingsScreen
import com.shohankhan.apexcalculator.feature.standard.StandardScreen
import com.shohankhan.apexcalculator.feature.tools.ComplexScreen
import com.shohankhan.apexcalculator.feature.tools.MatrixScreen
import com.shohankhan.apexcalculator.feature.tools.ToolsHubScreen

/** Central route table. Tab routes are the same strings stored in [ApexTab.route]. */
object ApexRoutes {
    const val HISTORY = "history"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val MATRIX = "tools/matrix"
    const val COMPLEX = "tools/complex"
    const val FORMULA_DETAIL = "formula/{formulaId}"
    const val SOLVER_DETAIL = "solver/{solverId}"
    const val TOOL_SOLVER = "tool/{solverId}"

    fun formula(id: String) = "formula/$id"

    fun solver(id: String) = "solver/$id"

    fun toolSolver(id: String) = "tool/$id"
}

@Composable
fun ApexApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    fun goToTab(tab: ApexTab) {
        navController.navigate(tab.route) {
            popUpTo(ApexTab.STANDARD.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun openSecondary(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    fun back() {
        if (!navController.popBackStack()) goToTab(ApexTab.STANDARD)
    }

    NavHost(
        navController = navController,
        startDestination = ApexTab.STANDARD.route,
        modifier = modifier,
    ) {
        composable(ApexTab.STANDARD.route) {
            StandardScreen(
                onNavigate = ::goToTab,
                onOpenHistory = { openSecondary(ApexRoutes.HISTORY) },
                onOpenFavorites = { openSecondary(ApexRoutes.FAVORITES) },
                onOpenSettings = { openSecondary(ApexRoutes.SETTINGS) },
                onOpenAbout = { openSecondary(ApexRoutes.ABOUT) },
                onOpenPrivacy = { openSecondary(ApexRoutes.PRIVACY) },
            )
        }
        composable(ApexTab.SCIENTIFIC.route) {
            ScientificScreen(
                onNavigate = ::goToTab,
                onOpenHistory = { openSecondary(ApexRoutes.HISTORY) },
                onOpenFavorites = { openSecondary(ApexRoutes.FAVORITES) },
                onOpenSettings = { openSecondary(ApexRoutes.SETTINGS) },
                onOpenAbout = { openSecondary(ApexRoutes.ABOUT) },
                onOpenPrivacy = { openSecondary(ApexRoutes.PRIVACY) },
            )
        }
        composable(ApexTab.FORMULA.route) {
            FormulaLabScreen(
                onOpenFormula = { id -> openSecondary(ApexRoutes.formula(id)) },
                onNavigate = ::goToTab,
                onOpenHistory = { openSecondary(ApexRoutes.HISTORY) },
                onOpenFavorites = { openSecondary(ApexRoutes.FAVORITES) },
                onOpenSettings = { openSecondary(ApexRoutes.SETTINGS) },
                onOpenAbout = { openSecondary(ApexRoutes.ABOUT) },
                onOpenPrivacy = { openSecondary(ApexRoutes.PRIVACY) },
            )
        }
        composable(ApexTab.QUICKSOLVE.route) {
            QuickSolveCatalogScreen(
                onOpenSolver = { id -> openSecondary(ApexRoutes.solver(id)) },
                onNavigate = ::goToTab,
                onOpenHistory = { openSecondary(ApexRoutes.HISTORY) },
                onOpenFavorites = { openSecondary(ApexRoutes.FAVORITES) },
                onOpenSettings = { openSecondary(ApexRoutes.SETTINGS) },
                onOpenAbout = { openSecondary(ApexRoutes.ABOUT) },
                onOpenPrivacy = { openSecondary(ApexRoutes.PRIVACY) },
            )
        }
        composable(ApexTab.PROGRAMMER.route) {
            ProgrammerScreen(
                onNavigate = ::goToTab,
                onOpenHistory = { openSecondary(ApexRoutes.HISTORY) },
                onOpenFavorites = { openSecondary(ApexRoutes.FAVORITES) },
                onOpenSettings = { openSecondary(ApexRoutes.SETTINGS) },
                onOpenAbout = { openSecondary(ApexRoutes.ABOUT) },
                onOpenPrivacy = { openSecondary(ApexRoutes.PRIVACY) },
            )
        }
        composable(ApexTab.TOOLS.route) {
            ToolsHubScreen(
                onOpenSolver = { id -> openSecondary(ApexRoutes.toolSolver(id)) },
                onOpenMatrix = { openSecondary(ApexRoutes.MATRIX) },
                onOpenComplex = { openSecondary(ApexRoutes.COMPLEX) },
                onNavigate = ::goToTab,
                onOpenHistory = { openSecondary(ApexRoutes.HISTORY) },
                onOpenFavorites = { openSecondary(ApexRoutes.FAVORITES) },
                onOpenSettings = { openSecondary(ApexRoutes.SETTINGS) },
                onOpenAbout = { openSecondary(ApexRoutes.ABOUT) },
                onOpenPrivacy = { openSecondary(ApexRoutes.PRIVACY) },
            )
        }

        composable(ApexRoutes.HISTORY) {
            HistoryScreen(
                onBack = ::back,
                onUseExpression = {
                    navController.navigate(ApexTab.STANDARD.route) {
                        popUpTo(ApexTab.STANDARD.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(ApexRoutes.FAVORITES) {
            FavoritesScreen(
                onOpenFormula = { id -> openSecondary(ApexRoutes.formula(id)) },
                onOpenSolver = { id -> openSecondary(ApexRoutes.solver(id)) },
                onBack = ::back,
            )
        }
        composable(ApexRoutes.SETTINGS) {
            SettingsScreen(
                onBack = ::back,
                onOpenPrivacy = { openSecondary(ApexRoutes.PRIVACY) },
            )
        }
        composable(ApexRoutes.ABOUT) {
            AboutScreen(
                onBack = ::back,
                onOpenPrivacy = { openSecondary(ApexRoutes.PRIVACY) },
            )
        }
        composable(ApexRoutes.PRIVACY) {
            PrivacyScreen(onBack = ::back)
        }
        composable(ApexRoutes.MATRIX) {
            MatrixScreen(onBack = ::back)
        }
        composable(ApexRoutes.COMPLEX) {
            ComplexScreen(onBack = ::back)
        }
        composable(
            route = ApexRoutes.FORMULA_DETAIL,
            arguments = listOf(navArgument("formulaId") { type = NavType.StringType }),
        ) { entry ->
            FormulaDetailScreen(
                formulaId = entry.arguments?.getString("formulaId").orEmpty(),
                onBack = ::back,
            )
        }
        composable(
            route = ApexRoutes.SOLVER_DETAIL,
            arguments = listOf(navArgument("solverId") { type = NavType.StringType }),
        ) { entry ->
            SolverScreen(
                solverId = entry.arguments?.getString("solverId").orEmpty(),
                source = CalculationSource.QUICK_SOLVE,
                onBack = ::back,
            )
        }
        composable(
            route = ApexRoutes.TOOL_SOLVER,
            arguments = listOf(navArgument("solverId") { type = NavType.StringType }),
        ) { entry ->
            SolverScreen(
                solverId = entry.arguments?.getString("solverId").orEmpty(),
                source = CalculationSource.TOOLS,
                onBack = ::back,
            )
        }
    }
}
