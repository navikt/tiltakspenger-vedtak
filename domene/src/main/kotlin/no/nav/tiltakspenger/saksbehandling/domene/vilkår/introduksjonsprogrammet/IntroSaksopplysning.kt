package no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface IntroSaksopplysning {

    val deltar: Periodisering<Deltagelse>
    val tidsstempel: LocalDateTime
    val totalePeriode: Periode

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?

    data class Søknad(
        override val deltar: Periodisering<Deltagelse>,
        override val tidsstempel: LocalDateTime,
    ) : IntroSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(deltar.perioder().isNotEmpty()) { "IntroSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = deltar.totalePeriode
    }

    data class Saksbehandler(
        override val deltar: Periodisering<Deltagelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : IntroSaksopplysning {
        init {
            require(deltar.perioder().isNotEmpty()) { "IntroSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = deltar.totalePeriode
    }
}
