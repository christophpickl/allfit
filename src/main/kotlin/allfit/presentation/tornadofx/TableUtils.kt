package allfit.presentation.tornadofx

import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.Rating
import allfit.presentation.renderStars
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.image.Image
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.imageview
import tornadofx.label

fun <T, S> TableView<S>.applyInitSort(column: TableColumn<S, T>) {
    column.isSortable = true
    column.sortType = TableColumn.SortType.ASCENDING
    sortOrder.add(column)
    sort()
}

fun <S> TableView<S>.imageColumn(
    maxWidth: Double,
    padding: Int = 10,
    title: String = "",
    withTableCell: TableCell<S, Image>.() -> Unit = {},
    valueProvider: (TableColumn.CellDataFeatures<S, Image>) -> ObservableValue<Image>,
) {
    column(title, valueProvider).fixedWidth(maxWidth + padding)
        .cellFormat {
            withTableCell()
            graphic = imageview(it) {
                fitWidth = maxWidth
                isPreserveRatio = true
            }
        }
}

fun <S> TableView<S>.imageColumn(
    title: String,
    image: Image,
    valueProvider: (TableColumn.CellDataFeatures<S, Boolean>) -> ObservableValue<Boolean>
) {
    column(title, valueProvider).fixedWidth(60)
        .cellFormat { isTrue ->
            if (isTrue) {
                alignment = Pos.CENTER
                graphic = imageview(image)
            }
        }
}

fun <S> TableView<S>.ratingColumn(
    valueProvider: (TableColumn.CellDataFeatures<S, Rating>) -> ObservableValue<Rating>
) {
    column("Rating", valueProvider)
        .fixedWidth(80)
        .cellFormat { rating ->
            graphic = label(rating.renderStars())
        }
}

fun <S> TableView<S>.favoriteColumn(
    valueProvider: (TableColumn.CellDataFeatures<S, Boolean>) -> ObservableValue<Boolean>
) {
    column("Favorite", valueProvider).fixedWidth(60)
        .cellFormat { isFavorite ->
            graphic =
                imageview(StaticIconStorage.get(if (isFavorite) StaticIcon.FavoriteFull else StaticIcon.FavoriteOutline)) {
                    alignment = Pos.CENTER
                }
        }
}

fun <S> TableView<S>.wishlistColumn(
    valueProvider: (TableColumn.CellDataFeatures<S, Boolean>) -> ObservableValue<Boolean>
) {

    column<S, Boolean>("Wishlist", valueProvider).fixedWidth(60)
        .cellFormat { isWishlisted ->
            graphic =
                imageview(StaticIconStorage.get(if (isWishlisted) StaticIcon.WishlistFull else StaticIcon.WishlistOutline)) {
                    alignment = Pos.CENTER
                }
        }
}