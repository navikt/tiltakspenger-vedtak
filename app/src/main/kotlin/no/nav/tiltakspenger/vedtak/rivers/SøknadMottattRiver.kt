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
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import java.time.LocalDate
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class SøknadMottattRiver(
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
                it.demandValue("@event_name", "søknad_mottatt")
                it.requireKey("@id")
                it.requireKey("@opprettet")
                it.requireKey("søknad")
                it.requireKey("søknad.ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        LOG.info("Received søknad")
        SECURELOG.info("Received søknad: ${packet.toJson()}")

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val søknadMottattHendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["søknad.ident"].asText(),
            søknad = mapSøknad(
                søknadDTO = packet["søknad"].asObject(SøknadDTO::class.java),
                innhentet = packet["@opprettet"].asLocalDateTime()
            )
        )

        søkerMediator.håndter(søknadMottattHendelse)
    }

    //Hvorfor finnes ikke dette i r&r?
    private fun <T> JsonNode?.asObject(clazz: Class<T>): T = objectMapper.treeToValue(this, clazz)

    private fun mapSøknad(søknadDTO: SøknadDTO, innhentet: LocalDateTime): Søknad {
        return Søknad(
            id = søknadDTO.id,
            fornavn = søknadDTO.fornavn,
            etternavn = søknadDTO.etternavn,
            ident = søknadDTO.ident,
            deltarKvp = søknadDTO.deltarKvp,
            deltarIntroduksjonsprogrammet = søknadDTO.deltarIntroduksjonsprogrammet,
            oppholdInstitusjon = søknadDTO.oppholdInstitusjon,
            typeInstitusjon = søknadDTO.typeInstitusjon,
            opprettet = søknadDTO.opprettet,
            barnetillegg = søknadDTO.barnetillegg.map { mapBarnetillegg(it) },
            innhentet = innhentet,
            arenaTiltak = null,
            brukerregistrertTiltak = null,
            trygdOgPensjon = null,
            fritekst = null,
        )
    }

    private fun mapBarnetillegg(barnetilleggDTO: BarnetilleggDTO): Barnetillegg {
        return Barnetillegg(
            fornavn = barnetilleggDTO.fornavn,
            etternavn = barnetilleggDTO.etternavn,
            alder = barnetilleggDTO.alder,
            ident = barnetilleggDTO.ident,
            land = barnetilleggDTO.land,
        )
    }

    // https://trello.com/c/KVY0kO8n/129-mapping-av-tiltakstype-fra-s%C3%B8knaden
    // Verdiene man kan angi i søknaden har korresponderende kodeverdier i Tiltaksnavn fra Arena
    private fun mapTiltaksType(tiltaksType: String): Tiltaksaktivitet.Tiltaksnavn? =
        when (tiltaksType) {
            "JOBSOK" -> Tiltaksaktivitet.Tiltaksnavn.JOBBK
            "PRAKSORD" -> Tiltaksaktivitet.Tiltaksnavn.ARBTREN
            "AMO" -> Tiltaksaktivitet.Tiltaksnavn.GRUPPEAMO
            "Annet" -> null //TODO: Hvordan mappe Annet?
            else -> null
        }

}

class SøknadDTO(
    val id: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val oppholdInstitusjon: Boolean?,
    val typeInstitusjon: String?,
    val tiltaksArrangoer: String?,
    val tiltaksType: String?,
    val opprettet: LocalDateTime?,
    val brukerRegistrertStartDato: LocalDate?,
    val brukerRegistrertSluttDato: LocalDate?,
    val systemRegistrertStartDato: LocalDate?,
    val systemRegistrertSluttDato: LocalDate?,
    val barnetillegg: List<BarnetilleggDTO>,
)

class BarnetilleggDTO(
    val fornavn: String?,
    val etternavn: String?,
    val alder: Int,
    val ident: String,
    val land: String,
)
