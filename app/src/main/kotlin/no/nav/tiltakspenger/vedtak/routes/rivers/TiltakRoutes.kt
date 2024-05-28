package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.meldinger.TiltakMottattHendelse
import no.nav.tiltakspenger.innsending.ports.InnsendingMediator
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import java.time.LocalDateTime
import kotlin.math.roundToInt

data class TiltakMottattDTO(
    val respons: TiltakResponsDTO,
    val ident: String,
    val journalpostId: String,
    val innhentet: LocalDateTime,
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
const val tiltakpath = "/rivers/tiltak"

fun Route.tiltakRoutes(
    innsendingMediator: InnsendingMediator,
    behandlingService: BehandlingService,
) {
    post(tiltakpath) {
        LOG.info { "Vi har mottatt tiltak fra river" }
        val tiltakDTO: TiltakMottattDTO = try {
            call.receive()
        } catch (t: Throwable) {
            LOG.info("Feil ved mapping fra json")
            LOG.info("Feil ved mapping fra json til kotlinkode", t)
            throw t
        }

        if (tiltakDTO.respons.tiltak != null) {
            val tiltak = mapTiltak(
                tiltakDTO = tiltakDTO.respons.tiltak!!,
                innhentet = tiltakDTO.innhentet,
            )
            val tiltakMottattHendelse = TiltakMottattHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = tiltakDTO.journalpostId,
                tiltaks = tiltak,
                tidsstempelTiltakInnhentet = tiltakDTO.innhentet,
            )

            behandlingService.hentBehandlingForJournalpostId(tiltakDTO.journalpostId)?.let { behandling ->
                behandlingService.oppdaterTiltak(behandling.id, tiltak)
            }

            SECURELOG.info { "Mottatt tiltak og laget hendelse : $tiltakMottattHendelse" }
            innsendingMediator.håndter(tiltakMottattHendelse)
            call.respond(message = "OK", status = HttpStatusCode.OK)
        } else {
            LOG.error { "Mottok en feil må skrive kode for å håndtere den ${tiltakDTO.respons.feil}" }
            throw RuntimeException("Mottok en feil ifm tiltak")
        }
    }
}

private fun mapTiltak(
    tiltakDTO: List<TiltakResponsDTO.TiltakDTO>,
    innhentet: LocalDateTime,
): List<Tiltak> {
    return tiltakDTO
        .filterNot { it.deltakelseFom == null }
        .filterNot { it.deltakelseTom == null }
        .map {
            Tiltak(
                id = it.id,
                gjennomføring = Tiltak.Gjennomføring(
                    id = it.gjennomforing.id,
                    arrangørnavn = it.gjennomforing.arrangørnavn,
                    typeNavn = it.gjennomforing.typeNavn,
                    typeKode = it.gjennomforing.arenaKode.name,
                    rettPåTiltakspenger = it.gjennomforing.arenaKode.rettPåTiltakspenger,
                ),
                deltakelseFom = it.deltakelseFom!!,
                deltakelseTom = it.deltakelseTom!!,
                deltakelseStatus = Tiltak.DeltakerStatus(
                    status = it.deltakelseStatus.name,
                    rettTilÅASøke = it.deltakelseStatus.rettTilÅSøke,
                ),
                deltakelseDagerUke = it.deltakelseDagerUke,
                deltakelseProsent = it.deltakelseProsent,
                kilde = it.kilde,
                registrertDato = it.registrertDato,
                innhentet = innhentet,
                antallDagerSaksopplysninger = AntallDagerSaksopplysninger(
                    antallDagerSaksopplysningerFraRegister = listOf(
                        PeriodeMedVerdi(
                            // TODO: Dette bør kanskje leve inni saksopplysningen?
                            // TODO: Vi må sørge for at dersom prosent == 100 og vi mangler antall dager, setter vi antall dager til 5
                            verdi =
                            if (it.deltakelseDagerUke != null) {
                                AntallDager(
                                    antallDager = it.deltakelseDagerUke!!.roundToInt(),
                                    kilde = Kilde.valueOf(it.kilde),
                                )
                            } else {
                                AntallDager(
                                    antallDager = 0,
                                    kilde = Kilde.valueOf(it.kilde),
                                )
                            },
                            periode = Periode(
                                fra = it.deltakelseFom!!,
                                til = it.deltakelseTom!!,
                            ),
                        ),
                    ),
                ),
            )
        }
}
