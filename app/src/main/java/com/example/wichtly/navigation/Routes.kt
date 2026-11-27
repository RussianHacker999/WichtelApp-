package com.example.wichtly.navigation

/**
 * All navigation destinations of the app.
 *
 * The constants are the route templates for the NavHost (with placeholders such as {groupId});
 * the functions build the concrete address for navigate() from them. RoutesTest checks that
 * template and function match.
 */
object Routes {
    const val ARG_GROUP_ID = "groupId"
    const val ARG_DRAW_ID = "drawId"
    const val ARG_PARTICIPANT_ID = "participantId"
    const val START = "start"
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
