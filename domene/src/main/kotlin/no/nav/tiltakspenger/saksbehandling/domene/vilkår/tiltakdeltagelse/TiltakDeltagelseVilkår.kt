package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate

/**
 * @param registerSaksopplysning Saksopplysninger som er avgjørende for vurderingen. Kan ikke ha hull. Må gå til kildesystem for å oppdatere/endre dersom vi oppdager feil i datasettet.
 */
data class TiltakDeltagelseVilkår private constructor(
    override val vurderingsperiode: Periode,
    val registerSaksopplysning: TiltakDeltagelseSaksopplysning.Register,
) : Vilkår {

    init {
        check(vurderingsperiode == registerSaksopplysning.deltagelsePeriode) { "Vurderingsperioden må være lik deltagelsesperioden" }
    }

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

    override fun utfall(): Periodisering<UtfallForPeriode> {
        // TODO Feriegave fra Kew: Hvorfor er dette en String? Hva er planen med TiltakDeltagelseSaksopplysning?
        val girRett = registerSaksopplysning.girRett
        val deltagelsePeriode = registerSaksopplysning.deltagelsePeriode
        val status = registerSaksopplysning.status

        return when {
            !girRett -> Periodisering(UtfallForPeriode.IKKE_OPPFYLT, deltagelsePeriode)
            brukerDeltarPåTiltak(status) -> Periodisering(UtfallForPeriode.OPPFYLT, deltagelsePeriode)
            // B&H: Har tatt utgangspunkt i at tom-dato må være før datoen SBH behandler søknaden for statusene deltatt og sluttet.
            // TODO Feriegave fra Kew: Foreslår at vi kommenterer ut disse som går på LocalDate.now() og heller setter de casene til UAVKLART.
            brukerHarDeltattOgSluttet(status) && deltagelsePeriode.tilOgMed.isBefore(LocalDate.now()) -> Periodisering(UtfallForPeriode.OPPFYLT, deltagelsePeriode)
            brukerHarDeltattOgSluttet(status) && deltagelsePeriode.tilOgMed.isAfter(LocalDate.now()) -> Periodisering(UtfallForPeriode.UAVKLART, deltagelsePeriode)
            // B&H: For nå har vi tenkt at det er lurt å ikke godkjenne søknader på tiltak frem i tid, og at de som er bakover i tid med denne statusen må sjekkes opp av SBH
            // TODO Feriegave fra Kew: Foreslår at vi kommenterer ut disse som går på LocalDate.now() og heller setter de casene til UAVKLART.
            brukerSkalBegynnePåTiltak(status) && deltagelsePeriode.tilOgMed.isBefore(LocalDate.now()) -> Periodisering(UtfallForPeriode.UAVKLART, deltagelsePeriode)
            brukerSkalBegynnePåTiltak(status) && deltagelsePeriode.tilOgMed.isAfter(LocalDate.now()) -> Periodisering(UtfallForPeriode.IKKE_OPPFYLT, deltagelsePeriode)
            else -> {
                Periodisering(UtfallForPeriode.IKKE_OPPFYLT, deltagelsePeriode)
            }
        }
    }

    override val lovreferanse = Lovreferanse.TILTAKSDELTAGELSE

    companion object {
        fun opprett(
            vurderingsperiode: Periode,
            registerSaksopplysning: TiltakDeltagelseSaksopplysning.Register,
        ): TiltakDeltagelseVilkår {
            return TiltakDeltagelseVilkår(
                vurderingsperiode = vurderingsperiode,
                registerSaksopplysning = registerSaksopplysning,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            registerSaksopplysning: TiltakDeltagelseSaksopplysning.Register,
            vurderingsperiode: Periode,
            utfall: Periodisering<UtfallForPeriode>,
        ): TiltakDeltagelseVilkår {
            return TiltakDeltagelseVilkår(
                registerSaksopplysning = registerSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            ).also {
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret i TiltakDeltagelseVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
