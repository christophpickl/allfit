package allfit.view

import allfit.domain.Categories
import allfit.persistence.CategoriesRepo
import mu.KotlinLogging.logger
import tornadofx.Controller

class MainController : Controller() {

    private val categoriesRepo: CategoriesRepo by di()
    private val logger = logger {}

    fun search(query: String) {
        logger.debug { "Search: [$query]" }
    }

    fun loadCategories(): Categories {
        return categoriesRepo.load()
    }
}
