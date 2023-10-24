package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import java.time.LocalDate

class DagpengerTolker {
    companion object {
        fun tolkeData(ytelser: List<YtelseSak>?, periode: Periode): List<Saksopplysning> {
            if (ytelser == null) {
                return listOf(
                    Saksopplysning(
                        fom = periode.fra,
                        tom = periode.til,
                        vilkår = Vilkår.DAGPENGER,
                        kilde = Kilde.ARENA,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
                    ),
                )
            }

            val ytelseListe = ytelser
                .filterNot { it.fomGyldighetsperiode.isAfter(it.tomGyldighetsperiode) } // Arena sender noen ganger ugyldig periode
                .filter {
                    Periode(
                        it.fomGyldighetsperiode.toLocalDate(),
                        (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX),
                    ).overlapperMed(periode)
                }
                .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.DAGP }

            if (ytelseListe.isEmpty()) {
                return listOf(
                    Saksopplysning(
                        fom = periode.fra,
                        tom = periode.til,
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
                        fom = maxOf(periode.fra, it.fomGyldighetsperiode.toLocalDate()),
                        tom = minOf(periode.til, (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX)),
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
