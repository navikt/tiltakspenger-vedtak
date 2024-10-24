package no.nav.tiltakspenger.vedtak.clients.person

import arrow.core.getOrElse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.tiltakspenger.felles.sikkerlogg
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklient
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.EnkelPerson
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway

class PersonHttpklient(
    endepunkt: String,
    private val getToken: suspend () -> AccessToken,
) : PersonGateway {
    private val personklient =
        FellesPersonklient.create(
            endepunkt = endepunkt,
            sikkerlogg = sikkerlogg,
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
     * TODO post-mvp jah: Dersom vi ønsker og sende saksbehandler sitt OBO-token, kan vi lage en egen metode for dette.
     */
    override suspend fun hentPerson(fnr: Fnr): PersonopplysningerSøker {
        return withContext(Dispatchers.IO) {
            val body = objectMapper.writeValueAsString(hentPersonQuery(fnr))
            personklient
                .hentPerson(fnr, getToken(), body)
                .map { mapPersonopplysninger(it, fnr) }
                .getOrElse { it.mapError() }
        }
    }

    override suspend fun hentEnkelPerson(fnr: Fnr): EnkelPerson {
        return withContext(Dispatchers.IO) {
            val body = objectMapper.writeValueAsString(hentEnkelPersonQuery(fnr))
            personklient.hentPerson(fnr, getToken(), body).map { it.toEnkelPerson(fnr) }.getOrElse { it.mapError() }
        }
    }
}
