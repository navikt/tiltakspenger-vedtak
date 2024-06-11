package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.innsending.domene.ForeldrepengerVedtak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår

class ForeldrepengerTolker {
    companion object {
        fun tolkeData(
            vedtak: List<ForeldrepengerVedtak>,
            vurderingsperiode: Periode,
        ): List<LivsoppholdYtelseSaksopplysning> {
            return ForeldrepengerVedtak.Ytelser.entries.filter { it.rettTilTiltakspenger }.map { type ->
                tolkeForEttVilkår(
                    vedtak = vedtak,
                    vurderingsperiode = vurderingsperiode,
                    type = type,
                    vilkår = when (type) {
                        ForeldrepengerVedtak.Ytelser.PLEIEPENGER_SYKT_BARN -> LivsoppholdDelVilkår.PLEIEPENGER_SYKT_BARN
                        ForeldrepengerVedtak.Ytelser.PLEIEPENGER_NÆRSTÅENDE -> LivsoppholdDelVilkår.PLEIEPENGER_NÆRSTÅENDE
                        ForeldrepengerVedtak.Ytelser.OMSORGSPENGER -> LivsoppholdDelVilkår.OMSORGSPENGER
                        ForeldrepengerVedtak.Ytelser.OPPLÆRINGSPENGER -> LivsoppholdDelVilkår.OPPLÆRINGSPENGER
                        ForeldrepengerVedtak.Ytelser.FORELDREPENGER -> LivsoppholdDelVilkår.FORELDREPENGER
                        ForeldrepengerVedtak.Ytelser.SVANGERSKAPSPENGER -> LivsoppholdDelVilkår.SVANGERSKAPSPENGER
                        else -> throw IllegalStateException("Ukjent ytelsestype ${type.name}")
                    },
                )
            }
        }
    }
}

private fun tolkeForEttVilkår(
    vedtak: List<ForeldrepengerVedtak>,
    vurderingsperiode: Periode,
    type: ForeldrepengerVedtak.Ytelser,
    vilkår: LivsoppholdDelVilkår,
): LivsoppholdYtelseSaksopplysning {
    return vedtak
        .filter { it.periode.overlapperMed(vurderingsperiode) }
        .filter { it.ytelse == type }
        .fold(
            LivsoppholdYtelseSaksopplysning(
                vilkår = vilkår,
                kilde = type.kilde,
                detaljer = "",
                harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                    .setVerdiForDelPeriode(
                        HarYtelse.HAR_IKKE_YTELSE,
                        vurderingsperiode,
                    ),
            ),
        ) { resultat: LivsoppholdYtelseSaksopplysning, vedtak: ForeldrepengerVedtak ->
            resultat.copy(
                harYtelse = resultat.harYtelse.setVerdiForDelPeriode(
                    HarYtelse.HAR_YTELSE,
                    vedtak.periode.overlappendePeriode(vurderingsperiode)!!,
                ),
            )
        }
}
