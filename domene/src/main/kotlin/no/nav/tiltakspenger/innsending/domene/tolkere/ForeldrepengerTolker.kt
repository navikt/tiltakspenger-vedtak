package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.innsending.domene.ForeldrepengerVedtak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår

class ForeldrepengerTolker {
    companion object {
        fun tolkeData(
            vedtak: List<ForeldrepengerVedtak>,
            periode: Periode,
        ): List<Saksopplysning> {
            return ForeldrepengerVedtak.Ytelser.entries.filter { it.rettTilTiltakspenger }.flatMap { type ->
                tolkeForEttVilkår(
                    vedtak = vedtak,
                    periode = periode,
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
                    kilde = type.kilde,
                    detaljer = "",
                    typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                ),
            )
        }
}
