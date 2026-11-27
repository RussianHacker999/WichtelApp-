package com.example.wichtly.ui.draws.detail.screen

import com.example.wichtly.domain.model.group.DrawType
import java.time.LocalDateTime

/**
 * UI model of a saved draw for the detail screen.
 *
 * @property groupName current name of the gift group (deliberately not a snapshot, see DrawDetailViewModel).
 *   null = the name could not be loaded; the UI then shows the placeholder from strings.xml.
 * @property drawnAt date and time of the draw (local time zone).
 * @property assignments the assignments (who gives to whom) of this draw.
 */
data class DrawDetailUiModel(
    val id: String,
    val groupId: String,
    val groupName: String?,
    val drawnAt: LocalDateTime,
    val drawType: DrawType,
    val assignments: List<DrawAssignmentUiModel>,
)

/**
 * UI model of a single assignment (giver -> receiver), including the receiver's wishlist at draw time.
 *
 * @property id primary key of the assignment; a unique list key even if a person appeared twice.
 * @property wishes the receiver's wish texts at draw time (snapshot).
 */
data class DrawAssignmentUiModel(
    val id: String,
    val giverId: String,
    val giverName: String,
    val receiverId: String,
    val receiverName: String,
    val wishes: List<String>,
)
