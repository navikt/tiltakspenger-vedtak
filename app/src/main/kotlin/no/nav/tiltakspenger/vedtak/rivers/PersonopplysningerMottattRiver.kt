package no.nav.tiltakspenger.vedtak.rivers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PersonopplysningerMottattRiver(
    private val søkerMediator: SøkerMediator,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf("personopplysninger"))
                it.demandKey("@løsning")
                it.requireKey("ident")
                it.requireKey("@opprettet")
                it.interestedIn("@løsning.personopplysninger.person")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info("Received personopplysninger")
        SECURELOG.info("Received personopplysninger for ident id: ${packet["ident"].asText()}")

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val personopplysningerMottattHendelse = PersonopplysningerMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["ident"].asText(),
            personopplysninger = mapPersonopplysninger(
                personopplysningerDTO = packet["@løsning.personopplysninger.person"].asObject(PersonopplysningerDTO::class.java),
                innhentet = packet["@opprettet"].asLocalDateTime(),
                ident = packet["ident"].asText(),
            )
        )

        søkerMediator.håndter(personopplysningerMottattHendelse)
    }

    //Hvorfor finnes ikke dette i r&r?
    private fun <T> JsonNode?.asObject(clazz: Class<T>): T = objectMapper.treeToValue(this, clazz)

    private fun mapPersonopplysninger(
        personopplysningerDTO: PersonopplysningerDTO,
        innhentet: LocalDateTime,
        ident: String
    ): Personopplysninger {
        return Personopplysninger(
            ident = ident,
            fødselsdato = personopplysningerDTO.fødselsdato,
            fornavn = personopplysningerDTO.fornavn,
            mellomnavn = personopplysningerDTO.mellomnavn,
            etternavn = personopplysningerDTO.etternavn,
            fortrolig = if (personopplysningerDTO.adressebeskyttelseGradering == AdressebeskyttelseGradering.FORTROLIG) true else false,
            strengtFortrolig = if (personopplysningerDTO.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG || personopplysningerDTO.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND) true else false,
            skjermet = null,
            innhentet = innhentet,
        )
    }
}
