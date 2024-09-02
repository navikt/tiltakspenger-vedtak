package no.nav.tiltakspenger.vedtak.clients.person

import arrow.core.getOrElse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklient
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import java.time.LocalDateTime

internal class PersonHttpklient(
    endepunkt: String,
    private val azureTokenProvider: AzureTokenProvider,
) : PersonGateway {
    private val personklient =
        FellesPersonklient.create(
            endepunkt = endepunkt,
        )

    private val objectMapper: ObjectMapper =
        JsonMapper
            .builder()
            .addModule(JavaTimeModule())
            .addModule(KotlinModule.Builder().build())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
            .build()

    /**
     * Benytter seg av [AzureTokenProvider] for å hente token for å hente personopplysninger vha. systembruker.
     * TODO pre-mvp jah: Dersom vi ønsker og sende saksbehandler sitt OBO-token, kan vi lage en egen metode for dette.
     */
    override suspend fun hentPerson(fnr: Fnr): List<Personopplysninger> {
        val token = AccessToken(azureTokenProvider.getToken())
        val body = objectMapper.writeValueAsString(hentPersonQuery(fnr))
        return personklient
            .hentPerson(fnr, token, body)
            .map { mapPersonopplysninger(it, LocalDateTime.now(), fnr) }
            .getOrElse { it.mapError() }
    }
}
