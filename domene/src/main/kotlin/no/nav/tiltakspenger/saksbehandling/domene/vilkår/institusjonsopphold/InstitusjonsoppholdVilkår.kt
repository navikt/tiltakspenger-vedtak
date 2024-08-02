package no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.Opphold.IKKE_OPPHOLD
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.Opphold.OPPHOLD

/**
 * Institusjonsopphold:
 *
 * @param søknadSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param utfall Selvom om utfallet er
 *
 */
data class InstitusjonsoppholdVilkår private constructor(
    val søknadSaksopplysning: InstitusjonsoppholdSaksopplysning,
    val saksbehandlerSaksopplysning: InstitusjonsoppholdSaksopplysning?,
    val avklartSaksopplysning: InstitusjonsoppholdSaksopplysning,
) : SkalErstatteVilkår {

    override val lovreferanse = Lovreferanse.INSTITUSJONSOPPHOLD

    override fun utfall(): Periodisering<Utfall2> {
        return avklartSaksopplysning.opphold.map {
            when (it) {
                OPPHOLD -> Utfall2.IKKE_OPPFYLT
                IKKE_OPPHOLD -> Utfall2.OPPFYLT
            }
        }
    }

    val totalePeriode: Periode = avklartSaksopplysning.totalePeriode

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
            søknadSaksopplysning: InstitusjonsoppholdSaksopplysning,
        ): InstitusjonsoppholdVilkår {
            return InstitusjonsoppholdVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = søknadSaksopplysning,
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            søknadSaksopplysning: InstitusjonsoppholdSaksopplysning,
            saksbehandlerSaksopplysning: InstitusjonsoppholdSaksopplysning?,
            avklartSaksopplysning: InstitusjonsoppholdSaksopplysning,
            utfall: Periodisering<Utfall2>,
        ): InstitusjonsoppholdVilkår {
            return InstitusjonsoppholdVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
            ).also {
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret i InstitusjonsoppholdVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
