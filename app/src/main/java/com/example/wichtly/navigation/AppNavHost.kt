package com.example.wichtly.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wichtly.logging.AppLog
import com.example.wichtly.ui.draws.detail.state.DrawDetailRoute
import com.example.wichtly.ui.draws.history.state.DrawHistoryRoute
import com.example.wichtly.ui.draws.preparation.state.PreparationRoute
import com.example.wichtly.ui.groups.detail.state.GroupDetailRoute
import com.example.wichtly.ui.groups.form.state.GroupFormRoute
import com.example.wichtly.ui.groups.list.state.GroupsRoute
import com.example.wichtly.ui.start.state.StartRoute
import com.example.wichtly.ui.wishlist.state.ParticipantWishlistRoute

private const val TAG = "AppNavHost"

/**
 * Navigation graph of the app.
 *
 * Protection against double taps: every navigation a user triggers by tapping is wrapped in
 * [dropUnlessResumed] or [dropUnlessResumedWithArg]. During the transition animation the old screen
 * is still visible and tappable, but its back stack entry is no longer RESUMED. Otherwise a second
 * tap on "Back" would remove another entry (down to an empty start destination), and a second tap
 * on a card would put the destination on the stack twice.
 *
 * Navigations that follow from an event (onSaved, onGroupDeleted, onDrawStarted) are not wrapped:
 * they are triggered exactly once by the ViewModel or a LaunchedEffect.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    // Log every destination change (only the route pattern, no arguments)
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            AppLog.d(TAG) { "Destination: ${destination.route}" }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.START,
        modifier = modifier,
    ) {
        // 0) Start screen (big wordmark, leads to the group list or the form)
        composable(Routes.START) {
            StartRoute(
                onOpenGroups = dropUnlessResumed { navController.navigate(Routes.GROUPS) { launchSingleTop = true } },
                onCreateGroup = dropUnlessResumed { navController.navigate(Routes.GROUP_CREATE) },
            )
        }

        // 1) Group list
        composable(Routes.GROUPS) {
            GroupsRoute(
                onAddGroup = dropUnlessResumed { navController.navigate(Routes.GROUP_CREATE) },
                onOpenGroup = dropUnlessResumedWithArg { groupId -> navController.navigate(Routes.groupDetail(groupId)) },
                onEditGroup = dropUnlessResumedWithArg { groupId -> navController.navigate(Routes.groupEdit(groupId)) },
                onPrepareDraw = dropUnlessResumedWithArg { groupId -> navController.navigate(Routes.preparation(groupId)) },
            )
        }

        // 2) Form: new group
        composable(Routes.GROUP_CREATE) {
            GroupFormRoute(
                groupId = null,
                onBack = dropUnlessResumed { navController.popBackStack() },
                onSaved = { groupId ->
                    // After creating, go straight to the new group; the form is removed from the back stack
                    navController.navigate(Routes.groupDetail(groupId)) {
                        popUpTo(Routes.GROUP_CREATE) { inclusive = true }
                    }
                },
            )
        }

        // 3) Form: edit group
        composable(
            route = Routes.GROUP_EDIT,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID))
            GroupFormRoute(
                groupId = groupId,
                onBack = dropUnlessResumed { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        // 4) Group detail
        composable(
            route = Routes.GROUP_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID))
            GroupDetailRoute(
                groupId = groupId,
                onBack = dropUnlessResumed { navController.popBackStack() },
                onEditGroup = dropUnlessResumedWithArg { id -> navController.navigate(Routes.groupEdit(id)) },
                onOpenWishlist = dropUnlessResumedWithArg { participantId -> navController.navigate(Routes.wishlist(participantId)) },
                onPrepareDraw = dropUnlessResumedWithArg { id -> navController.navigate(Routes.preparation(id)) },
                onOpenHistory = dropUnlessResumedWithArg { id -> navController.navigate(Routes.drawHistory(id)) },
                // Group deleted: go back to where the user came from (group list or start screen)
                onGroupDeleted = { navController.popBackStack() },
            )
        }

        // 5) Wishlist
        composable(
            route = Routes.WISHLIST,
            arguments = listOf(
                navArgument(Routes.ARG_PARTICIPANT_ID) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val participantId =
                requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_PARTICIPANT_ID))
            ParticipantWishlistRoute(
                participantId = participantId,
                onBack = dropUnlessResumed { navController.popBackStack() },
            )
        }

        // 6) Draw detail
        composable(
            route = Routes.DRAW_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_DRAW_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val drawId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_DRAW_ID))
            DrawDetailRoute(
                drawId = drawId,
                onBack = dropUnlessResumed { navController.popBackStack() },

            )
        }

        // 7) Draw history
        composable(
            route = Routes.DRAW_HISTORY,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID))
            DrawHistoryRoute(
                groupId = groupId,
                onBack = dropUnlessResumed { navController.popBackStack() },
                onOpenDraw = dropUnlessResumedWithArg { drawId -> navController.navigate(Routes.drawDetail(drawId)) },
            )
        }

        // 8) Draw preparation
        composable(
            route = Routes.PREPARATION,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val groupId = requireNotNull(backStackEntry.arguments?.getString(Routes.ARG_GROUP_ID))
            PreparationRoute(
                groupId = groupId,
                onBack = dropUnlessResumed { navController.popBackStack() },
                onDrawStarted = { drawId ->
                    // After the draw, "Back" must lead to the group detail, not to the preparation.
                    // Remove the preparation from the stack; if the group detail is already below it, it is
                    // reused (launchSingleTop), otherwise (started from the group list) it is placed below anew.
                    navController.navigate(Routes.groupDetail(groupId)) {
                        popUpTo(Routes.PREPARATION) { inclusive = true }
                        launchSingleTop = true
                    }
                    navController.navigate(Routes.drawDetail(drawId))
                },
            )
        }
    }
}

/**
 * Like [dropUnlessResumed], but for callbacks with one argument (e.g. a group ID).
 *
 * The click is ignored while the current back stack entry is not RESUMED.
 */
@Composable
private fun <T> dropUnlessResumedWithArg(block: (T) -> Unit): (T) -> Unit {
    val lifecycleOwner = LocalLifecycleOwner.current
    return { value ->
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            block(value)
        }
    }
}
