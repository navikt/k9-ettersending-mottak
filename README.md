# K9 Ettersending Mottak
![CI / CD](https://github.com/navikt/k9-ettersending-mottak/workflows/CI%20/%20CD/badge.svg)
![NAIS Alerts](https://github.com/navikt/k9-ettersending-mottak/workflows/Alerts/badge.svg)

Tjeneste som tar imot melding om ettersending for omsorgspenger og pleiepenger, og legger de til til prosessering.
Mottar melding som REST API-kall. Legges videre på en Kafka Topic som tjenesten [k9-ettersending-prosessering](https://github.com/navikt/k9-ettersending-prosessering) prosesserer.
Tjenesten lagrer vedlegg slik at det kun er en referanse til vedleggene som legges på topic.

## Versjon 1
### Path
/v1/ettersend

### Meldingsformat
- Gir 202 response med SøknadId som entity på formatet ```{"id":"b3106960-0a85-4e02-9221-6ae057c8e93f"}```
- Må være gyldig JSON
- Må inneholde soker.fodselsnummer som er et gyldig fødselsnummer/D-nummer
- Må inneholde soker.aktoer_id som er en gyldig aktør ID
- Må inneholde en liste vedlegg som inneholder mist et vedlegg på gyldig format
- vedlegg[x].content må være Base64 encoded vedlegg.
- Utover dette validerer ikke tjenesten ytterligere felter som sendes om en del av meldingen.

```json
{
	"soker": {
        "aktoer_id": "1234567",
		"fodselsnummer": "290990123456"
	},
	"vedlegg": [{
		"content": "iVBORw0KGg....ayne82ZEAAAAASUVORK5CYII=",
		"content_type": "image/png",
		"title": "Legeerklæring"
	}]
}
```

### Format på melding lagt på kafka
attributten "data" er tilsvarende søknaden som kommer inn i REST-API'et med noen unntak:
- "vedlegg" er byttet ut med "vedlegg_urls" som peker på vedleggene mellomlagret i [k9-dokument](https://github.com/navikt/k9-dokument)
- "soknad_id" lagt til
- Alle andre felter som har vært en del av JSON-meldingen som kom inn i REST-API'et vil også være en del av "data"-attributten i Kafka-meldingen.

Se [her](https://navikt.github.io/k9-ettersending-mottak) for meldingsdefinisjon, med eksempler.

### Metadata
#### Correlation ID vs Request ID
Correlation ID blir propagert videre, og har ikke nødvendigvis sitt opphav hos konsumenten.
Request ID blir ikke propagert videre, og skal ha sitt opphav hos konsumenten om den settes.

#### REST API
- Correlation ID må sendes som header 'X-Correlation-ID'
- Request ID kan sendes som heder 'X-Request-ID'
- Versjon på meldingen avledes fra pathen '/v1/ettersending' -> 1

## Alarmer
Vi bruker [nais-alerts](https://doc.nais.io/observability/alerts) for å sette opp alarmer. Disse finner man konfigurert i [nais/alerts.yml](nais/alerterator.yml).

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

Interne henvendelser kan sendes via Slack i kanalen #team-düsseldorf.
