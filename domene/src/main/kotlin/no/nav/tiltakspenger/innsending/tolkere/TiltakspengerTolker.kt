package no.nav.tiltakspenger.innsending.tolkere

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.innsending.YtelseSak
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.vilkår.Vilkår
import java.time.LocalDate

class TiltakspengerTolker {
    companion object {
        fun tolkeData(ytelser: List<YtelseSak>?, periode: Periode): List<Saksopplysning> {
            if (ytelser == null) {
                return listOf(
                    Saksopplysning(
                        fom = periode.fra,
                        tom = periode.til,
                        vilkår = Vilkår.TILTAKSPENGER,
                        kilde = Kilde.ARENA,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
                    ),
                )
            }

            val ytelseListe = ytelser
                .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.INDIV }
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
                        fom = periode.fra,
                        tom = periode.til,
                        vilkår = Vilkår.TILTAKSPENGER,
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
                        vilkår = Vilkår.TILTAKSPENGER,
                        kilde = Kilde.ARENA,
                        detaljer = "",
                        typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                    )
                }
        }
    }
}
