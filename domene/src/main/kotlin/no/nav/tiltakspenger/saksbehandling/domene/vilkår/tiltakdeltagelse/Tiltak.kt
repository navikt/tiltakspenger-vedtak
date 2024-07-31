package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
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
    val antallDagerSaksopplysninger: AntallDagerSaksopplysninger, // TODO Kew slett
) {
    data class Gjennomføring(
        val id: String,
        val arrangørnavn: String, // kan vi slette denne?
        val typeNavn: String,
        val typeKode: String,
        val rettPåTiltakspenger: Boolean,
    )

    data class DeltakerStatus(
        val status: String,
        val rettTilÅSøke: Boolean,
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

    fun lagVurderingAvTiltakdeltagelse(utfall: Utfall, detaljer: String = ""): Vurdering {
        return Vurdering(
            vilkår = Vilkår.TILTAKSDELTAGELSE,
            kilde = Kilde.SAKSB, // TODO: Finn ut av dette
            detaljer = detaljer,
            fom = deltakelseFom,
            tom = deltakelseTom,
            utfall = utfall,
            grunnlagId = this.id.toString(),
        )
    }

    fun utfall(vurderingsperiode: Periode): Periodisering<Utfall> {
        val vurdering = vilkårsvurderTiltaksdeltagelse()
        // Den følgende koden forutsetter at tiltakene ikke går ut over vurderingsperioden (som de ikke skal gjøre..)
        return Periodisering(Utfall.IKKE_OPPFYLT, vurderingsperiode).setVerdiForDelPeriode(
            vurdering.utfall,
            Periode(vurdering.fom!!, vurdering.tom!!),
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
                        Utfall.KREVER_MANUELL_VURDERING,
                        "Status tilsier at bruker har sluttet på tiltak, men tiltaksperioden er fremover i tid",
                    )
                }
            } else {
                lagVurderingAvTiltakdeltagelse(
                    Utfall.KREVER_MANUELL_VURDERING,
                    "Vi har mottatt en status vi ikke hånderer enda. Setter til manuell",
                )
            }
        } else {
            lagVurderingAvTiltakdeltagelse(Utfall.IKKE_OPPFYLT, "Tiltaket gir ikke rett på tiltakspenger")
        }

        return vurdering
    }
}

fun List<Tiltak>.vurderingsperiodeFraTiltak(): Periode? {
    val fraOgMed = this.filter { it.gjennomføring.rettPåTiltakspenger }.minOfOrNull { it.deltakelseFom }
    val tilOgMed = this.filter { it.gjennomføring.rettPåTiltakspenger }.maxOfOrNull { it.deltakelseTom }
    if (fraOgMed == null || tilOgMed == null) {
        return null
    }
    return Periode(fraOgMed, tilOgMed)
}
