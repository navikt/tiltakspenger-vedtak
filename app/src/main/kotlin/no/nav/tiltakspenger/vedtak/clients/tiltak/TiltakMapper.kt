package no.nav.tiltakspenger.vedtak.clients.tiltak

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.tiltak.TiltakResponsDTO.TiltakDTO
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import java.time.LocalDateTime
import kotlin.math.roundToInt

private fun mapAntallDager(tiltak: TiltakDTO): PeriodeMedVerdi<AntallDager> =
    PeriodeMedVerdi(
        verdi =
        if (tiltak.deltakelseDagerUke != null) {
            AntallDager(
                antallDager = tiltak.deltakelseDagerUke!!.roundToInt(),
                kilde = Kilde.valueOf(tiltak.kilde.uppercase()),
                saksbehandlerIdent = null,
            )
        } else {
            AntallDager(
                antallDager = if (tiltak.deltakelseProsent == 100f) 5 else 0,
                kilde = Kilde.valueOf(tiltak.kilde.uppercase()),
                saksbehandlerIdent = null,
            )
        },
        periode = Periode(
            fraOgMed = tiltak.deltakelseFom!!,
            tilOgMed = tiltak.deltakelseTom!!,
        ),
    )

internal fun mapTiltak(
    tiltakDTO: List<TiltakDTO>,
    innhentet: LocalDateTime,
): List<Tiltak> {
    return tiltakDTO
        .filterNot { it.deltakelseFom == null }
        .filterNot { it.deltakelseTom == null }
        .map {
            val antallDager = mapAntallDager(it)
            Tiltak(
                id = TiltakId.random(),
                eksternId = it.id,
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
                deltakelseProsent = it.deltakelseProsent,
                kilde = it.kilde,
                registrertDato = it.registrertDato,
                innhentet = innhentet,
                antallDagerSaksopplysninger = AntallDagerSaksopplysninger(
                    antallDagerSaksopplysningerFraRegister = listOf(antallDager),
                ).avklar(),
            )
        }
}
