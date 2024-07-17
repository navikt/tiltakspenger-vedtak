package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import java.time.LocalDateTime

/**
 * Kvalifiseringsprogrammet (KVP): https://www.nav.no/kvalifiseringsprogrammet
 *
 * @param søknadSaksopplysning Saksopplysninger som kan være avgjørende for vurderingen. Kan ikke ha hull. [avklartSaksopplysning]/faktumet er den avgjørende saksopplysningen.
 * @param avklartSaksopplysning Faktumet som avgjør om vilkåret er oppfylt eller ikke. Null implisiserer uavklart.
 * @param utfall Selvom om utfallet er
 *
 */
data class KVPVilkår private constructor(
    val søknadSaksopplysning: KvpSaksopplysning,
    val saksbehandlerSaksopplysning: KvpSaksopplysning?,
    val avklartSaksopplysning: KvpSaksopplysning,
    val utfall: Periodisering<Utfall2>,
) : SkalErstatteVilkår {

    val samletUtfall: SamletUtfall = when {
        utfall.perioder().any { it.verdi == Utfall2.UAVKLART } -> SamletUtfall.UAVKLART
        utfall.perioder().all { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.OPPFYLT
        utfall.perioder().all { it.verdi == Utfall2.IKKE_OPPFYLT } -> SamletUtfall.IKKE_OPPFYLT
        utfall.perioder().any() { it.verdi == Utfall2.OPPFYLT } -> SamletUtfall.DELVIS_OPPFYLT
        else -> throw IllegalStateException("Ugyldig utfall")
    }

    override val lovreferanse = Lovreferanse.KVP

    val totalePeriode: Periode = avklartSaksopplysning.totalePeriode

    fun leggTilSaksbehandlerSaksopplysning(command: LeggTilKvpSaksopplysningCommand): KVPVilkår {
        val kvpSaksopplysning = KvpSaksopplysning.Saksbehandler(
            deltar = Periodisering(
                command.deltakelseForPeriode.map { PeriodeMedVerdi(it.tilDeltagelse(), it.periode) },
            ).utvid(Deltagelse.DELTAR_IKKE, totalePeriode),
            årsakTilEndring = command.årsakTilEndring,
            saksbehandler = command.saksbehandler,
            tidsstempel = LocalDateTime.now(),
        )
        return this.copy(
            saksbehandlerSaksopplysning = kvpSaksopplysning,
            avklartSaksopplysning = kvpSaksopplysning,
            utfall = kvpSaksopplysning.vurderMaskinelt(),
        )
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
            søknadSaksopplysning: KvpSaksopplysning,
        ): KVPVilkår {
            return KVPVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = null,
                avklartSaksopplysning = søknadSaksopplysning,
                utfall = søknadSaksopplysning.vurderMaskinelt(),
            )
        }

        /**
         * Skal kun kalles fra database-laget og for assert av tester (expected).
         */
        fun fromDb(
            søknadSaksopplysning: KvpSaksopplysning,
            saksbehandlerSaksopplysning: KvpSaksopplysning?,
            avklartSaksopplysning: KvpSaksopplysning,
            utfall: Periodisering<Utfall2>,
        ): KVPVilkår {
            return KVPVilkår(
                søknadSaksopplysning = søknadSaksopplysning,
                saksbehandlerSaksopplysning = saksbehandlerSaksopplysning,
                avklartSaksopplysning = avklartSaksopplysning,
                utfall = utfall,
            )
        }
    }
}
