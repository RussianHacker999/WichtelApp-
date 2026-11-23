package com.example.wichtly.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wichtly.ui.groups.GroupsRoute
import com.example.wichtly.ui.groups.detail.GroupDetailRoute
import com.example.wichtly.ui.groups.form.GroupFormRoute
import com.example.wichtly.ui.wishlist.ParticipantWishlistRoute
import com.example.wichtly.ui.draws.DrawDetailRoute
import com.example.wichtly.ui.draws.DrawHistoryRoute
import com.example.wichtly.ui.groups.preparation.PreparationRoute


object Routes {
    const val ARG_GROUP_ID = "groupId"
    const val ARG_DRAW_ID = "drawId"
    const val ARG_PARTICIPANT_ID = "participantId"
    const val GROUPS = "groups"
    const val GROUP_CREATE = "group-form"
    const val GROUP_EDIT = "group-form/{$ARG_GROUP_ID}"
    const val GROUP_DETAIL = "group-detail/{$ARG_GROUP_ID}"
    const val WISHLIST = "wishlist/{$ARG_PARTICIPANT_ID}"
    const val DRAW_DETAIL = "draw-detail/{$ARG_DRAW_ID}"
    const val DRAW_HISTORY = "draw-history/{$ARG_GROUP_ID}"
    const val PREPARATION = "preparation/{$ARG_GROUP_ID}"


    fun groupEdit(groupId: String) = "group-form/$groupId"
    fun groupDetail(groupId: String) = "group-detail/$groupId"
    fun wishlist(participantId: String) = "wishlist/$participantId"

    fun drawDetail(drawId: String) = "draw-detail/$drawId"
    fun drawHistory(groupId: String) = "draw-history/$groupId"
    fun preparation(groupId: String) = "preparation/$groupId"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.GROUPS,
        modifier = modifier,
    ) {
        // 1) Gruppenliste
        composable(Routes.GROUPS) {
            GroupsRoute(
                onAddGroup = { navController.navigate(Routes.GROUP_CREATE) },
                onOpenGroup = { groupId -> navController.navigate(Routes.groupDetail(groupId)) },
                onEditGroup = { groupId -> navController.navigate(Routes.groupEdit(groupId)) },
                onPrepareDraw = { groupId -> navController.navigate(Routes.preparation(groupId)) },
            )
        }

        // 2) Formular: neue Gruppe
        composable(Routes.GROUP_CREATE) {
            GroupFormRoute(
                groupId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // 3) Formular: Gruppe bearbeiten
        composable(
            route = Routes.GROUP_EDIT,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID))
            GroupFormRoute(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // 4) Gruppendetail
        composable(
            route = Routes.GROUP_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID))
            GroupDetailRoute(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onEditGroup = { id -> navController.navigate(Routes.groupEdit(id)) },
                onOpenWishlist = { participantId ->
                    navController.navigate(
                        Routes.wishlist(
                            participantId
                        )
                    )
                },
                onPrepareDraw = { id -> navController.navigate(Routes.preparation(id)) }, // Auslosungs-Vorbereitung navigieren
                onOpenHistory = { id -> navController.navigate(Routes.drawHistory(id)) }, // zur Historie der Gruppe navigieren
            )
        }

        // 5) Wunschliste
        composable(
            route = Routes.WISHLIST,
            arguments = listOf(navArgument(Routes.ARG_PARTICIPANT_ID) {
                type = NavType.StringType
            }),
        ) { backStackEntry ->
            val participantId =
                requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_PARTICIPANT_ID))
            ParticipantWishlistRoute(
                participantId = participantId,
                onBack = { navController.popBackStack() },
            )
        }

        // 6) DrawDetail
        composable(
            route = Routes.DRAW_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_DRAW_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val drawId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_DRAW_ID))
            DrawDetailRoute(
                drawId = drawId,
                onBack = { navController.popBackStack() },

                )
        }

        // 7) DrawHistory
        composable(
            route = Routes.DRAW_HISTORY,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID))
            DrawHistoryRoute(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onOpenDraw = { drawId -> navController.navigate(Routes.drawDetail(drawId)) },
            )
        }

        // 8) Vorbereitung zum Losen
        composable(
            route = Routes.PREPARATION,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID))
            PreparationRoute(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onDrawStarted = { drawId -> navController.navigate(Routes.drawDetail(drawId)) },
            )
        }
    }
}
