package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.innsending.domene.YtelseSak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate

class DagpengerTolker {
    companion object {
        fun tolkeData(ytelser: List<YtelseSak>?, vurderingsperiode: Periode): LivoppholdSaksopplysning {
            if (ytelser == null) {
                return LivoppholdSaksopplysning(
                    vilkår = Vilkår.DAGPENGER,
                    kilde = Kilde.ARENA,
                    detaljer = "",
                    harYtelse = Periodisering(null, vurderingsperiode),
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
                    ).overlapperMed(vurderingsperiode)
                }

            if (ytelseListe.isEmpty()) {
                return LivoppholdSaksopplysning(
                    vilkår = Vilkår.DAGPENGER,
                    kilde = Kilde.ARENA,
                    detaljer = "",
                    harYtelse = Periodisering<HarYtelse?>(null, vurderingsperiode)
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_IKKE_YTELSE,
                            vurderingsperiode,
                        ),
                )
            }
            return ytelseListe
                .fold(
                    LivoppholdSaksopplysning(
                        vilkår = Vilkår.DAGPENGER,
                        kilde = Kilde.ARENA,
                        // TODO: Denne blir annerledes når vi ikke lenger har én saksopplysning per sak
                        detaljer = ytelseListe.lastOrNull()?.let { detaljerForDagpenger(it) } ?: "",
                        harYtelse = Periodisering<HarYtelse?>(null, vurderingsperiode)
                            .setVerdiForDelPeriode(
                                HarYtelse.HAR_IKKE_YTELSE,
                                vurderingsperiode,
                            ),
                    ),
                ) { resultat: LivoppholdSaksopplysning, ytelse: YtelseSak ->
                    resultat.copy(
                        harYtelse = resultat.harYtelse.setVerdiForDelPeriode(
                            HarYtelse.HAR_YTELSE,
                            Periode(
                                ytelse.fomGyldighetsperiode.toLocalDate(),
                                ytelse.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX,
                            )
                                .overlappendePeriode(vurderingsperiode)!!,
                        ),
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
