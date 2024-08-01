package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import java.time.LocalDate

/**
 * Tiltak
 *
 * @param registerSaksopplysning Saksopplysninger som er avgjørende for vurderingen. Kan ikke ha hull. Må gå til kildesystem for å oppdatere/endre dersom vi oppdager feil i datasettet.
 * @param utfall Selvom om utfallet er
 *
 */
data class TiltakDeltagelseVilkår private constructor(
    val registerSaksopplysning: TiltakDeltagelseSaksopplysning,
    val vurderingsperiode: Periode,
) : SkalErstatteVilkår {

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

    override fun utfall(): Periodisering<Utfall2> {
        val girRett = registerSaksopplysning.girRett
        val deltagelsePeriode = registerSaksopplysning.deltagelsePeriode
        val status = registerSaksopplysning.status

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

    override val lovreferanse = Lovreferanse.TILTAKSDELTAGELSE

    companion object {
        fun opprett(
            registerSaksopplysning: TiltakDeltagelseSaksopplysning,
            vurderingsperiode: Periode,
        ): TiltakDeltagelseVilkår {
            return TiltakDeltagelseVilkår(
                registerSaksopplysning = registerSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            registerSaksopplysning: TiltakDeltagelseSaksopplysning,
            vurderingsperiode: Periode,
            utfall: Periodisering<Utfall2>,
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
