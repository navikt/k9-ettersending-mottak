package no.nav.helse

import io.ktor.config.*
import no.nav.helse.dusseldorf.ktor.auth.issuers
import no.nav.helse.dusseldorf.ktor.auth.withoutAdditionalClaimRules
import no.nav.helse.dusseldorf.ktor.core.getOptionalString
import no.nav.helse.dusseldorf.ktor.core.getRequiredList
import no.nav.helse.dusseldorf.ktor.core.getRequiredString
import no.nav.helse.kafka.KafkaConfig
import java.net.URI

data class Configuration(private val config : ApplicationConfig) {

    private val issuers = config.issuers()

    init {
        if (issuers.isEmpty()) throw IllegalStateException("Må konfigureres opp minst en issuer.")
    }

    internal fun issuers() = issuers.withoutAdditionalClaimRules()

    internal fun getK9DokumentBaseUrl() = URI(config.getRequiredString("nav.k9_dokument_base_url", secret = false))

    internal fun getKafkaConfig() = config.getRequiredString("nav.kafka.bootstrap_servers", secret = false).let { bootstrapServers ->
        val trustStore = config.getOptionalString("nav.kafka.truststore_path", secret = false)?.let { trustStorePath ->
            config.getOptionalString("nav.kafka.credstore_password", secret = true)?.let { credstorePassword ->
                Pair(trustStorePath, credstorePassword)
            }
        }

        val keyStore = config.getOptionalString("nav.kafka.keystore_path", secret = false)?.let { keystorePath ->
            config.getOptionalString("nav.kafka.credstore_password", secret = true)?.let { credstorePassword ->
                Pair(keystorePath, credstorePassword)
            }
        }

        KafkaConfig(
            bootstrapServers = bootstrapServers,
            trustStore = trustStore,
            keyStore = keyStore
        )
    }

    private fun getScopesFor(operation: String) = config.getRequiredList("nav.auth.scopes.$operation", secret = false, builder = { it }).toSet()
    internal fun getLagreDokumentScopes() = getScopesFor("lagre-dokument")
}