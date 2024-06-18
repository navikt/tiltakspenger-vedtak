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
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.livsoppholdsytelser.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.tiltak.TiltakVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkårdata.ytelser.YtelseVilkår

data class VilkårSett private constructor(
    val livsoppholdVilkår: LivsoppholdVilkår,
    val kvpVilkårData: YtelseVilkår,
    val introVilkårData: YtelseVilkår,
    val institusjonsoppholdVilkårData: YtelseVilkår,
    val tiltakVilkår: TiltakVilkår,
    val samletUtfall: Periodisering<Utfall>,
) {

    fun oppdaterSaksopplysninger(saksopplysning: LivsoppholdYtelseSaksopplysning): VilkårSett {
        return this.copy(
            livsoppholdVilkår = livsoppholdVilkår.oppdaterSaksopplysninger(saksopplysning),
        ).vilkårsvurder()
    }

    fun oppdaterSaksopplysninger(saksopplysning: YtelseSaksopplysning): VilkårSett {
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

    fun vilkårsvurder(): VilkårSett {
        return this.copy(
            samletUtfall = Companion.vilkårsvurder(
                listOf(
                    livsoppholdVilkår,
                    kvpVilkårData,
                    introVilkårData,
                    institusjonsoppholdVilkårData,
                    tiltakVilkår,
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
            Inngangsvilkår.LIVSOPPHOLDSYTELSER to livsoppholdVilkår.vurdering().utfall,
            Inngangsvilkår.TILTAKSDELTAGELSE to tiltakVilkår.vurdering().utfall,
        )
    }

    fun oppdaterAntallDager(
        tiltakId: String,
        nyPeriodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    ): VilkårSett {
        return this.copy(
            tiltakVilkår = tiltakVilkår.oppdaterAntallDager(
                tiltakId,
                nyPeriodeMedAntallDager,
                saksbehandler,
            ),
        ).vilkårsvurder()
    }

    fun oppdaterTiltak(tiltak: List<Tiltak>): VilkårSett {
        return this.copy(
            tiltakVilkår = tiltakVilkår.oppdaterTiltak(
                tiltak,
            ),
        ).vilkårsvurder()
    }

    // TODO: Denne er ment å være midlertidig. Kanskje..?
    fun periodiseringAvSaksopplysningOgUtfall(): List<Periodisering<LivsoppholdSaksopplysningOgUtfallForPeriode>> {
        return livsoppholdVilkår.livsoppholdYtelser.map {
            val fakta = it.value.avklartYtelseSaksopplysning
            val vurdering = it.value.vurdering
            fakta.harYtelse.kombiner(vurdering.utfall) { harYtelse, utfall ->
                LivsoppholdSaksopplysningOgUtfallForPeriode(
                    fakta.vilkår,
                    fakta.kilde,
                    fakta.detaljer,
                    fakta.saksbehandler,
                    harYtelse,
                    utfall,
                )
            }
        }
    }

    companion object {
        operator fun invoke(vurderingsperiode: Periode): VilkårSett {
            val livsoppholdVilkår = LivsoppholdVilkår(vurderingsperiode)
            val kvpVilkårData = YtelseVilkår(vurderingsperiode, Inngangsvilkår.KVP)
            val introVilkårData = YtelseVilkår(vurderingsperiode, Inngangsvilkår.INTROPROGRAMMET)
            val institusjonsoppholdVilkårData = YtelseVilkår(
                vurderingsperiode,
                Inngangsvilkår.INSTITUSJONSOPPHOLD,
            )
            val tiltakVilkår = TiltakVilkår(vurderingsperiode)
            return VilkårSett(
                livsoppholdVilkår = livsoppholdVilkår,
                kvpVilkårData = kvpVilkårData,
                introVilkårData = introVilkårData,
                institusjonsoppholdVilkårData = institusjonsoppholdVilkårData,
                tiltakVilkår = tiltakVilkår,
                samletUtfall = vilkårsvurder(
                    listOf(
                        livsoppholdVilkår,
                        kvpVilkårData,
                        introVilkårData,
                        institusjonsoppholdVilkårData,
                        tiltakVilkår,
                    ),
                ),
            )
        }

        fun vilkårsvurder(
            inngangsvilkår: List<Inngangsvilkårsbehandling>,
        ): Periodisering<Utfall> {
            return inngangsvilkår
                .map { it.vurdering().utfall }
                .reduser(LivsoppholdVilkår.Companion::kombinerToUtfall)
        }
    }
}
