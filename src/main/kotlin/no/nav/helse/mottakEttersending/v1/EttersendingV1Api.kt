package no.nav.helse.mottakEttersending.v1

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.helse.Metadata
import no.nav.helse.SøknadId
import no.nav.helse.getSøknadId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import validate

private val logger: Logger = LoggerFactory.getLogger("no.nav.SoknadV1Api")

internal fun Route.SoknadV1Api(
    ettersendingV1MottakService: EttersendingV1MottakService
) {

    post("v1/ettersend") {
        val metadata = call.metadata()
        val søknad = withContext(Dispatchers.IO) {call.soknadEttersending()}
        val søknadId: SøknadId = søknad.søknadId ?: call.getSøknadId()

        ettersendingV1MottakService.leggTilProsessering(
            soknadId = søknadId,
            metadata = metadata,
            soknad = søknad
        )
        call.respond(HttpStatusCode.Accepted, mapOf("id" to søknadId.id))
    }
}

private suspend fun ApplicationCall.soknadEttersending() : EttersendingV1Incoming {
    val json = receiveStream().use { String(it.readAllBytes(), Charsets.UTF_8) }
    val incoming = EttersendingV1Incoming(json)
    incoming.validate()
    return incoming
}

private fun ApplicationCall.metadata() = Metadata(
    version = 1,
    correlationId = request.getCorrelationId()
)


private fun ApplicationRequest.getCorrelationId(): String {
    return header(HttpHeaders.XCorrelationId) ?: throw IllegalStateException("Correlation Id ikke satt")
}

private fun ApplicationResponse.getRequestId(): String {
    return headers[HttpHeaders.XRequestId] ?: throw IllegalStateException("Request Id ikke satt")
}
