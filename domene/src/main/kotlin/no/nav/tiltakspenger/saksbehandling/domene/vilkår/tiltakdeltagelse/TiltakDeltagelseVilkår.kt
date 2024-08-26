package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import mu.KotlinLogging
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

/**
 * @param registerSaksopplysning Saksopplysninger som er avgjørende for vurderingen. Kan ikke ha hull. Må gå til kildesystem for å oppdatere/endre dersom vi oppdager feil i datasettet.
 */
data class TiltakDeltagelseVilkår private constructor(
    override val vurderingsperiode: Periode,
    val registerSaksopplysning: TiltakDeltagelseSaksopplysning.Register,
) : Vilkår {
    private val logger = KotlinLogging.logger { }

    init {
        check(vurderingsperiode == registerSaksopplysning.deltagelsePeriode) { "Vurderingsperioden må være lik deltagelsesperioden" }
    }

    override fun utfall(): Periodisering<UtfallForPeriode> {
        val kometGirRett = registerSaksopplysning.girRett
        val deltagelsePeriode = registerSaksopplysning.deltagelsePeriode
        val status = registerSaksopplysning.status

        val tiltakspengerGirRett = status.rettTilÅSøke
        if (tiltakspengerGirRett != kometGirRett) {
            // TODO tiltak jah: skal tiltakspenger eller komet eie denne logikken?
            //  Se på dette sammen med Tia og Sølvi?
            logger.error {
                "rettTilSøke basert på statusen ($tiltakspengerGirRett) stemmer ikke overens med tiltak.gjennomføring. Saksopplysning: $registerSaksopplysning "
            }
        }

        return when {
            kometGirRett && tiltakspengerGirRett -> Periodisering(UtfallForPeriode.OPPFYLT, deltagelsePeriode)
            else -> Periodisering(UtfallForPeriode.UAVKLART, deltagelsePeriode)
        }
    }

    override val lovreferanse = Lovreferanse.TILTAKSDELTAGELSE

    companion object {
        fun opprett(
            vurderingsperiode: Periode,
            registerSaksopplysning: TiltakDeltagelseSaksopplysning.Register,
        ): TiltakDeltagelseVilkår =
            TiltakDeltagelseVilkår(
                vurderingsperiode = vurderingsperiode,
                registerSaksopplysning = registerSaksopplysning,
            )

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            registerSaksopplysning: TiltakDeltagelseSaksopplysning.Register,
            vurderingsperiode: Periode,
            utfall: Periodisering<UtfallForPeriode>,
        ): TiltakDeltagelseVilkår =
            TiltakDeltagelseVilkår(
                registerSaksopplysning = registerSaksopplysning,
                vurderingsperiode = vurderingsperiode,
            ).also {
                check(utfall == it.utfall()) {
                    "Mismatch mellom utfallet som er lagret i TiltakDeltagelseVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall()})"
                }
            }
    }
}
