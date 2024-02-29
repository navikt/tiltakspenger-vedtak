package no.nav.tiltakspenger.vedtak.service.brev

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.domene.behandling.Personopplysninger
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.libs.dokument.BrevDTO
import no.nav.tiltakspenger.libs.dokument.PersonaliaDTO
import no.nav.tiltakspenger.libs.dokument.TiltaksinfoDTO
import java.time.LocalDate
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun sendBrev(vedtak: Vedtak, rapidsConnection: RapidsConnection, personopplysninger: Personopplysninger.Søker) {
    mutableMapOf(
        "@event_name" to "vedtaksbrev",
        "@opprettet" to LocalDateTime.now(),
        "vedtaksbrev" to mapVedtaksBrevDTO(vedtak, personopplysninger),
    ).let { JsonMessage.newMessage(it) }
        .also { message ->
            SECURELOG.info { "Vi sender vedtaksbrev : ${message.toJson()}" }
            rapidsConnection.publish(vedtak.id.toString(), message.toJson())
        }
}

private fun mapVedtaksBrevDTO(vedtak: Vedtak, personopplysninger: Personopplysninger.Søker) =
    BrevDTO(
        personaliaDTO = mapPeronaliaDTO(vedtak, personopplysninger),
        tiltaksinfoDTO = mapTiltaksinfo(vedtak),
        fraDato = vedtak.periode.fra.toString(),
        tilDato = vedtak.periode.til.toString(),
        saksnummer = vedtak.sakId.toString(),
        barnetillegg = false,
        saksbehandler = vedtak.saksbehandler,
        kontor = "måkk",
        datoForUtsending = LocalDate.now(),
    )

private fun mapPeronaliaDTO(vedtak: Vedtak, personopplysninger: Personopplysninger.Søker) =
    PersonaliaDTO(
        ident = personopplysninger.ident,
        fornavn = personopplysninger.fornavn,
        etternavn = personopplysninger.etternavn,
        antallBarn = vedtak.behandling.søknad().barnetillegg.count { it.oppholderSegIEØS == Søknad.JaNeiSpm.Ja },
    )

fun mapTiltaksinfo(vedtak: Vedtak) =
    vedtak.behandling.tiltak
        .filter { it.id == vedtak.behandling.søknad().tiltak.id }
        .map {
            TiltaksinfoDTO(
                tiltak = it.gjennomføring.typeNavn,
                tiltaksnavn = it.gjennomføring.typeNavn,
                tiltaksnummer = it.gjennomføring.typeKode,
                arrangør = it.gjennomføring.arrangørnavn,
            )
        }.first()
