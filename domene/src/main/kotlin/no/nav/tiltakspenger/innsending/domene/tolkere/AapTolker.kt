package no.nav.tiltakspenger.innsending.domene.tolkere

import no.nav.tiltakspenger.innsending.domene.YtelseSak
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdYtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.LivsoppholdDelVilkår
import java.time.LocalDate

class AapTolker {
    companion object {
        fun tolkeData(ytelser: List<YtelseSak>?, vurderingsperiode: Periode): LivsoppholdYtelseSaksopplysning {
            if (ytelser == null) {
                return LivsoppholdYtelseSaksopplysning(
                    vilkår = LivsoppholdDelVilkår.AAP,
                    kilde = Kilde.ARENA,
                    detaljer = "",
                    harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode),
                )
            }

            val ytelseListe = ytelser
                .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.AA }
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
                return LivsoppholdYtelseSaksopplysning(
                    vilkår = LivsoppholdDelVilkår.AAP,
                    kilde = Kilde.ARENA,
                    detaljer = "",
                    harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                        .setVerdiForDelPeriode(
                            HarYtelse.HAR_IKKE_YTELSE,
                            vurderingsperiode,
                        ),
                )
            }

            return ytelseListe
                .fold(
                    LivsoppholdYtelseSaksopplysning(
                        vilkår = LivsoppholdDelVilkår.AAP,
                        kilde = Kilde.ARENA,
                        detaljer = "",
                        harYtelse = Periodisering(HarYtelse.IKKE_INNHENTET, vurderingsperiode)
                            .setVerdiForDelPeriode(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                    ),
                ) { resultat: LivsoppholdYtelseSaksopplysning, ytelse: YtelseSak ->
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
    }
}
