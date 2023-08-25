package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import java.time.LocalDate

class DagpengerFaktaHjelper {
    companion object {
        fun lagFaktaHjelper(ytelser: List<YtelseSak>?, periode: Periode) =
            ytelser
                .orEmpty()
                .filter {
                    Periode(
                        it.fomGyldighetsperiode.toLocalDate(),
                        (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX),
                    ).overlapperMed(periode)
                }
                .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.DAGP }
                .map {
                    Fakta.Dagpenger(
                        fom = periode.fra,
                        tom = periode.til,
                        vilkår = Vilkår.DAGPENGER,
                        kilde = "Arena",
                        detaljer = detaljerForDagpenger(it),
                    )
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
