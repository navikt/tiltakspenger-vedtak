package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.innsending.domene.ForeldrepengerVedtak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

class ForeldrepengerTolker {
    companion object {
        fun tolkeData(
            vedtak: List<ForeldrepengerVedtak>,
            vurderingsperiode: Periode,
        ): List<Saksopplysning> {
            return ForeldrepengerVedtak.Ytelser.entries.filter { it.rettTilTiltakspenger }.map { type ->
                tolkeForEttVilkår(
                    vedtak = vedtak,
                    vurderingsperiode = vurderingsperiode,
                    type = type,
                    vilkår = when (type) {
                        ForeldrepengerVedtak.Ytelser.PLEIEPENGER_SYKT_BARN -> Vilkår.PLEIEPENGER_SYKT_BARN
                        ForeldrepengerVedtak.Ytelser.PLEIEPENGER_NÆRSTÅENDE -> Vilkår.PLEIEPENGER_NÆRSTÅENDE
                        ForeldrepengerVedtak.Ytelser.OMSORGSPENGER -> Vilkår.OMSORGSPENGER
                        ForeldrepengerVedtak.Ytelser.OPPLÆRINGSPENGER -> Vilkår.OPPLÆRINGSPENGER
                        ForeldrepengerVedtak.Ytelser.FORELDREPENGER -> Vilkår.FORELDREPENGER
                        ForeldrepengerVedtak.Ytelser.SVANGERSKAPSPENGER -> Vilkår.SVANGERSKAPSPENGER
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
    vilkår: Vilkår,
): Saksopplysning {
    return vedtak
        .filter { it.periode.overlapperMed(vurderingsperiode) }
        .filter { it.ytelse == type }
        .fold(
            Saksopplysning(
                vilkår = vilkår,
                kilde = type.kilde,
                detaljer = "",
                harYtelseSaksopplysning = Periodisering<HarYtelseSaksopplysning?>(null, vurderingsperiode)
                    .setVerdiForDelPeriode(
                        HarYtelseSaksopplysning.HAR_IKKE_YTELSE,
                        vurderingsperiode,
                    ),
            ),
        ) { resultat: Saksopplysning, vedtak: ForeldrepengerVedtak ->
            resultat.copy(
                harYtelseSaksopplysning = resultat.harYtelseSaksopplysning.setVerdiForDelPeriode(
                    HarYtelseSaksopplysning.HAR_YTELSE,
                    vedtak.periode.overlappendePeriode(vurderingsperiode)!!,
                ),
            )
        }
}
