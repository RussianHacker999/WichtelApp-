package com.example.wichtly.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavBackStackEntry
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


@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // Log every destination change (route pattern only, no arguments)
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
        // 0) Start screen (large title, leads to the group list or the group form)
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

        // 2) Form: create a new group
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

        // 3) Form: edit an existing group
        composable(
            route = Routes.GROUP_EDIT,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            WithArgument(backStackEntry, Routes.ARG_GROUP_ID, navController) { groupId ->
                GroupFormRoute(
                    groupId = groupId,
                    onBack = dropUnlessResumed { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
        }

        // 4) Group detail
        composable(
            route = Routes.GROUP_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            WithArgument(backStackEntry, Routes.ARG_GROUP_ID, navController) { groupId ->
                GroupDetailRoute(
                    groupId = groupId,
                    onBack = dropUnlessResumed { navController.popBackStack() },
                    onEditGroup = dropUnlessResumedWithArg { id -> navController.navigate(Routes.groupEdit(id)) },
                    onOpenWishlist = dropUnlessResumedWithArg { participantId -> navController.navigate(Routes.wishlist(participantId)) },
                    onPrepareDraw = dropUnlessResumedWithArg { id -> navController.navigate(Routes.preparation(id)) }, // go to draw preparation
                    onOpenHistory = dropUnlessResumedWithArg { id -> navController.navigate(Routes.drawHistory(id)) }, // go to the group's draw history
                    // Group deleted: go back to where the user came from (group list or start screen)
                    onGroupDeleted = { navController.popBackStack() },
                )
            }
        }

        // 5) Wishlist of a participant
        composable(
            route = Routes.WISHLIST,
            arguments = listOf(navArgument(Routes.ARG_PARTICIPANT_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            WithArgument(backStackEntry, Routes.ARG_PARTICIPANT_ID, navController) { participantId ->
                ParticipantWishlistRoute(
                    participantId = participantId,
                    onBack = dropUnlessResumed { navController.popBackStack() },
                )
            }
        }

        // 6) Draw detail (reveal who gives a gift to whom)
        composable(
            route = Routes.DRAW_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_DRAW_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            WithArgument(backStackEntry, Routes.ARG_DRAW_ID, navController) { drawId ->
                DrawDetailRoute(
                    drawId = drawId,
                    onBack = dropUnlessResumed { navController.popBackStack() },
                )
            }
        }

        // 7) Draw history of a group
        composable(
            route = Routes.DRAW_HISTORY,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            WithArgument(backStackEntry, Routes.ARG_GROUP_ID, navController) { groupId ->
                DrawHistoryRoute(
                    groupId = groupId,
                    onBack = dropUnlessResumed { navController.popBackStack() },
                    onOpenDraw = dropUnlessResumedWithArg { drawId -> navController.navigate(Routes.drawDetail(drawId)) },
                )
            }
        }

        // 8) Draw preparation
        composable(
            route = Routes.PREPARATION,
            arguments = listOf(navArgument(Routes.ARG_GROUP_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            WithArgument(backStackEntry, Routes.ARG_GROUP_ID, navController) { groupId ->
                PreparationRoute(
                    groupId = groupId,
                    onBack = dropUnlessResumed { navController.popBackStack() },
                    onDrawStarted = { drawId ->
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
}

/**
 * Like [dropUnlessResumed], but for callbacks with one argument (e.g. a group ID).
 * The click is ignored as long as the current back stack entry is not RESUMED.
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

/**
 * Reads a required argument from the current route.
 * If present, the screen is shown. If missing, the error is logged and the user
 * is navigated back instead of crashing the app.
 */
@Composable
private fun WithArgument(
    backStackEntry: NavBackStackEntry,
    key: String,
    navController: NavController,
    content: @Composable (String) -> Unit,
) {
    // Argument found: show the screen with the value
    backStackEntry.arguments?.getString(key)?.let { value ->
        content(value)
    } ?: run {
        // Argument missing: navigation must not happen during composition, so use a LaunchedEffect
        LaunchedEffect(key) {
            AppLog.e(TAG, "Missing navigation argument '$key' for ${backStackEntry.destination.route}")
            navController.popBackStack()
        }
    }
}
