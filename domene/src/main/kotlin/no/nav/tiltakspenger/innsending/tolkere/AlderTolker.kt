package no.nav.tiltakspenger.innsending.tolkere

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.vilkår.Vilkår
import java.time.LocalDate

class AlderTolker {
    companion object {
        fun tolkeData(fdato: LocalDate, periode: Periode): List<Saksopplysning> =
            fdato.plusYears(18).let {
                if (periode.inneholder(it)) {
                    listOf(
                        Saksopplysning(
                            fom = periode.fra,
                            tom = it.minusDays(1),
                            kilde = Kilde.PDL,
                            vilkår = Vilkår.ALDER,
                            detaljer = "",
                            typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                            saksbehandler = null,
                        ),
                        Saksopplysning(
                            fom = it,
                            tom = periode.til,
                            kilde = Kilde.PDL,
                            vilkår = Vilkår.ALDER,
                            detaljer = "",
                            typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                            saksbehandler = null,
                        ),
                    )
                } else {
                    if (periode.før(it)) {
                        listOf(
                            Saksopplysning(
                                fom = periode.fra,
                                tom = periode.til,
                                kilde = Kilde.PDL,
                                vilkår = Vilkår.ALDER,
                                detaljer = "",
                                typeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
                                saksbehandler = null,
                            ),
                        )
                    } else {
                        listOf(
                            Saksopplysning(
                                fom = periode.fra,
                                tom = periode.til,
                                kilde = Kilde.PDL,
                                vilkår = Vilkår.ALDER,
                                detaljer = "",
                                typeSaksopplysning = TypeSaksopplysning.HAR_IKKE_YTELSE,
                                saksbehandler = null,
                            ),
                        )
                    }
                }
            }
    }
}
