package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.libs.arena.tiltak.ArenaTiltaksaktivitetResponsDTO
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.meldinger.ArenaTiltakMottattHendelse
import java.time.LocalDateTime

data class ArenaTiltakMottattDTO(
    val respons: ArenaTiltaksaktivitetResponsDTO,
    val ident: String,
    val journalpostId: String,
    val innhentet: LocalDateTime,
)

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")
val tiltakpath = "/rivers/tiltak"

fun Route.tiltakRoutes(innsendingMediator: InnsendingMediator) {
    post("$tiltakpath") {
        LOG.info { "Vi har mottatt tiltak fra river" }
        val arenaTiltak: ArenaTiltakMottattDTO = try {
            call.receive()
        } catch (t: Throwable) {
            // println("1")
            // println(t.message)
            // println(t.stackTraceToString())
            LOG.info("Feil ved mapping fra json")
            LOG.info("Feil ved mapping fra json til kotlinkode", t)
            throw t
        } finally {
            println("2")
        }
        println("3")

        if (arenaTiltak.respons.tiltaksaktiviteter != null) {
            val arenaTiltakMottattHendelse = ArenaTiltakMottattHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = arenaTiltak.journalpostId,
                tiltaksaktivitet = mapArenaTiltak(
                    tiltaksaktivitetDTO = arenaTiltak.respons.tiltaksaktiviteter!!,
                    innhentet = arenaTiltak.innhentet,
                )
            )

            SECURELOG.info { "Mottatt tiltak og laget hendelse : $arenaTiltakMottattHendelse" }
            innsendingMediator.håndter(arenaTiltakMottattHendelse)
            call.respond(message = "OK", status = HttpStatusCode.OK)
        } else {
            LOG.error { "Mottok en feil må skrive kode for å håndtere den ${arenaTiltak.respons.feil}" }
            throw RuntimeException("Mottok en feil ifm arenatiltak")
        }
    }
}

private fun mapArenaTiltak(
    tiltaksaktivitetDTO: List<ArenaTiltaksaktivitetResponsDTO.TiltaksaktivitetDTO>,
    innhentet: LocalDateTime
): List<Tiltaksaktivitet> {
    return tiltaksaktivitetDTO.map {
        Tiltaksaktivitet(
            tiltak = mapTiltaksnavn(it.tiltakType),
            aktivitetId = it.aktivitetId,
            tiltakLokaltNavn = it.tiltakLokaltNavn,
            arrangør = it.arrangoer,
            bedriftsnummer = it.bedriftsnummer,
            deltakelsePeriode = Tiltaksaktivitet.DeltakelsesPeriode(
                it.deltakelsePeriode?.fom,
                it.deltakelsePeriode?.tom
            ),
            deltakelseProsent = it.deltakelseProsent,
            deltakerStatus = mapDeltakerStatus(it.deltakerStatusType),
            statusSistEndret = it.statusSistEndret,
            begrunnelseInnsøking = it.begrunnelseInnsoeking,
            antallDagerPerUke = it.antallDagerPerUke,
            tidsstempelHosOss = innhentet,
        )
    }
}

// TODO: Skrive om til en when
private fun mapDeltakerStatus(dtoDeltakerStatus: ArenaTiltaksaktivitetResponsDTO.DeltakerStatusType): Tiltaksaktivitet.DeltakerStatus {
    return Tiltaksaktivitet.DeltakerStatus.valueOf(dtoDeltakerStatus.name)
}

// TODO: Skrive om til en when
private fun mapTiltaksnavn(dtoTiltaksnavn: ArenaTiltaksaktivitetResponsDTO.TiltakType): Tiltaksaktivitet.Tiltak {
    return Tiltaksaktivitet.Tiltak.valueOf(dtoTiltaksnavn.name)
}
