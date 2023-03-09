package allfit.view

import allfit.domain.Category
import allfit.service.DataStorage
import mu.KotlinLogging.logger
import tornadofx.Controller

class MainController : Controller() {

    private val dataStorage: DataStorage by di()
    private val logger = logger {}

    fun search(query: String) {
        logger.debug { "Search: [$query]" }
    }

    fun loadCategories(): List<Category> {
        return dataStorage.getCategories()
    }
}
