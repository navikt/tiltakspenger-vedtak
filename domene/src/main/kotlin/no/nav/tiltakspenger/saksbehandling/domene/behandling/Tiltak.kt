package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate
import java.time.LocalDateTime

data class Tiltak(
    val id: String,
    val gjennomføring: Gjennomføring,
    val deltakelseFom: LocalDate,
    val deltakelseTom: LocalDate,
    val deltakelseStatus: DeltakerStatus,
    val deltakelseDagerUke: Float?,
    val deltakelseProsent: Float?,
    val kilde: String,
    val registrertDato: LocalDateTime,
    val innhentet: LocalDateTime,
    val tiltaksdeltagelseVurdering: Vurdering,
) {
    companion object {
        fun tempVurdering(): Vurdering {
            return Vurdering.KreverManuellVurdering(
                vilkår = Vilkår.TILTAKSDELTAGELSE,
                kilde = Kilde.SAKSB, // TODO: Finn ut av dette
                detaljer = "temp-detaljer",
                fom = LocalDate.MIN,
                tom = LocalDate.MAX,
            )
        }
    }

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

    fun lagVurderingAvTiltakdeltagelse(utfall: Utfall, detaljer: String = ""): Vurdering {
        return when (utfall) {
            Utfall.OPPFYLT -> Vurdering.Oppfylt(
                vilkår = Vilkår.TILTAKSDELTAGELSE,
                kilde = Kilde.SAKSB, // TODO: Finn ut av dette
                detaljer = detaljer,
                fom = deltakelseFom,
                tom = deltakelseTom,
            )

            Utfall.IKKE_OPPFYLT -> Vurdering.IkkeOppfylt(
                vilkår = Vilkår.TILTAKSDELTAGELSE,
                kilde = Kilde.SAKSB, // TODO: Finn ut av dette
                detaljer = detaljer,
                fom = deltakelseFom,
                tom = deltakelseTom,
            )

            Utfall.KREVER_MANUELL_VURDERING -> Vurdering.KreverManuellVurdering(
                vilkår = Vilkår.TILTAKSDELTAGELSE,
                kilde = Kilde.SAKSB, // TODO: Finn ut av dette
                detaljer = detaljer,
                fom = deltakelseFom,
                tom = deltakelseTom,
            )
        }
    }

    fun vilkårsvurderTiltaksdeltagelse(): Tiltak {
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
                throw IkkeImplementertException("Støtter ikke saksbehandling av deltagelse som ikke er påbegynt")
            }
        } else {
            lagVurderingAvTiltakdeltagelse(Utfall.IKKE_OPPFYLT, "Tiltaket gir ikke rett på tiltakspenger")
        }

        return this.copy(tiltaksdeltagelseVurdering = vurdering)
    }
}
