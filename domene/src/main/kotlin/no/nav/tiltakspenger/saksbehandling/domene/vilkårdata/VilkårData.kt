package no.nav.tiltakspenger.saksbehandling.domene.vilkårdata

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.libs.periodisering.Periodisering.Companion.reduser
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Inngangsvilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.livsoppholdsytelser.LivsoppholdVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.tiltak.TiltakVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.ytelser.YtelseVilkårData

data class VilkårData private constructor(
    val livsoppholdVilkårData: LivsoppholdVilkårData,
    val kvpVilkårData: YtelseVilkårData,
    val introVilkårData: YtelseVilkårData,
    val institusjonsoppholdVilkårData: YtelseVilkårData,
    val tiltakVilkårData: TiltakVilkårData,
    val samletUtfall: Periodisering<Utfall>,
) {

    fun oppdaterSaksopplysninger(saksopplysning: LivsoppholdYtelseSaksopplysning): VilkårData {
        return this.copy(
            livsoppholdVilkårData = livsoppholdVilkårData.oppdaterSaksopplysninger(saksopplysning),
        ).vilkårsvurder()
    }

    fun oppdaterSaksopplysninger(saksopplysning: YtelseSaksopplysning): VilkårData {
        return when (saksopplysning.vilkår) {
            Inngangsvilkår.ALDER -> TODO()
            Inngangsvilkår.INSTITUSJONSOPPHOLD -> this.copy(
                institusjonsoppholdVilkårData = institusjonsoppholdVilkårData.oppdaterSaksopplysning(saksopplysning),
            )

            Inngangsvilkår.INTROPROGRAMMET -> this.copy(
                introVilkårData = introVilkårData.oppdaterSaksopplysning(saksopplysning),
            )

            Inngangsvilkår.KVP -> this.copy(
                kvpVilkårData = kvpVilkårData.oppdaterSaksopplysning(saksopplysning),
            )

            Inngangsvilkår.LIVSOPPHOLDSYTELSER -> throw IllegalStateException("")
            Inngangsvilkår.TILTAKSDELTAGELSE -> throw IllegalStateException("")
        }.vilkårsvurder()
    }

    // TODO: Burde vært prekalkulert og lagret
    fun samletUtfall(): Periodisering<Utfall> {
        return samletUtfall
    }

    fun vilkårsvurder(): VilkårData {
        return this.copy(
            samletUtfall = Companion.vilkårsvurder(
                listOf(
                    livsoppholdVilkårData,
                    kvpVilkårData,
                    introVilkårData,
                    institusjonsoppholdVilkårData,
                    tiltakVilkårData,
                ),
            ),
        )
    }

    fun utfallPerInngangsvilkår(): Map<Inngangsvilkår, Periodisering<Utfall>> {
        return mapOf(
            Inngangsvilkår.ALDER to TODO(),
            Inngangsvilkår.INSTITUSJONSOPPHOLD to institusjonsoppholdVilkårData.vurdering().utfall,
            Inngangsvilkår.INTROPROGRAMMET to introVilkårData.vurdering().utfall,
            Inngangsvilkår.KVP to kvpVilkårData.vurdering().utfall,
            Inngangsvilkår.LIVSOPPHOLDSYTELSER to livsoppholdVilkårData.vurdering().utfall,
            Inngangsvilkår.TILTAKSDELTAGELSE to tiltakVilkårData.vurdering().utfall,
        )
    }

    fun oppdaterAntallDager(
        tiltakId: String,
        nyPeriodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    ): VilkårData {
        return this.copy(
            tiltakVilkårData = tiltakVilkårData.oppdaterAntallDager(
                tiltakId,
                nyPeriodeMedAntallDager,
                saksbehandler,
            ),
        ).vilkårsvurder()
    }

    fun oppdaterTiltak(tiltak: List<Tiltak>): VilkårData {
        return this.copy(
            tiltakVilkårData = tiltakVilkårData.oppdaterTiltak(
                tiltak,
            ),
        ).vilkårsvurder()
    }

    companion object {
        operator fun invoke(vurderingsperiode: Periode): VilkårData {
            val livsoppholdVilkårData = LivsoppholdVilkårData(vurderingsperiode)
            val kvpVilkårData = YtelseVilkårData(vurderingsperiode, Inngangsvilkår.KVP)
            val introVilkårData = YtelseVilkårData(vurderingsperiode, Inngangsvilkår.INTROPROGRAMMET)
            val institusjonsoppholdVilkårData = YtelseVilkårData(
                vurderingsperiode,
                Inngangsvilkår.INSTITUSJONSOPPHOLD,
            )
            val tiltakVilkårData = TiltakVilkårData(vurderingsperiode)
            return VilkårData(
                livsoppholdVilkårData = livsoppholdVilkårData,
                kvpVilkårData = kvpVilkårData,
                introVilkårData = introVilkårData,
                institusjonsoppholdVilkårData = institusjonsoppholdVilkårData,
                tiltakVilkårData = tiltakVilkårData,
                samletUtfall = vilkårsvurder(
                    listOf(
                        livsoppholdVilkårData,
                        kvpVilkårData,
                        introVilkårData,
                        institusjonsoppholdVilkårData,
                        tiltakVilkårData,
                    ),
                ),
            )
        }

        fun vilkårsvurder(
            inngangsvilkår: List<Inngangsvilkårsbehandling>,
        ): Periodisering<Utfall> {
            return inngangsvilkår
                .map { it.vurdering().utfall }
                .reduser(LivsoppholdVilkårData.Companion::kombinerToUtfall)
        }
    }
}
