package allfit.service

import allfit.api.PagedParams
import allfit.api.models.MetaJson
import allfit.api.models.PagedJson
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode

private val log = logger("allfit.service.KtorUtils")

suspend fun <ITEM, ROOT : PagedJson<ITEM>, PARAMS : PagedParams<PARAMS>> getPageUntilExhausted(
    originalParams: PARAMS,
    requestor: suspend (PARAMS) -> ROOT
): List<ITEM> {
    val data = mutableListOf<ITEM>()
    var currentParams = originalParams
    var lastMeta: MetaJson
    do {
        val result = requestor(currentParams)
        data += result.data
        currentParams = currentParams.nextPage()
        lastMeta = result.meta
        log.debug { "Received meta info for page: ${lastMeta.pagination}" }
    } while (currentParams.page <= lastMeta.pagination.total_pages)
    return data
}

suspend fun HttpResponse.requireOk() {
    if (status != HttpStatusCode.OK) {
        System.err.println(bodyAsText())
        error("Invalid status code: $status requesting ${request.url}")
    }
}
