package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.innsending.domene.YtelseSak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate

class DagpengerTolker {
    companion object {
        fun tolkeData(ytelser: List<YtelseSak>?, periode: Periode): List<Saksopplysning> {
            if (ytelser == null) {
                return listOf(
                    Saksopplysning(
                        fom = periode.fraOgMed,
                        tom = periode.tilOgMed,
                        vilkår = Vilkår.DAGPENGER,
                        kilde = Kilde.ARENA,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
                    ),
                )
            }

            val ytelseListe = ytelser
                .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.DAGP }
                .filterNot {
                    it.fomGyldighetsperiode.toLocalDate()
                        .isAfter(it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX)
                } // Arena sender noen ganger ugyldig periode
                .filter {
                    Periode(
                        it.fomGyldighetsperiode.toLocalDate(),
                        (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX),
                    ).overlapperMed(periode)
                }

            if (ytelseListe.isEmpty()) {
                return listOf(
                    Saksopplysning(
                        fom = periode.fraOgMed,
                        tom = periode.tilOgMed,
                        vilkår = Vilkår.DAGPENGER,
                        kilde = Kilde.ARENA,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                    ),
                )
            }
            return ytelseListe
                .map {
                    Saksopplysning(
                        fom = maxOf(periode.fraOgMed, it.fomGyldighetsperiode.toLocalDate()),
                        tom = minOf(periode.tilOgMed, (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX)),
                        vilkår = Vilkår.DAGPENGER,
                        kilde = Kilde.ARENA,
                        detaljer = detaljerForDagpenger(it),
                        typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                    )
                }
        }

        private fun detaljerForDagpenger(sak: YtelseSak): String =
            when {
                sak.antallUkerIgjen != null && sak.antallDagerIgjen != null -> "${sak.antallUkerIgjen} uker (${sak.antallDagerIgjen} dager) igjen"
                sak.antallUkerIgjen != null && sak.antallDagerIgjen == null -> "${sak.antallUkerIgjen} uker igjen"
                sak.antallUkerIgjen == null && sak.antallDagerIgjen != null -> "${sak.antallDagerIgjen} dager igjen"
                else -> "Ukjent antall uker igjen"
            }
    }
}
