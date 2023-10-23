package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår

class ForeldrepengerTolker {
    companion object {
        fun tolkeData(
            vedtak: List<ForeldrepengerVedtak>,
            periode: Periode,
        ): List<Saksopplysning> {
            return ForeldrepengerVedtak.Ytelser.entries.flatMap { type ->
                tolkeForEttVilkår(
                    vedtak = vedtak,
                    periode = periode,
                    type = type,
                    vilkår = when (type) {
                        ForeldrepengerVedtak.Ytelser.PLEIEPENGER_SYKT_BARN -> Vilkår.PLEIEPENGER_SYKT_BARN
                        ForeldrepengerVedtak.Ytelser.PLEIEPENGER_NÆRSTÅENDE -> Vilkår.PLEIEPENGER_NÆRSTÅENDE
                        ForeldrepengerVedtak.Ytelser.OMSORGSPENGER -> Vilkår.OMSORGSPENGER
                        ForeldrepengerVedtak.Ytelser.OPPLÆRINGSPENGER -> Vilkår.OPPLÆRINGSPENGER
                        ForeldrepengerVedtak.Ytelser.ENGANGSTØNAD -> Vilkår.FORELDREPENGER // TODO()
                        ForeldrepengerVedtak.Ytelser.FORELDREPENGER -> Vilkår.FORELDREPENGER
                        ForeldrepengerVedtak.Ytelser.SVANGERSKAPSPENGER -> Vilkår.SVANGERSKAPSPENGER
                        ForeldrepengerVedtak.Ytelser.FRISINN -> Vilkår.FORELDREPENGER // TODO()
                    },
                )
            }
        }
    }
}

private fun tolkeForEttVilkår(
    vedtak: List<ForeldrepengerVedtak>,
    periode: Periode,
    type: ForeldrepengerVedtak.Ytelser,
    vilkår: Vilkår,
): List<Saksopplysning> {
    return vedtak
        .filter {
            Periode(
                it.periode.fra,
                it.periode.til,
            ).overlapperMed(periode)
        }
        .filter { it.ytelse == type }
        .map {
            Saksopplysning(
                fom = maxOf(periode.fra, it.periode.fra),
                tom = minOf(periode.til, it.periode.til),
                vilkår = vilkår,
                kilde = when (it.kildesystem) {
                    ForeldrepengerVedtak.Kildesystem.FPSAK -> Kilde.FPSAK
                    ForeldrepengerVedtak.Kildesystem.K9SAK -> Kilde.K9SAK
                },
                detaljer = "",
                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
            )
        }
        .ifEmpty {
            listOf(
                Saksopplysning(
                    fom = periode.fra,
                    tom = periode.til,
                    vilkår = vilkår,
                    kilde = when (vilkår) {
                        Vilkår.OMSORGSPENGER -> Kilde.K9SAK
                        Vilkår.OPPLÆRINGSPENGER -> Kilde.K9SAK
                        Vilkår.PLEIEPENGER_NÆRSTÅENDE -> Kilde.K9SAK
                        Vilkår.PLEIEPENGER_SYKT_BARN -> Kilde.K9SAK
                        else -> Kilde.FPSAK
                    },
                    detaljer = "",
                    typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                ),
            )
        }
}
