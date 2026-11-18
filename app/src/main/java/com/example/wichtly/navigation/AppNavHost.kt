package com.example.wichtly.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wichtly.ui.GroupsScreen
import com.example.wichtly.ui.groups.GroupsUiState
import com.example.wichtly.ui.groups.detail.GroupDetailRoute
import com.example.wichtly.ui.groups.form.GroupFormRoute
import com.example.wichtly.ui.wishlist.ParticipantWishlistRoute

object Routes {
    const val ARG_GROUP_ID = "groupId"
    const val ARG_PARTICIPANT_ID = "participantId"

    const val GROUPS = "groups"
    const val GROUP_CREATE = "group-form"
    const val GROUP_EDIT = "group-form/{$ARG_GROUP_ID}"
    const val GROUP_DETAIL = "group-detail/{$ARG_GROUP_ID}"
    const val WISHLIST = "wishlist/{$ARG_PARTICIPANT_ID}"

    fun groupEdit(groupId: String) = "group-form/$groupId"
    fun groupDetail(groupId: String) = "group-detail/$groupId"
    fun wishlist(participantId: String) = "wishlist/$participantId"
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
            // TODO: Sobald Person C GroupsScreen um Callbacks erweitert hat, hier verdrahten
            GroupsScreen(uiState = GroupsUiState())
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
                onOpenWishlist = { participantId -> navController.navigate(Routes.wishlist(participantId)) },
            )
        }

        // 5) Wunschliste
        composable(
            route = Routes.WISHLIST,
            arguments = listOf(navArgument(Routes.ARG_PARTICIPANT_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val participantId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_PARTICIPANT_ID))
            ParticipantWishlistRoute(
                participantId = participantId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}