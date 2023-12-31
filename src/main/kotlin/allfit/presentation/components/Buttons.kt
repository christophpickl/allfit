package allfit.presentation.components

import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import javafx.event.EventTarget
import javafx.scene.control.ToggleButton

fun EventTarget.favoriteToggleButton(
    op: ToggleButton.() -> Unit = {}
): ToggleButton =
    imagedToggleButton(
        effect = ImageToggleEffect.Saturation,
        imageTrue = StaticIconStorage.get(StaticIcon.FavoriteFull),
        imageFalse = StaticIconStorage.get(StaticIcon.FavoriteOutline),
        op = op,
    )

fun EventTarget.wishlistToggleButton(
    op: ToggleButton.() -> Unit = {}
): ToggleButton =
    imagedToggleButton(
        effect = ImageToggleEffect.Saturation,
        imageTrue = StaticIconStorage.get(StaticIcon.WishlistFull),
        imageFalse = StaticIconStorage.get(StaticIcon.WishlistOutline),
        op = op,
    )
