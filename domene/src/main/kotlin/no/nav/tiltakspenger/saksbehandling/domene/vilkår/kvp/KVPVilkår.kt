package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SkalErstatteVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse.DELTAR_IKKE
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
) : SkalErstatteVilkår {

    override val lovreferanse = Lovreferanse.KVP

    override fun utfall(): Periodisering<Utfall2> {
        return avklartSaksopplysning.deltar.map { it.vurderMaskinelt() }
    }

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
            ).also {
                check(utfall == it.utfall()) { "Mismatch mellom utfallet som er lagret i KVPVilkår ($utfall), og utfallet som har blitt utledet (${it.utfall()})" }
            }
        }
    }
}
