package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.YtelseSak
import no.nav.tiltakspenger.innsending.domene.meldinger.YtelserMottattHendelse
import no.nav.tiltakspenger.innsending.domene.tolkere.AapTolker
import no.nav.tiltakspenger.innsending.domene.tolkere.DagpengerTolker
import no.nav.tiltakspenger.innsending.ports.InnsendingMediator
import no.nav.tiltakspenger.libs.arena.ytelse.ArenaYtelseResponsDTO
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import java.time.LocalDateTime

data class ArenaYtelserMottattDTO(
    val respons: ArenaYtelseResponsDTO,
    val ident: String,
    val journalpostId: String,
    val innhentet: LocalDateTime,
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val ytelsepath = "/rivers/ytelser"

fun Route.ytelseRoutes(
    innsendingMediator: InnsendingMediator,
    behandlingService: BehandlingService,
) {
    post("$ytelsepath") {
        LOG.info { "Vi har mottatt ytelser fra river" }

        val arenaYtelser = call.receive<ArenaYtelserMottattDTO>()

        val ytelser = mapYtelser(
            ytelseSakDTO = arenaYtelser.respons.saker!!, // Hvis denne smeller må vi kode opp en FeilHendelse
            tidsstempelHosOss = arenaYtelser.innhentet,
        )

        behandlingService.hentBehandlingForJournalpostId(arenaYtelser.journalpostId)?.let { behandling ->
            AapTolker.tolkeData(ytelser, behandling.vurderingsperiode).forEach { saksopplysning ->
                behandlingService.leggTilSaksopplysning(behandling.id, saksopplysning)
            }
            DagpengerTolker.tolkeData(ytelser, behandling.vurderingsperiode).forEach { saksopplysning ->
                behandlingService.leggTilSaksopplysning(behandling.id, saksopplysning)
            }
        }

        val ytelserMottattHendelse = YtelserMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = arenaYtelser.journalpostId,
            ytelseSak = ytelser,
            tidsstempelYtelserInnhentet = arenaYtelser.innhentet,
        )
        SECURELOG.info { " Mottatt ytelser og laget hendelse : $ytelserMottattHendelse" }
        innsendingMediator.håndter(ytelserMottattHendelse)
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}

private fun mapYtelser(
    ytelseSakDTO: List<ArenaYtelseResponsDTO.SakDTO>,
    tidsstempelHosOss: LocalDateTime,
): List<YtelseSak> {
    return ytelseSakDTO.map { ytelse ->
        YtelseSak(
            fomGyldighetsperiode = ytelse.gyldighetsperiodeFom,
            tomGyldighetsperiode = ytelse.gyldighetsperiodeTom,
            datoKravMottatt = ytelse.kravMottattDato,
            dataKravMottatt = null, // Kan fjernes
            fagsystemSakId = ytelse.fagsystemSakId,
            status = ytelse.status?.let { s -> mapSakStatus(s) },
            ytelsestype = ytelse.sakType?.let { y -> mapSakType(y) },
            vedtak = ytelse.vedtak.map { vedtak ->
                YtelseSak.YtelseVedtak(
                    beslutningsDato = vedtak.beslutningsDato,
                    periodetypeForYtelse = vedtak.vedtakType?.let { p -> mapVedtakType(p) },
                    vedtaksperiodeFom = vedtak.vedtaksperiodeFom,
                    vedtaksperiodeTom = vedtak.vedtaksperiodeTom,
                    vedtaksType = vedtak.rettighetType?.let { v -> mapRettighetType(v) },
                    status = vedtak.status?.let { s -> mapVedtakStatus(s) },
                )
            },
            antallDagerIgjen = ytelse.antallDagerIgjen,
            antallUkerIgjen = ytelse.antallUkerIgjen,
            tidsstempelHosOss = tidsstempelHosOss,
        )
    }
}

private fun mapVedtakStatus(dto: ArenaYtelseResponsDTO.VedtakStatusType): YtelseSak.YtelseVedtak.YtelseVedtakStatus {
    return YtelseSak.YtelseVedtak.YtelseVedtakStatus.valueOf(dto.name)
}

private fun mapRettighetType(dto: ArenaYtelseResponsDTO.RettighetType): YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype {
    return YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype.valueOf(dto.name)
}

private fun mapVedtakType(dto: ArenaYtelseResponsDTO.VedtakType): YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse {
    return YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse.valueOf(dto.name)
}

private fun mapSakType(dto: ArenaYtelseResponsDTO.SakType): YtelseSak.YtelseSakYtelsetype {
    return YtelseSak.YtelseSakYtelsetype.valueOf(dto.name)
}

private fun mapSakStatus(dto: ArenaYtelseResponsDTO.SakStatusType): YtelseSak.YtelseSakStatus {
    return YtelseSak.YtelseSakStatus.valueOf(dto.name)
}
