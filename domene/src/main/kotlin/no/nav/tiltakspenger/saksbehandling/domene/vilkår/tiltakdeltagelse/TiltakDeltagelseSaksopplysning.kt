package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface TiltakDeltagelseSaksopplysning {
    val tiltakNavn: String
    val kilde: String
    val deltagelsePeriode: Periode
    val girRett: Boolean
    val status: String
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(): Periodisering<Utfall2>

    data class Tiltak(
        override val tiltakNavn: String,
        override val tidsstempel: LocalDateTime,
        override val deltagelsePeriode: Periode,
        override val girRett: Boolean,
        override val status: String,
        override val kilde: String,
    ) : TiltakDeltagelseSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        private fun brukerDeltarPåTiltak(status: String): Boolean {
            return status.equals("Gjennomføres", ignoreCase = true) ||
                status.equals("Deltar", ignoreCase = true)
        }

        private fun brukerHarDeltattOgSluttet(status: String): Boolean {
            return status.equals("Har sluttet", ignoreCase = true) ||
                status.equals("Fullført", ignoreCase = true) ||
                status.equals("Avbrutt", ignoreCase = true) ||
                status.equals("Deltakelse avbrutt", ignoreCase = true) ||
                status.equals("Gjennomføring avbrutt", ignoreCase = true) ||
                status.equals("Gjennomføring avlyst", ignoreCase = true)
        }

        private fun brukerSkalBegynnePåTiltak(status: String): Boolean {
            return status.equals("Godkjent tiltaksplass", ignoreCase = true) ||
                status.equals("Venter på oppstart", ignoreCase = true)
        }

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return when {
                girRett && brukerDeltarPåTiltak(status) -> Periodisering(Utfall2.OPPFYLT, deltagelsePeriode)
                // B&H: Har tatt utgangspunkt i at tom-dato må være før datoen SBH behandler søknaden for statusene deltatt og sluttet.
                girRett && brukerHarDeltattOgSluttet(status) && deltagelsePeriode.tilOgMed.isBefore(LocalDate.now()) -> Periodisering(Utfall2.OPPFYLT, deltagelsePeriode)
                girRett && brukerHarDeltattOgSluttet(status) && deltagelsePeriode.tilOgMed.isAfter(LocalDate.now()) -> Periodisering(Utfall2.UAVKLART, deltagelsePeriode)
                // B&H: For nå har vi tenkt at det er lurt å ikke godkjenne søknader på tiltak frem i tid, og at de som er bakover i tid med denne statusen må sjekkes opp av SBH
                girRett && brukerSkalBegynnePåTiltak(status) && deltagelsePeriode.tilOgMed.isBefore(LocalDate.now()) -> Periodisering(Utfall2.UAVKLART, deltagelsePeriode)
                girRett && brukerSkalBegynnePåTiltak(status) && deltagelsePeriode.tilOgMed.isAfter(LocalDate.now()) -> Periodisering(Utfall2.IKKE_OPPFYLT, deltagelsePeriode)
                !girRett -> Periodisering(Utfall2.IKKE_OPPFYLT, deltagelsePeriode)
                else -> {
                    Periodisering(Utfall2.IKKE_OPPFYLT, deltagelsePeriode)
                }
            }
        }
    }
}
