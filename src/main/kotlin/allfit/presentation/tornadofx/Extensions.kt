package allfit.presentation.tornadofx

import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Region
import javafx.scene.web.WebView

fun WebView.setAllHeights(heights: Double) {
    maxHeight = heights
    minHeight = heights
    prefHeight = heights
}

fun Region.setAllHeights(heights: Double) {
    maxHeight = heights
    minHeight = heights
    prefHeight = heights
}

fun <T, S> TableView<S>.applyInitSort(column: TableColumn<S, T>) {
    column.isSortable = true
    column.sortType = TableColumn.SortType.ASCENDING
    sortOrder.add(column)
    sort()
}
