package no.nav.tiltakspenger.vedtak.innsending.tolkere

import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.innsending.YtelseSak
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
                .filterNot { it.fomGyldighetsperiode.isAfter(it.tomGyldighetsperiode) } // Arena sender noen ganger ugyldig periode
                .filter {
                    Periode(
                        it.fomGyldighetsperiode.toLocalDate(),
                        (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX),
                    ).overlapperMed(periode)
                }
                .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.INDIV }

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
