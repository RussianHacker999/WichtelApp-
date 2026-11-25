package com.example.wichtly.navigation

/**
 * Alle Navigationsziele der App.
 *
 * Die Konstanten sind die Routen-Vorlagen für den NavHost (mit Platzhaltern wie {groupId}),
 * die Funktionen bauen daraus die konkrete Adresse für navigate(). RoutesTest prüft, dass
 * Vorlage und Funktion zusammenpassen.
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
