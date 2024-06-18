package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate
import java.time.LocalDateTime

data class Tiltak(
    val id: TiltakId,
    val eksternId: String,
    val gjennomføring: Gjennomføring,
    val deltakelseFom: LocalDate,
    val deltakelseTom: LocalDate,
    val deltakelseStatus: DeltakerStatus,
    val deltakelseProsent: Float?,
    val kilde: String,
    val registrertDato: LocalDateTime,
    val innhentet: LocalDateTime,
    val antallDagerSaksopplysninger: AntallDagerSaksopplysninger,
) {
    data class Gjennomføring(
        val id: String,
        val arrangørnavn: String,
        val typeNavn: String,
        val typeKode: String,
        val rettPåTiltakspenger: Boolean,
    )

    data class DeltakerStatus(
        val status: String,
        val rettTilÅASøke: Boolean,
    )

    fun brukerDeltarPåTiltak(status: String): Boolean {
        return status.equals("Gjennomføres", ignoreCase = true) ||
            status.equals("Deltar", ignoreCase = true)
    }

    fun brukerHarDeltattOgSluttet(status: String): Boolean {
        return status.equals("Har sluttet", ignoreCase = true) ||
            status.equals("Fullført", ignoreCase = true) ||
            status.equals("Avbrutt", ignoreCase = true) ||
            status.equals("Deltakelse avbrutt", ignoreCase = true) ||
            status.equals("Gjennomføring avbrutt", ignoreCase = true) ||
            status.equals("Gjennomføring avlyst", ignoreCase = true)
    }

    fun leggTilAntallDagerFraSaksbehandler(nyVerdi: PeriodeMedVerdi<AntallDager>): Tiltak {
        val tiltaksPeriode = Periode(fra = deltakelseFom, til = deltakelseTom)

        val oppdatertAntallDager =
            antallDagerSaksopplysninger.leggTilAntallDagerFraSaksbehandler(tiltaksPeriode, nyVerdi)

        return this.copy(
            antallDagerSaksopplysninger = oppdatertAntallDager.avklar(),
        )
    }

    fun lagVurderingAvTiltakdeltagelse(utfall: Utfall, detaljer: String = ""): Vurdering {
        return Vurdering(
            utfall = Periodisering(Utfall.UAVKLART, Periode(this.deltakelseFom, this.deltakelseTom))
                .setVerdiForDelPeriode(utfall, Periode(this.deltakelseFom, this.deltakelseTom)),
            detaljer = detaljer,
        )
    }

    fun vilkårsvurderTiltaksdeltagelse(): Vurdering {
        val vurdering = if (gjennomføring.rettPåTiltakspenger) {
            if (brukerDeltarPåTiltak(deltakelseStatus.status)) {
                lagVurderingAvTiltakdeltagelse(Utfall.OPPFYLT)
            } else if (brukerHarDeltattOgSluttet(deltakelseStatus.status)) {
                if (deltakelseTom.isBefore(LocalDate.now())) {
                    lagVurderingAvTiltakdeltagelse(Utfall.OPPFYLT)
                } else {
                    lagVurderingAvTiltakdeltagelse(
                        Utfall.UAVKLART,
                        "Status tilsier at bruker har sluttet på tiltak, men tiltaksperioden er fremover i tid",
                    )
                }
            } else {
                lagVurderingAvTiltakdeltagelse(
                    Utfall.UAVKLART,
                    "Vi har mottatt en status vi ikke hånderer enda. Setter til manuell",
                )
            }
        } else {
            lagVurderingAvTiltakdeltagelse(Utfall.IKKE_OPPFYLT, "Tiltaket gir ikke rett på tiltakspenger")
        }

        return vurdering
    }
}
