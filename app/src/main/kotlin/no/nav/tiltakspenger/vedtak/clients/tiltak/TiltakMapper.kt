package no.nav.tiltakspenger.vedtak.clients.tiltak

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.TiltakDTO
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak.Gjennomføring
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import java.time.LocalDateTime

internal fun mapTiltak(
    tiltakDTO: List<TiltakDTO>,
    innhentet: LocalDateTime,
): List<Tiltak> {
    return tiltakDTO
        .filterNot { it.deltakelseFom == null }
        .filterNot { it.deltakelseTom == null }
        .map {
            Tiltak(
                id = TiltakId.random(),
                eksternId = it.id,
                gjennomføring = Gjennomføring(
                    id = it.gjennomforing.id,
                    arrangørnavn = it.gjennomforing.arrangørnavn,
                    typeNavn = it.gjennomforing.typeNavn,
                    typeKode = it.gjennomforing.arenaKode.name,
                    rettPåTiltakspenger = it.gjennomforing.arenaKode.rettPåTiltakspenger,
                ),
                deltakelseFom = it.deltakelseFom!!,
                deltakelseTom = it.deltakelseTom!!,
                deltakelseStatus = it.deltakelseStatus.name,
                deltakelseProsent = it.deltakelseProsent,
                kilde = when {
                    it.kilde.lowercase().contains("komet") -> Tiltakskilde.Komet
                    it.kilde.lowercase().contains("arena") -> Tiltakskilde.Arena
                    else -> throw IllegalStateException("Kunne ikke parse tiltak fra tiltakspenger-tiltak. Ukjent kilde: ${it.kilde}. Forventet Arena eller Komet. Tiltaksid: ${it.id}")
                },
                registrertDato = it.registrertDato,
                innhentet = innhentet,
            )
        }
}
