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
import com.example.wichtly.ui.groups.list.state.GroupsRoute
import com.example.wichtly.ui.groups.detail.state.GroupDetailRoute
import com.example.wichtly.ui.groups.form.state.GroupFormRoute
import com.example.wichtly.ui.wishlist.state.ParticipantWishlistRoute
import com.example.wichtly.ui.draws.detail.state.DrawDetailRoute
import com.example.wichtly.ui.draws.history.state.DrawHistoryRoute
import com.example.wichtly.ui.draws.preparation.state.PreparationRoute
import com.example.wichtly.ui.start.state.StartRoute
import com.example.wichtly.logging.AppLog

private const val TAG = "Navigation"

/**
 * Navigation der App.
 *
 * Schutz vor doppelten Klicks: Alle Navigationen, die ein Nutzer per Tippen auslöst, sind in
 * [dropUnlessResumed] bzw. [dropUnlessResumedWithArg] gekapselt. Während der Übergangsanimation
 * ist der alte Screen noch sichtbar und antippbar, sein Back-Stack-Eintrag aber nicht mehr RESUMED.
 * Ein zweiter Tipp auf "Zurück" würde sonst einen weiteren Eintrag entfernen (bis hin zur leeren
 * Startdestination), ein zweiter Tipp auf eine Karte das Ziel doppelt auf den Stack legen.
 *
 * Nicht gekapselt sind Navigationen, die aus einem Ereignis folgen (onSaved, onGroupDeleted,
 * onDrawStarted): Sie werden genau einmal vom ViewModel bzw. einem LaunchedEffect ausgelöst.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // Jeden Zielwechsel protokollieren (nur das Routen-Muster, keine Argumente)
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            AppLog.d(TAG) { "Ziel: ${destination.route}" }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.START,
        modifier = modifier,
    ) {
        // 0) Startseite (großer Schriftzug, führt zur Gruppenliste oder zum Formular)
        composable(Routes.START) {
            StartRoute(
                onOpenGroups = dropUnlessResumed { navController.navigate(Routes.GROUPS) { launchSingleTop = true } },
                onCreateGroup = dropUnlessResumed { navController.navigate(Routes.GROUP_CREATE) },
            )
        }

        // 1) Gruppenliste
        composable(Routes.GROUPS) {
            GroupsRoute(
                onAddGroup = dropUnlessResumed { navController.navigate(Routes.GROUP_CREATE) },
                onOpenGroup = dropUnlessResumedWithArg { groupId -> navController.navigate(Routes.groupDetail(groupId)) },
                onEditGroup = dropUnlessResumedWithArg { groupId -> navController.navigate(Routes.groupEdit(groupId)) },
                onPrepareDraw = dropUnlessResumedWithArg { groupId -> navController.navigate(Routes.preparation(groupId)) },
            )
        }

        // 2) Formular: neue Gruppe
        composable(Routes.GROUP_CREATE) {
            GroupFormRoute(
                groupId = null,
                onBack = dropUnlessResumed { navController.popBackStack() },
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
                onBack = dropUnlessResumed { navController.popBackStack() },
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
                onBack = dropUnlessResumed { navController.popBackStack() },
                onEditGroup = dropUnlessResumedWithArg { id -> navController.navigate(Routes.groupEdit(id)) },
                onOpenWishlist = dropUnlessResumedWithArg { participantId -> navController.navigate(Routes.wishlist(participantId)) },
                onPrepareDraw = dropUnlessResumedWithArg { id -> navController.navigate(Routes.preparation(id)) }, // Auslosungs-Vorbereitung navigieren
                onOpenHistory = dropUnlessResumedWithArg { id -> navController.navigate(Routes.drawHistory(id)) }, // zur Historie der Gruppe navigieren
                // Gruppe gelöscht: zurück dorthin, woher man kam (Gruppenliste oder Startseite)
                onGroupDeleted = { navController.popBackStack() },
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
                onBack = dropUnlessResumed { navController.popBackStack() },
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
                onBack = dropUnlessResumed { navController.popBackStack() },

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
                onBack = dropUnlessResumed { navController.popBackStack() },
                onOpenDraw = dropUnlessResumedWithArg { drawId -> navController.navigate(Routes.drawDetail(drawId)) },
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
                onBack = dropUnlessResumed { navController.popBackStack() },
                onDrawStarted = { drawId ->
                    // Nach dem Losen darf "Zurück" nicht mehr in die Vorbereitung führen, sondern ins Gruppendetail.
                    // Vorbereitung vom Stack nehmen; liegt das Gruppendetail schon darunter, wird es wiederverwendet
                    // (launchSingleTop), sonst (Start über die Gruppenliste) neu darunter gelegt.
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
 * Wie [dropUnlessResumed], aber für Callbacks mit einem Argument (z. B. eine Gruppen-ID).
 * Der Klick wird ignoriert, solange der aktuelle Back-Stack-Eintrag nicht RESUMED ist.
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
