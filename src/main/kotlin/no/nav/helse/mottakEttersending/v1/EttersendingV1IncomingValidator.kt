import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.mottakEttersending.v1.EttersendingV1Incoming

internal fun EttersendingV1Incoming.validate() {
    val violations = mutableSetOf<Violation>()
    if (vedlegg.isEmpty()) {
        violations.add(
            Violation(
                parameterName = "vedlegg",
                parameterType = ParameterType.ENTITY,
                reason = "Det må sendes minst et vedlegg.",
                invalidValue = vedlegg
            )
        )
    }

    if (!søkerAktørId.id.erKunSiffer()) {
        violations.add(
            Violation(
                parameterName = "søker.aktørId",
                parameterType = ParameterType.ENTITY,
                reason = "Ikke gyldig Aktør ID.",
                invalidValue = søkerAktørId.id
            )
        )
    }

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}