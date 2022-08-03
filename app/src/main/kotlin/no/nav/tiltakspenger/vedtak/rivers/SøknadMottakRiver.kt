package no.nav.tiltakspenger.vedtak.rivers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime
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
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse

internal class SøknadMottakRiver(
    private val søkerMediator: SøkerMediator,
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
        private val logg = KotlinLogging.logger {}
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
        logg.info("Received søknad for ident id: ${packet["søknad.ident"].asText()}")

        //Metrics.mottakskanalInc(packet["mottaksKanal"].asText())

        val søknadMottattHendelse = SøknadMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            ident = packet["søknad.ident"].asText(),
            søknad = mapSøknad(
                eksternSøknad = packet["søknad"].asObject(EksternSøknad::class.java),
                innhentet = packet["@opprettet"].asLocalDateTime()
            )
        )

        søkerMediator.håndter(søknadMottattHendelse)
    }

    //Hvorfor finnes ikke dette i r&r?
    private fun <T> JsonNode?.asObject(clazz: Class<T>): T = objectMapper.treeToValue(this, clazz)

    private fun mapSøknad(eksternSøknad: EksternSøknad, innhentet: LocalDateTime): Søknad {
        return Søknad(
            id = eksternSøknad.id,
            fornavn = eksternSøknad.fornavn,
            etternavn = eksternSøknad.etternavn,
            ident = eksternSøknad.ident,
            deltarKvp = eksternSøknad.deltarKvp,
            deltarIntroduksjonsprogrammet = eksternSøknad.deltarIntroduksjonsprogrammet,
            oppholdInstitusjon = eksternSøknad.oppholdInstitusjon,
            typeInstitusjon = eksternSøknad.typeInstitusjon,
            tiltaksArrangoer = eksternSøknad.tiltaksArrangoer,
            tiltaksType = eksternSøknad.tiltaksType,
            opprettet = eksternSøknad.opprettet,
            brukerRegistrertStartDato = eksternSøknad.brukerRegistrertStartDato,
            brukerRegistrertSluttDato = eksternSøknad.brukerRegistrertSluttDato,
            systemRegistrertStartDato = eksternSøknad.systemRegistrertStartDato,
            systemRegistrertSluttDato = eksternSøknad.systemRegistrertSluttDato,
            barnetillegg = eksternSøknad.barnetillegg.map { mapBarnetillegg(it) },
            innhentet = innhentet,
        )
    }

    private fun mapBarnetillegg(eksternBarnetillegg: EksternBarnetillegg): Barnetillegg {
        return Barnetillegg(
            fornavn = eksternBarnetillegg.fornavn,
            etternavn = eksternBarnetillegg.etternavn,
            alder = eksternBarnetillegg.alder,
            ident = eksternBarnetillegg.ident,
            bosted = eksternBarnetillegg.bosted,
        )
    }
}

class EksternSøknad(
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
    val barnetillegg: List<EksternBarnetillegg>,
)

class EksternBarnetillegg(
    val fornavn: String?,
    val etternavn: String?,
    val alder: Int,
    val ident: String,
    val bosted: String
)


