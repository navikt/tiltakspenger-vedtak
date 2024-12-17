package no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold

import no.nav.tiltakspenger.felles.exceptions.StøtterIkkeUtfallException
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.Opphold.IKKE_OPPHOLD
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.Opphold.OPPHOLD

/**
 * Institusjonsopphold:
 *
 * @param søknadSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 */
data class InstitusjonsoppholdVilkår private constructor(
    override val vurderingsperiode: Periode,
    val søknadSaksopplysning: InstitusjonsoppholdSaksopplysning.Søknad,
    val saksbehandlerSaksopplysning: InstitusjonsoppholdSaksopplysning.Saksbehandler?,
    val avklartSaksopplysning: InstitusjonsoppholdSaksopplysning,
) : Vilkår {
    override fun krymp(nyPeriode: Periode): InstitusjonsoppholdVilkår {
        if (vurderingsperiode == nyPeriode) return this
        require(vurderingsperiode.inneholderHele(nyPeriode)) { "Ny periode ($nyPeriode) må være innenfor vurderingsperioden ($vurderingsperiode)" }
        val nySøknadSaksopplysning = søknadSaksopplysning.oppdaterPeriode(nyPeriode)
        val nySaksbehandlerSaksopplysning = saksbehandlerSaksopplysning?.oppdaterPeriode(nyPeriode)
        return InstitusjonsoppholdVilkår(
            vurderingsperiode = nyPeriode,
            søknadSaksopplysning = nySøknadSaksopplysning,
            saksbehandlerSaksopplysning = nySaksbehandlerSaksopplysning,
            avklartSaksopplysning = nySaksbehandlerSaksopplysning ?: nySøknadSaksopplysning,
        )
    }

    override val lovreferanse = Lovreferanse.INSTITUSJONSOPPHOLD

    override val utfall: Periodisering<UtfallForPeriode> =
        avklartSaksopplysning.opphold.map {
            when (it) {
                OPPHOLD -> throw StøtterIkkeUtfallException("Støtter ikke delvis innvilgelse eller avslag")
                IKKE_OPPHOLD -> UtfallForPeriode.OPPFYLT
            }
        }

    init {
        if (saksbehandlerSaksopplysning != null) {
            require(søknadSaksopplysning.totalePeriode == saksbehandlerSaksopplysning.totalePeriode) {
                "søknadSaksopplysning (${søknadSaksopplysning.totalePeriode}) og saksbehandlerSaksopplysning(${saksbehandlerSaksopplysning.totalePeriode}) må ha samme totale periode."
            }
            require(saksbehandlerSaksopplysning.totalePeriode == avklartSaksopplysning.totalePeriode) {
                "saksbehandlerSaksopplysning (${saksbehandlerSaksopplysning.totalePeriode}) og avklartSaksopplysning(${avklartSaksopplysning.totalePeriode}) må ha samme totale periode."
            }
        }
        require(søknadSaksopplysning.totalePeriode == avklartSaksopplysning.totalePeriode) {
            "søknadSaksopplysning (${søknadSaksopplysning.totalePeriode}) og avklartSaksopplysning(${avklartSaksopplysning.totalePeriode}) må ha samme totale periode."
        }
    }

    companion object {
        fun opprett(
            vurderingsperiode: Periode,
            søknadSaksopplysning: InstitusjonsoppholdSaksopplysning.Søknad,
        ): InstitusjonsoppholdVilkår =
            InstitusjonsoppholdVilkår(
                vurderingsperiode = vurderingsperiode,
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = søknadSaksopplysning,
            )

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            vurderingsperiode: Periode,
            søknadSaksopplysning: InstitusjonsoppholdSaksopplysning.Søknad,
            saksbehandlerSaksopplysning: InstitusjonsoppholdSaksopplysning.Saksbehandler?,
            avklartSaksopplysning: InstitusjonsoppholdSaksopplysning,
            utfall: Periodisering<UtfallForPeriode>,
        ): InstitusjonsoppholdVilkår =
            InstitusjonsoppholdVilkår(
                vurderingsperiode = vurderingsperiode,
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
            ).also {
                check(utfall == it.utfall) {
                    "Mismatch mellom utfallet som er lagret i InstitusjonsoppholdVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall})"
                }
            }
    }
}
