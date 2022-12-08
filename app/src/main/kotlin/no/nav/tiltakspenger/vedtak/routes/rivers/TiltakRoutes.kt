package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import no.nav.tiltakspenger.vedtak.rivers.TiltaksaktivitetDTO
import java.time.LocalDateTime

data class ArenaTiltakMottattDTO(
    val tiltak: List<TiltaksaktivitetDTO>?,
    val ident: String,
    val journalpostId: String,
    val innhentet: LocalDateTime,
    val feil: String?
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun Route.tiltakRoutes(innsendingMediator: InnsendingMediator) {
    post("/rivers/tiltak") {
        LOG.info { "Vi har mottatt tiltak fra river" }
        val arenaTiltak = call.receive<ArenaTiltakMottattDTO>()

        val arenaTiltakMottattHendelse = ArenaTiltakMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = arenaTiltak.journalpostId,
            feil = arenaTiltak.feil
                ?.let { ArenaTiltakMottattHendelse.Feilmelding.valueOf(it) },
            tiltaksaktivitet = mapArenaTiltak(
                tiltaksaktivitetDTO = arenaTiltak.tiltak,
                innhentet = arenaTiltak.innhentet,
            )
        )

        SECURELOG.info {" Mottatt tiltak og laget hendelse : $arenaTiltakMottattHendelse" }
        innsendingMediator.håndter(arenaTiltakMottattHendelse)
    }
}

private fun mapArenaTiltak(
    tiltaksaktivitetDTO: List<TiltaksaktivitetDTO>?,
    innhentet: LocalDateTime
): List<Tiltaksaktivitet>? {
    return tiltaksaktivitetDTO?.map {
        Tiltaksaktivitet(
            tiltak = mapTiltaksnavn(it.tiltaksnavn),
            aktivitetId = it.aktivitetId,
            tiltakLokaltNavn = it.tiltakLokaltNavn,
            arrangør = it.arrangoer,
            bedriftsnummer = it.bedriftsnummer,
            deltakelsePeriode = Tiltaksaktivitet.DeltakelsesPeriode(
                it.deltakelsePeriode?.fom,
                it.deltakelsePeriode?.tom
            ),
            deltakelseProsent = it.deltakelseProsent,
            deltakerStatus = it.deltakerStatus.innerText.let { s -> mapDeltakerStatus(s) },
            statusSistEndret = it.statusSistEndret,
            begrunnelseInnsøking = it.begrunnelseInnsoeking,
            antallDagerPerUke = it.antallDagerPerUke,
            tidsstempelHosOss = innhentet,
        )
    }
}

private fun mapDeltakerStatus(dtoDeltakerStatus: TiltaksaktivitetDTO.DeltakerStatusEnum): Tiltaksaktivitet.DeltakerStatus {
    return Tiltaksaktivitet.DeltakerStatus.valueOf(dtoDeltakerStatus.name)
}

private fun mapTiltaksnavn(dtoTiltaksnavn: TiltaksaktivitetDTO.TiltaksnavnEnum): Tiltaksaktivitet.Tiltak {
    return Tiltaksaktivitet.Tiltak.valueOf(dtoTiltaksnavn.name)
}
