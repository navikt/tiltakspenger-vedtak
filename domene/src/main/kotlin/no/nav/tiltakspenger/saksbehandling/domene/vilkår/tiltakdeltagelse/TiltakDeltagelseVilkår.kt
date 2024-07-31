package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2

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

    override fun utfall(): Periodisering<Utfall2> {
        val girRett = registerSaksopplysning.girRett
        val deltagelsePeriode = registerSaksopplysning.deltagelsePeriode
        return when {
            girRett -> Periodisering(Utfall2.OPPFYLT, deltagelsePeriode)
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
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
