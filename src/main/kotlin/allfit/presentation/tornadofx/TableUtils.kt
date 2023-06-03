package allfit.presentation.tornadofx

import allfit.presentation.PresentationConstants
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.image.Image
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.imageview

fun <S> TableView<S>.imageColumn(valueProvider: (TableColumn.CellDataFeatures<S, Image>) -> ObservableValue<Image>) {
    column("", valueProvider).fixedWidth(PresentationConstants.tableImageWidth + 10)
        .cellFormat {
            graphic = imageview(it) {
                fitWidth = PresentationConstants.tableImageWidth
                isPreserveRatio = true
            }
        }
}

fun <T, S> TableView<S>.applyInitSort(column: TableColumn<S, T>) {
    column.isSortable = true
    column.sortType = TableColumn.SortType.ASCENDING
    sortOrder.add(column)
    sort()
}
