package no.nav.tiltakspenger.vedtak.service.vedtak

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.domene.behandling.Personopplysninger
import no.nav.tiltakspenger.domene.brev.BrevDTO
import no.nav.tiltakspenger.domene.brev.Tiltaksinfo
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun sendBrev(vedtak: Vedtak, rapidsConnection: RapidsConnection, personopplysninger: Personopplysninger) {
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

private fun mapVedtaksBrevDTO(vedtak: Vedtak, personopplysninger: Personopplysninger) =
    BrevDTO(
        personalia = personopplysninger.getPersonalia(),
        tiltaksinfo = mapTiltaksinfo(vedtak),
        fraDato = vedtak.periode.fra.toString(),
        tilDato = vedtak.periode.til.toString(),
        saksnummer = vedtak.sakId.toString(),
        barnetillegg = false,
        saksbehandler = vedtak.saksbehandler,
        kontor = "måkk",
        innsendingTidspunkt = LocalDateTime.now(),
    )

fun mapTiltaksinfo(vedtak: Vedtak) =
    vedtak.behandling.tiltak
        .filter { it.id == vedtak.behandling.søknad().tiltak.id }
        .map {
            Tiltaksinfo(
                tiltak = it.gjennomføring.typeNavn,
                tiltaksnavn = it.gjennomføring.typeNavn,
                tiltaksnummer = it.gjennomføring.typeKode,
                arrangør = it.gjennomføring.arrangørnavn,
            )
        }.first()
