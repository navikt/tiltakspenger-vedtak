package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vedtak.meldinger.YtelserMottattHendelse
import no.nav.tiltakspenger.vedtak.rivers.YtelseSakDTO
import no.nav.tiltakspenger.vedtak.rivers.YtelseSakStatusEnum
import no.nav.tiltakspenger.vedtak.rivers.YtelseSakYtelsetypeEnum
import no.nav.tiltakspenger.vedtak.rivers.YtelseVedtakPeriodeTypeForYtelseEnum
import no.nav.tiltakspenger.vedtak.rivers.YtelseVedtakStatusEnum
import no.nav.tiltakspenger.vedtak.rivers.YtelseVedtakVedtakstypeEnum
import java.time.LocalDateTime

data class ArenaYtelserMottattDTO(
    val ytelser: List<YtelseSakDTO>,
    val ident: String,
    val journalpostId: String,
    val innhentet: LocalDateTime,
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val ytelsepath = "/rivers/ytelser"

fun Route.ytelseRoutes(innsendingMediator: InnsendingMediator) {
    post("$ytelsepath") {
        LOG.info { "Vi har mottatt ytelser fra river" }
//        val arenaYtelser = try {
//            deserialize<ArenaYtelserMottattDTO>(call.receive())
//        } catch (e: Error) {
//            println(e)
//        }
        val arenaYtelser = call.receive<ArenaYtelserMottattDTO>()

        val ytelserMottattHendelse = YtelserMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = arenaYtelser.journalpostId,
            ytelseSak = mapYtelser(
                ytelseSakDTO = arenaYtelser.ytelser,
                tidsstempelHosOss = arenaYtelser.innhentet,
            )
        )
        SECURELOG.info { " Mottatt ytelser og laget hendelse : $ytelserMottattHendelse" }
        innsendingMediator.håndter(ytelserMottattHendelse)
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}

private fun mapYtelser(
    ytelseSakDTO: List<YtelseSakDTO>,
    tidsstempelHosOss: LocalDateTime
): List<YtelseSak> {
    return ytelseSakDTO.map { ytelse ->
        YtelseSak(
            fomGyldighetsperiode = ytelse.fomGyldighetsperiode,
            tomGyldighetsperiode = ytelse.tomGyldighetsperiode,
            datoKravMottatt = ytelse.datoKravMottatt,
            dataKravMottatt = ytelse.dataKravMottatt,
            fagsystemSakId = ytelse.fagsystemSakId,
            status = ytelse.status?.let { s -> mapStatus(s) },
            ytelsestype = ytelse.ytelsestype?.let { y -> mapYtelsetype(y) },
            vedtak = ytelse.vedtak.map { vedtak ->
                YtelseSak.YtelseVedtak(
                    beslutningsDato = vedtak.beslutningsDato,
                    periodetypeForYtelse = vedtak.periodetypeForYtelse?.let { p -> mapPeriodeType(p) },
                    vedtaksperiodeFom = vedtak.vedtaksperiodeFom,
                    vedtaksperiodeTom = vedtak.vedtaksperiodeTom,
                    vedtaksType = vedtak.vedtaksType?.let { v -> mapVedtakstype(v) },
                    status = vedtak.status?.let { s -> mapVedtakStatus(s) },
                )
            },
            antallDagerIgjen = ytelse.antallDagerIgjen,
            antallUkerIgjen = ytelse.antallUkerIgjen,
            tidsstempelHosOss = tidsstempelHosOss,
        )
    }
}

private fun mapVedtakStatus(dto: YtelseVedtakStatusEnum): YtelseSak.YtelseVedtak.YtelseVedtakStatus {
    return YtelseSak.YtelseVedtak.YtelseVedtakStatus.valueOf(dto.name)
}

private fun mapVedtakstype(dto: YtelseVedtakVedtakstypeEnum): YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype {
    return YtelseSak.YtelseVedtak.YtelseVedtakVedtakstype.valueOf(dto.name)
}

private fun mapPeriodeType(dto: YtelseVedtakPeriodeTypeForYtelseEnum): YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse {
    return YtelseSak.YtelseVedtak.YtelseVedtakPeriodeTypeForYtelse.valueOf(dto.name)
}

private fun mapYtelsetype(dto: YtelseSakYtelsetypeEnum): YtelseSak.YtelseSakYtelsetype {
    return YtelseSak.YtelseSakYtelsetype.valueOf(dto.name)
}

private fun mapStatus(dto: YtelseSakStatusEnum): YtelseSak.YtelseSakStatus {
    return YtelseSak.YtelseSakStatus.valueOf(dto.name)
}
