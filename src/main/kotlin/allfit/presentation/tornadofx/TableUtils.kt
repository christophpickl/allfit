package allfit.presentation.tornadofx

import allfit.presentation.logic.StaticIcon
import allfit.presentation.logic.StaticIconStorage
import allfit.presentation.models.Rating
import allfit.presentation.renderStars
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.image.Image
import kotlin.reflect.KProperty1
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.hbox
import tornadofx.imageview
import tornadofx.label
import tornadofx.readonlyColumn

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

private fun <S> TableView<S>.iconColumn(
    title: String,
    valueProvider: (TableColumn.CellDataFeatures<S, Boolean>) -> ObservableValue<Boolean>,
    iconTrue: StaticIcon,
    iconFalse: StaticIcon,
) {
    column(title, valueProvider).fixedWidth(60)
        .cellFormat { isTrue ->
            graphic =
                imageview(StaticIconStorage.get(if (isTrue) iconTrue else iconFalse)) {
                    alignment = Pos.CENTER
                }
        }
}

fun <S> TableView<S>.favoriteColumn(valueProvider: (TableColumn.CellDataFeatures<S, Boolean>) -> ObservableValue<Boolean>) {
    iconColumn("Favorite", valueProvider, StaticIcon.FavoriteFull, StaticIcon.FavoriteOutline)
}

fun <S> TableView<S>.wishlistColumn(valueProvider: (TableColumn.CellDataFeatures<S, Boolean>) -> ObservableValue<Boolean>) {
    iconColumn("Wishlist", valueProvider, StaticIcon.WishlistFull, StaticIcon.WishlistOutline)
}

interface Imageable {
    val image: Image
}

inline fun <reified S, IMG : Imageable> TableView<S>.iconsColumn(
    getter: KProperty1<S, ObservableList<IMG>>,
    width: Int
) {
    readonlyColumn("Icons", getter)
        .fixedWidth(width)
        .cellFormat { icons: ObservableList<IMG> ->
            graphic = hbox(spacing = 10.0) {
                icons.map { icon ->
                    imageview(icon.image) {
                        alignment = Pos.CENTER_LEFT
                    }
                }
            }
        }
}
