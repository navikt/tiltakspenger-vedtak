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
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PersonopplysningerMottattRiver(
    private val innsendingMediator: InnsendingMediator,
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
                it.requireKey("journalpostId")
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
            journalpostId = packet["journalpostId"].asText(),
            personopplysninger = mapPersonopplysninger(
                dto = packet["@løsning.personopplysninger.person"].asObject(PersonopplysningerDTO::class.java),
                innhentet = packet["@opprettet"].asLocalDateTime(),
                ident = packet["ident"].asText(),
            ),
        )

        innsendingMediator.håndter(personopplysningerMottattHendelse)
    }

    //Hvorfor finnes ikke dette i r&r?
    private fun <T> JsonNode?.asObject(clazz: Class<T>): T = objectMapper.treeToValue(this, clazz)

    private fun mapPersonopplysninger(
        dto: PersonopplysningerDTO,
        innhentet: LocalDateTime,
        ident: String,
    ): List<Personopplysninger> {
        return dto.barn.filter { it.kanGiRettPåBarnetillegg() }.map {
            Personopplysninger.BarnMedIdent(
                ident = it.ident,
                fødselsdato = it.fødselsdato,
                fornavn = it.fornavn,
                mellomnavn = it.mellomnavn,
                etternavn = it.etternavn,
                fortrolig = it.adressebeskyttelseGradering == AdressebeskyttelseGradering.FORTROLIG,
                strengtFortrolig = it.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG,
                strengtFortroligUtland = dto.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND,
                oppholdsland = null, // TODO: fix!
                tidsstempelHosOss = innhentet,
            )
        } + dto.barnUtenFolkeregisteridentifikator.filter { it.kanGiRettPåBarnetillegg() }.map { barn ->
            Personopplysninger.BarnUtenIdent(
                fødselsdato = barn.fødselsdato,
                fornavn = barn.fornavn,
                mellomnavn = barn.mellomnavn,
                etternavn = barn.etternavn,
                tidsstempelHosOss = innhentet,
            )
        } + Personopplysninger.Søker(
            ident = ident,
            fødselsdato = dto.fødselsdato,
            fornavn = dto.fornavn,
            mellomnavn = dto.mellomnavn,
            etternavn = dto.etternavn,
            fortrolig = dto.adressebeskyttelseGradering == AdressebeskyttelseGradering.FORTROLIG,
            strengtFortrolig = dto.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG,
            strengtFortroligUtland = dto.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND,
            skjermet = null,
            kommune = dto.gtKommune,
            bydel = dto.gtBydel,
            tidsstempelHosOss = innhentet,
        )
    }
}
