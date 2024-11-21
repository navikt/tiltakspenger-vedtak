package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.exceptions.StøtterIkkeUtfallException
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
        check(vurderingsperiode == registerSaksopplysning.deltagelsePeriode) { "Vurderingsperioden ($vurderingsperiode) må være lik deltagelsesperioden (${registerSaksopplysning.deltagelsePeriode})" }
    }

    override val utfall: Periodisering<UtfallForPeriode> = run {
        val rettTilTiltakspenger = registerSaksopplysning.girRett
        val deltagelsePeriode = registerSaksopplysning.deltagelsePeriode
        val status = registerSaksopplysning.status
        val rettTilÅSøke = status.rettTilÅSøke

        if (!rettTilÅSøke || !rettTilTiltakspenger) {
            // TODO post-mvp jah: Vi utleder girRett i tiltakspenger-tiltak. Her kan vi heller bruke en felles algoritme i libs, istedet for å sende den over nettverk.
            throw StøtterIkkeUtfallException(
                "Per dags dato får brukere kun søke dersom vi har whitelistet tiltakets status og klassekode. Dette tiltaket fører til avslag. RettTilÅSøke: $rettTilÅSøke og RettTilTiltakspenger: $rettTilTiltakspenger",
            )
        }
        Periodisering(UtfallForPeriode.OPPFYLT, deltagelsePeriode)
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
                check(utfall == it.utfall) {
                    "Mismatch mellom utfallet som er lagret i TiltakDeltagelseVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall})"
                }
            }
    }
}
