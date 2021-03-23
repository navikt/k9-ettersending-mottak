package no.nav.helse.mottakEttersending.v1

import no.nav.helse.AktoerId
import no.nav.helse.SøknadId
import org.json.JSONObject
import java.net.URI
import java.util.*

internal object JsonKeys {
    internal const val søker = "søker"
    internal const val aktørId = "aktørId"
    internal const val søknadId = "søknadId"
    internal const val fødselsnummer = "fødselsnummer"
    internal const val vedleggUrls = "vedleggUrls"
    internal const val vedlegg = "vedlegg"
    internal const val content = "content"
    internal const val contentType = "contentType"
    internal const val title = "title"
    internal const val titler = "titler"
}

internal class EttersendingV1Incoming(json: String) {
    private val jsonObject = JSONObject(json)
    internal val vedlegg: List<Vedlegg>
    internal val søknadId: SøknadId?

    private fun hentVedlegg() : List<Vedlegg> {
        val vedlegg = mutableListOf<Vedlegg>()
        jsonObject.getJSONArray(JsonKeys.vedlegg).forEach {
            val vedleggJson = it as JSONObject
            vedlegg.add(
                Vedlegg(
                    content = Base64.getDecoder().decode(vedleggJson.getString(JsonKeys.content)),
                    contentType = vedleggJson.getString(JsonKeys.contentType),
                    title = vedleggJson.getString(JsonKeys.title)
                )
            )
        }
        return vedlegg.toList()
    }

    init {
        vedlegg = hentVedlegg()
        jsonObject.remove(JsonKeys.vedlegg)
        søknadId = hentSøknadId()
    }

    internal val søkerAktørId = AktoerId(jsonObject.getJSONObject(JsonKeys.søker).getString(
        JsonKeys.aktørId
    ))

    internal fun hentSøknadId(): SøknadId? = when (val søknadId = jsonObject.optString(JsonKeys.søknadId, "")) {
        "" -> null
        else -> SøknadId(søknadId)
    }

    internal fun medSoknadId(soknadId: SøknadId): EttersendingV1Incoming {
        jsonObject.put(JsonKeys.søknadId, soknadId.id)
        return this
    }

    internal fun medVedleggTitler() : EttersendingV1Incoming{
        val listeOverTitler = mutableListOf<String>()
        for(vedlegg in vedlegg){
            listeOverTitler.add(vedlegg.title)
        }
        jsonObject.put(JsonKeys.titler, listeOverTitler)
        return this
    }

    internal fun medVedleggUrls(vedleggUrls: List<URI>) : EttersendingV1Incoming {
        jsonObject.put(JsonKeys.vedleggUrls, vedleggUrls)
        return this
    }

    internal fun somOutgoing() =
        EttersendingV1Outgoing(jsonObject)
}

internal class EttersendingV1Outgoing(internal val jsonObject: JSONObject) {
    internal val soknadId = SøknadId(jsonObject.getString(JsonKeys.søknadId))
    internal val vedleggUrls = hentVedleggUrls()

    private fun hentVedleggUrls() : List<URI> {
        val vedleggUrls = mutableListOf<URI>()
        jsonObject.getJSONArray(JsonKeys.vedleggUrls).forEach {
            vedleggUrls.add(URI(it as String))
        }
        return vedleggUrls.toList()
    }
}

data class Vedlegg(
    val content: ByteArray,
    val contentType: String,
    val title: String
)
